package com.muhmmadnabil.projemanag.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.webkit.MimeTypeMap

object Constants {
    const val USERS = "Users"
    const val IMAGE: String = "image"
    const val NAME: String = "name"
    const val MOBILE: String = "mobile"
    const val EMAIL: String = "email"
    const val ASSIGNED_TO = "assignedTo"
    const val BOARDS: String = "boards"
    const val READ_STORAGE_PERMISSION_CODE = 1
    const val PICK_IMAGE_REQUEST_CODE = 2
    const val DOCUMENT_ID: String = "documentId"
    const val TASK_LIST: String = "taskList"
    const val BOARD_DETAIL: String = "board_detail"
    const val ID: String = "id"
    const val TASK_LIST_ITEM_POSITION: String = "task_list_item_position"
    const val CARD_LIST_ITEM_POSITION: String = "card_list_item_position"
    const val BOARD_MEMBERS_LIST: String = "board_members_list"
    const val SELECT: String = "Select"
    const val UN_SELECT: String = "UnSelect"
    const val PROJEMANAG_PREFERENCES = "Projemanag_preferences"
    const val FCM_TOKEN_UPDATED = "fcmTokenUpdated"
    const val FCM_TOKEN = "fcmToken"
    const val FCM_BASE_URL = "https://fcm.googleapis.com/fcm/send"
    const val FCM_AUTHORIZATION = "authorization"
    const val FCM_KEY = "key"
    const val FCM_SERVER_KEY =
        "AAAA3umgKD0:APA91bH1kl47q_QyGFwattfVsySVZuteFhexl0utldPoTiqo1aR8Pm_6wQWXvd04TQmz3_jlmt9casvTkbXeG_KPPunQn9oWE-r6bKtzG6ox6GWaemr5SeYCs7wZKyO49a_Bc4v9s_1H"
    const val FCM_KEY_TITLE = "title"
    const val FCM_KEY_MESSAGE = "message"
    const val FCM_KEY_DATA = "data"
    const val FCM_KEY_TO = "to"


    fun showImageChooser(activity: Activity) {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        activity.startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    fun getFileExtension(activity: Activity, uri: Uri): String? {
        return MimeTypeMap.getSingleton()
            .getExtensionFromMimeType(activity.contentResolver.getType(uri))
    }

}