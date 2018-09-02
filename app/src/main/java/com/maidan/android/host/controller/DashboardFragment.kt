package com.maidan.android.host.controller
import android.app.DatePickerDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.maidan.android.host.models.BookingInfo
import com.maidan.android.host.R
import java.util.ArrayList


class DashboardFragment: Fragment() {


    private lateinit var bookings:RecyclerView
    private lateinit var myDataSet: ArrayList<BookingInfo>
    private lateinit var btn: Button


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.receipt, container, false)

//        myDataSet = ArrayList()
//
//        myDataSet.add(BookingInfo("BOOKED","12:00","3:00","Ahmad Ali","Model Town Whites"))
//        myDataSet.add(BookingInfo("BOOKED","3:00","6:00","Zaid Ikhlas","Stags"))
//        myDataSet.add(BookingInfo("BOOKED","3:00","6:00","Zaid Ikhlas","Stags"))
//        myDataSet.add(BookingInfo("BOOKED","3:00","6:00","Zaid Ikhlas","Stags"))
//        myDataSet.add(BookingInfo("BOOKED","3:00","6:00","Zaid Ikhlas","Stags"))
//        myDataSet.add(BookingInfo("BOOKED","3:00","6:00","Zaid Ikhlas","Stags"))
//        myDataSet.add(BookingInfo("BOOKED","3:00","6:00","Ali Khaliq","Stags"))
//
//        bookings = view.findViewById(R.id.dailyBooking)
//        bookings.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
//        bookings.adapter = DailyBookingAdaptor(myDataSet);

        return view
    }



}

