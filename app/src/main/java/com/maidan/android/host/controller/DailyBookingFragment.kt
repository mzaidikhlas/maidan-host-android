package com.maidan.android.host.controller


import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson

import com.maidan.android.host.R
import com.maidan.android.host.adaptor.DateBookingAdapter
import com.maidan.android.host.models.Booking
import com.maidan.android.host.models.User
import com.maidan.android.host.models.Venue
import com.maidan.android.host.retrofit.ApiInterface
import com.maidan.android.host.retrofit.ApiResponse
import com.maidan.android.host.retrofit.PayloadFormat
import com.maidan.android.host.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import kotlin.collections.ArrayList

class DailyBookingFragment : Fragment() {

    //Layout
    private lateinit var dateTxt: TextView
    private lateinit var bookingsRecycler: RecyclerView
    private lateinit var newBookingNameTxt: EditText
    private lateinit var newBookingFromTxt: TextView
    private lateinit var newBookingToTxt: TextView
    private lateinit var bookNow: Button
    private lateinit var progressBar: ProgressBar

    private var dt: String? = null
    private val TAG = "BookingDailyFragment"
    private var user: User? = null
    private lateinit var venues: ArrayList<Venue>

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    //Api Call Response
    private lateinit var payload: ArrayList<PayloadFormat>
    private lateinit var bookings: ArrayList<Booking>

    private lateinit var popupBooking: Button
    private lateinit var popupAddBooking: Dialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_daily_booking, container, false)

        //Layouts init
        dateTxt = view.findViewById(R.id.bookingDate)
        bookingsRecycler = view.findViewById(R.id.dailyBooking)
        popupBooking = view.findViewById(R.id.addBooking)
        progressBar = view.findViewById(R.id.dailyBookingProgressBar)

        progressBar.visibility = View.VISIBLE

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!

        if (arguments != null){
            dt = arguments!!.getString("date")
            bookings = arguments!!.getSerializable("bookings") as ArrayList<Booking>
        }

        popupAddBooking = Dialog(context)
        popupAddBooking.setCanceledOnTouchOutside(true)
        popupBooking.setOnClickListener {
            Log.d(TAG, "popup")
            showAddBookingPopup()
        }

        dateTxt.text = dt

        bookingsRecycler.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)
        if (bookings.isNotEmpty()){
            progressBar.visibility = View.INVISIBLE
            bookingsRecycler.adapter = DateBookingAdapter(bookings)
        }else {
            progressBar.visibility = View.INVISIBLE
            Toast.makeText(context, "No bookings found of this date", Toast.LENGTH_SHORT).show()
        }
        return view
    }

    //GetBooking with date
    private fun createBooking(booking: Booking) {
       progressBar.visibility = View.VISIBLE

       currentUser.getIdToken(true).addOnCompleteListener{task ->
           if (task.isSuccessful){
               val idToken = task.result.token
               val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)
               val call: Call<ApiResponse> = apiService.createBooking(idToken!!, booking)
               call.enqueue(object: Callback<ApiResponse>{
                   override fun onFailure(call: Call<ApiResponse>?, t: Throwable?) {
                       progressBar.visibility = View.INVISIBLE
                       throw t!!
                   }

                   override fun onResponse(call: Call<ApiResponse>?, response: Response<ApiResponse>?) {
                       if (response!!.isSuccessful){
                            Log.d(TAG,"In response")
                            if (response.body()!!.getStatusCode() == 201){
                                if (response.body()!!.getType() == "Booking"){
                                    progressBar.visibility = View.INVISIBLE
                                    bookings.add(booking)
                                    popupAddBooking.hide()
                                    bookingsRecycler.adapter.notifyDataSetChanged()
                                    Toast.makeText(context,"New Booking created", Toast.LENGTH_LONG).show()

                                }else{
                                    progressBar.visibility = View.INVISIBLE
                                }
                            }else{
                                progressBar.visibility = View.INVISIBLE
                            }
                       }else{
                           progressBar.visibility = View.INVISIBLE
                       }
                   }
               })
           }
       }
    }
    fun showAddBookingPopup()
    {
        popupAddBooking.setContentView(R.layout.popup_add_booking)
        newBookingNameTxt = popupAddBooking.findViewById(R.id.name)
        newBookingFromTxt = popupAddBooking.findViewById(R.id.from)
        newBookingToTxt = popupAddBooking.findViewById(R.id.to)
        bookNow = popupAddBooking.findViewById(R.id.book)

        bookNow.setOnClickListener {
            Log.d(TAG, "Book")
            if (newBookingFromTxt.text.isNotEmpty() && newBookingToTxt.text.isNotEmpty() && newBookingNameTxt.text.isNotEmpty()) {
                Log.d(TAG, "Do able")
               // val b = Booking(venues[0], null, user!!, newBookingToTxt.text.toString(), newBookingFromTxt.text.toString(), dt!!)
               // Log.d(TAG, b.toString())
                bookNow.isEnabled = false
                val loggedInUser = activity!!.intent.extras.getSerializable("loggedInUser") as User
                val newBooking = Booking(bookings[0].getVenue(),null,loggedInUser,
                        newBookingToTxt.text.toString(), newBookingFromTxt.text.toString(), dt!!)
                createBooking(newBooking)
            }
            else
                Toast.makeText(context, "All fields are required", Toast.LENGTH_LONG).show()
        }

        newBookingFromTxt.setOnClickListener {
            val c = Calendar.getInstance()
            val hourOfDay = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(context,R.style.DatePickerTheme,
                    TimePickerDialog.OnTimeSetListener { view, hr, min ->
                        Log.d(TAG, "Hour: $hr, Min: $min")
                        val timeString = "$hr:$min"
                        newBookingFromTxt.text = timeString
                    }, hourOfDay, minute, false)
            timePickerDialog.show()

        }
        newBookingToTxt.setOnClickListener {
            val c = Calendar.getInstance()
            val hourOfDay = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(context,R.style.DatePickerTheme,
                    TimePickerDialog.OnTimeSetListener { view, hr, min ->
                        Log.d(TAG, "Hour: $hr, Min: $min")
                        val timeString = "$hr:$min"
                        newBookingToTxt.text = timeString
                    }, hourOfDay, minute, false)

            timePickerDialog.show()
        }
        popupAddBooking.show()
    }
}
