package com.example.smartapp.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.smartapp.R
import com.example.smartapp.base.BaseActivity
import com.example.smartapp.data.AppDatabase
import com.example.smartapp.data.preference.PreferenceManager
import com.example.smartapp.data.tables.AppInfo
import com.example.smartapp.ui.configuration.ConfigurationActivity
import com.example.smartapp.ui.dashboard.DashboardActivity
import com.example.smartapp.ui.login.LoginActivity
import com.example.smartapp.utils.AppConstants
import com.example.smartapp.utils.isDevicesConfigured
import kotlinx.coroutines.launch

class SplashActivity : BaseActivity() {

    var fetchedAppInfoDao: AppInfo? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        addAppInfoDataToDB()
    }

    private fun manageNavigation() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (PreferenceManager.getInstance(this)!!.getBoolean(AppConstants.IS_LOGGED_IN)) {

                // apply isConfiguration check on below line
                if(isDevicesConfigured(fetchedAppInfoDao)){
                    startActivity(Intent(this, DashboardActivity::class.java))
                }else {
                    startActivity(Intent(this, ConfigurationActivity::class.java))
                }
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()

        }, 2000) // 2 seconds delay
    }

    private fun addAppInfoDataToDB() {
        lifecycleScope.launch {
            val appInfoDao = AppDatabase.getDatabase(this@SplashActivity).appInfoDao()
            fetchedAppInfoDao = appInfoDao.getAppInfo()
            if (fetchedAppInfoDao == null) {
                val addInfo =
                    AppInfo(isDevicesAddedInConfigMode = false, isMacAddressUpdated = false)
                appInfoDao.insertInfo(addInfo)
            }

            manageNavigation()

        }
    }
    }

