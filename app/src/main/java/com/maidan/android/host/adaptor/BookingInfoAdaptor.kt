package com.maidan.android.host.adaptor

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.maidan.android.host.R
import com.maidan.android.host.models.Booking
import java.text.DateFormat
import java.util.*

class BookingInfoAdaptor(private val bookings: ArrayList<Booking>) : RecyclerView.Adapter<BookingInfoAdaptor.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.booking_information, parent, false)
        return ViewHolder(view);
    }

    override fun getItemCount(): Int {
        return bookings.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val booking: Booking = bookings[position]

        val tempCalendar = Calendar.getInstance()
        tempCalendar.time = DateFormat.getDateInstance(DateFormat.FULL).parse(booking.getBookingDate())
        holder.currentDate.text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(tempCalendar.time)
        tempCalendar.time = DateFormat.getDateInstance(DateFormat.FULL).parse(booking.getToBookingDate())
        holder.currentDateTo.text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(tempCalendar.time)

//        holder.currentDateTo.text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(booking.getToBookingDate())
//        holder.currentDate.text = booking.getBookingDate()
//        holder.currentDateTo.text = booking.getToBookingDate()
        holder.fromtime.text = booking.getStartTime()
        holder.toTime.text = booking.getDurationOfBooking()
        holder.user.text = booking.getUser().getName()
        holder.venue.text = booking.getVenue().getName()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val currentDate = itemView.findViewById(R.id.currentDate) as TextView
        val currentDateTo = itemView.findViewById(R.id.currentDateTo) as TextView
        val fromtime = itemView.findViewById(R.id.fromTime) as TextView
        val toTime = itemView.findViewById(R.id.toTime) as TextView
        val user = itemView.findViewById(R.id.userName) as TextView
        val venue = itemView.findViewById(R.id.venue) as TextView

    }
}