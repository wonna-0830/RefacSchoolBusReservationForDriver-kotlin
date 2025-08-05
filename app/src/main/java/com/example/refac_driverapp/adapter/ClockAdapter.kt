package com.example.refac_driverapp.adapter

import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.refac_driverapp.R
import com.example.refac_driverapp.data.model.StationInfo

class ClockAdapter(private val stationList: List<StationInfo>) :
    RecyclerView.Adapter<ClockAdapter.StationViewHolder>() {

    class StationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val stationName: TextView = view.findViewById(R.id.textStationName)
        val passengerCount: TextView = view.findViewById(R.id.textPassengerCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_station, parent, false)
        return StationViewHolder(view)
    }

    override fun onBindViewHolder(holder: StationViewHolder, position: Int) {
        val station = stationList[position]

        holder.stationName.text = station.name
        holder.stationName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f) // 🔍 글씨 크게!
        holder.stationName.setTypeface(null, Typeface.BOLD) // 🔍 글씨 진하게!

        holder.passengerCount.text = "${station.reservationCount}명"
        holder.passengerCount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        holder.passengerCount.setTypeface(null, Typeface.BOLD)

        // 예약자 수에 따라 색상 변경
        val color = when {
            station.reservationCount == 0 -> Color.GRAY
            station.reservationCount <= 3 -> Color.parseColor("#4CAF50") // 초록
            else -> Color.parseColor("#F44336") // 빨강
        }
        holder.passengerCount.setBackgroundColor(color)
    }

    override fun getItemCount(): Int = stationList.size
}
