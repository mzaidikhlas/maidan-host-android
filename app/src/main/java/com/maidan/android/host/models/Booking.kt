package com.maidan.android.host.models

import java.io.Serializable

data class Booking(private var venue: Venue, private var transaction: Transaction?, private var user: User,
                   private var durationOfBooking: String, private var startTime: String, private var bookingDate: String, private var status: String): Serializable {

    //Getters
    fun getVenue(): Venue {return this.venue}
    fun getTransaction(): Transaction? {return this.transaction}
    fun getUser(): User {return this.user}
    fun getDurationOfBooking(): String {return this.durationOfBooking}
    fun getStartTime(): String {return this.startTime}
    fun getBookingDate(): String {return this.bookingDate}
    fun getStatus(): String {return this.status}

    //Setters
    fun setVenue(venue: Venue){this.venue = venue}
    fun setTransaction(transaction: Transaction) {this.transaction = transaction}
    fun setUser(user: User) {this.user = user}
    fun setDurationOfBooking(durationOfBooking: String) {this.durationOfBooking = durationOfBooking}
    fun setStartTime(startTime: String) {this.startTime = startTime}
    fun setBookingDate(bookingDate: String) {this.bookingDate = bookingDate}
    fun setStatus(status: String) {this.status = status}
}