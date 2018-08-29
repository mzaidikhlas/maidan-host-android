package com.maidan.android.host.adaptor

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.maidan.android.host.R
import com.maidan.android.host.models.Booking

class DateBookingAdapter(private val bookings: ArrayList<Booking>) : RecyclerView.Adapter<DateBookingAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.daily_booking_info, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return bookings.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val booking: Booking = bookings[position]
        holder.venueSpecificTxt.text = booking.getVenue().getName()
        holder.fromTimeTxt.text = booking.getStartTime()
        holder.toTimeTxt.text = booking.getDurationOfBooking()
        holder.bookingUsernameTxt.text = booking.getUser().getName()
        holder.bookingStatusTxt.text = booking.getStatus()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val venueSpecificTxt = itemView.findViewById<TextView>(R.id.venue_specific)
        val fromTimeTxt = itemView.findViewById<TextView>(R.id.from_Time)
        val toTimeTxt = itemView.findViewById<TextView>(R.id.to_Time)
        val amPMTxt = itemView.findViewById<TextView>(R.id.AMPM)
        val bookingStatusTxt = itemView.findViewById<TextView>(R.id.status)
        val bookingUsernameTxt = itemView.findViewById<TextView>(R.id.user_Name)
    }
}