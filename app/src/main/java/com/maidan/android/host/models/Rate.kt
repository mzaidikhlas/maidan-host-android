package com.maidan.android.host.models

data class Rate(private var perHrRate: Int, private var nightRate: Int, private var peakRate: Int,
                private var clientServiceFee: Int, private var vendorServiceFee: Int){

    fun getPerHrRate(): Int{return this.perHrRate}

    fun getNightRate(): Int{return this.nightRate}

    fun getPeakRate(): Int{return this.peakRate}

    fun getClientServiceFee(): Int{return this.clientServiceFee}

    fun getVendorServiceFee(): Int{return this.vendorServiceFee}

    fun setPerHrRate(perHrRate: Int){this.perHrRate = perHrRate}

    fun setNightRate(nightRate: Int){this.nightRate = nightRate}

    fun setPeakRate(peakRate: Int){this.peakRate= peakRate}

    fun setClientServiceFee(clientServiceFee: Int){this.clientServiceFee = clientServiceFee}

    fun setVendorServiceFee(vendorServiceFee: Int){this.vendorServiceFee = vendorServiceFee}

}