package com.maidan.android.host.models

import java.io.Serializable

data class Transaction (private var convenienceFee: Float, private var totalHours: Float, private var totalHoursPrice: Float,
                        private var total: Float, private var taxes: Float, private var paymentMethod: String, private var customerType: String): Serializable{
    //Getters
    fun getCustomerType(): String{return this.customerType}
    fun getConvenienceFee(): Float{return this.convenienceFee}
    fun getTotalHours(): Float{return this.totalHours}
    fun getTotalHoursPrice(): Float{return this.totalHoursPrice}
    fun getTotal(): Float{return this.total}
    fun getTaxes(): Float{return this.taxes}
    fun getPaymentMethod(): String{return this.paymentMethod}
}