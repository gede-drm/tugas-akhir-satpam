package com.geded.apartemenkusecurity

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class Helper {
    companion object {
        fun logoutSystem(activity: Activity, status: String) {
            var shared: SharedPreferences =
                activity.getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
            var editor: SharedPreferences.Editor = shared.edit()
            editor.putString(LoginActivity.USERNAME, "")
            editor.putString(LoginActivity.NAME, "")
            editor.putInt(LoginActivity.SECURITY_ID, 0)
            editor.putString(LoginActivity.TOWER, "")
            editor.putInt(LoginActivity.TOWER_ID, 0)
            editor.putString(LoginActivity.TOKEN, "")
            editor.apply()

            var alertMessage = ""
            if (status == "noshift") {
                alertMessage = "Shift Anda telah berakhir, Terima kasih!"
            } else {
                alertMessage =
                    "Anda tercatat bertugas pada tower lain. Mohon lakukan login kembali!"
            }

            val builder = AlertDialog.Builder(activity)
            builder.setCancelable(false)
            builder.setTitle("Logout Otomatis")
            builder.setMessage(alertMessage)
            builder.setPositiveButton("OK") { dialog, which ->
                val intent = Intent(activity, LoginActivity::class.java)
                activity.startActivity(intent)
                activity.finish()
            }
            builder.create().show()
        }
    }
}