package com.example.refac_driverapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RouteTime : ComponentActivity() {
    private lateinit var spinnerRoute: Spinner
    private lateinit var spinnerTime: Spinner
    private lateinit var btnCheck: Button
    private var doubleBackToExitPressedOnce = false
    private lateinit var routeMap: HashMap<String, List<String>> // 노선명 → 시간 리스트


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_time)

        spinnerRoute = findViewById(R.id.SpinnerRoute)
        spinnerTime = findViewById(R.id.SpinnerTime)
        btnCheck = findViewById(R.id.reservation)

        routeMap = HashMap()

        val dbRef = FirebaseDatabase.getInstance().getReference("routes")
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val routeList = mutableListOf("노선을 선택해주세요")
                val tempRouteMap = HashMap<String, List<String>>() // 실시간용 임시 맵

                for (routeSnapshot in snapshot.children) {
                    val isPinned = routeSnapshot.child("isPinned").getValue(Boolean::class.java) ?: false
                    if (!isPinned) continue

                    val routeName = routeSnapshot.child("name").getValue(String::class.java) ?: continue
                    val timeList = mutableListOf<String>()
                    for (timeSnap in routeSnapshot.child("times").children) {
                        timeSnap.getValue(String::class.java)?.let { timeList.add(it) }
                    }
                    timeList.sort()
                    tempRouteMap[routeName] = timeList
                    routeList.add(routeName)

                }
                routeMap = tempRouteMap

                val routeAdapter = ArrayAdapter(this@RouteTime, android.R.layout.simple_spinner_item, routeList)
                routeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerRoute.adapter = routeAdapter
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@RouteTime, "노선 정보를 불러오지 못했어요 🥲", Toast.LENGTH_SHORT).show()
            }
        })

        spinnerRoute.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedRoute = spinnerRoute.selectedItem.toString()
                if (selectedRoute == "노선을 선택해주세요") {
                    spinnerTime.adapter = null
                    return
                }

                val rawTimeList = routeMap[selectedRoute] ?: emptyList()
                Log.d("RouteDebug", "선택된 노선: $selectedRoute")
                Log.d("RouteDebug", "해당 노선의 시간 리스트: ${routeMap[selectedRoute]}")

                val now = Calendar.getInstance()
                val currentTimeInMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)

                val filteredTimeList = rawTimeList.filter { time ->
                    if (!time.contains(":")) return@filter false
                    val (hour, minute) = time.split(":").map { it.toIntOrNull() ?: return@filter false }
                    hour * 60 + minute > currentTimeInMinutes
                }

                val timeListWithPrompt = listOf("시간을 선택하세요") + filteredTimeList
                val timeAdapter = ArrayAdapter(this@RouteTime, android.R.layout.simple_spinner_item, timeListWithPrompt)
                timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinnerTime.post {
                    spinnerTime.adapter = timeAdapter
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnCheck.setOnClickListener {
            val selectedRoute = spinnerRoute.selectedItem?.toString()
            val selectedTime = spinnerTime.selectedItem?.toString()

            if (selectedRoute == null || selectedRoute == "노선을 선택해주세요") {
                Toast.makeText(this, "노선을 선택해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedTime == null || selectedTime == "시간을 선택하세요") {
                Toast.makeText(this, "시간을 선택해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val currentUser = FirebaseAuth.getInstance().currentUser
            val ref = FirebaseDatabase.getInstance().reference
            val uniqueKey = "$selectedRoute|$selectedTime|$date"
            val record = DrivedRecord(selectedRoute, selectedTime, "", date, uniqueKey)

            ref.child("drivers")
                .child(currentUser!!.uid)
                .child("drived")
                .child(uniqueKey)
                .setValue(record)
                .addOnSuccessListener {
                    val intent = Intent(this, Clock::class.java)
                    intent.putExtra("route", selectedRoute)
                    intent.putExtra("time", selectedTime)
                    intent.putExtra("date", date)
                    intent.putExtra("pushKey", uniqueKey)
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