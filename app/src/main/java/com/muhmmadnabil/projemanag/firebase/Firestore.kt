package com.muhmmadnabil.projemanag.firebase

import android.app.Activity
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.muhmmadnabil.projemanag.activities.*
import com.muhmmadnabil.projemanag.models.Board
import com.muhmmadnabil.projemanag.models.User
import com.muhmmadnabil.projemanag.utils.Constants
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class Firestore {

    private val fireStore = FirebaseFirestore.getInstance()

    fun registerUser(activity: SignUpActivity, userInfo: User) {
        fireStore.collection(Constants.USERS).document(getCurrentUserId())
            .set(userInfo, SetOptions.merge()).addOnSuccessListener {
                activity.userRegisteredSuccess()
            }
    }

    fun createBoard(activity: CreateBoardActivity, board: Board) {
        fireStore.collection(Constants.BOARDS)
            .document()
            .set(board, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(activity, "Board created successfully.", Toast.LENGTH_SHORT).show()
                activity.boardCreatedSuccessfully()
            }.addOnFailureListener { exception ->
                activity.hideProgressDialog()
            }
    }

    fun getBoardsList(activity: MainActivity) {

        GlobalScope.launch {
            fireStore.collection(Constants.BOARDS)
                .whereArrayContains(Constants.ASSIGNED_TO, getCurrentUserId())
                .get()
                .addOnSuccessListener { document ->
                    val boardList = ArrayList<Board>()
                    for (i in document.documents) {
                        val board = i.toObject(Board::class.java)!!
                        board.documentId = i.id
                        boardList.add(board)
                    }
                    activity.populateBoardsListToUi(boardList)
                }.addOnFailureListener {
                    activity.hideProgressDialog()
                }
        }


    }

    fun addUpdateTaskList(activity: Activity, board: Board) {
        val taskListHashMap = HashMap<String, Any>()
        taskListHashMap[Constants.TASK_LIST] = board.taskList

        fireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(taskListHashMap)
            .addOnSuccessListener {
                if (activity is TaskListActivity)
                    activity.addUpdateTaskListSuccess()
                else if (activity is CardDetailsActivity)
                    activity.addUpdateTaskListSuccess()
            }.addOnFailureListener { exception ->
                if (activity is TaskListActivity)
                    activity.hideProgressDialog()
                else if (activity is CardDetailsActivity)
                    activity.hideProgressDialog()
            }

    }

    fun updateUserProfileData(activity: Activity, userHashMap: HashMap<String, Any>) {
        fireStore.collection(Constants.USERS).document(getCurrentUserId()).update(userHashMap)
            .addOnSuccessListener {
                Toast.makeText(activity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()

                when (activity) {
                    is MainActivity -> {
                        activity.tokenUpdateSuccess()
                    }
                    is MyProfileActivity -> {
                        activity.profileUpdateSuccess()
                    }
                }

            }.addOnFailureListener { e ->
                when (activity) {
                    is MainActivity -> {
                        activity.hideProgressDialog()
                    }
                    is MyProfileActivity -> {
                        activity.hideProgressDialog()
                    }
                }
                Toast.makeText(activity, "Error when updating the profile!", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    fun loadUserData(activity: Activity, readBoardsList: Boolean = false) {
        fireStore.collection(Constants.USERS).document(getCurrentUserId())
            .get().addOnSuccessListener { document ->
                val loggedInUser = document.toObject(User::class.java)!!

                when (activity) {
                    is SignInActivity -> {
                        activity.signInSuccess(loggedInUser)
                    }
                    is MainActivity -> {
                        activity.updateNavigationUserDetails(loggedInUser, readBoardsList)
                    }
                    is MyProfileActivity -> {
                        activity.setUserDataInUi(loggedInUser)
                    }
                }
            }.addOnFailureListener {
                when (activity) {
                    is SignInActivity -> {
                        activity.hideProgressDialog()
                    }
                }
            }
    }

    fun getCurrentUserId(): String {
        val currentUser = FirebaseAuth.getInstance().currentUser
        var currentUserId = ""
        if (currentUser != null) {
            currentUserId = currentUser.uid
        }
        return currentUserId
    }

    fun getBoardDetails(activity: TaskListActivity, documentId: String) {
        fireStore.collection(Constants.BOARDS)
            .document(documentId)
            .get()
            .addOnSuccessListener { document ->
                val board = document.toObject(Board::class.java)!!
                board.documentId = document.id
                activity.boardDetails(board)
            }.addOnFailureListener {
                activity.hideProgressDialog()
            }
    }

    fun getAssignedMembersListDetails(activity: Activity, assignedTo: ArrayList<String>) {
        fireStore.collection(Constants.USERS)
            .whereIn(Constants.ID, assignedTo)
            .get()
            .addOnSuccessListener { document ->

                val usersList: ArrayList<User> = ArrayList()

                for (i in document.documents) {
                    val user = i.toObject(User::class.java)!!
                    usersList.add(user)
                }

                if (activity is MemberActivity)
                    activity.setupMembersList(usersList)
                else if (activity is TaskListActivity)
                    activity.boardMemberDetailsList(usersList)
            }.addOnFailureListener { e ->
                if (activity is MemberActivity)
                    activity.hideProgressDialog()
                else if (activity is TaskListActivity)
                    activity.hideProgressDialog()
            }
    }

    fun getMemberDetails(activity: MemberActivity, email: String) {
        fireStore.collection(Constants.USERS)
            .whereEqualTo(Constants.EMAIL, email)
            .get()
            .addOnSuccessListener { document ->
                if (document.documents.size > 0) {
                    val user = document.documents[0].toObject(User::class.java)!!
                    activity.memberDetails(user)
                } else {
                    activity.hideProgressDialog()
                    activity.showErrorSnackBar("No such member found")
                }
            }.addOnFailureListener { e ->
                activity.hideProgressDialog()
            }
    }

    fun assignMemberToBoard(activity: MemberActivity, board: Board, user: User) {

        val assignedToHashMap = HashMap<String, Any>()
        assignedToHashMap[Constants.ASSIGNED_TO] = board.assignedTo

        fireStore.collection(Constants.BOARDS)
            .document(board.documentId)
            .update(assignedToHashMap)
            .addOnSuccessListener {
                activity.memberAssignSuccess(user)
            }.addOnFailureListener {
                activity.hideProgressDialog()
            }
    }

}