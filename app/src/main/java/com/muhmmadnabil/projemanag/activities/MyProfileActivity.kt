package com.muhmmadnabil.projemanag.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.muhmmadnabil.projemanag.R
import com.muhmmadnabil.projemanag.firebase.Firestore
import com.muhmmadnabil.projemanag.models.User
import com.muhmmadnabil.projemanag.utils.Constants
import com.muhmmadnabil.projemanag.utils.Constants.PICK_IMAGE_REQUEST_CODE
import com.muhmmadnabil.projemanag.utils.Constants.READ_STORAGE_PERMISSION_CODE
import com.muhmmadnabil.projemanag.utils.Constants.getFileExtension
import com.muhmmadnabil.projemanag.utils.Constants.showImageChooser
import kotlinx.android.synthetic.main.activity_my_profile.*

class MyProfileActivity : BaseActivity() {

    private var selectedImageUri: Uri? = null
    private var profileImageUri: String = ""
    private lateinit var userDetails: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_profile)

        setupActionBar()

        Firestore().loadUserData(this)

        iv_user_image.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.READ_EXTERNAL_STORAGE
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                showImageChooser(this)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        btn_update.setOnClickListener {
            if (selectedImageUri != null) {
                uploadUserImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                updateUserProfileData()
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showImageChooser(this)
            } else {
                Toast.makeText(
                    this,
                    "Oops,you just denied the permission for storage. You can also allow it from settings.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == PICK_IMAGE_REQUEST_CODE && data!!.data != null) {
            selectedImageUri = data.data

            try {
                Glide
                    .with(this@MyProfileActivity)
                    .load(selectedImageUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_user_place_holder)
                    .into(iv_user_image)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

        }
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_my_profile_activity)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar!!.title = resources.getString(R.string.my_profile)
        toolbar_my_profile_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun setUserDataInUi(user: User) {

        userDetails = user

        Glide
            .with(this@MyProfileActivity)
            .load(user.image)
            .centerCrop()
            .placeholder(R.drawable.ic_user_place_holder)
            .into(iv_user_image)

        et_name.setText(user.name)
        et_email.setText(user.email)
        if (user.mobile != 0L) {
            et_mobile.setText(user.mobile.toString())
        }
    }

    private fun uploadUserImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        if (selectedImageUri != null) {
            val sRef = FirebaseStorage.getInstance().reference.child(
                "USER_IMAGE" + System.currentTimeMillis() + "." + getFileExtension(this,selectedImageUri!!)
            )

            sRef.putFile(selectedImageUri!!).addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                    profileImageUri = uri.toString()
                    updateUserProfileData()

                    hideProgressDialog()
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(this@MyProfileActivity, exception.message, Toast.LENGTH_LONG).show()
                hideProgressDialog()

            }

        }
    }

    fun profileUpdateSuccess() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)

        finish()
    }

    private fun updateUserProfileData() {
        val userHashMap = HashMap<String, Any>()
        var anyChanges = false

        if (profileImageUri.isNotEmpty() && profileImageUri != userDetails.image) {
            userHashMap[Constants.IMAGE] = profileImageUri
            anyChanges = true
        }

        if (et_name.text.toString() != userDetails.name) {
            userHashMap[Constants.NAME] = et_name.text.toString()
            anyChanges = true
        }

        if (et_email.text.toString() != userDetails.email) {
            userHashMap[Constants.EMAIL] = et_email.text.toString()
            anyChanges = true
        }

        if (et_mobile.text.toString() != userDetails.mobile.toString()) {
            userHashMap[Constants.MOBILE] = et_mobile.text.toString().toLong()
            anyChanges = true
        }

        if (anyChanges)
            Firestore().updateUserProfileData(this, userHashMap)
    }

}