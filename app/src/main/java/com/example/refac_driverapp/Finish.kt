package com.example.refac_driverapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Finish : AppCompatActivity() {
    private var doubleBackToExitPressedOnce = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish)

        val btnLogout = findViewById<Button>(R.id.logOutButton)
        val btnSelectRoute = findViewById<Button>(R.id.nextButton)
        val btnExitApp = findViewById<Button>(R.id.finishApp)
        val selectBus = findViewById<Button>(R.id.selectBus)

        //Firebase에 저장된 노선과 시간 정의 + 요소와 연결 후 텍스트 보이기
        val pushKey = intent.getStringExtra("pushKey")

        if (pushKey != null) {
            val currentUser = FirebaseAuth.getInstance().currentUser!!
            val ref = FirebaseDatabase.getInstance().reference
                .child("drivers")
                .child(currentUser.uid)
                .child("drived")
                .child(pushKey)

            ref.get().addOnSuccessListener { snapshot ->
                val route = snapshot.child("route").getValue(String::class.java) ?: "알 수 없음"
                val time = snapshot.child("time").getValue(String::class.java) ?: "알 수 없음"
                val endTime = snapshot.child("endTime").getValue(String::class.java) ?: "종료 시간 없음"

                findViewById<TextView>(R.id.drivedRoute).text = "노선: $route"
                findViewById<TextView>(R.id.drivedTime).text = "시간: $time"
                findViewById<TextView>(R.id.finishTime).text = "종료시간: $endTime"

            }.addOnFailureListener {
                Toast.makeText(this, "운행 정보 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w("Finish", "pushKey가 null이라 운행 정보 못 가져옴!")
        }

        // 로그아웃 버튼
        btnLogout.setOnClickListener {
            Log.d("Finish", "로그아웃 버튼 눌림")
            AlertDialog.Builder(this)
                .setTitle("로그아웃")
                .setMessage("정말 로그아웃하시겠습니까?")
                .setPositiveButton("예") { _, _ ->
                    FirebaseAuth.getInstance().signOut()

                    val pref = getSharedPreferences("login_prefs", MODE_PRIVATE)
                    val editor = pref.edit()
                    editor.clear()
                    editor.commit()

                    val intent = Intent(this, Login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("아니오", null)
                .show()
        }

        selectBus.setOnClickListener{
            val intent = Intent(this, SelectBusList::class.java)
            startActivity(intent)
            finish()
        }

        // 다른 노선 선택 버튼
        btnSelectRoute.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("노선 선택")
                .setMessage("처음으로 돌아가시겠습니까?")
                .setPositiveButton("예") { _, _ ->
                    val intent = Intent(this, RouteTime::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("아니오", null)
                .show()
        }

        // 앱 종료 버튼
        btnExitApp.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("앱 종료")
                .setMessage("정말 종료하시겠습니까?")
                .setPositiveButton("예") { _, _ ->
                    finishAffinity()
                    System.exit(0)
                }
                .setNegativeButton("아니오", null)
                .show()
        }

        // 뒤로 두 번 누르면 앱 종료
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finishAffinity()
                    return
                }

                doubleBackToExitPressedOnce = true
                Toast.makeText(this@Finish, "한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()

                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000)
            }
        })
        Log.d("Finish", "onCreate 끝까지 실행됨")

    }
}