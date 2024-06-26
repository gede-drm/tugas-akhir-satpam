package com.geded.apartemenkusecurity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.NoCache
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenkusecurity.databinding.ActivityLoginBinding
import org.json.JSONObject

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    companion object{
        val USERNAME = "USERNAME"
        val NAME = "NAME"
        val SECURITY_ID = "SECURITY_ID"
        val TOWER = "TOWER"
        val TOWER_ID = "TOWER_ID"
        val TOKEN = "TOKEN"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)

        var usernameSP = shared.getString(LoginActivity.USERNAME, "")
        var security_idSP = shared.getInt(LoginActivity.SECURITY_ID, 0)
        if(usernameSP!="")
        {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        binding.btnLogin.setOnClickListener {
            val inputUsername = binding.txtUsername.text
            val inputPassword = binding.txtPassword.text
            if((inputUsername.toString() != "") && (inputPassword.toString() != "")) {
                var q = Volley.newRequestQueue(this)
                val url = Global.urlWS + "login"

                val stringRequest = object : StringRequest(Method.POST, url,
                    Response.Listener {
                        Log.d("Success", it)
                        var obj = JSONObject(it)
                        var resultDb = obj.getString("status")
                        if (resultDb == "success") {
                            var array = obj.getJSONObject("data")
                            var username = array["username"]
                            var name = array["security_name"]
                            var security_id = array["security_id"]
                            var tower = array["tower_name"]
                            var tower_id = array["tower_id"]
                            var token = array["token"]

                            var editor: SharedPreferences.Editor = shared.edit()
                            editor.putString(USERNAME, username.toString())
                            editor.putString(NAME, name.toString())
                            editor.putInt(SECURITY_ID, security_id.toString().toInt())
                            editor.putString(TOWER, tower.toString())
                            editor.putInt(TOWER_ID, tower_id.toString().toInt())
                            editor.putString(TOKEN, token.toString())
                            editor.apply()

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            this.finish()
                        } else if (resultDb == "noshift") {
                            val builder = AlertDialog.Builder(this)
                            builder.setTitle("Security Login")
                            builder.setMessage("Login tidak dapat dilakukan!\nAnda sedang tidak tercatat bertugas.")
                            builder.setPositiveButton("OK", null)
                            builder.create().show()
                        } else {
                            Toast.makeText(
                                this,
                                "Username atau Password Salah!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    Response.ErrorListener {
                        Toast.makeText(this, "Connection Error", Toast.LENGTH_SHORT).show()
                    }) {

                    override fun getParams(): MutableMap<String, String> {
                        val params = HashMap<String, String>()
                        params["username"] = inputUsername.toString()
                        params["password"] = inputPassword.toString()
                        return params
                    }
                }
                q.cache.clear()
                stringRequest.cacheEntry?.ttl = 2000
                stringRequest.setShouldCache(false)
                q.add(stringRequest)
            }
            else{
                Toast.makeText(this, "Username atau Password Tidak Boleh Kosong!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}