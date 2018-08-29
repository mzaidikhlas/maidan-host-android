package com.maidan.android.host.controller

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import com.maidan.android.host.adaptor.BookingInfoAdaptor
import com.maidan.android.host.R
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
import java.util.*
import kotlin.collections.ArrayList
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener

class HomeFragment : Fragment() {

    private val TAG = "Home"

    private lateinit var recyclerView: RecyclerView
    private lateinit var homeCalender: CalendarView
    private lateinit var progressBar: ProgressBar
    private lateinit var dateString: String
    private lateinit var venuesSpinner: Spinner
    private var venueName = "Venue 1"
    private lateinit var displayBookings: ArrayList<Booking>

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private var user: User? = null
    private lateinit var venues: ArrayList<Venue>

    //Api Call Response
    private lateinit var payload: ArrayList<PayloadFormat>
    private var bookings: ArrayList<Booking>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        val loggedInUser = activity!!.intent.extras.getSerializable("loggedInUser") as User
        Log.d(TAG, "User: Activity $loggedInUser")

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!

        progressBar = view.findViewById(R.id.bookingProgressBar)
        venuesSpinner = view.findViewById(R.id.homeVenuesSpinner)
        venuesSpinner.setPopupBackgroundResource(R.drawable.light_green_to_dark_green_1)

        //Venues spinner init
        val spinnerArray = ArrayList<String>()
        for (item: Venue in loggedInUser.getVenues()!!)
            spinnerArray.add(item.getName())

        val adapter = ArrayAdapter<String>(
                context, android.R.layout.simple_spinner_item, spinnerArray)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        venuesSpinner.adapter = adapter
        venuesSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                getBookingsWithOwner(venuesSpinner.selectedItem as String)
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                venuesSpinner.requestFocus()
            }
        }

        val c: Calendar = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        homeCalender = view.findViewById(R.id.homeCalendarView)
        dateString = "$day/$month/$year"
        homeCalender.setOnDateChangeListener { calenderView, y, m, d ->
            progressBar.visibility = View.VISIBLE
            dateString = "$d/$m/$y"
            val dailyBookingFragment = DailyBookingFragment()
            val bundle = Bundle()
            bundle.putString("date", dateString)
            if (bookings != null)
                bundle.putSerializable("bookings", bookings)

            dailyBookingFragment.arguments = bundle
            progressBar.visibility = View.INVISIBLE
            fragmentManager!!.beginTransaction().replace(R.id.fragment_layout, dailyBookingFragment).addToBackStack("homeFragment").commit()
        }
        homeCalender.minDate = c.timeInMillis
        Log.d(TAG, dateString)

        getBookingsWithOwner(venuesSpinner.selectedItem as String)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)

        return view
    }

    private fun getBookingsWithOwner(venueName: String) {
        progressBar.visibility = View.VISIBLE
        currentUser.getIdToken(true).addOnCompleteListener{task ->
            if (task.isSuccessful){
                val idToken = task.result.token
                val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)
                val call: Call<ApiResponse> = apiService.getBookingsByOwnerPhone(currentUser.phoneNumber!!, idToken!!)
                call.enqueue(object: Callback<ApiResponse> {
                    override fun onFailure(call: Call<ApiResponse>?, t: Throwable?) {
                        progressBar.visibility = View.INVISIBLE
                        throw t!!
                    }

                    override fun onResponse(call: Call<ApiResponse>?, response: Response<ApiResponse>?) {
                        if (response!!.isSuccessful){
                            Log.d(TAG,"In response")
                            if (response.body()!!.getStatusCode() == 200){
                                if (response.body()!!.getType() == "Booking"){
                                    payload = response.body()!!.getPayload()
                                    if (payload.isNotEmpty()) {
                                        Log.d(TAG, response.toString())
                                        val gson = Gson()
                                        var booking: Booking
                                        bookings = ArrayList()
                                        displayBookings = ArrayList()
                                        for (item: PayloadFormat in payload) {
                                            val jsonObject = gson.toJsonTree(item.getData()).asJsonObject
                                            Log.d(TAG, "Json$jsonObject")
                                            booking = gson.fromJson(jsonObject, Booking::class.java)
                                            bookings!!.add(booking)

                                            if (booking.getVenue().getName() == venueName)
                                                displayBookings.add(booking)
                                        }
                                        progressBar.visibility = View.INVISIBLE
                                        recyclerView.adapter = BookingInfoAdaptor(displayBookings)
                                        recyclerView.adapter.notifyDataSetChanged()
                                    }else{
                                        progressBar.visibility = View.INVISIBLE
                                        Toast.makeText(context, "No Bookings found of this ground $venueName", Toast.LENGTH_LONG).show()
                                    }
                                }else{
                                    Toast.makeText(context, "Booking Typed", Toast.LENGTH_LONG).show()
                                    progressBar.visibility = View.INVISIBLE
                                }
                            }else{
                                Toast.makeText(context, "Booking code", Toast.LENGTH_LONG).show()
                                progressBar.visibility = View.INVISIBLE
                            }
                        }else{
                            Toast.makeText(context, "No Response", Toast.LENGTH_LONG).show()
                            progressBar.visibility = View.INVISIBLE
                        }
                    }
                })
            }
        }
    }
}

