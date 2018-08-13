package com.maidan.android.host.retrofit

data class PayloadFormat(private var id: String, private var data: Any ) {

    //Getters
    fun getDocId(): String {return this.id}
    fun getData(): Any {return this.data}

    //Setters
    fun setDocId(docId: String){this.id = docId}
    fun setData(data: Any){ this.data = data }
}