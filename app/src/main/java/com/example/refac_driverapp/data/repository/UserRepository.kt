package com.example.refac_driverapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()

    suspend fun login(email: String, password: String): Result<Boolean> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return Result.failure(Exception("UID 없음"))

            val snapshot = db.getReference("drivers").child(uid).get().await()
            val isBanned = snapshot.child("isBanned").getValue(Boolean::class.java) ?: false

            if (isBanned) {
                auth.signOut()
                Result.failure(Exception("정지된 계정입니다."))
            } else {
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun register(email: String, password: String, name: String): Result<Unit> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = authResult.user?.uid ?: return Result.failure(Exception("UID 없음"))
            val joinDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val driverInfo = mapOf(
                "name" to name,
                "email" to email,
                "joinDate" to joinDate
            )

            db.getReference("drivers").child(uid).setValue(driverInfo).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}
