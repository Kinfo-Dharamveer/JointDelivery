package com.jointdelivery.appviewmodule.loginactivities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.jointdelivery.MyApplication
import com.jointdelivery.R
import com.jointdelivery.auth.AuthManager
import com.jointdelivery.appviewmodule.registeractivities.YourInfoActivity
import com.jointdelivery.utilities.CommonResponseModel
import com.jointdelivery.utilities.CommonUtil
import com.jointdelivery.utilities.Constants
import com.scyhealth.basemodule.interfaces.PostAPIResultCallback
import com.scyhealth.loginmodule.LoginApiMethods
import kotlinx.android.synthetic.main.activity_otp_screen_layout.*
import retrofit2.Response
import javax.inject.Inject

class OtpScreenActivity : AppCompatActivity(), PostAPIResultCallback<CommonResponseModel> {

    @Inject
    lateinit var authManager: AuthManager

    override fun onResponse(response: Response<CommonResponseModel>) {
        CommonUtil.hideProgress()

        if (response.body()!!.success) {
            CommonUtil.showShortToast(applicationContext, response.body()!!.message)
            val intent = Intent(this, YourInfoActivity::class.java)
            intent.putExtra(Constants.KEY_PHONE, authManager.getRegisteredPhoneNumber())
            startActivity(intent)
        } else {
            CommonUtil.showSnackbar(main_layout, response.body()!!.message)
        }
    }

    override fun onFailure() {
        CommonUtil.hideProgress()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_otp_screen_layout)
        (application as MyApplication).getAppComponent()?.inject(this)

        text_phone_no.setText(authManager.getRegisteredPhoneNumber())
        main_layout.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                when (event?.action) {
                    MotionEvent.ACTION_DOWN ->
                        CommonUtil.hideKeyboard(v!!, applicationContext)
                }

                return v?.onTouchEvent(event) ?: true
            }
        })
        txt_pin_entry.addTextChangedListener(object : TextWatcher {

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s != null && s.length == 6) {
                    if (CommonUtil.isConnectingToInternet(applicationContext)) {
                        CommonUtil.showProgress(this@OtpScreenActivity)
                        LoginApiMethods.verifyOtpWithPhoneNumber(
                            authManager.getRegisteredPhoneNumber(),
                            txt_pin_entry.text.toString().trim(), intent.getStringExtra("code"),
                            this@OtpScreenActivity
                        )
                    } else {
                        CommonUtil.showSnackbar(main_layout, resources.getString(R.string.please_connect_internet))
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable) {

            }
        })
    }

    fun onBackClick(view: View) {
        finish()
    }

    fun submit(view: View) {

        if (CommonUtil.isConnectingToInternet(applicationContext)) {
            CommonUtil.showProgress(this)
            LoginApiMethods.verifyOtpWithPhoneNumber(
                authManager.getRegisteredPhoneNumber(),
                txt_pin_entry.text.toString().trim(), intent.getStringExtra("code"),
                this
            )
        } else {
            CommonUtil.showSnackbar(main_layout, resources.getString(R.string.please_connect_internet))
        }

    }
}