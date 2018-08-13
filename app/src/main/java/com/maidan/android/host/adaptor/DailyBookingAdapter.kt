package com.maidan.android.host.adaptor

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.maidan.android.host.models.BookingInfo
import com.maidan.android.host.R
import java.util.ArrayList

class DailyBookingAdaptor(val booking: ArrayList<BookingInfo>) : RecyclerView.Adapter<DailyBookingAdaptor.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.daily_booking_info, parent, false)
        return ViewHolder(view);
    }

    override fun getItemCount(): Int {
        return booking.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val bookingItem: BookingInfo = booking[position];
        holder.status.text = bookingItem.date;
        holder.fromtime.text = bookingItem.from
        holder.toTime.text = bookingItem.to
        holder.user.text = bookingItem.userName
        holder.venue.text = bookingItem.venue
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val status = itemView.findViewById(R.id.status) as TextView;
        val fromtime = itemView.findViewById(R.id.from_Time) as TextView;
        val toTime = itemView.findViewById(R.id.to_Time) as TextView;
        val user = itemView.findViewById(R.id.user_Name) as TextView
        val venue = itemView.findViewById(R.id.venue_specific) as TextView

    }
}