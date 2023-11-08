package com.geded.apartemenkusecurity

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenkusecurity.databinding.ActivityDetailPackageBinding
import com.squareup.picasso.Picasso
import org.json.JSONObject

class DetailPackageActivity : AppCompatActivity() {
    private lateinit var binding:ActivityDetailPackageBinding
    companion object{
        val PACKAGE_ID = "PACKAGE_ID"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPackageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val package_id = intent.getStringExtra(PACKAGE_ID)
        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        val token = shared.getString(LoginActivity.TOKEN, "").toString()

        var q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "package/detail"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener {
                val obj = JSONObject(it)
                if(obj.getString("status")=="success") {
                    val unitObj = obj.getJSONObject("data")
                    binding.txtUnitDP.setText(unitObj.getString("unit_no"))
                    binding.txtReceiveDateDP.setText(unitObj.getString("receive_date"))
                    binding.txtDescriptionDP.setText(unitObj.getString("description"))
                    val url = unitObj.getString("photo_url")
                    Picasso.get().load(url).into(binding.imgDetailPackage)
                }
            },
            Response.ErrorListener {
                val builder = AlertDialog.Builder(this)
                builder.setCancelable(false)
                builder.setTitle("Terjadi Masalah")
                builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                builder.setPositiveButton("OK"){dialog, which->
                    this.finish()
                }
                builder.create().show()
            }
            )
        {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["package_id"] = package_id.toString()
                params["token"] = token.toString()
                return params
            }
        }
        q.add(stringRequest)
    }
}