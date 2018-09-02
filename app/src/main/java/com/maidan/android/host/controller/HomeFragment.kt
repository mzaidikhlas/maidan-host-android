package com.maidan.android.host.controller

import android.app.AlertDialog
import android.content.Context
import android.graphics.drawable.AnimationDrawable
import android.nfc.Tag
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
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
    private lateinit var displayBookings: ArrayList<Booking>
    private var dialog: AlertDialog? = null
    private lateinit var animation: AnimationDrawable

    private var loggedInUser: User? = null

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    //Api Call Response
    private lateinit var payload: ArrayList<PayloadFormat>
    private var bookings: ArrayList<Booking>? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Log.d(TAG, "OnAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "OnCreate")

        if (savedInstanceState != null) {
            loggedInUser = savedInstanceState.getSerializable("loggedInUser") as User
        }else{
            if (arguments != null) {
                loggedInUser = arguments!!.get("loggedInUser") as User
                Log.d(TAG, loggedInUser.toString())
            }
        }

        //Preventing user to create booking when this user dont have any venue
        if (loggedInUser!!.getVenues() == null){
            Log.d(TAG, "No venues")
            Toast.makeText(context, "You dont have added any ground please ask maidan team for updates", Toast.LENGTH_LONG).show()
        }

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d(TAG, "OnCreateView")
        // Inflate the layout for this fragment
        if (view != null) Log.d(TAG, "Reusing view")

        val view = if (view != null) view else inflater.inflate(R.layout.fragment_home, container, false)

        progressBar = view!!.findViewById(R.id.bookingProgressBar)
        venuesSpinner = view.findViewById(R.id.homeVenuesSpinner)
        venuesSpinner.setPopupBackgroundResource(R.drawable.light_green_to_dark_green_1)
        recyclerView = view.findViewById(R.id.recyclerView)
        homeCalender = view.findViewById(R.id.homeCalendarView)

        //Venues spinner init
        Log.d(TAG,"Logged in user $loggedInUser")
        val spinnerArray = ArrayList<String>()
        if (loggedInUser!!.getVenues() != null) {
            for (item: Venue in loggedInUser!!.getVenues()!!)
                spinnerArray.add(item.getName())

            val adapter = ArrayAdapter<String>(
                    context, android.R.layout.simple_spinner_item, spinnerArray)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            venuesSpinner.adapter = adapter
            venuesSpinner.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                    val selectedItem = venuesSpinner.getItemAtPosition(position)
                    getBookingsWithOwner(selectedItem as String)
                }

                override fun onNothingSelected(parentView: AdapterView<*>) {
                    venuesSpinner.requestFocus()
                }
            }
        }
        //Calender
        val c: Calendar = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        dateString = "$day/$month/$year"
        homeCalender.setOnDateChangeListener { calenderView, y, m, d ->
            dateString = "$d/$m/$y"
            val dailyBookingFragment = DailyBookingFragment()
            val bundle = Bundle()
            bundle.putString("date", dateString)
            if (bookings != null)
                bundle.putSerializable("bookings", bookings)
            else Log.d(TAG, "Bookings empty in home")
            bundle.putSerializable("loggedInUser", loggedInUser)
            dailyBookingFragment.arguments = bundle
            fragmentManager!!.beginTransaction().addToBackStack("homeFragment").replace(R.id.fragment_layout, dailyBookingFragment).commit()
        }
        homeCalender.minDate = c.timeInMillis
        Log.d(TAG, dateString)

        //bookings recyclerview
        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "OnActivityCreated")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "OnStart")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "OnResume")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "OnPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "OnStop")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "OnDestroyView")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.d(TAG, "OnSaveInstanceState")
        outState.putSerializable("loggedInUser", loggedInUser)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "OnDestroy")
    }

    override fun onDetach() {
        Log.d(TAG, "OnDetach")
        super.onDetach()
    }

    private fun getBookingsWithOwner(venueName: String) {
        showProgressDialog()

        currentUser.getIdToken(true).addOnCompleteListener{task ->
            if (task.isSuccessful){
                val idToken = task.result.token
                val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)
                val call: Call<ApiResponse> = apiService.getBookingsByOwnerPhone(currentUser.phoneNumber!!, idToken!!)
                call.enqueue(object: Callback<ApiResponse> {
                    override fun onFailure(call: Call<ApiResponse>?, t: Throwable?) {
                        hideProgressDialog()
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
                                        Log.d(TAG, "Bookings call $bookings")
                                        hideProgressDialog()
                                        recyclerView.adapter = BookingInfoAdaptor(displayBookings)
                                        recyclerView.adapter.notifyDataSetChanged()
                                    }else{
                                        hideProgressDialog()
                                        Toast.makeText(context, "No Bookings found of this ground $venueName", Toast.LENGTH_LONG).show()
                                    }
                                }else{
                                    hideProgressDialog()
                                    Toast.makeText(context, "Booking Typed", Toast.LENGTH_LONG).show()
                                }
                            }else{
                                hideProgressDialog()
                                Toast.makeText(context, "Booking code", Toast.LENGTH_LONG).show()
                            }
                        }else{
                            hideProgressDialog()
                            Toast.makeText(context, "No Response", Toast.LENGTH_LONG).show()
                        }
                    }
                })
            }
        }
    }

    private fun showProgressDialog() {
        val builder = AlertDialog.Builder(context)
        val dialogView = layoutInflater.inflate(R.layout.progress_dialog, null)
        val loader = dialogView.findViewById<ImageView>(R.id.loadingProgressbar)
        builder.setView(dialogView)
        builder.setCancelable(false)
        dialog = builder.create()
        dialog!!.window.setLayout(600,400)
        dialog!!.show()
        animation = loader.drawable as AnimationDrawable
        animation.start()
        activity!!.window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun hideProgressDialog(){
        animation.stop()
        dialog!!.dismiss()
        activity!!.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}

