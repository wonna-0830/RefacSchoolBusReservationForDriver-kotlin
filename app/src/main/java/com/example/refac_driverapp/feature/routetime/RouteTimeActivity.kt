package com.example.refac_driverapp.feature.routetime

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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.refac_driverapp.R
import com.example.refac_driverapp.data.model.DrivedRecord
import com.example.refac_driverapp.data.repository.RouteTimeRepository
import com.example.refac_driverapp.feature.clock.ClockActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RouteTimeActivity : AppCompatActivity() {

    private val viewModel: RouteTimeViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RouteTimeViewModel(RouteTimeRepository()) as T
            }
        }
    }

    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_route_time)

        val spinnerRoute = findViewById<Spinner>(R.id.SpinnerRoute)
        val spinnerTime = findViewById<Spinner>(R.id.SpinnerTime)
        val btnCheck = findViewById<Button>(R.id.reservation)

        viewModel.loadRoutes()

        lifecycleScope.launchWhenStarted {
            viewModel.routes.collectLatest { routeMap ->
                val routeList = listOf("노선을 선택해주세요") + routeMap.keys
                val adapter = ArrayAdapter(this@RouteTimeActivity, android.R.layout.simple_spinner_item, routeList)
                spinnerRoute.adapter = adapter
            }
        }

        spinnerRoute.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val route = spinnerRoute.selectedItem.toString()
                if (route == "노선을 선택해주세요") {
                    spinnerTime.adapter = null
                    return
                }

                val timeList = listOf("시간을 선택하세요") + viewModel.getFilteredTimes(route)
                val timeAdapter = ArrayAdapter(this@RouteTimeActivity, android.R.layout.simple_spinner_item, timeList)
                spinnerTime.adapter = timeAdapter
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnCheck.setOnClickListener {
            val route = spinnerRoute.selectedItem?.toString()
            val time = spinnerTime.selectedItem?.toString()

            if (route == null || route == "노선을 선택해주세요") {
                Toast.makeText(this, "노선을 선택해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (time == null || time == "시간을 선택하세요") {
                Toast.makeText(this, "시간을 선택해주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.saveRecord(route, time)
        }

        lifecycleScope.launchWhenStarted {
            viewModel.saveResult.collectLatest { result ->
                result?.onSuccess { pushKey ->
                    val intent = Intent(this@RouteTimeActivity, ClockActivity::class.java)
                    intent.putExtra("route", spinnerRoute.selectedItem.toString())
                    intent.putExtra("time", spinnerTime.selectedItem.toString())
                    intent.putExtra("date", SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()))
                    intent.putExtra("pushKey", pushKey)
                    startActivity(intent)
                }?.onFailure {
                    Toast.makeText(this@RouteTimeActivity, "운행 정보 저장 실패 😢", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // 뒤로가기 두 번 눌러 종료
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finishAffinity()
                } else {
                    doubleBackToExitPressedOnce = true
                    Toast.makeText(this@RouteTimeActivity, "한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        doubleBackToExitPressedOnce = false
                    }, 2000)
                }
            }
        })

    }
}
