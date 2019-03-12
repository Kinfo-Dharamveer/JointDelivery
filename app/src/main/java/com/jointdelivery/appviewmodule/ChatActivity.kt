package com.jointdelivery.appviewmodule

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.jointdelivery.R
import com.jointdelivery.utilities.CommonUtil
import com.jointdelivery.utilities.Constants
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions
import kotlinx.android.synthetic.main.activity_chat_layout.*
import kotlinx.android.synthetic.main.tool_bar_layout.*

open class ChatActivity : AppCompatActivity() {
    lateinit var dialog: AlertDialog
    lateinit var imageUri: Uri

    lateinit var picturePath: String
    internal lateinit var emojIcon: EmojIconActions
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_layout)
        back_button.visibility = View.VISIBLE

        emojIcon = EmojIconActions(this, main_layout, edittext_chatbox, emojiImage)
        emojIcon.ShowEmojIcon()
        emojIcon.setIconsIds(R.drawable.ic_action_keyboard, R.drawable.smiley)
        emojIcon.setKeyboardListener(object : EmojIconActions.KeyboardListener {
            override fun onKeyboardClose() {
            }

            override fun onKeyboardOpen() {
            }

        })

        fileSelect.setOnClickListener {
            if (CommonUtil.isConnectingToInternet(this)) {

                if (!CommonUtil.hasPermissions(this, Constants.CAMERA_PERMISSIONS)) {
                    ActivityCompat.requestPermissions(this, Constants.CAMERA_PERMISSIONS, Constants.PERMISSION_ALL)
                } else {
                    openCustomDialog()
                }

            } else {
                CommonUtil.showSnackbar(main_layout, resources.getString(R.string.please_connect_internet))
            }

        }


    }

    fun emojiClick(view: View) {

    }

    fun openCustomDialog() {
        val inflater = layoutInflater
        val alertLayout = inflater.inflate(R.layout.dialog_choose_file_type, null)

        val alert = AlertDialog.Builder(this)
        alert.setView(alertLayout)

        dialog = alert.create()
        dialog.show()
    }


    fun onCameraClick(view: View) {
        dialog.dismiss()
        val content = ContentValues()
        content.put(MediaStore.Images.Media.TITLE, "New Picture")
        content.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, content)
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        startActivityForResult(intent, 1)
    }

    fun onGalleryClick(view: View) {
        dialog.dismiss()
        val pickPhotoIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhotoIntent, 2)
    }

    override fun onActivityReenter(resultCode: Int, data: Intent?) {
        super.onActivityReenter(resultCode, data)
    }
}