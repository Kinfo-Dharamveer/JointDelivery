package com.jointdelivery.appviewmodule

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.jointdelivery.MyApplication
import com.jointdelivery.R
import com.jointdelivery.adapters.MessagesAdapter
import com.jointdelivery.adapters.NotificationAdapter
import com.jointdelivery.auth.AuthManager
import kotlinx.android.synthetic.main.activity_notifications.*
import kotlinx.android.synthetic.main.tool_bar_layout.*

import javax.inject.Inject

open class NotificationsActivity : AppCompatActivity() {


    lateinit var messagesAdapter: NotificationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        initView()
        toolbar_title.setText(resources.getString(R.string.notification))
        back_button.setOnClickListener {
            onBackPressed()
        }
    }


    fun initView() {

        rv_notificatons_list.layoutManager = LinearLayoutManager(this)
        messagesAdapter = NotificationAdapter(
            this
        )
        rv_notificatons_list.adapter = messagesAdapter
    }


}