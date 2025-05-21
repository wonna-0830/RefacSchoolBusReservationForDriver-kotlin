package com.example.refac_driverapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.compose.animation.core.snap
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.view.WindowManager

class Clock : ComponentActivity() {
    // 실시간 시계용 변수
    private lateinit var textCurrentTime: TextView
    private val timeHandler = android.os.Handler()
    private lateinit var timeRunnable: Runnable
    private lateinit var btnCheck: Button

    private var finishClickCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clock)
        btnCheck = findViewById(R.id.startButton)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerStations)
        recyclerView.layoutManager = LinearLayoutManager(this)
        val textRouteName = findViewById<TextView>(R.id.textRouteName)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        /* 기사님이 선택한 노선과 시간 받기 (RouteTime 클래스에서) */
        val selectedRoute = intent.getStringExtra("route") ?: ""
        val selectedTime = intent.getStringExtra("time")?:""

        //TextView에 들어갈 노선과 시간을 동적으로 연결
        textRouteName.text = "$selectedRoute ($selectedTime)"
        textCurrentTime = findViewById(R.id.textCurrentTime)
        // 현재 시각을 계속 업데이트할 Runnable
        timeRunnable = object : Runnable {
            override fun run() {
                val currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                textCurrentTime.text = currentTime
                timeHandler.postDelayed(this, 1000) // 1초마다 반복
            }
        }

        timeHandler.post(timeRunnable) // 처음 시작

        /* 노선에 따라 정류장이 다르기 때문에 동적으로 정류장을 보여주려면 목록을 만들어야함 */
        val stationNames = when (selectedRoute) {
            "교내순환" -> listOf("정문", "B1", "C7", "C13", "D6", "A2(건너편)")
            "하양역->교내순환" -> listOf("하양역", "정문", "B1", "C7", "C13", "D6", "A2(건너편)")
            "안심역->교내순환" -> listOf("안심역(3번출구)", "정문", "B1", "C7", "C13", "D6", "A2(건너편)")
            "사월역->교내순환" -> listOf("사월역(3번출구)", "정문", "B1", "C7", "C13", "D6", "A2(건너편)")
            "A2->안심역->사월역" -> listOf("A2(건너편)", "안심역", "사월역")
            else -> emptyList()
        }

        val database = FirebaseDatabase.getInstance().reference
        /* Firebase에서 전체 사용자 예약을 불러와 그 안의 reservation을 하나하나 확인 */
        database.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val stationCountMap = mutableMapOf<String, Int>()
                stationNames.forEach { stationCountMap[it] = 0 }

                for (userSnapshot in snapshot.children) {
                    val reservations = userSnapshot.child("reservations").children
                    for (reservation in reservations) {
                        val route = reservation.child("route").getValue(String::class.java)
                        val station = reservation.child("station").getValue(String::class.java) ?: continue
                        val time = reservation.child("time").getValue(String::class.java)
                        val date = reservation.child("date").getValue(String::class.java)
                        val today = SimpleDateFormat("yy-MM-dd", Locale.getDefault()).format(Date())

                        if (route == selectedRoute && time == selectedTime && date == today && station in stationCountMap) {
                            stationCountMap[station] = stationCountMap[station]!! + 1
                        }
                    }
                }

                val stationInfoList = stationNames.map { station ->
                    StationInfo(station, stationCountMap[station] ?: 0)
                }

                recyclerView.adapter = ClockAdapter(stationInfoList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "실시간 데이터 수신 실패: ${error.message}")
            }
        })


        //뒤로가기 클릭 시 페이지 이동, 탈주 방지 (운전 중에 사용하는 페이지이기 때문에 막음)
        onBackPressedDispatcher.addCallback(this) {
            Toast.makeText(this@Clock, "운전 중에는 뒤로가기 버튼이 비활성화됩니다.", Toast.LENGTH_SHORT).show()
        }


        btnCheck.setOnClickListener {
            //운행종료 버튼도
            if (finishClickCount == 0) {
                Toast.makeText(this, "한 번 더 누르면 운행이 종료됩니다.", Toast.LENGTH_SHORT).show()
                finishClickCount++
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

                //RouteTime에서 Clock으로 보낸 노선과 시간을 다시 Finish 페이지로 넘김
                val route = intent.getStringExtra("route")
                val time = intent.getStringExtra("time")
                val date = intent.getStringExtra("date")
                val uniqueKey = intent.getStringExtra("pushKey")
                val endTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())

                //운행종료 버튼 클릭 시점의 나머지 endTime 부분을 데이터베이스에 저장
                val currentUser = FirebaseAuth.getInstance().currentUser
                val ref = FirebaseDatabase.getInstance().reference
                ref.child("drivers")
                    .child(currentUser!!.uid)
                    .child("drived")
                    .child(uniqueKey!!)
                    .child("endTime")
                    .setValue(endTime)

                // 정상 선택 후 처리
                val intent = Intent(this, Finish::class.java)
                intent.putExtra("pushKey", uniqueKey)
                startActivity(intent)
                finish()
            }

        }
    }
    override fun onDestroy() {
        super.onDestroy()
        timeHandler.removeCallbacks(timeRunnable) // 메모리 누수 방지!!
    }

}