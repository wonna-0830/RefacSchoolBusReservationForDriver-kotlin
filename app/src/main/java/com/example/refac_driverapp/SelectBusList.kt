package com.example.refac_driverapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Locale


class SelectBusList : AppCompatActivity() {
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selectbuslist)

        val currentUser = FirebaseAuth.getInstance().currentUser
        val database = FirebaseDatabase.getInstance().reference
        val drivedList = ArrayList<DrivedRecord>()

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val textNoReservation = findViewById<TextView>(R.id.textNoReservation)
        val backBtn = findViewById<Button>(R.id.btn_home)

        val decoration = RecyclerViewDecoration(20)
        recyclerView.addItemDecoration(decoration)

        progressBar.visibility = View.VISIBLE

        database.child("drivers")
            .child(currentUser!!.uid)
            .child("drived")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    drivedList.clear()
                    for (data in snapshot.children) {
                        val record = data.getValue(DrivedRecord::class.java)
                        record?.pushKey = data.key ?: ""
                        if (record != null) {
                            drivedList.add(record)
                        }
                    }
                    progressBar.visibility = View.GONE

                    // 최신 날짜순 정렬 (내림차순)
                    val sortedList = drivedList.sortedByDescending {
                        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
                    }

                    if (drivedList.isEmpty()) {
                        textNoReservation.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        textNoReservation.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE

                        recyclerView.layoutManager = LinearLayoutManager(this@SelectBusList)
                        recyclerView.adapter = DriveAdapter(
                            ArrayList(sortedList),
                            onListEmpty = {
                                textNoReservation.visibility = View.VISIBLE
                                recyclerView.visibility = View.GONE
                            },
                            onItemClick = { record ->
                                val intent = Intent(this@SelectBusList, Finish::class.java)
                                intent.putExtra("pushKey", record.pushKey)  // ← 여기서 정확하게 전달됨!
                                startActivity(intent)
                                finish()
                            }
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@SelectBusList, "데이터를 불러오지 못했습니다", Toast.LENGTH_SHORT).show()
                }
            })
        backBtn.setOnClickListener{
            if (drivedList.isNotEmpty()) {
                val recentRecord = drivedList.maxByOrNull {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
                }

                val intent = Intent(this@SelectBusList, Finish::class.java)
                intent.putExtra("pushKey", recentRecord?.pushKey)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "운행 기록이 없습니다.", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(this@SelectBusList, "한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()

                Handler(Looper.getMainLooper()).postDelayed({
                    doubleBackToExitPressedOnce = false
                }, 2000)
            }
        })


    }
}