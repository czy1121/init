package com.demo.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import me.reezy.cosmo.init.InitManager
import me.reezy.demo.init.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        InitManager.launch(application, false, modules = listOf("app"))
    }
}