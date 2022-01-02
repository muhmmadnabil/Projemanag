package com.muhmmadnabil.projemanag.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.muhmmadnabil.projemanag.R
import com.muhmmadnabil.projemanag.adapters.TaskListItemsAdapter
import com.muhmmadnabil.projemanag.firebase.Firestore
import com.muhmmadnabil.projemanag.models.Board
import com.muhmmadnabil.projemanag.models.Card
import com.muhmmadnabil.projemanag.models.Task
import com.muhmmadnabil.projemanag.models.User
import com.muhmmadnabil.projemanag.utils.Constants
import kotlinx.android.synthetic.main.activity_task_list.*

class TaskListActivity : BaseActivity() {

    private lateinit var boardDetails: Board
    private lateinit var boardDocumentId: String
    lateinit var assignedMembersDetailList: ArrayList<User>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_list)

        if (intent.hasExtra(Constants.DOCUMENT_ID)) {
            boardDocumentId = intent.getStringExtra(Constants.DOCUMENT_ID)!!
        }

        showProgressDialog(resources.getString(R.string.please_wait))
        Firestore().getBoardDetails(this, boardDocumentId)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == MEMBERS_REQUEST_CODE || requestCode == CARD_DETAILS_REQUEST_CODE) {
            showProgressDialog(resources.getString(R.string.please_wait))
            Firestore().getBoardDetails(this, boardDocumentId)
        }
    }

    fun cardDetails(taskListPosition: Int, cardPosition: Int) {
        val intent = Intent(this, CardDetailsActivity::class.java)
        intent.putExtra(Constants.BOARD_DETAIL, boardDetails)
        intent.putExtra(Constants.TASK_LIST_ITEM_POSITION, taskListPosition)
        intent.putExtra(Constants.CARD_LIST_ITEM_POSITION, cardPosition)
        intent.putExtra(Constants.BOARD_MEMBERS_LIST, assignedMembersDetailList)
        startActivityForResult(intent, CARD_DETAILS_REQUEST_CODE)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_members, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_members -> {
                val intent = Intent(this, MemberActivity::class.java)
                intent.putExtra(Constants.BOARD_DETAIL, boardDetails)
                startActivityForResult(intent, MEMBERS_REQUEST_CODE)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupActionBar() {
        setSupportActionBar(toolbar_task_list_activity)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_back)
        supportActionBar!!.title = boardDetails.name

        toolbar_task_list_activity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    fun boardDetails(board: Board) {

        boardDetails = board

        hideProgressDialog()
        setupActionBar()



        showProgressDialog(resources.getString(R.string.please_wait))
        Firestore().getAssignedMembersListDetails(this, boardDetails.assignedTo)
    }

    fun addUpdateTaskListSuccess() {
        hideProgressDialog()

        showProgressDialog(resources.getString(R.string.please_wait))
        Firestore().getBoardDetails(this, boardDetails.documentId)
    }

    fun createTaskList(taskListName: String) {
        val task = Task(taskListName, Firestore().getCurrentUserId())
        boardDetails.taskList.add(0, task)
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        Firestore().addUpdateTaskList(this, boardDetails)
    }

    fun updateTaskList(position: Int, listName: String, model: Task) {
        val task = Task(listName, model.createdBy)

        boardDetails.taskList[position] = task
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        Firestore().addUpdateTaskList(this, boardDetails)
    }

    fun deleteTaskList(position: Int) {
        boardDetails.taskList.removeAt(position)

        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        showProgressDialog(resources.getString(R.string.please_wait))
        Firestore().addUpdateTaskList(this, boardDetails)
    }

    fun addCardToTaskList(position: Int, cardName: String) {
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        val cardAssignedUsersList: ArrayList<String> = ArrayList()
        cardAssignedUsersList.add(Firestore().getCurrentUserId())

        val card = Card(cardName, Firestore().getCurrentUserId(), cardAssignedUsersList)

        val cardsList = boardDetails.taskList[position].cards
        cardsList.add(card)

        val task = Task(
            boardDetails.taskList[position].title,
            boardDetails.taskList[position].createdBy,
            cardsList
        )

        boardDetails.taskList[position] = task

        showProgressDialog(resources.getString(R.string.please_wait))

        Firestore().addUpdateTaskList(this, boardDetails)
    }

    fun boardMemberDetailsList(list: ArrayList<User>) {
        assignedMembersDetailList = list
        hideProgressDialog()

        val addTaskList = Task(resources.getString(R.string.add_list))
        boardDetails.taskList.add(addTaskList)

        rv_task_list.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        rv_task_list.setHasFixedSize(true)

        val adapter = TaskListItemsAdapter(this, boardDetails.taskList)
        rv_task_list.adapter = adapter

    }

    fun updateCardsInTaskList(taskListPosition: Int, cards: ArrayList<Card>) {
        boardDetails.taskList.removeAt(boardDetails.taskList.size - 1)

        boardDetails.taskList[taskListPosition].cards = cards

        showProgressDialog(resources.getString(R.string.please_wait))
        Firestore().addUpdateTaskList(this,boardDetails)
    }

    companion object {
        const val MEMBERS_REQUEST_CODE: Int = 13
        const val CARD_DETAILS_REQUEST_CODE: Int = 14
    }

}