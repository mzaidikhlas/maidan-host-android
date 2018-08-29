package com.maidan.android.host


import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit


class LoginPhoneVerificationFragment : Fragment() {

    private lateinit var verifyCodeEdtTxt: EditText
    private lateinit var verifyCodeBtn: Button
    private lateinit var verifyCodeProgressBar: ProgressBar
    private lateinit var number: String
    private lateinit var verificationId : String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login_phone_verification, container, false)

        if (arguments != null){
            number = arguments!!.getString("phonenumber")
        }

        verifyCodeEdtTxt = view.findViewById(R.id.verifyNumberCode)
        verifyCodeBtn = view.findViewById(R.id.verifyCodeBtn)
        verifyCodeProgressBar = view.findViewById(R.id.verifyProgressBar)

        sendVerificationCode(number)

        verifyCodeBtn.setOnClickListener {
            val code = verifyCodeEdtTxt.text.toString()

            if (code.isEmpty() || code.length < 6){
                verifyCodeEdtTxt.error = "Enter code ..."
                verifyCodeEdtTxt.requestFocus()
            }else{
                val credential = PhoneAuthProvider.getCredential(verificationId, code)
                signInWithCredential (credential)
                Toast.makeText(context, "Hey $code", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun sendVerificationCode(number: String){
         PhoneAuthProvider.getInstance().verifyPhoneNumber(
                 number,
                 60,
                 TimeUnit.SECONDS,
                 activity!!,
                 mCallBack
         )
    }

    private val mCallBack = object: PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

        override fun onCodeSent(p0: String?, p1: PhoneAuthProvider.ForceResendingToken?) {
            super.onCodeSent(p0, p1)
            verificationId = p0!!
        }

        override fun onVerificationCompleted(p0: PhoneAuthCredential?) {
            val code = p0!!.smsCode
            if (code.isNullOrEmpty() || code!!.length < 6){
                verifyCodeEdtTxt.error = "Enter code.."
                verifyCodeEdtTxt.requestFocus()
            }else{
                signInWithCredential(p0)
            }
        }

        override fun onVerificationFailed(p0: FirebaseException?) {
            Log.d("LoginActivity", p0.toString())
            fragmentManager!!.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            Toast.makeText(context, "Error ${p0!!.message}", Toast.LENGTH_SHORT).show()
        }

    }

    private fun signInWithCredential(phoneAuthCredential: PhoneAuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential).addOnCompleteListener { task ->
            if (task.isSuccessful){
                Toast.makeText(context, "Successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(context, LoadingActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }else {
                // Sign in failed, display a message and update the UI
                Log.w("LoginActivity", "signInWithCredential:failure", task.exception)
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    // The verification code entered was invalid
                    Toast.makeText(context, "The verification code entered was invalid", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
