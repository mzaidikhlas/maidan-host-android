package com.maidan.android.host.controller
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.maidan.android.host.LoginActivity
import com.maidan.android.host.models.BookingInfo
import com.maidan.android.host.R
import java.util.ArrayList


class SettingsFragment: Fragment() {
    private lateinit var signoutTxt: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var pictureUpload: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment     
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

     //   pictureUpload = view.findViewById(R.id.venueImage)
        signoutTxt = view.findViewById(R.id.signout)
        signoutTxt.setOnClickListener {
            mAuth.signOut()
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
//        pictureUpload.setOnClickListener {
//
//            openGallery()
//        }
        return view
    }

//
}

