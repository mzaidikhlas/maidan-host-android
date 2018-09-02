package com.maidan.android.host.controller

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter

class MyXAxisValueFormatter(value: ArrayList<String>) : IAxisValueFormatter {



    private var mValues: ArrayList<String> = value


    override fun getFormattedValue(value: Float, axis: AxisBase?): String {
        return mValues[value.toInt()]
    }

}