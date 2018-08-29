package com.maidan.android.host.controller


import android.app.Dialog
import android.app.FragmentManager
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
import com.maidan.android.host.models.Transaction
import com.maidan.android.host.models.User
import com.maidan.android.host.models.Venue
import com.maidan.android.host.retrofit.ApiInterface
import com.maidan.android.host.retrofit.ApiResponse
import com.maidan.android.host.retrofit.PayloadFormat
import com.maidan.android.host.retrofit.RetrofitClient
import kotlinx.android.synthetic.main.popup_add_booking.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
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

    private var dt: String? = null
    private val TAG = "BookingDailyFragment"
    private var user: User? = null
    private lateinit var venues: ArrayList<Venue>

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    private lateinit var loggedInUser: User

    //Api Call Response
    private lateinit var payload: ArrayList<PayloadFormat>
    private var bookings: ArrayList<Booking>? = null

    private lateinit var popupBooking: Button
    private lateinit var popupAddBooking: Dialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_daily_booking, container, false)
        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!

        //Layouts init
        dateTxt = view.findViewById(R.id.bookingDate)
        bookingsRecycler = view.findViewById(R.id.dailyBooking)
        popupBooking = view.findViewById(R.id.addBooking)
        progressBar = view.findViewById(R.id.dailyBookingProgressBar)
        venuesSpinner = view.findViewById(R.id.dailyBookingVenuesSpinner)
        backBtn = view.findViewById(R.id.dailyBookingBack)

        //Showing progressbar
        progressBar.visibility = View.VISIBLE

        //populating layout
        dateTxt.text = dt

        //Getting bundle data
        if (arguments != null){
            dt = arguments!!.getString("date")
            bookings = arguments!!.get("bookings") as ArrayList<Booking>?
        }else Log.d(TAG, "Arguments empty")
        loggedInUser = activity!!.intent.extras.getSerializable("loggedInUser") as User

        //Preventing user to add booking with no venue
        if (loggedInUser.getVenues()!!.isEmpty())
            popupBooking.isEnabled = false

        //Setting up recycler view for bookings
        bookingsRecycler.layoutManager = LinearLayoutManager(context, LinearLayout.VERTICAL, false)

        //Back button listener
        backBtn.setOnClickListener {
            fragmentManager!!.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        //venues spinner init
        val spinnerArray = ArrayList<String>()
        for (item: Venue in loggedInUser.getVenues()!!)
            spinnerArray.add(item.getName())

        val adapter = ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, spinnerArray)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        venuesSpinner.adapter = adapter
        venuesSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View, position: Int, id: Long) {
                //Getting bookings from db
                setBookings(venuesSpinner.selectedItem as String)
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                venuesSpinner.requestFocus()
            }
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

    //Creating new booking
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
                                    if (bookings == null)
                                        bookings = ArrayList()

                                    bookings!!.add(booking)
                                    setBookings(venuesSpinner.selectedItem as String)
                                    popupAddBooking.hide()
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

    //New Booking popup
    fun showAddBookingPopup()
    {
        //Time validation
        var toTimeSeconds: Int? = null
        var fromTimeSeconds: Int? = null

        //Popup layouts init
        popupAddBooking.setContentView(R.layout.popup_add_booking)
        newBookingNameTxt = popupAddBooking.findViewById(R.id.name)
        newBookingFromTxt = popupAddBooking.findViewById(R.id.from)
        newBookingToTxt = popupAddBooking.findViewById(R.id.to)
        bookNow = popupAddBooking.findViewById(R.id.book)

        bookNow.letterSpacing = 0.3F

        bookNow.setOnClickListener {
            Log.d(TAG, "Book")
            if (newBookingFromTxt.text.isNotEmpty() && newBookingToTxt.text.isNotEmpty() && newBookingNameTxt.text.isNotEmpty()) {
                if (toTimeSeconds!! > fromTimeSeconds!!){
                    bookNow.isEnabled = false
                    var venue: Venue? = null
                    for (venueItem: Venue in loggedInUser.getVenues()!!){
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
                    val transaction = Transaction(convenienceFee, playHrs, pricePerHr, total, taxes, "manualCashReceiving")
                    Log.d(TAG, "Transaction $transaction")

                    //Initializing booking object
                    val newBooking = Booking(venue,transaction,loggedInUser,
                            newBookingToTxt.text.toString(), newBookingFromTxt.text.toString(), dt!!, "booked")

                    //Creating new booking in db
                    createBooking(newBooking)
                }else{
                    newBookingToTxt.error = "Please enter correct time"
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

            // Launch Time Picker Dialog
            val timePickerDialog = TimePickerDialog(context,R.style.DatePickerTheme,
                    TimePickerDialog.OnTimeSetListener { view, hr, min ->
                        //Converting time in seconds for validation check
                        fromTimeSeconds = ((hr*3600)+(min*60))
                        Log.d(TAG, "To time $fromTimeSeconds")

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
                        //Converting time in seconds for validation check
                        toTimeSeconds = ((hr*3600)+(min*60))
                        Log.d(TAG, "From time $toTimeSeconds")

                        Log.d(TAG, "Hour: $hr, Min: $min")
                        val timeString = "$hr:$min"
                        newBookingToTxt.text = timeString
                    }, hourOfDay, minute, false)
            timePickerDialog.show()
        }
        popupAddBooking.show()
    }

    private fun setBookings(venueName: String) {
        val filteredBookings: ArrayList<Booking> = ArrayList()
        if (bookings != null){
            for(booking: Booking in bookings!!){
                if ((booking.getVenue().getName() == venueName) && (booking.getBookingDate() == dt))
                    filteredBookings.add(booking)
            }
            if (filteredBookings.isNotEmpty()){
                progressBar.visibility = View.INVISIBLE
                bookingsRecycler.adapter = DateBookingAdapter(filteredBookings)
                bookingsRecycler.adapter.notifyDataSetChanged()
            }else{
                progressBar.visibility = View.INVISIBLE
                bookingsRecycler.adapter = DateBookingAdapter(filteredBookings)
                bookingsRecycler.adapter.notifyDataSetChanged()
                Toast.makeText(context, "No booking found of this venue", Toast.LENGTH_SHORT).show()
            }
        }else{
            progressBar.visibility = View.INVISIBLE
            Toast.makeText(context, "No booking found of this date", Toast.LENGTH_SHORT).show()
        }
    }
}
