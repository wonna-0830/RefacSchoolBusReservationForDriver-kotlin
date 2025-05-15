package com.example.refac_driverapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import com.google.firebase.auth.FirebaseAuth
import androidx.core.content.edit

class Login : ComponentActivity() {
    private lateinit var etEmail: EditText
    private lateinit var etPwd: EditText
    private lateinit var btnLogin: Button
    private lateinit var mBtnRegister: Button
    private lateinit var autoLogin: CheckBox
    private lateinit var auth: FirebaseAuth
    private var doubleBackToExitPressedOnce = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        etEmail = findViewById(R.id.et_email)
        etPwd = findViewById(R.id.et_pwd)
        btnLogin = findViewById(R.id.btn_login)
        mBtnRegister = findViewById(R.id.btn_register)
        autoLogin = findViewById(R.id.autoLogin)
        autoLogin.isChecked = false


        val prefs = getSharedPreferences("login_prefs", MODE_PRIVATE)
        val isAutoLogin = prefs.getBoolean("autoLogin", false)

        Log.d("AutoLogin", "자동 로그인 상태: $autoLogin")

        if (isAutoLogin) {
            val email = prefs.getString("email", null)
            val password = prefs.getString("password", null)

            if (!email.isNullOrEmpty() && !password.isNullOrEmpty()) {
                Log.d("AutoLogin", "자동 로그인 시도 중: $email")
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        Toast.makeText(this, "자동 로그인 성공!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, RouteTime::class.java)
                        startActivity(intent)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "자동 로그인 실패", Toast.LENGTH_SHORT).show()
                    }
            }
        }

        //로그인 버튼 클릭 시 이메일 or 비밀번호 입력 안했을 때 로그인 불가 로직
        //+자동 로그인 버튼 클릭해놨을 때 자동으로 로그인 돼 메인 페이지로 넘어가게 하는 로직
        btnLogin.setOnClickListener{
            val email = etEmail.text.toString().trim()
            val password = etPwd.text.toString().trim()


            if (email.isEmpty() || password.isEmpty() || !email.contains("@")){
                Toast.makeText(this, "이메일과 비밀번호를 모두 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ✅ 여기서 체크박스 상태 가져와서 저장!
            val isChecked = autoLogin.isChecked
            val editor = prefs.edit()
            editor.putString("email", email)
            editor.putString("password", password)
            editor.putBoolean("autoLogin", isChecked)
            editor.apply()

            loginUser(email, password)
        }

        // 회원가입 버튼 → RegisterActivity 이동
        mBtnRegister.setOnClickListener {
            startActivity(Intent(this, Register::class.java))
            finish()
        }

        // 뒤로 두 번 누르면 앱 종료
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finishAffinity()
                    return
                }

                doubleBackToExitPressedOnce = true
                Toast.makeText(this@Login, "한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()

                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000)
            }
        })
    }
    private fun loginUser(email: String, password: String){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                val progressBar = findViewById<ProgressBar>(R.id.progressBar)
                progressBar.visibility = View.GONE //로그인 성공 바로 전에 로딩 아이콘 숨김 처리
                if(task.isSuccessful){
                    Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, RouteTime::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "로그인 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}