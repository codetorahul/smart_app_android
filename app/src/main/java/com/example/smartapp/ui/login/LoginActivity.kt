package com.example.smartapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import com.example.smartapp.base.BaseActivity
import com.example.smartapp.data.AppDatabase
import com.example.smartapp.data.preference.PreferenceManager
import com.example.smartapp.databinding.ActivityLoginBinding
import com.example.smartapp.ui.configuration.ConfigurationActivity
import com.example.smartapp.ui.dashboard.DashboardActivity
import com.example.smartapp.utils.AppConstants
import com.example.smartapp.utils.isDevicesConfigured
import kotlinx.coroutines.launch

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.loginButton.setOnClickListener {

            var isValid = true

            // Email validation
            if (!isValidEmail(binding.emailEditText.text.toString().trim())) {
                binding.emailErrorTextView.visibility = TextView.VISIBLE
                binding.emailErrorTextView.text = "Invalid email format"
                isValid = false
            } else if (binding.emailEditText.text.toString().trim() != AppConstants.DEMO_EMAIL) {
                binding.emailErrorTextView.visibility = TextView.VISIBLE
                binding.emailErrorTextView.text = "Wrong Email."
                isValid = false
            } else {
                binding.emailErrorTextView.visibility = TextView.GONE
            }

            // Password validation
            if (binding.passwordEditText.text.toString().trim().length < 6) {
                binding.passwordErrorTextView.visibility = TextView.VISIBLE
                binding.passwordErrorTextView.text = "Password must be at least 6 characters"
                isValid = false
            } else if (binding.passwordEditText.text.toString()
                    .trim() != AppConstants.DEMO_PASSWORD
            ) {
                binding.passwordErrorTextView.visibility = TextView.VISIBLE
                binding.passwordErrorTextView.text = "Wrong Password"
                isValid = false
            } else {
                binding.passwordErrorTextView.visibility = TextView.GONE
            }

            if (isValid) {
                PreferenceManager.getInstance(this)!!.putBoolean(AppConstants.IS_LOGGED_IN, true)

                lifecycleScope.launch {
                    val appInfoDao = AppDatabase.getDatabase(this@LoginActivity).appInfoDao()
                    val fetchedAppInfoDao = appInfoDao.getAppInfo()
                    if (fetchedAppInfoDao != null) {
                        if (isDevicesConfigured(fetchedAppInfoDao)) {
                            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))

                        } else {
                            startActivity(
                                Intent(
                                    this@LoginActivity,
                                    ConfigurationActivity::class.java
                                )
                            )

                        }

                        finish()
                    }else{
                        startActivity(
                            Intent(
                                this@LoginActivity,
                                ConfigurationActivity::class.java
                            )
                        )
                        finish()
                    }
                    // Handle successful login (e.g., network request, navigate to another activity)
                    //  Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                }
            }
        }


    }

    // Email validation function
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}