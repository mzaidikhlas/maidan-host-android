package com.maidan.android.host

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.PersistableBundle
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.WindowManager
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.maidan.android.host.controller.SettingsFragment
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
import kotlin.math.log

class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private var dialog: AlertDialog? = null
    private lateinit var animation: AnimationDrawable

    private var loggedInUser: User? = null
    private val TAG = "MainActivity"
    private var fragmentCallCount = 0

    private val mOnNavigationItemSelectedListener = OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, HomeFragment()).commit()
                //redirect()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
              supportFragmentManager.beginTransaction().replace(R.id.fragment_layout,StatsFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, SettingsFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "OnStart: Main")
        if (loggedInUser != null){
            Log.d(TAG, "User $loggedInUser")
//            supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, HomeFragment()).commit()
        }else {
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

                                                if (loggedInUser != null){
                                                    (loggedInUser as User).setId(payload[0].getDocId())
                                                }
                                                Log.d(TAG, loggedInUser.toString())
                                                supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, HomeFragment()).commit()
                                            } else {
                                                hideProgressDialog()
                                                val intent = Intent(applicationContext, SignupDetailsActivity::class.java)
                                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                startActivity(intent)
                                            }
                                        }
                                    } else {
                                        hideProgressDialog()
                                        val intent = Intent(applicationContext, SignupDetailsActivity::class.java)
                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        startActivity(intent)
//                                        mAuth.signOut()
//                                        Toast.makeText(applicationContext, "OnCode: ${response.body()!!.getStatusCode()}", Toast.LENGTH_LONG).show()
//                                        hideProgressDialog()
//                                        val intent = Intent(applicationContext, LoginActivity::class.java)
//                                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//                                        startActivity(intent)
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
        this.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        mAuth = FirebaseAuth.getInstance()
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "OnDestroy: Main")
    }

    fun getLoggedInUser(): User? {
        return this.loggedInUser
    }

    fun showProgressDialog() {
        val builder = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.progress_dialog, null)
        val loader = dialogView.findViewById<ImageView>(R.id.loadingProgressbar)
        builder.setView(dialogView)
        builder.setCancelable(false)
        dialog = builder.create()
        dialog!!.show()
        dialog!!.window.setLayout(400,400)
        dialog!!.window.setBackgroundDrawableResource(R.drawable.loader_styles)
        animation = loader.drawable as AnimationDrawable
        animation.start()
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    fun hideProgressDialog(){
        animation.stop()
        dialog!!.dismiss()
        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
    }

    fun getFragmentCount (): Int {return this.fragmentCallCount}
    fun setFragmentCount(count: Int){this.fragmentCallCount = count}
}

