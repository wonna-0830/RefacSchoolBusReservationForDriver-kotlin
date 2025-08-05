package com.example.refac_driverapp.data.repository

import com.example.refac_driverapp.data.model.DrivedRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class FinishRepository {
    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun fetchDrivedRouteInfo(pushKey: String): Result<DrivedRecord> {
        return try {
            val uid = auth.currentUser?.uid ?: return Result.failure(Exception("로그인 정보 없음"))
            val snapshot = db.getReference("drivers").child(uid).child("drived").child(pushKey).get().await()

            val record = snapshot.getValue(DrivedRecord::class.java)
                ?: return Result.failure(Exception("운행 정보를 불러올 수 없습니다."))

            Result.success(DrivedRecord(record.route, record.time, record.endTime))

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
