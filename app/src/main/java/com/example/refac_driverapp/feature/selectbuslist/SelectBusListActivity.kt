package com.example.refac_driverapp.feature.selectbuslist

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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.refac_driverapp.R
import com.example.refac_driverapp.adapter.DriveAdapter
import com.example.refac_driverapp.data.model.DrivedRecord
import com.example.refac_driverapp.data.repository.SelectBusListRepository
import com.example.refac_driverapp.design.RecyclerViewDecoration
import com.example.refac_driverapp.feature.finish.FinishActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Locale


class SelectBusListActivity : AppCompatActivity() {

    private val viewModel: SelectBusListViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SelectBusListViewModel(SelectBusListRepository()) as T
            }
        }
    }

    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selectbuslist)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val textNoReservation = findViewById<TextView>(R.id.textNoReservation)
        val backBtn = findViewById<Button>(R.id.btn_home)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(RecyclerViewDecoration(20))

        progressBar.visibility = View.VISIBLE
        viewModel.loadDrivedList()

        lifecycleScope.launchWhenStarted {
            viewModel.drivedList.collectLatest { list ->
                progressBar.visibility = View.GONE

                if (list.isEmpty()) {
                    textNoReservation.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                } else {
                    textNoReservation.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    recyclerView.adapter = DriveAdapter(
                        ArrayList(list),
                        onListEmpty = {
                            textNoReservation.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        },
                        onItemClick = { record ->
                            val intent = Intent(this@SelectBusListActivity, FinishActivity::class.java)
                            intent.putExtra("pushKey", record.pushKey)
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.error.collectLatest {
                it?.let { msg ->
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@SelectBusListActivity, msg, Toast.LENGTH_SHORT).show()
                }
            }
        }

        backBtn.setOnClickListener {
            val recent = viewModel.drivedList.value.maxByOrNull {
                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
            }

            if (recent != null) {
                val intent = Intent(this, FinishActivity::class.java)
                intent.putExtra("pushKey", recent.pushKey)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "운행 기록이 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (doubleBackToExitPressedOnce) {
                    finishAffinity()
                } else {
                    doubleBackToExitPressedOnce = true
                    Toast.makeText(this@SelectBusListActivity, "한 번 더 누르면 앱이 종료됩니다.", Toast.LENGTH_SHORT).show()
                    Handler(Looper.getMainLooper()).postDelayed({
                        doubleBackToExitPressedOnce = false
                    }, 2000)
                }
            }
        })
    }
}
