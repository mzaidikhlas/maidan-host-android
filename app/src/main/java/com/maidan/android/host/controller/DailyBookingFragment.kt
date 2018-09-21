package com.maidan.android.host.controller


import android.app.*
import android.content.Context
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
import com.maidan.android.host.MainActivity

import com.maidan.android.host.R
import com.maidan.android.host.adaptor.DateBookingAdapter
import com.maidan.android.host.models.Booking
import com.maidan.android.host.models.Transaction
import com.maidan.android.host.models.User
import com.maidan.android.host.models.Venue
import com.maidan.android.host.retrofit.ApiInterface
import com.maidan.android.host.retrofit.ApiResponse
import com.maidan.android.host.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DateFormat
import java.util.*
import kotlin.collections.ArrayList

class DailyBookingFragment : Fragment() {

    //Layout
    private lateinit var dateTxt: TextView
    private lateinit var bookingsRecycler: RecyclerView
    private lateinit var newBookingNameTxt: EditText
    private lateinit var newBookingFromTxt: TextView
    private lateinit var bookNow: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var venuesSpinner: Spinner
    private lateinit var backBtn: Button
    private lateinit var dateTo: Spinner

    private var dt: String? = null
    private var today: Date? = null
    private val TAG = "BookingDailyFragment"
    private var count = 1

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    private var loggedInUser: User? = null

    //Api Call Response
    private var bookings: ArrayList<Booking>? = null

    private lateinit var popupBooking: Button
    private lateinit var popupAddBooking: Dialog

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        Log.d(TAG, "OnAttach")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "OnCreate")

        loggedInUser = (activity as MainActivity).getLoggedInUser()

        //Getting bundle data
        if (arguments != null) {
            dt = arguments!!.getString("date")
            bookings = arguments!!.get("bookings") as ArrayList<Booking>?
        } else Log.d(TAG, "Arguments empty")

        val c = Calendar.getInstance()
        today = c.time
        Log.d(TAG, "Time ${c.time}")

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d(TAG, "OnCreateView")
        // Inflate the layout for this fragment
        if (view != null) Log.d(TAG, "Reusing view")

        val view = if (view != null) view else inflater.inflate(R.layout.fragment_daily_booking, container, false)

        //Layouts init
        dateTxt = view!!.findViewById(R.id.bookingDate)
        bookingsRecycler = view.findViewById(R.id.dailyBooking)
        popupBooking = view.findViewById(R.id.addBooking)
        progressBar = view.findViewById(R.id.dailyBookingProgressBar)
        venuesSpinner = view.findViewById(R.id.dailyBookingVenuesSpinner)
        backBtn = view.findViewById(R.id.dailyBookingBack)

        //populating layout
        dateTxt.text = dt

        //Setting up recycler view for bookings
        bookingsRecycler.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)

        //Back button listener
        backBtn.setOnClickListener {
            fragmentManager!!.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        //venues spinner init
        if (loggedInUser!!.getVenues() != null) {
            val spinnerArray = ArrayList<String>()
            for (item: Venue in loggedInUser!!.getVenues()!!)
                spinnerArray.add(item.getName())

            val adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, spinnerArray)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            venuesSpinner.adapter = adapter
            venuesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                    val selectedItem = venuesSpinner.getItemAtPosition(position)

                    Log.d(TAG, "Selected item $selectedItem")
                    //Getting bookings from db
                    showProgressDialog()
                    filterBookings(selectedItem as String)
                }

                override fun onNothingSelected(parentView: AdapterView<*>) {
                    venuesSpinner.requestFocus()
                }
            }
        }else {
            popupBooking.isEnabled = false
            Log.d(TAG, "No venues")
            Toast.makeText(context, "You cannot book now. Please ask maidan to add your venues", Toast.LENGTH_LONG).show()
        }

        //New booking popup init
        popupAddBooking = Dialog(context)
        popupAddBooking.setCanceledOnTouchOutside(true)
        popupBooking.setOnClickListener {
            Log.d(TAG, "popup")
            showAddBookingPopup()
        }

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
        outState.putInt("count", count++)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "OnDestroy")
    }

    override fun onDetach() {
        Log.d(TAG, "OnDetach")
        super.onDetach()
    }

    //Creating new booking
    private fun createBooking(booking: Booking) {
       showProgressDialog()

       currentUser.getIdToken(true).addOnCompleteListener{task ->
           if (task.isSuccessful){
               val idToken = task.result.token
               val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)
               val call: Call<ApiResponse> = apiService.createBooking(idToken!!, booking)
               call.enqueue(object: Callback<ApiResponse>{
                   override fun onFailure(call: Call<ApiResponse>?, t: Throwable?) {
                       hideProgressDialog()
                       throw t!!
                   }

                   override fun onResponse(call: Call<ApiResponse>?, response: Response<ApiResponse>?) {
                       if (response!!.isSuccessful){
                            Log.d(TAG,"In response")
                            if (response.body()!!.getStatusCode() == 201){
                                if (response.body()!!.getType() == "Booking"){
                                    if (bookings == null)
                                        bookings = ArrayList()
                                    showAddBookingPopup()
                                    bookings!!.add(booking)
                                    filterBookings(venuesSpinner.selectedItem as String)
                                    popupAddBooking.hide()
                                    Toast.makeText(context,"New Booking created", Toast.LENGTH_LONG).show()

                                }else{
                                    hideProgressDialog()
                                }
                            }else{
                                Toast.makeText(context, "For some reason booking not created", Toast.LENGTH_LONG).show()
                                hideProgressDialog()
                            }
                       }else{
                           Toast.makeText(context, "Some thing is wrong with making server call", Toast.LENGTH_LONG).show()
                           hideProgressDialog()
                       }
                   }
               })
           }
       }
    }

    //New Booking popup
    fun showAddBookingPopup() {
        //Time validation
        var toTimeSeconds: Int? = null
        var fromTimeSeconds: Int? = null

        //Popup layouts init
        popupAddBooking.setContentView(R.layout.popup_add_booking)
        newBookingNameTxt = popupAddBooking.findViewById(R.id.name)
        newBookingFromTxt = popupAddBooking.findViewById(R.id.from)
        bookNow = popupAddBooking.findViewById(R.id.book)
        dateTo = popupAddBooking.findViewById(R.id.toHours)

        bookNow.letterSpacing = 0.3F

        bookNow.setOnClickListener {
            Log.d(TAG, "Book")



            if (newBookingFromTxt.text.isNotEmpty() && newBookingNameTxt.text.isNotEmpty()) {

                //Parsing play hours
                val playHrs = dateTo.selectedItem.toString().split(" ")

                //Time date calculations
                val temp = Calendar.getInstance()
                //From Time
                temp.time = DateFormat.getDateInstance(DateFormat.FULL).parse(dt)
                val time = newBookingFromTxt.text.toString().split(":")
                temp.set(Calendar.HOUR, time[0].toInt())
                temp.set(Calendar.MINUTE, time[1].toInt())
                val from = temp.time
                val fromDate = DateFormat.getDateInstance(DateFormat.FULL).format(temp.time)
                val fromTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(temp.time)

                //To time
                temp.set(Calendar.HOUR, (temp.get(Calendar.HOUR) + playHrs[0].toInt()))
                val to = temp.time
                val toTime = DateFormat.getTimeInstance(DateFormat.SHORT).format(temp.time)
                val toDate = DateFormat.getDateInstance(DateFormat.FULL).format(temp.time)
                //Finish

                if (slotCheck(from, to)) {
                    bookNow.isEnabled = false
                    //Fetching venue object
                    var venue: Venue? = null
                    for (venueItem: Venue in loggedInUser!!.getVenues()!!) {
                        if (venueItem.getName() == venuesSpinner.selectedItem) {
                            venue = venueItem
                            break
                        }
                    }
                    //Calculating bills
                    val pricePerHr: Float = playHrs[0].toFloat() * venue!!.getRate().getPerHrRate()
                    val convenienceFee: Float = pricePerHr / venue.getRate().getVendorServiceFee()
                    val taxes = 0F
                    val total = pricePerHr + convenienceFee + taxes

                    //Initializing transaction object
                    val transaction = Transaction(convenienceFee, playHrs[0].toFloat(), pricePerHr, total, taxes, "manualCashReceiving",
                            "walk-in")
                    Log.d(TAG, "Transaction $transaction")

                    //Initializing booking object
                    val newBooking = Booking(null, venue, transaction, loggedInUser!!,
                            toTime, fromTime, fromDate, toDate, "booked", to, from)

                    //Creating new booking in db
                    createBooking(newBooking)
                }else{
                    bookNow.isEnabled = true
                    Toast.makeText(context, "This slot is unavailable", Toast.LENGTH_SHORT).show()
                }
            }
            else{
                Toast.makeText(context, "All fields are required", Toast.LENGTH_LONG).show()
            }
        }

        newBookingFromTxt.setOnClickListener {
            val c = Calendar.getInstance()
            val hourOfDay = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(context,R.style.DatePickerTheme,
                    TimePickerDialog.OnTimeSetListener { _, hr, min ->
                        //Converting time in seconds for validation check
                        c.time = DateFormat.getDateInstance(DateFormat.FULL).parse(dt)
                        c.set(Calendar.HOUR, hr)
                        c.set(Calendar.MINUTE, min)

                        if (today!! <= c.time){
                            newBookingFromTxt.error = null
                            newBookingFromTxt.clearFocus()

                            val timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.time)
                            newBookingFromTxt.text = timeString
                        }else{
                            newBookingFromTxt.error = "Enter valid time"
                            newBookingFromTxt.requestFocus()
                        }
                    }, hourOfDay, minute, false)
            timePickerDialog.show()
        }
        popupAddBooking.show()
    }

    private fun slotCheck(from: Date?, to: Date?): Boolean {
        var flag = true
        var i = 0
        if (bookings != null) {
            while (i < bookings!!.size){
                val startB = bookings!![i].getFrom()
                val endB = bookings!![i].getTo()
                if ((from!! <= endB) && (to!! >= startB)){
                    flag = false
                    break
                }
                i++
            }
        }
        return flag
    }

    private fun filterBookings(venueName: String) {
        val filteredBookings: ArrayList<Booking>
        if (bookings != null){
            filteredBookings = ArrayList()
            for (booking: Booking in bookings!!){
                if ((booking.getVenue().getName() == venueName) && (booking.getBookingDate() == dt)) {
                    filteredBookings.add(booking)
                }
            }
            if (filteredBookings.isNotEmpty()){
                hideProgressDialog()
                bookingsRecycler.adapter = DateBookingAdapter(filteredBookings)
                bookingsRecycler.adapter.notifyDataSetChanged()
            }else{
                hideProgressDialog()
                Toast.makeText(context, "No booking found of this date", Toast.LENGTH_SHORT).show()
            }
        }else{
            hideProgressDialog()
            Toast.makeText(context, "No bookings found", Toast.LENGTH_SHORT).show()
        }
    }

    //User entertainment
    private fun showProgressDialog() {
        (activity as MainActivity).showProgressDialog()
    }
    private fun hideProgressDialog(){
        (activity as MainActivity).hideProgressDialog()
    }
}
