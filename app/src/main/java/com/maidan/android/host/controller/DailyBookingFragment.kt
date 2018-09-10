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
    private lateinit var newBookingToTxt: TextView
    private lateinit var bookNow: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var venuesSpinner: Spinner
    private lateinit var backBtn: Button
    private lateinit var dateTo_btn:TextView
    private lateinit var dateFrom_btn: TextView
    private var dateString: String? = null
    private lateinit var datePicker : DatePickerDialog
    private lateinit var date_to: TextView

    private var availableSlotsFrom: ArrayList<Int>? = null
    private var availableSlotsTo: ArrayList<Int>? = null

    private var dt: String? = null
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
                    setBookings(selectedItem as String)
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
//                                    booking.setRef(response.body()!!.getPayload()[0].getDocId())
                                    bookings!!.add(booking)
                                    setBookings(venuesSpinner.selectedItem as String)
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
        newBookingToTxt = popupAddBooking.findViewById(R.id.to)
        bookNow = popupAddBooking.findViewById(R.id.book)
        dateTo_btn = popupAddBooking.findViewById(R.id.date_to)
        dateFrom_btn = popupAddBooking.findViewById(R.id.date_from)

        val c: Calendar = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        bookNow.letterSpacing = 0.3F

        if (fromTimeSeconds == null)
            newBookingToTxt.isEnabled = false

        dateTo_btn.text = dt
        dateFrom_btn.text = dt
        dateTo_btn.setOnClickListener {
            var selectedCalender: Calendar? = null

            datePicker = DatePickerDialog(context,R.style.DatePickerTheme,
                    DatePickerDialog.OnDateSetListener { view, yr, monthOfYear, dayOfMonth ->

                        selectedCalender = Calendar.getInstance()
                        selectedCalender!!.set(yr,monthOfYear,dayOfMonth)
                        val selectedDate = DateFormat.getDateInstance(DateFormat.FULL).format(selectedCalender!!.time)

                        dateString = selectedDate
                        dateTo_btn.text = dateString
                    },year,month,day)
            if (selectedCalender != null){
                datePicker.datePicker.updateDate(selectedCalender!!.get(Calendar.YEAR), selectedCalender!!.get(Calendar.MONTH)
                        , selectedCalender!!.get(Calendar.DAY_OF_MONTH))
            }
            datePicker.datePicker.minDate = c.timeInMillis
            datePicker.show()
        }

        bookNow.setOnClickListener {
            Log.d(TAG, "Book")
            if (newBookingFromTxt.text.isNotEmpty() && newBookingToTxt.text.isNotEmpty() && newBookingNameTxt.text.isNotEmpty()) {
                if (toTimeSeconds!! > fromTimeSeconds!!){
                    bookNow.isEnabled = false
                    newBookingToTxt.error = null
                    newBookingToTxt.clearFocus()
                    var venue: Venue? = null
                    for (venueItem: Venue in loggedInUser!!.getVenues()!!){
                        if (venueItem.getName() == venuesSpinner.selectedItem) {
                            venue = venueItem
                            break
                        }
                    }
                    //Calculating bills
                    val playHrs: Float =  ((toTimeSeconds!! - fromTimeSeconds!!)/3600).toFloat()
                    val pricePerHr: Float = playHrs*venue!!.getRate().getPerHrRate()
                    val convenienceFee: Float = pricePerHr/venue.getRate().getVendorServiceFee()
                    val taxes = 0F
                    val total = pricePerHr + convenienceFee + taxes

                    //Initializing transaction object
                    val transaction = Transaction(convenienceFee, playHrs, pricePerHr, total, taxes, "manualCashReceiving",
                            "walk-in")
                    Log.d(TAG, "Transaction $transaction")


                    if (availabiltyFromCheck(fromTimeSeconds!!) && availabiltyToCheck(toTimeSeconds!!)){
                        if (availabiltyCheck(fromTimeSeconds!!, toTimeSeconds!!)) {
                            newBookingToTxt.error = null
                            newBookingToTxt.clearFocus()
                            //Initializing booking object
                            val newBooking = Booking(null, venue, transaction, loggedInUser!!,
                                    newBookingToTxt.text.toString(), newBookingFromTxt.text.toString(), dt!!, dateTo_btn.text.toString(),"booked")

                            //Creating new booking in db
                            createBooking(newBooking)
                        }else{
                            bookNow.isEnabled = true
                            newBookingToTxt.error = "This time slot is unavailable"
                            newBookingToTxt.requestFocus()
                        }
                    }
                }else{
                    bookNow.isEnabled = true
                    newBookingToTxt.error = "Please enter correct time"
                    newBookingToTxt.requestLayout()
                }
            }
            else{
//                if (newBookingToTxt.text.isNullOrEmpty()){
//                    newBookingToTxt.error = "Time required"
//                }
//                if (newBookingFromTxt.text.isNullOrEmpty()){
//                    newBookingFromTxt.error = "Time required"
//                }
//                if (newBookingNameTxt.text.isNullOrEmpty()){
//                    newBookingNameTxt.error = "Name required"
//                }
                Toast.makeText(context, "All fields are required", Toast.LENGTH_LONG).show()
            }
        }

        newBookingFromTxt.setOnClickListener {
            val c = Calendar.getInstance()
            val hourOfDay = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)
            var h = hourOfDay
            var m = minute
            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(context,R.style.DatePickerTheme,
                    TimePickerDialog.OnTimeSetListener { view, hr, min ->
                        //Converting time in seconds for validation check
                        c.time = DateFormat.getDateInstance(DateFormat.FULL).parse(dt)
                        c.set(Calendar.HOUR_OF_DAY, hr)
                        c.set(Calendar.MINUTE, min)
                        fromTimeSeconds = ((hr*3600)+(min*60))

                        if (!availabiltyFromCheck(fromTimeSeconds!!)){
                            newBookingFromTxt.error = "This slot is not available"
                            newBookingFromTxt.requestFocus()
                        }
                        else {
                            newBookingToTxt.isEnabled = true
                            Log.d(TAG, "From true")
                            newBookingFromTxt.error = null
                            newBookingFromTxt.clearFocus()
                        }

                        Log.d(TAG, "From time $fromTimeSeconds")

                        val timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.time)
                        newBookingFromTxt.text = timeString
                    }, hourOfDay, minute, false)
            timePickerDialog.updateTime(h,m)
            timePickerDialog.show()

        }
        newBookingToTxt.setOnClickListener {
            val c = Calendar.getInstance()
            val hourOfDay = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)
            var h = hourOfDay
            var m = minute
            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(context,R.style.DatePickerTheme,
                    TimePickerDialog.OnTimeSetListener { view, hr, min ->
                        //Converting time in seconds for validation check

                        c.time = DateFormat.getDateInstance(DateFormat.FULL).parse(dateTo_btn.text.toString())
                        c.set(Calendar.HOUR_OF_DAY, hr)
                        c.set(Calendar.MINUTE, min)

                        toTimeSeconds = ((hr*3600)+(min*60))

                        if (!availabiltyToCheck(toTimeSeconds!!)){
                            Log.d(TAG, "To false")
                            newBookingToTxt.error = "This slot is not available"
                            newBookingToTxt.requestFocus()
                        }
                        else {
                            Log.d(TAG, "To true")
                            newBookingToTxt.error = null
                            newBookingToTxt.clearFocus()
                        }

                        Log.d(TAG, "To time $toTimeSeconds")

                        val timeString = DateFormat.getTimeInstance(DateFormat.SHORT).format(c.time)
                        newBookingToTxt.text = timeString
                    }, hourOfDay, minute, false)
            timePickerDialog.updateTime(h,m)
            timePickerDialog.show()
        }
        popupAddBooking.show()
    }

    private fun availabiltyFromCheck(from: Int): Boolean{
        var i = 0
        var flag = false

        if (dt == dateTo_btn.text) {
            while (i < availableSlotsFrom!!.size) {
                if (from >= availableSlotsFrom!![i]) {
                    if (from < availableSlotsTo!![i])
                        flag = true
                }
                i++
            }
        }else flag = true
        return flag
    }
    private fun availabiltyToCheck(to: Int): Boolean{
        var i = 0
        var flag = false

        if (dt == dateTo_btn.text) {
            while (i < availableSlotsTo!!.size) {
                if (to < availableSlotsTo!![i]) {
                    if (to >= availableSlotsFrom!![i])
                        flag = true
                }
                i++
            }
        }else flag = true
        return flag
    }

    private fun availabiltyCheck(from:Int, to:Int): Boolean{
        val count = if (availableSlotsFrom!!.size > availableSlotsTo!!.size) availableSlotsFrom!!.size else availableSlotsTo!!.size
        var i = 0
        var flag = false
        if (dt == dateTo_btn.text) {
            while (i < count) {
                if (from >= availableSlotsFrom!![i]) {
                    if (to <= availableSlotsTo!![i])
                        flag = true
                }
                i++
            }
        }else flag = true
        return flag
    }

    //Populating layout with bookings
    private fun setBookings(venueName: String) {
        val filteredBookings: ArrayList<Booking> = ArrayList()
        availableSlotsFrom = ArrayList()
        availableSlotsTo = ArrayList()
        Log.d(TAG, "Bookings $bookings")
        if (bookings != null){
            var timeFrom:ArrayList<String>
            var timeTo:ArrayList<String>
            var temp = 0
            for(booking: Booking in bookings!!){
                Log.d(TAG, "Booking venue ${booking.getVenue().getName()}")
                Log.d(TAG, "Venue name $venueName")
                Log.d(TAG, "Booking date ${booking.getBookingDate()}")
                Log.d(TAG, "Date $dt")
                if ((booking.getVenue().getName() == venueName) && (booking.getBookingDate() == dt)) {
                    Log.d(TAG, "1")
                    filteredBookings.add(booking)

                    //Parsing dates
                    timeFrom = booking.getStartTime().split(":") as ArrayList<String>
                    timeTo = booking.getDurationOfBooking().split(":") as ArrayList<String>

                    //Converting dates in seconds to do some calculations
                    val timeFromInSeconds = ((timeFrom[0].toInt() * 3600) + (timeFrom[1].toInt() * 60))
                    val timeToInSeconds = ((timeTo[0].toInt() * 3600) + (timeTo[1].toInt() * 60))

                    //Make available slots list
                    availableSlotsFrom!!.add(temp)
                    availableSlotsTo!!.add(timeFromInSeconds)
                    temp = timeToInSeconds
                }
            }
            if (filteredBookings.isNotEmpty()) {
                availableSlotsFrom!!.add(temp)
                availableSlotsTo!!.add(86400)

                availableSlotsFrom!!.sort()
                availableSlotsTo!!.sort()
                Log.d(TAG, "Avail from $availableSlotsFrom")
                Log.d(TAG, "Avail to $availableSlotsTo")

                hideProgressDialog()
                bookingsRecycler.adapter = DateBookingAdapter(filteredBookings)
                bookingsRecycler.adapter.notifyDataSetChanged()
            }else{
                availableSlotsFrom!!.add(0)
                availableSlotsTo!!.add(86400)
                hideProgressDialog()
                Toast.makeText(context, "No booking found of this date", Toast.LENGTH_SHORT).show()
            }
        }else{
            availableSlotsFrom!!.add(0)
            availableSlotsTo!!.add(86400)
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
