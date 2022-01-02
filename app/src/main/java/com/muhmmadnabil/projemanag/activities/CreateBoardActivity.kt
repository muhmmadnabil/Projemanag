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
import com.muhmmadnabil.projemanag.models.Board
import com.muhmmadnabil.projemanag.utils.Constants
import com.muhmmadnabil.projemanag.utils.Constants.READ_STORAGE_PERMISSION_CODE
import com.muhmmadnabil.projemanag.utils.Constants.showImageChooser
import kotlinx.android.synthetic.main.activity_create_board.*
import kotlinx.android.synthetic.main.activity_my_profile.*

class CreateBoardActivity : BaseActivity() {

    private var selectedImageUri: Uri? = null

    private lateinit var userName: String

    private var boardImageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_board)

        setupActionBar()

        if (intent.hasExtra(Constants.NAME)) {
            userName = intent.getStringExtra(Constants.NAME).toString()
        }

        iv_board_image.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                showImageChooser(this)
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_STORAGE_PERMISSION_CODE
                )
            }
        }

        btn_create.setOnClickListener {
            if (selectedImageUri != null) {
                uploadBoardImage()
            } else {
                showProgressDialog(resources.getString(R.string.please_wait))
                createBoard()
            }
        }
    }

    private fun createBoard() {
        val assignedUserArrayList = ArrayList<String>()
        assignedUserArrayList.add(getCurrentUserID())

        var board =
            Board(et_board_name.text.toString(), boardImageUrl, userName, assignedUserArrayList)

        Firestore().createBoard(this, board)
    }

    private fun uploadBoardImage() {
        showProgressDialog(resources.getString(R.string.please_wait))

        val sRef = FirebaseStorage.getInstance().reference.child(
            "BOARD_IMAGE" + System.currentTimeMillis() + "." + Constants.getFileExtension(
                this,
                selectedImageUri!!
            )
        )

        sRef.putFile(selectedImageUri!!).addOnSuccessListener { taskSnapshot ->
            taskSnapshot.metadata!!.reference!!.downloadUrl.addOnSuccessListener { uri ->
                boardImageUrl = uri.toString()
                createBoard()

                hideProgressDialog()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this@CreateBoardActivity, exception.message, Toast.LENGTH_LONG).show()
            hideProgressDialog()

        }
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_create_board_activity)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar!!.title = resources.getString(R.string.create_board_title)

        toolbar_create_board_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun boardCreatedSuccessfully() {
        hideProgressDialog()

        setResult(Activity.RESULT_OK)

        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Constants.showImageChooser(this)
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
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.PICK_IMAGE_REQUEST_CODE && data!!.data != null) {
            selectedImageUri = data.data

            try {
                Glide
                    .with(this@CreateBoardActivity)
                    .load(selectedImageUri)
                    .centerCrop()
                    .placeholder(R.drawable.ic_board_place_holder)
                    .into(iv_board_image)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

        }
    }

}