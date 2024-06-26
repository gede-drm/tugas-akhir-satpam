package com.geded.apartemenkusecurity

import android.content.Context
import android.content.SharedPreferences
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenkusecurity.databinding.ActivityDetailPermissionBinding
import org.json.JSONObject

class DetailPermissionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailPermissionBinding
    companion object{
        val PERMISSION_ID = "PERMISSION_ID"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailPermissionBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val permission_id = intent.getStringExtra(PERMISSION_ID)
        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        val token = shared.getString(LoginActivity.TOKEN, "").toString()

        binding.progressBarDPerm.isVisible = true
        binding.txtUnitDPerm.isVisible = false
        binding.txtTenantDPerm.isVisible = false
        binding.txtDescriptionDPerm.isVisible = false
        binding.txtDurationDPerm.isVisible = false
        binding.txtWorkerNumDPerm.isVisible = false
        binding.txtWorkerPermitDPerm.isVisible = false
        binding.txtSecOfficerDPerm.isVisible = false

        var q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "permission/detail"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener {
                val obj = JSONObject(it)
                if(obj.getString("status")=="success") {
                    val permObj = obj.getJSONObject("data")
                    binding.progressBarDPerm.isVisible = false
                    binding.txtUnitDPerm.isVisible = true
                    binding.txtTenantDPerm.isVisible = true
                    binding.txtDescriptionDPerm.isVisible = true
                    binding.txtDurationDPerm.isVisible = true
                    binding.txtWorkerNumDPerm.isVisible = true
                    binding.txtWorkerPermitDPerm.isVisible = true
                    binding.txtSecOfficerDPerm.isVisible = true

                    binding.txtUnitDPerm.text = permObj.getString("unit_no")
                    binding.txtTenantDPerm.text = permObj.getString("tenant")
                    binding.txtDescriptionDPerm.text = permObj.getString("description")
                    binding.txtDurationDPerm.text = "Mulai: " + permObj.getString("start_date") + "\nSelesai: " + permObj.getString("end_date")
                    binding.txtWorkerNumDPerm.text = permObj.getString("number_of_worker") + " orang"
                    val permits = permObj.getJSONArray("permits")
                    var workers = ""
                    for (i in 0 until permits.length()) {
                        var permitObj = permits.getJSONObject(i)
                        if(i == permits.length()){
                            workers += (i+1).toString() + ". " + permitObj.getString("worker_name") + " (" + permitObj.getString("idcard_number") + ")"
                        }
                        else{
                            workers += (i+1).toString() + ". " + permitObj.getString("worker_name") + " (" + permitObj.getString("idcard_number") + ")\n"
                        }
                    }
                    binding.txtWorkerPermitDPerm.text = workers
                    binding.txtSecOfficerDPerm.text = permObj.getString("officer")
                }
                else if(obj.getString("status") == "securityprob" || obj.getString("status") == "notauthenticated") {
                    var securityStatus = ""
                    if(obj.getString("status") == "notauthenticated"){
                        securityStatus = "noshift"
                    }
                    else {
                        securityStatus = obj.getString("securitystatus")
                    }
                    Helper.logoutSystem(this, securityStatus)
                }
            },
            Response.ErrorListener {
                Log.d("ERROR VOLLEY", it.message.toString())
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
                params["permission_id"] = permission_id.toString()
                params["token"] = token.toString()
                return params
            }
        }
        q.add(stringRequest)
    }
}