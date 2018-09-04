package com.maidan.android.host

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import com.maidan.android.host.models.User
import com.maidan.android.host.retrofit.ApiInterface
import com.maidan.android.host.retrofit.ApiResponse
import com.maidan.android.host.retrofit.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import com.google.firebase.storage.StorageReference
import com.google.firebase.auth.FirebaseUser
import com.maidan.android.host.models.Location
import com.maidan.android.host.models.Rate
import com.maidan.android.host.models.Venue
import java.util.*


class SignupDetailsActivity : AppCompatActivity() {

    //Firebase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mStorageRef: StorageReference
    private lateinit var currentUser: FirebaseUser

    private lateinit var user: User
    private val TAG = "SignupDetails"

    // Upload Image
    private var PICK_IMAGE = 100
    private var imageUri : Uri? = null

    //layout
    private lateinit var userImage: ImageButton
    private lateinit var nameTxt: TextView
    private lateinit var emailTxt: TextView
    private lateinit var phoneNumberTxt: EditText
    private lateinit var cnicTxt: EditText
    private lateinit var dobTxt: TextView
    private lateinit var genderSpinner: Spinner
    private lateinit var submitBtn: Button
    private var dateString: String? = null
    private lateinit var progressBar: ProgressBar
    private lateinit var dobPicker: DatePickerDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_details)

        mAuth = FirebaseAuth.getInstance()
        mStorageRef = FirebaseStorage.getInstance().reference

        currentUser = mAuth.currentUser!!

        userImage = findViewById(R.id.uploadPicture)
        nameTxt = findViewById(R.id.name)
        emailTxt = findViewById(R.id.email)
        phoneNumberTxt = findViewById(R.id.phonenumber)
        cnicTxt = findViewById(R.id.CNIC)
        dobTxt = findViewById(R.id.DOB)
        genderSpinner = findViewById(R.id.gender)
        submitBtn = findViewById(R.id.signup_submit_btn)
        progressBar = findViewById(R.id.signupProgressBar)


        // Date of Birth DatePicker
        dobTxt.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            dateString = "$day/$month/$year"

            dobPicker = DatePickerDialog(this,android.R.style.Theme_Holo_Dialog,
                    DatePickerDialog.OnDateSetListener { view, yr, monthOfYear, dayOfMonth ->
                        Log.d(TAG, "Year: $yr, Month $monthOfYear, Day: $dayOfMonth")

                        dateString = "$dayOfMonth/$monthOfYear/$yr"
                        dateString = "$dayOfMonth/$monthOfYear/$yr"
                        dobTxt.text = dateString
                    },year,month,day)
            dobPicker.show()
            Log.d(TAG,dateString)
        }
        //populating layout
        if (currentUser.phoneNumber != null) {
            phoneNumberTxt.setText(currentUser.phoneNumber!!, TextView.BufferType.EDITABLE)
            phoneNumberTxt.isEnabled = false
        }

        // Upload Picture
        userImage.setOnClickListener {
            openGallery()
        }

        submitBtn.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            submitBtn.isEnabled = false

            if (phoneNumberTxt.text.isNotEmpty() && cnicTxt.text.isNotEmpty()
                    && dobTxt.text.isNotEmpty() && nameTxt.text.isNotEmpty() && emailTxt.text.isNotEmpty()) {

                var validationFlag = true

                if (cnicTxt.length() < 13) {
                    cnicTxt.error = "Valid cnic is required"
                    cnicTxt.requestFocus()
                    validationFlag = false
                }
                if (!emailTxt.text.contains("@")) {
                    emailTxt.error = "Enter valid email address"
                    emailTxt.requestFocus()
                    validationFlag = false
                }
                if (validationFlag){
                    if (imageUri != null) uploadImage() else createUser(null)
                }
                else{
                    progressBar.visibility = View.INVISIBLE
                    submitBtn.isEnabled = true
                }
            }else{
                progressBar.visibility = View.INVISIBLE
                submitBtn.isEnabled = true
                cnicTxt.error = "required"
                cnicTxt.requestFocus()

                emailTxt.error = "required"
                emailTxt.requestFocus()

                nameTxt.error = "required"
                nameTxt.requestFocus()

                emailTxt.error = "required"
                emailTxt.requestFocus()

                dobTxt.error = "required"
                dobTxt.requestFocus()
                Toast.makeText(applicationContext, "All Fields are required", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openGallery() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery,PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE)
        {
            imageUri = data!!.data
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
                userImage.setImageBitmap(bitmap)
                Toast.makeText(applicationContext, "image uri $imageUri", Toast.LENGTH_LONG).show()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    private fun uploadImage(){
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val riversRef = mStorageRef.child("avatar/${currentUser.uid}")

        riversRef.putFile(imageUri!!)
                .addOnSuccessListener { taskSnapshot ->
                    progressDialog.dismiss()
                    // Get a URL to the uploaded content
                    val downloadUrl = taskSnapshot.uploadSessionUri
                    createUser(downloadUrl.toString())
                }
                .addOnFailureListener {
                    // Handle unsuccessful uploads
                    progressDialog.dismiss()
                    Toast.makeText(applicationContext, "Failed ", Toast.LENGTH_SHORT).show()
                }
                .addOnProgressListener { taskSnapshot ->
                    val progress = (100.0*taskSnapshot.bytesTransferred /taskSnapshot
                            .totalByteCount)
                    progressDialog.setMessage("Uploaded ${progress.toInt()}%")
                }
    }

    private fun createUser (avatar: String?){
        val venue = Venue("Venue 1",
                Location(28.70837, 77.195427, "Pakistan", "Lahore", "Model Town")
            ,null, true, null, null, "Cricket",
                Rate(1500, 100, 100, 2,8),
                3, "32123","123123")
        val venue2 = Venue("Venue 2",
                Location(28.70837, 77.195427, "Pakistan", "Lahore", "Model Town")
                ,null, true, null, null, "Cricket",
                Rate(1500, 100, 100, 2,8),
                3, "32123","123123")

        val venue3 = Venue("Venue 3",
                Location(28.70837, 77.195427, "Pakistan", "Lahore", "Model Town")
                ,null, true, null, null, "Cricket",
                Rate(1500, 100, 100, 2,8),
                3, "32123","123123")
        val venues = ArrayList<Venue>()
        venues.add(venue)
        venues.add(venue2)
        venues.add(venue3)
        user = User(emailTxt.text.toString(), nameTxt.text.toString(), null, phoneNumberTxt.text.toString(), cnicTxt.text.toString(), avatar,
                dobTxt.text.toString(), genderSpinner.selectedItem.toString(), false, true, null, venues)

        Log.d(TAG, "User: $user")

        currentUser.getIdToken(true).addOnCompleteListener { task ->
            val idToken = task.result.token

            val apiService: ApiInterface = RetrofitClient.instance.create(ApiInterface::class.java)

            val call: Call<ApiResponse> = apiService.createUser(idToken!!, user)
            call.enqueue(object: Callback<ApiResponse> {
                override fun onFailure(call: Call<ApiResponse>?, t: Throwable?) {
                    Log.d(TAG, t.toString())
                    progressBar.visibility = View.INVISIBLE
                    submitBtn.isEnabled = true
                    throw t!!
                }

                override fun onResponse(call: Call<ApiResponse>?, response: Response<ApiResponse>?) {
                    if (response!!.isSuccessful){
                        Log.d(TAG, "OnResponse")
                        progressBar.visibility = View.INVISIBLE
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }
                }
            })
        }
    }
}
