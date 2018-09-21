package com.maidan.android.host.models

import java.io.Serializable
import java.util.*

data class Booking(private var ref: String?, private var venue: Venue, private var transaction: Transaction?, private var user: User,
                   private var durationOfBooking: String, private var startTime: String, private var bookingDate: String,
                   private var toBookingDate: String?, private var status: String,
                   private var to: Date?, private var from: Date?): Serializable {

    //Getters
    fun getTo():Date? {return this.to}
    fun getFrom():Date? {return this.from}
    fun getToBookingDate():String?{return this.toBookingDate}
    fun getRef(): String? {return this.ref}
    fun getVenue(): Venue {return this.venue}
    fun getTransaction(): Transaction? {return this.transaction}
    fun getUser(): User {return this.user}
    fun getDurationOfBooking(): String {return this.durationOfBooking}
    fun getStartTime(): String {return this.startTime}
    fun getBookingDate(): String {return this.bookingDate}
    fun getStatus(): String {return this.status}

    //Setters
    fun setTo(to: Date?) { this.to = to}
    fun setFrom(from: Date?) { this.from = from}
    fun setToBookingDate(toBookingDate: String){this.toBookingDate = toBookingDate}
    fun setRef(ref: String){this.ref = ref}
    fun setVenue(venue: Venue){this.venue = venue}
    fun setTransaction(transaction: Transaction) {this.transaction = transaction}
    fun setUser(user: User) {this.user = user}
    fun setDurationOfBooking(durationOfBooking: String) {this.durationOfBooking = durationOfBooking}
    fun setStartTime(startTime: String) {this.startTime = startTime}
    fun setBookingDate(bookingDate: String) {this.bookingDate = bookingDate}
    fun setStatus(status: String) {this.status = status}
}