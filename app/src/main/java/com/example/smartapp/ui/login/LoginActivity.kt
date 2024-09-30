package com.example.smartapp.ui.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.setContent
import com.example.smartapp.R
import com.example.smartapp.base.BaseActivity
import com.example.smartapp.data.preference.PreferenceManager
import com.example.smartapp.databinding.ActivityDashboardBinding
import com.example.smartapp.databinding.ActivityLoginBinding
import com.example.smartapp.ui.dashboard.DashboardActivity
import com.example.smartapp.utils.AppConstants

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
            }
            else if (binding.emailEditText.text.toString().trim() != AppConstants.DEMO_EMAIL) {
                binding.emailErrorTextView.visibility = TextView.VISIBLE
                binding.emailErrorTextView.text = "Wrong Email."
                isValid = false
            }else{
                binding.emailErrorTextView.visibility = TextView.GONE
            }

            // Password validation
            if (binding.passwordEditText.text.toString().trim().length < 6) {
                binding.passwordErrorTextView.visibility = TextView.VISIBLE
                binding.passwordErrorTextView.text = "Password must be at least 6 characters"
                isValid = false
            }
            else if(binding.passwordEditText.text.toString().trim() != AppConstants.DEMO_PASSWORD){
                binding.passwordErrorTextView.visibility = TextView.VISIBLE
                binding.passwordErrorTextView.text = "Wrong Password"
                isValid = false
            }else {
                binding.passwordErrorTextView.visibility = TextView.GONE
            }

            if (isValid) {
                PreferenceManager.getInstance(this)!!.putBoolean(AppConstants.IS_LOGGED_IN, true)
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
                // Handle successful login (e.g., network request, navigate to another activity)
              //  Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
            }
        }


    }

    // Email validation function
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}