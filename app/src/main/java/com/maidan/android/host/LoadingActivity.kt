package com.maidan.android.host

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import com.maidan.android.host.models.User
import com.maidan.android.host.retrofit.ApiInterface
import com.maidan.android.host.retrofit.ApiResponse
import com.maidan.android.host.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoadingActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private val TAG = "LoadingActivity"
    private lateinit var loggedInUser: User

    private lateinit var mAuth: FirebaseAuth


    override fun onStart() {
        super.onStart()
        val user = mAuth.currentUser
        if (user != null){
            user.getIdToken(true).addOnCompleteListener {task ->
                if (task.isSuccessful){
                    val idToken = task.result.token
                    val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)
                    Log.d(TAG, "Number ${user.phoneNumber!!}")
                    val call: Call<ApiResponse> = apiService.getUserInfoByPhone(user.phoneNumber!!,idToken!!)
                    call.enqueue(object: Callback<ApiResponse>{
                        override fun onFailure(call: Call<ApiResponse>?, t: Throwable?) {
                            Toast.makeText(applicationContext, "OnFailure: ${t!!.message}", Toast.LENGTH_LONG).show()
                            Log.d(TAG, t.toString())
                            throw t
                        }

                        override fun onResponse(call: Call<ApiResponse>?, response: Response<ApiResponse>?) {
                            if (response!!.isSuccessful){
                                if (response.body()!!.getStatusCode() == 200){
                                    if (response.body()!!.getType() == "User"){
                                        val payload = response.body()!!.getPayload()
                                        if (payload.isNotEmpty()){
                                            val gson = Gson()
                                            val jsonObject = gson.toJsonTree(payload[0].getData()).asJsonObject
                                            loggedInUser = gson.fromJson(jsonObject, User::class.java)
                                            Log.d(TAG, loggedInUser.toString())
                                            val mainActivity = Intent(applicationContext, MainActivity::class.java)
                                            mainActivity.putExtra("firebaseUser", user)
                                            Log.d(TAG, "User: $user")
                                            mainActivity.putExtra("loggedInUser", loggedInUser)
                                            mainActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            progressBar.visibility = View.INVISIBLE
                                            startActivity(mainActivity)
                                        }else{
                                            progressBar.visibility = View.INVISIBLE
                                            val intent = Intent(applicationContext, SignupDetailsActivity::class.java)
                                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            startActivity(intent)
                                        }
                                    }
                                }else{
                                    mAuth.signOut()
                                    Toast.makeText(applicationContext, "OnCode: ${response.body()!!.getStatusCode()}", Toast.LENGTH_LONG).show()
                                    val intent = Intent(applicationContext, LoginActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                }
                            }else{
                                mAuth.signOut()
                                Toast.makeText(applicationContext, "OnResponseFailure ", Toast.LENGTH_SHORT).show()
                                val intent = Intent(applicationContext, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                        }

                    })
                }
                else{
                    Toast.makeText(applicationContext, "Error ${task.exception}", Toast.LENGTH_LONG).show()
                    Log.d(TAG,  "Yeh hai ${task.exception}")
                    throw task.exception!!
                }
            }
        }else{
            val intent = Intent(applicationContext, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        progressBar = findViewById(R.id.loadingProgressbar)
        progressBar.visibility = View.VISIBLE
        mAuth = FirebaseAuth.getInstance()
    }
}
