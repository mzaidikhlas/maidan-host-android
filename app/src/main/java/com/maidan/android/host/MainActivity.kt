package com.maidan.android.host

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.BottomNavigationView.OnNavigationItemSelectedListener
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.maidan.android.host.controller.DashboardFragment
import com.maidan.android.host.controller.HomeFragment
import com.maidan.android.host.controller.StatsFragment
import kotlinx.android.synthetic.main.activity_main.*

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
                supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, StatsFragment()).commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        if (currentUser == null){
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }else{
            supportFragmentManager.beginTransaction().replace(R.id.fragment_layout, HomeFragment()).commit()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }
}

