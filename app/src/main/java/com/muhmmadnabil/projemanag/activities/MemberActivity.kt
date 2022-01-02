package com.muhmmadnabil.projemanag.activities

import android.app.Activity
import android.app.Dialog
import android.os.AsyncTask
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.muhmmadnabil.projemanag.R
import com.muhmmadnabil.projemanag.adapters.MemberListItemsAdapter
import com.muhmmadnabil.projemanag.firebase.Firestore
import com.muhmmadnabil.projemanag.models.Board
import com.muhmmadnabil.projemanag.models.User
import com.muhmmadnabil.projemanag.utils.Constants
import kotlinx.android.synthetic.main.activity_member.*
import kotlinx.android.synthetic.main.dialog_search_member.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

class MemberActivity : BaseActivity() {

    private lateinit var boardDetails: Board
    private lateinit var assignedMembersList: ArrayList<User>
    private var anyChangesMade: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member)

        setupActionBar()

        if (intent.hasExtra(Constants.BOARD_DETAIL)) {
            boardDetails = intent.getParcelableExtra<Board>(Constants.BOARD_DETAIL)!!
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        Firestore().getAssignedMembersListDetails(this, boardDetails.assignedTo)

    }

    fun setupMembersList(list: ArrayList<User>) {
        hideProgressDialog()
        assignedMembersList = list
        hideProgressDialog()

        rv_members_list.layoutManager = LinearLayoutManager(this)
        rv_members_list.setHasFixedSize(true)

        val adapter = MemberListItemsAdapter(this, list)
        rv_members_list.adapter = adapter

    }

    fun memberDetails(user: User) {
        boardDetails.assignedTo.add(user.id)
        Firestore().assignMemberToBoard(this, boardDetails, user)
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_members_activity)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar!!.title = resources.getString(R.string.members)

        toolbar_members_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_member, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_member -> {
                dialogSearchMember()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogSearchMember() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_search_member)
        dialog.tv_add.setOnClickListener {
            val email = dialog.et_email_search_member.text.toString()
            if (email.isNotEmpty()) {
                dialog.dismiss()
                showProgressDialog(resources.getString(R.string.please_wait))
                Firestore().getMemberDetails(this, email)
            } else {
                Toast.makeText(this, "Please enter email address.", Toast.LENGTH_SHORT).show()
            }
        }
        dialog.tv_cancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onBackPressed() {
        if (anyChangesMade) {
            setResult(Activity.RESULT_OK)
        }
        super.onBackPressed()
    }

    fun memberAssignSuccess(user: User) {
        hideProgressDialog()
        assignedMembersList.add(user)

        anyChangesMade = true

        setupMembersList(assignedMembersList)

        SendNotificationToUserAsyncTask(boardDetails.name, user.fcmToken).execute()
    }

    private inner class SendNotificationToUserAsyncTask(val boardName: String, val token: String) :
        AsyncTask<Any, Void, String>() {

        override fun doInBackground(vararg p0: Any?): String {
            var result: String
            var connection: HttpURLConnection? = null
            try {
                val url = URL(Constants.FCM_BASE_URL)
                connection = url.openConnection() as HttpURLConnection
                connection.doOutput = true
                connection.doInput = true
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"


                connection.setRequestProperty("Content-Type", "application/json")
                connection.setRequestProperty("Charset", "utf-8")
                connection.setRequestProperty("Accept", "application/json")

                connection.setRequestProperty(
                    Constants.FCM_AUTHORIZATION,
                    "${Constants.FCM_KEY}=${Constants.FCM_SERVER_KEY}"
                )

                connection.useCaches = false

                val wr = DataOutputStream(connection.outputStream)
                val jsonRequest = JSONObject()
                val dataObject = JSONObject()
                dataObject.put(Constants.FCM_KEY_TITLE, "Assigned to the board $boardName")
                dataObject.put(
                    Constants.FCM_KEY_MESSAGE,
                    "You have been assigned th the new Board by ${assignedMembersList[0].name}"
                )

                jsonRequest.put(Constants.FCM_KEY_DATA, dataObject)
                jsonRequest.put(Constants.FCM_KEY_TO, token)

                wr.writeBytes(jsonRequest.toString())
                wr.flush()
                wr.close()

                val httpResult: Int = connection.responseCode
                if (httpResult == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream

                    val reader = BufferedReader(InputStreamReader(inputStream))

                    val sb = StringBuilder()
                    var line: String?
                    try {
                        while (reader.readLine().also { line = it } != null) {
                            sb.append(line + "\n")
                        }
                    } catch (ex: IOException) {
                        ex.printStackTrace()
                    } finally {
                        try {
                            inputStream.close()
                        } catch (ex: IOException) {
                            ex.printStackTrace()
                        }
                    }
                    result = sb.toString()
                } else {
                    result = connection.responseMessage
                }
            } catch (ex: SocketTimeoutException) {
                result = "Connection Timeout"
            } catch (ex: Exception) {
                result = "Error : " + ex.message
            } finally {
                connection?.disconnect()
            }

            return result
        }

    }

}