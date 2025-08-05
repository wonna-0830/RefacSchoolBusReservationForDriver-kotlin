package com.example.refac_driverapp.data.repository

import com.example.refac_driverapp.data.model.DrivedRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class SelectBusListRepository {
    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun fetchDrivedList(): Result<List<DrivedRecord>> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("로그인 정보 없음"))
            val snapshot = db.getReference("drivers").child(uid).child("drived").get().await()

            val list = snapshot.children.mapNotNull { snap ->
                val record = snap.getValue(DrivedRecord::class.java)
                record?.apply { pushKey = snap.key ?: "" }
            }

            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
