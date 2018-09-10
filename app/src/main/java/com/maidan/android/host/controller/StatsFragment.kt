package com.maidan.android.host.controller

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.maidan.android.host.R
import com.anychart.enums.HoverMode
import com.anychart.enums.TooltipPositionMode
import android.util.Log
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.enums.Anchor
import com.anychart.enums.Position
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import com.maidan.android.host.MainActivity
import com.maidan.android.host.models.Booking
import com.maidan.android.host.models.User
import com.maidan.android.host.retrofit.ApiInterface
import com.maidan.android.host.retrofit.ApiResponse
import com.maidan.android.host.retrofit.PayloadFormat
import com.maidan.android.host.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StatsFragment: Fragment() {

    //Layouts
    private lateinit var anyChartView: AnyChartView
    private lateinit var progressBar: ProgressBar
    private lateinit var totalRevenueTxt: TextView
    private lateinit var venueNameTxt: TextView
    private lateinit var ownerNameTxt: TextView
    private lateinit var noOfBookingsTxt: TextView
    private lateinit var creditTxt: TextView
    private lateinit var debitTxt: TextView

    private var loggedInUser: User? = null

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    //Local
    private val TAG = "StatsFragment"

    //Api Call Response
    private lateinit var payload: ArrayList<PayloadFormat>
    private var bookings: ArrayList<Booking>? = null

    //Revenue Calculations
    private var totalRevenue = 0.0
    private var data: ArrayList<DataEntry>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loggedInUser = (activity as MainActivity).getLoggedInUser()
        Log.d(TAG, loggedInUser.toString())

        mAuth = FirebaseAuth.getInstance()
        currentUser = mAuth.currentUser!!
        getBookings()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_stats, container, false)
        anyChartView = view.findViewById(R.id.earningsBarChart)
        totalRevenueTxt = view.findViewById(R.id.earnings)
        venueNameTxt = view.findViewById(R.id.venueName)
        ownerNameTxt = view.findViewById(R.id.ownerName)
        noOfBookingsTxt = view.findViewById(R.id.noOfBookings)
        creditTxt = view.findViewById(R.id.credit)
        debitTxt = view.findViewById(R.id.debit)
        progressBar = view.findViewById(R.id.chartLoading)

        creditTxt.text = "0"
        debitTxt.text = "0"

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

    //Getting all bookings
    private fun getBookings(){
        showProgressDialog()
        currentUser.getIdToken(true).addOnCompleteListener{task ->
            if (task.isSuccessful){
                val idToken = task.result.token
                val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)
                val call: Call<ApiResponse> = apiService.getBookingsByOwnerPhone(loggedInUser!!.getId()!!, idToken!!)
                call.enqueue(object: Callback<ApiResponse> {
                    override fun onFailure(call: Call<ApiResponse>?, t: Throwable?) {
                        hideProgressDialog()
                        Toast.makeText(context, "Error $t", Toast.LENGTH_LONG).show()
                        Log.d(TAG, "Error $t")
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
                                        for (item: PayloadFormat in payload) {
                                            val jsonObject = gson.toJsonTree(item.getData()).asJsonObject
                                            Log.d(TAG, "Json$jsonObject")
                                            booking = gson.fromJson(jsonObject, Booking::class.java)
                                            bookings!!.add(booking)
                                        }
                                        calculateRevenue()
                                        Log.d(TAG, "Bookings call $bookings")
                                        (activity as MainActivity).hideProgressDialog()
                                    }else{
                                        hideProgressDialog()
                                        Toast.makeText(context, "No Bookings found", Toast.LENGTH_LONG).show()
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

    private fun calculateRevenue(){
        var mon = 0.0
        var tus = 0.0
        var wed = 0.0
        var thu = 0.0
        var fri = 0.0
        var sat = 0.0
        var sun = 0.0
        if (bookings != null){
            var date: List<String>
            var total = 0F
            for (booking: Booking in bookings!!){
                Log.d(TAG, "Booking $booking")
                total = booking.getTransaction()!!.getTotal()
                date = booking.getBookingDate().split(",")
                when (date[0]){
                    "Monday" -> mon += total
                    "Tuesday" -> tus += total
                    "Wednesday" -> wed += total
                    "Thursday" -> thu += total
                    "Friday" -> fri += total
                    "Saturday" -> sat += total
                    "Sunday" -> sun += total
                }
                totalRevenue += total
            }
        }
        venueNameTxt.text = "All Venues"
        ownerNameTxt.text = bookings!![0].getUser().getName()
        noOfBookingsTxt.text = bookings!!.size.toString()
        totalRevenueTxt.text = "Rs. $totalRevenue"
        data = ArrayList()
        data!!.add(ValueDataEntry("Mon", mon/1000))
        data!!.add(ValueDataEntry("Tue", tus/1000))
        data!!.add(ValueDataEntry("Wed", wed/1000))
        data!!.add(ValueDataEntry("Thu", thu/1000))
        data!!.add(ValueDataEntry("Fri", fri/1000))
        data!!.add(ValueDataEntry("Sat", sat/1000))
        data!!.add(ValueDataEntry("Sun", sun/1000))

        Log.d(TAG, "DataList $data")
        Log.d(TAG, "TotalRevenue $totalRevenue")

        populateCharts()
    }

    private fun populateCharts(){
        //List must be divided by 1000
        val cartesian = AnyChart.column()
        val column = cartesian.column(data).color("#42DE95")
        AnyChart.bar().palette()

        column.tooltip()
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0.0)
                .offsetY(5.0)
                .format("PKR {%Value}{groupsSeparator: }")

        cartesian.animation(true)

        cartesian.yScale().minimum(0)

        cartesian.yAxis(0).labels().format("{%Value}K")

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
        cartesian.interactivity().hoverMode(HoverMode.BY_X)

        anyChartView.setChart(cartesian)
        progressBar.visibility = View.GONE
    }

    //User entertainment
    private fun showProgressDialog() {
        (activity as MainActivity).showProgressDialog()
    }
    private fun hideProgressDialog(){
        (activity as MainActivity).hideProgressDialog()
    }

}