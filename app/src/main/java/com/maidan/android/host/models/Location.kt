package com.maidan.android.host.models

import java.io.Serializable

data class Location(private var latitude: Double, private var longitude: Double, private var country: String,
                    private var city: String, private var area: String): Serializable{

    //Getters
    fun getLatitude(): Double{return this.latitude}
    fun getLongitude(): Double{return this.longitude}
    fun getCountry(): String{return this.country}
    fun getCity(): String{return this.city}
    fun getArea(): String{return this.area}

    //Setters
    fun setLatitude(latitude: Double){this.latitude = latitude}
    fun setLongitude(longitude: Double){this.longitude = longitude}
    fun setCountry(country: String){this.country = country}
    fun setCity(city: String){this.city = city}
    fun setArea(area: String){this.area = area}
}