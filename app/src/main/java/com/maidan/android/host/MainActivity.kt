package com.maidan.android.host

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.maidan.android.host.controller.DashboardFragment
import com.maidan.android.host.controller.HomeFragment
import com.maidan.android.host.controller.login.SignupDetailsFragment
import com.maidan.android.host.retrofit.ApiInterface
import com.maidan.android.host.retrofit.ApiResponse
import com.maidan.android.host.retrofit.PayloadFormat
import com.maidan.android.host.retrofit.RetrofitClient
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.booking_information.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth

    private val TAG = "HostMainActivity"

    private val mOnNavigationItemSelectedListener = OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, HomeFragment()).commit();
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
              supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, DashboardFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
       //         supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, StatsFragment()).commit();
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        if (currentUser == null){
            Log.d(TAG, "Idhr hsi")
            this.startActivity(Intent(this, LoginActivity::class.java))
        }else{
            Log.d(TAG, "Here")
            currentUser.getIdToken(true).addOnCompleteListener{task ->
                if (task.isSuccessful){
                    val idToken = task.result.token
                    val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)
                    val call: Call<ApiResponse> = apiService.getUserInfoByEmail(idToken!!)
                    call.enqueue(object: Callback<ApiResponse> {
                        override fun onFailure(call: Call<ApiResponse>?, t: Throwable?) {
                            throw t!!
                        }

                        override fun onResponse(call: Call<ApiResponse>?, response: Response<ApiResponse>?) {
                            if (response!!.isSuccessful){
                                Log.d(TAG,"In response")
                                if (response.body()!!.getStatusCode() == 200){
                                    if (response.body()!!.getType() == "User"){
                                        val payload = response.body()!!.getPayload()
                                        if (payload.isEmpty()){
                                            navigation.visibility = View.INVISIBLE
                                            val signupDetailsFragment = SignupDetailsFragment()

                                            val bundle = Bundle()
                                            bundle.putString("name", currentUser.displayName)
                                            bundle.putString("email", currentUser.email)
                                            bundle.putString("number", currentUser.phoneNumber)
                                            bundle.putString("password", null)

                                            signupDetailsFragment.arguments = bundle
                                            supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, signupDetailsFragment).commit()
                                        }else{
                                            navigation.visibility = View.VISIBLE
                                            supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, HomeFragment()).commit()
                                        }
                                    }
                                }
                            }
                        }

                    })
                }
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)


    }
}

