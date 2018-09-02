package com.maidan.android.host.controller

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import java.util.ArrayList
import com.maidan.android.host.R

class StatsFragment: Fragment() {

    private lateinit var earningChart:BarChart
    private lateinit var weekDays:ArrayList<String>
    private lateinit var weeklyEarnings: ArrayList<BarEntry>
    private lateinit var barSetData:BarDataSet
    private lateinit var barData:BarData


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_stats, container, false)

        weeklyEarnings = ArrayList()
        weeklyEarnings.add(BarEntry(5500f,1f))
        weeklyEarnings.add(BarEntry(3000f,2f))
        weeklyEarnings.add(BarEntry(4400f,3f))
        weeklyEarnings.add(BarEntry(67000f,4f))
        weeklyEarnings.add(BarEntry(2300f,5f))
        weeklyEarnings.add(BarEntry(300f,6f))
        weeklyEarnings.add(BarEntry(11000f,7f))

        barSetData = BarDataSet(weeklyEarnings,"Earnings")
        earningChart = BarChart(context)

        weekDays = ArrayList()
        weekDays.add("Monday")
        weekDays.add("Tuesday")
        weekDays.add("Wednesday")
        weekDays.add("Thursday")
        weekDays.add("Friday")
        weekDays.add("Saturday")
        weekDays.add("Sunday")

        barData = BarData(barSetData)


        earningChart.invalidate()


        return view
    }

}