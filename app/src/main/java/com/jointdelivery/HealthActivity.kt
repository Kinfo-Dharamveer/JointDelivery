package com.jointdelivery

import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.health_layout.*

class HealthActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.health_layout)

        spinner3.adapter =
            ArrayAdapter<String>(
                this,
                R.layout.radio_spinner_item,
                resources.getStringArray(R.array.spinner_health_Type)
            )
    }
}