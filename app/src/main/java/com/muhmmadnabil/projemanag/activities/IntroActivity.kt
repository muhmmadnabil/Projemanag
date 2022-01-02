package com.muhmmadnabil.projemanag.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.muhmmadnabil.projemanag.R
import kotlinx.android.synthetic.main.activity_intro.*

class IntroActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        btn_intro_sign_up.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        btn_intro_sign_in.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

    }
}