package com.example.refac_driverapp.data.repository

import com.example.refac_driverapp.data.model.DrivedRecord
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class RouteTimeRepository {
    private val db = FirebaseDatabase.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun fetchPinnedRoutes(): Map<String, List<String>> {
        val snapshot = db.getReference("routes").get().await()
        val routeMap = mutableMapOf<String, List<String>>()

        for (routeSnapshot in snapshot.children) {
            val isPinned = routeSnapshot.child("isPinned").getValue(Boolean::class.java) ?: false
            if (!isPinned) continue

            val routeName = routeSnapshot.child("name").getValue(String::class.java) ?: continue
            val times = routeSnapshot.child("times").children.mapNotNull { it.getValue(String::class.java) }.sorted()
            routeMap[routeName] = times
        }

        return routeMap
    }

    suspend fun saveDrivedRecord(route: String, time: String): Result<String> {
        val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("로그인 정보 없음"))
        val uniqueKey = "$route|$time|$date"
        val record = DrivedRecord(route, time, "", date, uniqueKey)

        db.getReference("drivers").child(uid).child("drived").child(uniqueKey).setValue(record).await()
        return Result.success(uniqueKey)
    }
}
