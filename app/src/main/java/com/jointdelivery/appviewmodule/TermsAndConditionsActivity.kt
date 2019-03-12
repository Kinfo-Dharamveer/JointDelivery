package com.jointdelivery.appviewmodule

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jointdelivery.R
import kotlinx.android.synthetic.main.tool_bar_layout.*

class TermsAndConditionsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_conditions)
        toolbar_title.setText(resources.getString(R.string.termaandconditions))

        back_button.setOnClickListener {
            onBackPressed()
        }
    }

}