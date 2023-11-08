package com.geded.apartemenkusecurity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenkusecurity.databinding.ActivityMainBinding
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val fragments: ArrayList<Fragment> = ArrayList()
    var security_id = 0
    var tower_id = 0
    var token = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        security_id = shared.getInt(LoginActivity.SECURITY_ID, 0)
        tower_id = shared.getInt(LoginActivity.TOWER_ID,0)
        token = shared.getString(LoginActivity.TOKEN, "").toString()

        /* BottomNav */
        fragments.add(PackageListFragment())
        fragments.add(PermissionListFragment())
        fragments.add(ProfileFragment())

        binding.viewPager.adapter = ViewPagerAdapter(this, fragments)

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                binding.bottomNavView.selectedItemId = binding.bottomNavView.menu.getItem(position).itemId
            }
        })
        binding.bottomNavView.setOnItemSelectedListener {
            binding.viewPager.currentItem = when(it.itemId){
                R.id.itemPackage -> 0
                R.id.itemPermission -> 1
                R.id.itemProfile -> 2
                else -> 0
            }
            true
        }
    }

    override fun onResume() {
        super.onResume()
        var q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "checkshift"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener {
                var obj = JSONObject(it)
                var resultDb = obj.getString("status")
                if (resultDb == "noshift" || resultDb == "othershift" || resultDb == "notauthenticated") {
                    var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
                    var editor: SharedPreferences.Editor = shared.edit()
                    editor.putString(LoginActivity.USERNAME, "")
                    editor.putString(LoginActivity.NAME, "")
                    editor.putInt(LoginActivity.SECURITY_ID, 0)
                    editor.putString(LoginActivity.TOWER, "")
                    editor.putInt(LoginActivity.TOWER_ID, 0)
                    editor.putString(LoginActivity.TOKEN, "")
                    editor.apply()

                    var alertMessage = ""
                    if(resultDb == "noshift" || resultDb == "notauthenticated"){
                        alertMessage = "Shift Anda telah berakhir, Terima kasih!"
                    }
                    else{
                        alertMessage = "Anda tercatat bertugas pada tower lain. Mohon lakukan login kembali!"
                    }

                    val builder = AlertDialog.Builder(this)
                    builder.setCancelable(false)
                    builder.setTitle("Logout Otomatis")
                    builder.setMessage(alertMessage)
                    builder.setPositiveButton("OK"){dialog, which->
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        this.finish()
                    }
                    builder.create().show()
                }
            },
            Response.ErrorListener {
            }) {

            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["security"] = security_id.toString()
                params["tower"] = tower_id.toString()
                params["token"] = token.toString()
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }
}