package com.maidan.android.host.controller

import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import java.text.DecimalFormat


class MyYAxisValueFormatter: IAxisValueFormatter {

    private val mFormat: DecimalFormat

    /** this is only needed if numbers are returned, else return 0  */
    val decimalDigits: Int
        get() = 1

    init {
        // format values to 1 decimal digit
        mFormat = DecimalFormat("###,###,##0.0")
    }

    override fun getFormattedValue(value: Float, axis: AxisBase): String {
        // "value" represents the position of the label on the axis (x or y)
        return mFormat.format(value) + " $"
    }
}