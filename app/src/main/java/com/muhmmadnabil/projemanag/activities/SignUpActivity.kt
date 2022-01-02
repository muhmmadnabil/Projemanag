package com.muhmmadnabil.projemanag.activities

import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import com.google.firebase.auth.FirebaseAuth
import com.muhmmadnabil.projemanag.R
import com.muhmmadnabil.projemanag.firebase.Firestore
import com.muhmmadnabil.projemanag.models.User
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUpActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        setupActionBar()

        btn_sign_up.setOnClickListener {
            registerUser()
        }


    }

    fun userRegisteredSuccess() {
        Toast.makeText(
            this,
            "you have successfully registered.",
            Toast.LENGTH_LONG
        ).show()
        hideProgressDialog()
        FirebaseAuth.getInstance().signOut()
        finish()
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_sign_up_activity)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_back)

        toolbar_sign_up_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun registerUser() {
        val name = et_sign_up_name.text.toString().trim { it <= ' ' }
        val email = et_sign_up_email.text.toString().trim { it <= ' ' }
        val password = et_sign_up_password.text.toString().trim { it <= ' ' }

        if (validateForm(name, email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val firebaseUser = task.result!!.user!!
                        val registeredEmail = firebaseUser.email!!
                        val user = User(firebaseUser.uid, name, registeredEmail)
                        Firestore().registerUser(this,user)

                    } else {
                        Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }

    }

    private fun validateForm(name: String, email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(name) && TextUtils.isEmpty(email) && TextUtils.isEmpty(password) -> {
                et_sign_up_name.error = "Please enter a Name"
                et_sign_up_email.error = "Please enter an email"
                et_sign_up_password.error = "Please enter a valid password"
                false
            }
            TextUtils.isEmpty(name) && TextUtils.isEmpty(email) -> {
                et_sign_up_name.error = "Please enter a Name"
                et_sign_up_email.error = "Please enter an email"
                false
            }
            TextUtils.isEmpty(name) && TextUtils.isEmpty(password) -> {
                et_sign_up_name.error = "Please enter a Name"
                et_sign_up_password.error = "Please enter a valid password"
                false
            }
            TextUtils.isEmpty(email) && TextUtils.isEmpty(password) -> {
                et_sign_up_email.error = "Please enter an email"
                et_sign_up_password.error = "Please enter a valid password"
                false
            }
            TextUtils.isEmpty(name) -> {
                et_sign_up_name.error = "Please enter a Name"
                false
            }
            TextUtils.isEmpty(email) -> {
                et_sign_up_email.error = "Please enter an email"
                false
            }
            TextUtils.isEmpty(password) -> {
                et_sign_up_password.error = "Please enter a valid password"
                false
            }
            else -> {
                true
            }
        }
    }

}