package com.maidan.android.host.retrofit

data class ApiResponse(private var statusCode: Int, private var statusMessage: String, private var type: String,
                       private var payload: ArrayList<PayloadFormat>, private var message: String) {

    //Getters
    fun getStatusCode(): Int {return this.statusCode}
    fun getStatusMessage(): String {return this.statusMessage}
    fun getPayload(): ArrayList<PayloadFormat> {return this.payload}
    fun getMessage(): String {return this.message}
    fun getType(): String {return this.type}

    //Setters
    fun setStatusCode(statusCode: Int) {this.statusCode = statusCode}
    fun setStatusMessage(message: String){this.statusMessage = message}
    fun setPayload(payload: ArrayList<PayloadFormat>){this.payload = payload}
    fun setMessage(message: String){this.message = message}
    fun setType(type: String){this.type = type}

}