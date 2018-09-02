package com.maidan.android.host


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class LoginPhoneFragment : Fragment() {

    private lateinit var phoneEdtTxt: EditText
    private lateinit var submitBtn: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login_phone, container, false)

        phoneEdtTxt = view.findViewById(R.id.loginPhoneNo)
        submitBtn = view.findViewById(R.id.login_btn)

        submitBtn.setOnClickListener{
            val number = phoneEdtTxt.text.toString()
            if (number.isEmpty() || number.length < 10){
                phoneEdtTxt.error = "Valid number is required"
                phoneEdtTxt.requestFocus()
            }else{
                val numStr = "+92$number"
                val verifyNumber = LoginPhoneVerificationFragment()
                val bundle = Bundle()
                bundle.putString("phonenumber", numStr)
                verifyNumber.arguments = bundle
                fragmentManager!!.beginTransaction().addToBackStack("loginPhoneFragment").replace(R.id.login_fragment_layout, verifyNumber).commit()
                Toast.makeText(context, "Number is $numStr", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }
}
