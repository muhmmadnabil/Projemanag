package com.muhmmadnabil.projemanag.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.muhmmadnabil.projemanag.R
import com.muhmmadnabil.projemanag.models.User
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignInActivity : BaseActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()

        setupActionBar()

        btn_sign_in.setOnClickListener {
            signInRegisteredUser()
        }

    }

    fun signInSuccess(user: User) {
        hideProgressDialog()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_sign_in_activity)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_back)

        toolbar_sign_in_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    private fun signInRegisteredUser() {
        val email = et_sign_in_email.text.toString().trim { it <= ' ' }
        val password = et_sign_in_password.text.toString().trim { it <= ' ' }

        if (validateForm(email, password)) {
            showProgressDialog(resources.getString(R.string.please_wait))

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        hideProgressDialog()
                        val user = auth.currentUser
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        hideProgressDialog()
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun validateForm(email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(email) && TextUtils.isEmpty(password) -> {
                et_sign_in_email.error = "Please enter an email"
                et_sign_in_password.error = "Please enter a valid password"
                false
            }
            TextUtils.isEmpty(email) -> {
                et_sign_in_email.error = "Please enter an email"
                false
            }
            TextUtils.isEmpty(password) -> {
                et_sign_in_password.error = "Please enter a valid password"
                false
            }
            else -> {
                true
            }
        }
    }

}