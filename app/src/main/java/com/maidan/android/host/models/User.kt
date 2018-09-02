package com.maidan.android.host.models

import java.io.Serializable

data class User(private var email: String, private var name: String, private var password: String?,
                private var phone: String, private var cnic: String, private var displayAvatar: String?,
                private var dob: String, private var gender: String, private var isClient: Boolean,
                private var isOwner: Boolean, private var userRecord: UserRecord?, private var venues: ArrayList<Venue>?): Serializable {

    //Getters
    fun getIsClient():Boolean{return this.isClient}

    fun getIsOwner():Boolean{return this.isOwner}

    fun getEmail(): String {
        return this.email
    }

    fun getName(): String {
        return this.name
    }

    fun getPassword(): String? {
        return this.password
    }

    fun getPhone(): String{
        return this.phone
    }
    fun getCnic(): String{
        return this.cnic
    }

    fun getUserRecord(): UserRecord? {
        return this.userRecord
    }

    fun getDob(): String {
        return this.dob
    }

    fun getGender(): String {
        return this.gender
    }

    fun getDisplayAvatar(): String? {
        return this.displayAvatar
    }

    fun getVenues(): ArrayList<Venue>? { return this.venues }

    //Setters
    fun setVenues(venues: ArrayList<Venue>){ this.venues = venues }

    fun setIsClient(isClient: Boolean){this.isClient = isClient}

    fun setIsOwner(isOwner: Boolean){this.isOwner = isOwner}

    fun setDob(dob: String){
        this.dob = dob
    }

    fun setGender(gender: String){
        this.gender = gender
    }

    fun setEmail(email: String){
        this.email = email
    }

    fun setName(name: String){
        this.name = name
    }

    fun setPassword(password: String){
        this.password = password
    }

    fun setPhone(phone: String){
        this.phone = phone
    }
    fun setCnic(cnic: String){
        this.cnic = cnic
    }

    fun setDisplayAvatar(displayAvatar: String){
        this.displayAvatar = displayAvatar
    }
    fun setUserRecord(userRecord: UserRecord){
        this.userRecord = userRecord
    }

}