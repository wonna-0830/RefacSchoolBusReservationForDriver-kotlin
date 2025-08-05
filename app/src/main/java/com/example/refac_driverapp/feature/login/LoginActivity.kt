package com.example.refac_driverapp.feature.login

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.lifecycle.lifecycleScope
import com.example.refac_driverapp.R
import com.example.refac_driverapp.feature.routetime.RouteTimeActivity
import com.example.refac_driverapp.viewmodel.LoginViewModel
import com.example.refac_driverapp.viewmodel.LoginViewModel.LoginUiState
import com.example.refac_driverapp.data.repository.UserRepository
import com.example.refac_driverapp.feature.register.RegisterActivity
import kotlinx.coroutines.flow.collectLatest

class LoginActivity : AppCompatActivity() {

    private val viewModel: LoginViewModel by viewModels {
        object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return LoginViewModel(UserRepository()) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val etEmail: EditText = findViewById(R.id.et_email)
        val etPwd: EditText = findViewById(R.id.et_pwd)
        val btnLogin: Button = findViewById(R.id.btn_login)
        val autoLogin: CheckBox = findViewById(R.id.autoLogin)
        val progressBar: ProgressBar = findViewById(R.id.progressBar)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val pwd = etPwd.text.toString()

            if (email.isBlank() || pwd.isBlank()) {
                Toast.makeText(this, "이메일과 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.login(email, pwd)
        }

        val btnRegister: Button = findViewById(R.id.btn_register)

        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.loginState.collectLatest { state ->
                when (state) {
                    is LoginUiState.Loading -> progressBar.visibility = ProgressBar.VISIBLE
                    is LoginUiState.Success -> {
                        progressBar.visibility = ProgressBar.GONE
                        // 자동 로그인 저장
                        getSharedPreferences("MyApp", MODE_PRIVATE).edit {
                            putBoolean("autoLogin", autoLogin.isChecked)
                            putString("userEmail", etEmail.text.toString())
                            putString("userPassword", etPwd.text.toString())
                        }
                        Toast.makeText(this@LoginActivity, "로그인 성공!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, RouteTimeActivity::class.java))
                        finish()
                    }
                    is LoginUiState.Error -> {
                        progressBar.visibility = ProgressBar.GONE
                        Toast.makeText(this@LoginActivity, "실패: ${state.message}", Toast.LENGTH_SHORT).show()
                    }
                    else -> Unit
                }
            }
        }
    }
}
