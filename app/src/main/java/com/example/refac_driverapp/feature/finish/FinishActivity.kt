package com.example.refac_driverapp.feature.finish

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.refac_driverapp.R
import com.example.refac_driverapp.data.repository.FinishRepository
import com.example.refac_driverapp.feature.login.LoginActivity
import com.example.refac_driverapp.feature.routetime.RouteTimeActivity
import com.example.refac_driverapp.feature.selectbuslist.SelectBusListActivity
import kotlinx.coroutines.flow.collectLatest

class FinishActivity : AppCompatActivity() {

    private val viewModel: FinishViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return FinishViewModel(FinishRepository()) as T
            }
        }
    }

    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_finish)

        val pushKey = intent.getStringExtra("pushKey") ?: ""
        val drivedRoute = findViewById<TextView>(R.id.drivedRoute)
        val drivedTime = findViewById<TextView>(R.id.drivedTime)
        val finishTime = findViewById<TextView>(R.id.finishTime)

        val btnLogout = findViewById<Button>(R.id.logOutButton)
        val btnSelectRoute = findViewById<Button>(R.id.nextButton)
        val btnExitApp = findViewById<Button>(R.id.finishApp)
        val btnSelectBus = findViewById<Button>(R.id.selectBus)

        viewModel.loadRouteInfo(pushKey)

        lifecycleScope.launchWhenStarted {
            viewModel.routeInfo.collectLatest { info ->
                info?.let {
                    drivedRoute.text = "노선: ${it.route}"
                    drivedTime.text = "시간: ${it.time}"
                    finishTime.text = "종료시간: ${it.endTime}"
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.error.collectLatest {
                it?.let { msg -> Toast.makeText(this@FinishActivity, msg, Toast.LENGTH_SHORT).show() }
            }
        }

        btnLogout.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("로그아웃")
                .setMessage("정말 로그아웃하시겠습니까?")
                .setPositiveButton("예") { _, _ ->
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    getSharedPreferences("login_prefs", MODE_PRIVATE).edit().clear().apply()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("아니오", null)
                .show()
        }

        btnSelectRoute.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("노선 선택")
                .setMessage("처음으로 돌아가시겠습니까?")
                .setPositiveButton("예") { _, _ ->
                    val intent = Intent(this, RouteTimeActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("아니오", null)
                .show()
        }

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

        btnSelectBus.setOnClickListener {
            val intent = Intent(this, SelectBusListActivity::class.java)
            startActivity(intent)
            finish()
        }

        onBackPressedDispatcher.addCallback(this) {
            if (doubleBackToExitPressedOnce) {
                finishAffinity()
                return@addCallback
            }

            doubleBackToExitPressedOnce = true
            Toast.makeText(this@FinishActivity, "한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()

            Handler(Looper.getMainLooper()).postDelayed({
                doubleBackToExitPressedOnce = false
            }, 2000)
        }
    }
}
