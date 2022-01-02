package com.muhmmadnabil.projemanag.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.installations.FirebaseInstallations
import com.muhmmadnabil.projemanag.R
import com.muhmmadnabil.projemanag.adapters.BoardItemsAdapter
import com.muhmmadnabil.projemanag.firebase.Firestore
import com.muhmmadnabil.projemanag.models.Board
import com.muhmmadnabil.projemanag.models.User
import com.muhmmadnabil.projemanag.utils.Constants
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.main_content.*
import kotlinx.android.synthetic.main.nav_header_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    companion object {
        const val MY_PROFILE_REQUEST_CODE = 11
        const val CREATE_BOARD_REQUEST_CODE = 12
    }

    private lateinit var userName: String
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBar()

        nav_view.setNavigationItemSelectedListener(this)

        sharedPreferences =
            this.getSharedPreferences(Constants.PROJEMANAG_PREFERENCES, MODE_PRIVATE)

        val tokenUpdated = sharedPreferences.getBoolean(Constants.FCM_TOKEN_UPDATED, false)

        if (tokenUpdated) {
            Firestore().loadUserData(this, true)
        } else {
            FirebaseInstallations.getInstance().id.addOnSuccessListener { token ->
                updateFCMToken(token)
            }
        }


        Firestore().loadUserData(this, true)

        fab_create_board.setOnClickListener {
            val intent = Intent(this, CreateBoardActivity::class.java)
            intent.putExtra(Constants.NAME, userName)
            startActivityForResult(intent, CREATE_BOARD_REQUEST_CODE)
        }


    }

    fun populateBoardsListToUi(boardsList: ArrayList<Board>) {
        if (boardsList.size > 0) {
            rv_boards_list.visibility = View.VISIBLE
            tv_no_boards_available.visibility = View.GONE

            rv_boards_list.layoutManager = LinearLayoutManager(this)
            rv_boards_list.setHasFixedSize(true)

            val adapter = BoardItemsAdapter(this, boardsList)
            rv_boards_list.adapter = adapter

            adapter.setOnClickListener(object : BoardItemsAdapter.OnClickListener {
                override fun onClick(position: Int, model: Board) {
                    val intent = Intent(this@MainActivity, TaskListActivity::class.java)
                    intent.putExtra(Constants.DOCUMENT_ID, model.documentId)
                    startActivity(intent)
                }

            })

        } else {
            rv_boards_list.visibility = View.GONE
            tv_no_boards_available.visibility = View.VISIBLE
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_main)
        toolbar_main.setNavigationIcon(R.drawable.ic_menu)
        toolbar_main.setNavigationOnClickListener {
            toggleDrawer()
        }
    }

    private fun toggleDrawer() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            drawer_layout.openDrawer(GravityCompat.START)
        }
    }

    override fun onBackPressed() {

        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            doubleBackToExit()
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_my_profile -> {
                startActivityForResult(
                    Intent(this, MyProfileActivity::class.java),
                    MY_PROFILE_REQUEST_CODE
                )
            }
            R.id.nav_sign_out -> {
                FirebaseAuth.getInstance().signOut()

                sharedPreferences.edit().clear().apply()

                val intent = Intent(this, IntroActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()

            }
        }
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun updateNavigationUserDetails(user: User, readBoardsList: Boolean) {

        GlobalScope.launch(Dispatchers.Main) {
            userName = user.name

            Glide
                .with(this@MainActivity)
                .load(user.image)
                .centerCrop()
                .placeholder(R.drawable.ic_user_place_holder)
                .into(iv_user)

            tv_user.text = user.name

            if (readBoardsList) {

                Firestore().getBoardsList(this@MainActivity)
            }
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == MY_PROFILE_REQUEST_CODE) {
            Firestore().loadUserData(this)
        } else if (resultCode == RESULT_OK && requestCode == CREATE_BOARD_REQUEST_CODE) {
            Firestore().getBoardsList(this)
        }
    }

    fun tokenUpdateSuccess() {
        val editor: SharedPreferences.Editor = sharedPreferences.edit()
        editor.putBoolean(Constants.FCM_TOKEN_UPDATED, true)
        editor.apply()

        Firestore().loadUserData(this, true)
    }

    private fun updateFCMToken(token: String) {
        val userHashMap = HashMap<String, Any>()
        userHashMap[Constants.FCM_TOKEN] = token

        Firestore().updateUserProfileData(this, userHashMap)
    }

}