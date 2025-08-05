package com.example.refac_driverapp.data.repository

import com.example.refac_driverapp.data.model.StationInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class ClockRepository {
    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun fetchReservationCounts(route: String, time: String): List<StationInfo> {
        val stationNames = when (route) {
            "교내순환" -> listOf("정문", "B1", "C7", "C13", "D6", "A2(건너편)")
            "하양역->교내순환" -> listOf("하양역", "정문", "B1", "C7", "C13", "D6", "A2(건너편)")
            "안심역->교내순환" -> listOf("안심역(3번출구)", "정문", "B1", "C7", "C13", "D6", "A2(건너편)")
            "사월역->교내순환" -> listOf("사월역(3번출구)", "정문", "B1", "C7", "C13", "D6", "A2(건너편)")
            "A2->안심역->사월역" -> listOf("A2(건너편)", "안심역", "사월역")
            else -> emptyList()
        }

        val resultMap = stationNames.associateWith { 0 }.toMutableMap()
        val today = SimpleDateFormat("yy-MM-dd", Locale.getDefault()).format(Date())
        val snapshot = db.getReference("users").get().await()

        for (user in snapshot.children) {
            val reservations = user.child("reservations").children
            for (reservation in reservations) {
                val resRoute = reservation.child("route").getValue(String::class.java)
                val resTime = reservation.child("time").getValue(String::class.java)
                val resDate = reservation.child("date").getValue(String::class.java)
                val station = reservation.child("station").getValue(String::class.java) ?: continue

                if (resRoute == route && resTime == time && resDate == today && station in resultMap) {
                    resultMap[station] = resultMap[station]!! + 1
                }
            }
        }

        return stationNames.map { StationInfo(it, resultMap[it] ?: 0) }
    }

    suspend fun saveEndTime(pushKey: String) {
        val uid = auth.currentUser?.uid ?: return
        val endTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        db.getReference("drivers").child(uid).child("drived").child(pushKey).child("endTime")
            .setValue(endTime).await()
    }
}
