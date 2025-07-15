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
import com.google.firebase.database.FirebaseDatabase

class Login : ComponentActivity() {
    private lateinit var mEtEmail: EditText
    private lateinit var mEtPwd: EditText
    private lateinit var mBtnLogin: Button
    private lateinit var mBtnRegister: Button
    private lateinit var autoLogin: CheckBox
    private lateinit var auth: FirebaseAuth
    private var doubleBackToExitPressedOnce = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        mEtEmail = findViewById(R.id.et_email)
        mEtPwd = findViewById(R.id.et_pwd)
        mBtnLogin = findViewById(R.id.btn_login)
        mBtnRegister = findViewById(R.id.btn_register)
        autoLogin = findViewById(R.id.autoLogin)
        autoLogin.isChecked = false


        mBtnLogin.setOnClickListener {
            val strEmail = mEtEmail.text.toString()
            val strPwd = mEtPwd.text.toString()
            val checkBoxAutoLogin = findViewById<CheckBox>(R.id.autoLogin)

            when {
                strEmail.isEmpty() -> {
                    Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                strPwd.isEmpty() -> {
                    Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                !strEmail.contains("@") -> {
                    Toast.makeText(this, "알맞지 않은 이메일 형식입니다.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }


            auth.signInWithEmailAndPassword(strEmail, strPwd)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        val uid = user?.uid

                        if (uid != null) {
                            // Realtime DB에서 isBanned 확인
                            val userRef =
                                FirebaseDatabase.getInstance().getReference("drivers").child(uid)
                            userRef.get().addOnSuccessListener { snapshot ->
                                val isBanned =
                                    snapshot.child("isBanned").getValue(Boolean::class.java)
                                        ?: false

                                if (isBanned) {
                                    Toast.makeText(
                                        this,
                                        "정지된 계정입니다. 관리자에게 문의하세요.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    auth.signOut() // 강제 로그아웃
                                } else {
                                    // 🔐 자동 로그인 정보 저장
                                    val sharedPref = getSharedPreferences("MyApp", MODE_PRIVATE)
                                    with(sharedPref.edit()) {
                                        putBoolean("autoLogin", checkBoxAutoLogin.isChecked)
                                        putString("userEmail", strEmail)
                                        putString("userPassword", strPwd)
                                        apply()
                                    }

                                    Toast.makeText(this, "로그인에 성공하셨습니다.", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this, RouteTime::class.java))
                                    finish()
                                }
                            }.addOnFailureListener {
                                Toast.makeText(this, "유저 정보 확인 중 오류가 발생했습니다.", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }
                }
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