package com.muhmmadnabil.projemanag.activities

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import com.muhmmadnabil.projemanag.R
import com.muhmmadnabil.projemanag.firebase.Firestore
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val typeFace = Typeface.createFromAsset(assets, "carbon.bl-regular.ttf")
        tv_splash.typeface = typeFace

        Handler().postDelayed({

            val currentUserId = Firestore().getCurrentUserId()
            if (currentUserId.isNotEmpty()) {

                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, IntroActivity::class.java))
            }
            finish()
        }, 2500)
    }
}