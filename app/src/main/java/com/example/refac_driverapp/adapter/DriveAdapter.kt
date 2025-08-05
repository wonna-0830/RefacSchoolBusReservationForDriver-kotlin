package com.example.refac_driverapp.adapter

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.refac_driverapp.R
import com.example.refac_driverapp.data.model.DrivedRecord
import com.google.firebase.database.FirebaseDatabase

class DriveAdapter(private val reservationList: ArrayList<DrivedRecord>,
                   private val onListEmpty: () -> Unit,
                   private val onItemClick: (DrivedRecord) -> Unit)
    : RecyclerView.Adapter<DriveAdapter.ReservationViewHolder>() {

    //item_list.xml의 보여줄 데이터를 하나씩 인플레이트
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ReservationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list, parent, false)
        return ReservationViewHolder(view)
    }

    //데이터 바인딩 + 삭제 버튼 클릭 이벤트
    override fun onBindViewHolder(holder: ReservationViewHolder, position: Int) {
        val reservation = reservationList[position]

        holder.textRoute.text = reservation.route
        holder.textTime.text = reservation.time
        holder.textStation.text = reservation.endTime
        holder.textDate.text = reservation.date

        //삭제 버튼 클릭시 현재 운전자의 운행기록을 데이터베이스에서 삭제
        holder.btnDelete.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("운행 기록 삭제")
                .setMessage("\uD83D\uDEA8 운행 노선을 잘못 선택한 경우에만 삭제할 수 있습니다.\n삭제된 노선에 대한 불이익은 책임지지 않습니다.\n정말 삭제하시겠습니까?")
                .setPositiveButton("확인") { dialog, _ ->
                    val currentUser =
                        com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    currentUser?.let { user ->
                        val ref = FirebaseDatabase.getInstance().reference
                            .child("drivers")
                            .child(user.uid)
                            .child("drived")
                            .child(reservation.pushKey)

                        ref.removeValue()
                    }
                    reservationList.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, reservationList.size)

                    if (reservationList.isEmpty()) {
                        onListEmpty()
                    }
                    Toast.makeText(holder.itemView.context, "운행 기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton("취소", null) // 취소 누르면 아무 일도 안 함
                .show()
        }
        holder.itemView.setOnClickListener {
            onItemClick(reservation)
        }

    }

    override fun getItemCount(): Int = reservationList.size

    //item_list.xml에 보여줄 데이터를 묶기
    class ReservationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textRoute: TextView = itemView.findViewById(R.id.textRoute)
        val textTime: TextView = itemView.findViewById(R.id.textTime)
        val textStation: TextView = itemView.findViewById(R.id.textPlace)
        val textDate: TextView = itemView.findViewById(R.id.textDate)
        val btnDelete: Button = itemView.findViewById(R.id.btnCancel)
    }


}