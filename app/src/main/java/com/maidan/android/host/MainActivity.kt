package com.maidan.android.host

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.maidan.android.host.controller.DashboardFragment
import com.maidan.android.host.controller.HomeFragment
import com.maidan.android.host.controller.StatsFragment
import com.maidan.android.host.models.User
import com.maidan.android.host.retrofit.ApiInterface
import com.maidan.android.host.retrofit.ApiResponse
import com.maidan.android.host.retrofit.RetrofitClient
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private var dialog: AlertDialog? = null
    private lateinit var animation: AnimationDrawable

    var loggedInUser: User? = null
    private val TAG = "MainActivity"

    private val mOnNavigationItemSelectedListener = OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                redirect()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
              supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, DashboardFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, StatsFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "OnStart: Main")
        if (loggedInUser != null){
            Log.d(TAG, "hai idhr")
            Log.d(TAG, "User $loggedInUser")
            redirect()
        }else {
            Log.d(TAG, "Nope")
            val user = mAuth.currentUser
            if (user != null) {
                showProgressDialog()
                user.getIdToken(true).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val idToken = task.result.token
                        val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)
                        Log.d(TAG, "Number ${user.phoneNumber!!}")
                        val call: Call<ApiResponse> = apiService.getUserInfoByPhone(user.phoneNumber!!, idToken!!)
                        call.enqueue(object : Callback<ApiResponse> {
                            override fun onFailure(call: Call<ApiResponse>?, t: Throwable?) {
                                hideProgressDialog()
                                Log.d(TAG, t.toString())
                                throw t!!
                            }

                            override fun onResponse(call: Call<ApiResponse>?, response: Response<ApiResponse>?) {
                                if (response!!.isSuccessful) {
                                    if (response.body()!!.getStatusCode() == 200) {
                                        if (response.body()!!.getType() == "User") {
                                            val payload = response.body()!!.getPayload()
                                            if (payload.isNotEmpty()) {
                                                val gson = Gson()
                                                val jsonObject = gson.toJsonTree(payload[0].getData()).asJsonObject
                                                loggedInUser = gson.fromJson(jsonObject, User::class.java)
                                                Log.d(TAG, loggedInUser.toString())
                                                hideProgressDialog()
                                                redirect()

                                            } else {
                                                hideProgressDialog()
                                                val intent = Intent(applicationContext, SignupDetailsActivity::class.java)
                                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                startActivity(intent)
                                            }
                                        }
                                    } else {
                                        mAuth.signOut()
                                        Toast.makeText(applicationContext, "OnCode: ${response.body()!!.getStatusCode()}", Toast.LENGTH_LONG).show()
                                        hideProgressDialog()
                                        val intent = Intent(applicationContext, LoginActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
                                    }
                                } else {
                                    mAuth.signOut()
                                    Toast.makeText(applicationContext, "OnResponseFailure ", Toast.LENGTH_SHORT).show()
                                    hideProgressDialog()
                                    val intent = Intent(applicationContext, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                }
                            }

                        })
                    } else {
                        hideProgressDialog()
                        Toast.makeText(applicationContext, "Error ${task.exception}", Toast.LENGTH_LONG).show()
                        Log.d(TAG, "Yeh hai ${task.exception}")
                        throw task.exception!!
                    }
                }
            } else {
                val intent = Intent(applicationContext, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
        }
    }

    private fun redirect(){
        val fragment = HomeFragment()
        val bundle = Bundle()
        bundle.putSerializable("loggedInUser", loggedInUser)
        fragment.arguments = bundle
        supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, fragment).commit()
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        super.onSaveInstanceState(outState, outPersistentState)
        Log.d(TAG, "OnSaveInstanceState: Main")
        if (loggedInUser != null)
            outState!!.putSerializable("loggedInUser", loggedInUser)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.d(TAG, "OnCreate: Main")

        mAuth = FirebaseAuth.getInstance()
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "OnDestroy: Main")
    }

    private fun showProgressDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.progress_dialog, null)
        val loader = dialogView.findViewById<ImageView>(R.id.loadingProgressbar)
        builder.setView(dialogView)
        builder.setCancelable(false)
        dialog = builder.create()
        dialog!!.window.setLayout(600,400)
        dialog!!.show()
        animation = loader.drawable as AnimationDrawable
        animation.start()
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    private fun hideProgressDialog(){
        animation.stop()
        dialog!!.dismiss()
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }
}

