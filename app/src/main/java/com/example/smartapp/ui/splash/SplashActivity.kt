package com.example.smartapp.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import com.example.smartapp.R
import com.example.smartapp.base.BaseActivity
import com.example.smartapp.data.preference.PreferenceManager
import com.example.smartapp.ui.dashboard.DashboardActivity
import com.example.smartapp.ui.login.LoginActivity
import com.example.smartapp.utils.AppConstants

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        Handler(Looper.getMainLooper()).postDelayed({
            if(PreferenceManager.getInstance(this)!!.getBoolean(AppConstants.IS_LOGGED_IN)){
                startActivity(Intent(this, DashboardActivity::class.java))
            }else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()

        }, 2000) // 2 seconds delay
    }
}