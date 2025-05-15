package com.example.refac_driverapp

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.refac_driverapp.ui.theme.Refac_DriverAppTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//자동 로그인을 위한 로직

        val prefs = getSharedPreferences("MyApp", MODE_PRIVATE)
        val isAutoLogin = prefs.getBoolean("autoLogin", false)

        if (isAutoLogin) {
            val email = prefs.getString("userEmail", null)
            val password = prefs.getString("userPassword", null)

            if (email != null && password != null) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        startActivity(Intent(this, RouteTime::class.java))
                        finish()
                    }
                    .addOnFailureListener {
                        // 실패했을 때는 그냥 Login으로 넘어가야 함!
                        moveToLogin()
                    }
            } else {
                moveToLogin()
            }
        } else {
            moveToLogin()
        }

    }
    // 로그인으로 넘어가는 함수
    private fun moveToLogin() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, Login::class.java))
            finish()
        }, 5000)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Refac_DriverAppTheme {
        Greeting("Android")
    }
}