package com.jointdelivery.appviewmodule.loginactivities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import com.jointdelivery.MyApplication
import com.jointdelivery.R
import com.jointdelivery.auth.ApiClientWithoutHeader
import com.jointdelivery.auth.AuthManager
import com.jointdelivery.basemodule.activities.BaseActivity
import com.jointdelivery.basemodule.activities.HomeActivity
import com.jointdelivery.appviewmodule.model.LoginErrorModel
import com.jointdelivery.appviewmodule.model.LoginModel
import com.jointdelivery.utilities.CommonUtil
import com.jointdelivery.utilities.Constants
import com.scyhealth.basemodule.interfaces.PostAPIResultCallback
import com.scyhealth.loginmodule.LoginApiMethods
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.jointdelivery.appviewmodule.registeractivities.SignUpActivityWithPhone
import com.jointdelivery.utilities.ConnectionManager
import com.sendbird.android.SendBird

import kotlinx.android.synthetic.main.tool_bar_layout.*


class LoginActivity : BaseActivity(), PostAPIResultCallback<LoginModel> {

    @Inject
    lateinit var authManager: AuthManager

    fun SignUpClick(view: View) {
        val intent = Intent(this, SignUpActivityWithPhone::class.java)
        startActivity(intent)
    }

    @SuppressLint("SetTextI18n", "ServiceCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.jointdelivery.R.layout.activity_login)
        initView()
/*
        scroll_layout.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN -> CommonUtil.hideKeyboard(v!!, applicationContext)
                }

                return v?.onTouchEvent(event) ?: true
            }
        })*/


    }

    private fun initView() {
        (application as MyApplication).getAppComponent()?.inject(this)
    }

    override fun onBackPressed() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }


    fun LoginToApp(view: View) {
        if (CommonUtil.isConnectingToInternet(applicationContext)) {
            if (CommonUtil.checkEmptyString(editText_user)) {
                CommonUtil.showSnackbar(main_layout, resources.getString(R.string.please_enter_email_phone))
                return
            }

            if (CommonUtil.checkEmptyString(editText2_password)) {
                CommonUtil.showSnackbar(main_layout, resources.getString(R.string.please_enter_password))
                return
            }

            CommonUtil.showProgress(this)
            LoginApiMethods.login(
                this,
                editText_user.text.toString().trim(),
                editText2_password.text.toString().trim(),
                Constants.GRANT_TYPE
            )
        } else {
            CommonUtil.showSnackbar(main_layout, resources.getString(R.string.please_connect_internet))
        }
    }


    fun OnForgetPasswordClick(view: View) {
        val intent = Intent(this, ForgetPasswordActivity::class.java)
        startActivity(intent)
    }

    override fun onResponse(response: Response<LoginModel>) {
        CommonUtil.hideProgress()
        if (true) {
            if (response.errorBody() != null) run {
                val errorConverter = ApiClientWithoutHeader.client.responseBodyConverter<LoginErrorModel>(
                    LoginErrorModel::class.java,
                    arrayOfNulls<Annotation>(0)
                )
                try {
                    val error = errorConverter.convert(response.errorBody()!!)
                    showDialog(error!!.errorDescription)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                if (response.body() != null) {
                    CommonUtil.saveLoginSession(authManager, response)

                    connectToSendBird("janak14")

                } else {
                    showDialog(resources.getString(R.string.something_wrong))
                }
            }
        }
    }

    override fun onFailure() {
        CommonUtil.hideProgress()
        showDialog(resources.getString(R.string.something_wrong))
    }

    private fun showDialog(message: String) {
        val adb = AlertDialog.Builder(this)
        var dialogs = Dialog(this)
        dialogs.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialogs.setCancelable(false)
        dialogs.setContentView(R.layout.dialog_response)
        dialogs.window.setBackgroundDrawableResource(android.R.color.transparent)
        // (That new View is just there to have something inside the dialog that can grow big enough to cover the whole screen.)

        val body = dialogs.findViewById(R.id.textView2_content) as TextView
        val title = dialogs.findViewById(R.id.textView_title) as TextView
        back_button?.visibility = View.VISIBLE
        back_button?.setOnClickListener {
            onBackPressed()
        }
        body.setText(message)
        val yesBtn = dialogs.findViewById(com.jointdelivery.R.id.button_ok) as Button
        yesBtn.setOnClickListener {
            dialogs.dismiss()
        }

        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialogs.getWindow()!!.getAttributes())
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        dialogs.show()
        dialogs.getWindow()!!.setAttributes(lp)
    }


    private fun connectToSendBird(userId: String) {
        // Show the loading indicator


        ConnectionManager.login(userId, SendBird.ConnectHandler { user, e ->
            // Callback received; hide the progress bar.


            if (e != null) {
                // Error!
                Toast.makeText(
                    this@LoginActivity, "" + e.code + ": " + e.message,
                    Toast.LENGTH_SHORT
                )
                    .show()

                Log.d("ErrorMessage", e.code.toString())
                e.printStackTrace()
                authManager.setConnected(true)

                return@ConnectHandler
            }

            CommonUtil.hideProgress()
            Toast.makeText(applicationContext, "Connected to chat server", Toast.LENGTH_LONG).show()

            // Update the user's nickname
            /* updateCurrentUserInfo(userNickname)
             updateCurrentUserPushToken()*/

            updateCurrentUserInfo(authManager.getUserName())

        })
    }

    fun updateCurrentUserInfo(userNickname: String) {
        SendBird.updateCurrentUserInfo(userNickname, null, SendBird.UserInfoUpdateHandler { e ->
            if (e != null) {
                // Error!
                Toast.makeText(
                    this@LoginActivity, "" + e.code + ":" + e.message,
                    Toast.LENGTH_SHORT
                )
                    .show()

                // Show update failed snackbar
                CommonUtil.showSnackbar(main_layout, "Update user nickname failed")


                return@UserInfoUpdateHandler
            }

            authManager.setNickname(userNickname)
            val intent: Intent
            if (authManager.getBackgroundProcessDone()) {

                intent = Intent(this, HomeActivity::class.java)
            } else {
                intent = Intent(this, BackgroundProcessActivity::class.java)
            }

            startActivity(intent)
            finishAffinity()
        })
    }

    companion object {
        private val TAG = "LoginActivity==>"
    }
}
