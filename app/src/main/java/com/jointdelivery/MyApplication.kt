package com.jointdelivery

import android.app.Application
import com.jointdelivery.di.AppComponent
import com.jointdelivery.di.ApplicationModule
import com.jointdelivery.di.DaggerAppComponent
import com.jointdelivery.utilities.Constants
import com.sendbird.android.SendBird

open class MyApplication: Application() {
    private var sAppComponent: AppComponent? = null

    fun getAppComponent(): AppComponent? {
        if (sAppComponent == null) {
            buildAppComponent()
        }
        return sAppComponent
    }

    override fun onCreate() {
        super.onCreate()
        buildAppComponent()
        SendBird.init(Constants.CHAT_SEND_BIRD_ID, applicationContext);
    }

    private fun buildAppComponent() {
        sAppComponent = DaggerAppComponent
            .builder()
            .applicationModule(ApplicationModule(this))
            .build()
    }
}
