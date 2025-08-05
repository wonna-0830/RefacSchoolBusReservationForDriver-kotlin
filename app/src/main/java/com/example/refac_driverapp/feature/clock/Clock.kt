package com.example.refac_driverapp.feature.clock

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.addCallback
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.view.WindowManager
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.refac_driverapp.feature.finish.FinishActivity
import com.example.refac_driverapp.R
import com.example.refac_driverapp.adapter.ClockAdapter
import com.example.refac_driverapp.data.repository.ClockRepository
import kotlinx.coroutines.flow.collectLatest
import android.os.Handler


class ClockActivity : AppCompatActivity() {
    private val viewModel: ClockViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ClockViewModel(ClockRepository()) as T
            }
        }
    }

    private lateinit var timeHandler: Handler
    private lateinit var timeRunnable: Runnable
    private var finishClickCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clock)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val selectedRoute = intent.getStringExtra("route") ?: ""
        val selectedTime = intent.getStringExtra("time") ?: ""
        val pushKey = intent.getStringExtra("pushKey") ?: ""
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerStations)
        val textRouteName = findViewById<TextView>(R.id.textRouteName)
        val textCurrentTime = findViewById<TextView>(R.id.textCurrentTime)
        val btnCheck = findViewById<Button>(R.id.startButton)

        recyclerView.layoutManager = LinearLayoutManager(this)
        textRouteName.text = "$selectedRoute ($selectedTime)"

        // 실시간 시간
        timeHandler = Handler(Looper.getMainLooper())
        timeRunnable = object : Runnable {
            override fun run() {
                val now = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
                textCurrentTime.text = now
                timeHandler.postDelayed(this, 1000)
            }
        }
        timeHandler.post(timeRunnable)

        // ViewModel로부터 리스트 받아오기
        lifecycleScope.launchWhenStarted {
            viewModel.stationList.collectLatest { list ->
                recyclerView.adapter = ClockAdapter(list)
            }
        }

        viewModel.loadStationInfo(selectedRoute, selectedTime)

        onBackPressedDispatcher.addCallback(this) {
            Toast.makeText(this@ClockActivity, "운전 중에는 뒤로가기가 비활성화됩니다.", Toast.LENGTH_SHORT).show()
        }

        btnCheck.setOnClickListener {
            if (finishClickCount == 0) {
                Toast.makeText(this, "한 번 더 누르면 운행이 종료됩니다.", Toast.LENGTH_SHORT).show()
                finishClickCount++
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                viewModel.endDrive(pushKey) {
                    val intent = Intent(this, FinishActivity::class.java)
                    intent.putExtra("pushKey", pushKey)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timeHandler.removeCallbacks(timeRunnable)
    }
}

