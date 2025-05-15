package com.example.refac_driverapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RouteTime : ComponentActivity() {
    private lateinit var spinnerRoute: Spinner
    private lateinit var spinnerTime: Spinner
    private lateinit var btnCheck: Button
    private var doubleBackToExitPressedOnce = false

    //노선에 따른 시간을 보여주기 위한 map 정의 (노선이 key가 되고 그 key를 기준으로 시간 설정)
    private val routeMap = mapOf(
        "교내순환" to listOf("시간을 선택하세요", "08:30", "08:35", "08:40", "08:55", "09:00"
            , "09:30", "09:40", "09:50", "10:00", "10:10", "10:20", "10:30"
            , "10:40", "10:50", "11:00"),
        "사월역->교내순환" to listOf("시간을 선택하세요", "08:00", "08:05", "08:20", "08:40", "09:00"
            , "09:30", "09:50", "10:00"),
        "하양역->교내순환" to listOf("시간을 선택하세요", "08:30", "08:50", "08:57", "09:10"),
        "안심역->교내순환" to listOf("시간을 선택하세요", "08:10", "08:20", "08:25", "08:30", "08:40"
            , "08:50", "09:00", "09:30", "09:50", "10:10", "10:20", "10:30"
            , "12:20"),
        "A2->안심역->사월역" to listOf("시간을 선택하세요", "16:00", "16:15", "16:30", "16:40", "16:50", "17:00", "17:16", "17:30")
    )

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_time)

        spinnerRoute = findViewById(R.id.SpinnerRoute)
        spinnerTime = findViewById(R.id.SpinnerTime)
        btnCheck = findViewById(R.id.reservation)

        val routeList = listOf("노선을 선택해주세요") + routeMap.keys.toList()
        val routeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, routeList)
        routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRoute.adapter = routeAdapter //어댑터를 spinnerRoute에 연결

        //노선 선택시 시간 리스트 변경
        spinnerRoute.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedRoute = routeList[position]
                val rawTimeList = routeMap[selectedRoute] ?: emptyList()

                if (selectedRoute == "노선을 선택해주세요") {
                    spinnerTime.adapter = null
                    return
                }

                // 현재 시간 기준 분 단위로 계산
                val now = Calendar.getInstance()
                val currentHour = now.get(Calendar.HOUR_OF_DAY)
                val currentMinute = now.get(Calendar.MINUTE)
                val currentTimeInMinutes = currentHour * 60 + currentMinute

                // 현재 시간 기준 필터
                val filteredTimeList = rawTimeList.filter { time ->
                    if (!time.contains(":")) return@filter false
                    val parts = time.split(":")
                    val hour = parts[0].toInt()
                    val minute = parts[1].toInt()
                    val totalMinutes = hour * 60 + minute

                    totalMinutes > currentTimeInMinutes
                }

                // 안내 문구 추가
                val timeListWithPrompt = listOf("시간을 선택하세요") + filteredTimeList
                val timeAdapter = ArrayAdapter(this@RouteTime, android.R.layout.simple_spinner_item, timeListWithPrompt)
                timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerTime.adapter = timeAdapter

                val selectedTime = spinnerTime.selectedItem.toString()
                if (selectedTime == "시간을 선택하세요") return
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        // 확인 버튼 클릭 시 Clock 페이지로 이동
        btnCheck.setOnClickListener {
            val selectedRouteItem = spinnerRoute.selectedItem
            val selectedTimeItem = spinnerTime.selectedItem
            val selectedRoute = selectedRouteItem.toString()
            val selectedTime = selectedTimeItem.toString()

            if (selectedRouteItem == null || selectedRouteItem.toString() == "노선을 선택해주세요") {
                Toast.makeText(this, "노선을 선택해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedTimeItem == null || selectedTimeItem.toString() == "시간을 선택하세요") {
                Toast.makeText(this, "시간을 선택해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            //데이터베이스에 저장
            val route = spinnerRoute.selectedItem.toString()
            val time = spinnerTime.selectedItem.toString()
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val currentUser = FirebaseAuth.getInstance().currentUser
            val ref = FirebaseDatabase.getInstance().reference
            val uniquekey = "$selectedRoute|$selectedTime|$date"
            val record = DrivedRecord(route, time, "", date, uniquekey)

            // ✅ Firebase에 운행 시작 정보 저장
            ref.child("drivers")
                .child(currentUser!!.uid)
                .child("drived")
                .child(uniquekey)
                .setValue(record)
                .addOnSuccessListener {
                    // 저장 성공하면 Clock 페이지로 이동 (pushKey 같이 전달!)
                    val intent = Intent(this, Clock::class.java)
                    intent.putExtra("route", route)
                    intent.putExtra("time", time)
                    intent.putExtra("date", date)
                    intent.putExtra("pushKey", uniquekey)
                    startActivity(intent)
                }
                .addOnFailureListener {
                    Toast.makeText(this, "운행 정보 저장 실패 😢", Toast.LENGTH_SHORT).show()
                }

        }
        // 뒤로 두 번 누르면 앱 종료
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finishAffinity()
                    return
                }

                doubleBackToExitPressedOnce = true
                Toast.makeText(this@RouteTime, "한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()

                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000)
            }
        })

    }
}