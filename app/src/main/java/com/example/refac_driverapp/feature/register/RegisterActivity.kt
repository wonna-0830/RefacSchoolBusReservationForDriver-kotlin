package com.example.refac_driverapp.feature.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.refac_driverapp.R
import com.example.refac_driverapp.data.repository.UserRepository
import com.example.refac_driverapp.feature.login.LoginActivity
import com.example.refac_driverapp.feature.register.RegisterViewModel.RegisterState
import kotlinx.coroutines.flow.collectLatest

class RegisterActivity : AppCompatActivity() {

    private val viewModel: RegisterViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return RegisterViewModel(UserRepository()) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val etEmail = findViewById<EditText>(R.id.et_email)
        val etPwd = findViewById<EditText>(R.id.et_pwd)
        val etName = findViewById<EditText>(R.id.et_name)
        val btnRegister = findViewById<Button>(R.id.btn_register)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPwd.text.toString().trim()
            val name = etName.text.toString().trim()

            viewModel.register(email, password, name)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.registerState.collectLatest { state ->
                when (state) {
                    is RegisterState.Loading -> progressBar.visibility = View.VISIBLE
                    is RegisterState.Success -> {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@RegisterActivity, "회원가입 성공!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                        finish()
                    }
                    is RegisterState.Error -> {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@RegisterActivity, state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }
}
