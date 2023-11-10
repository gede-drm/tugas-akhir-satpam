package com.geded.apartemenkusecurity

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenkusecurity.databinding.ActivityWorkerPermitsBinding
import org.json.JSONObject

class WorkerPermitsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWorkerPermitsBinding
    var workersList:ArrayList<Worker> = ArrayList()
    companion object{
        val PERMISSION_PERMIT_ID = "PERMISSION_PERMIT_ID"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkerPermitsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val permission_id = intent.getStringExtra(PERMISSION_PERMIT_ID)

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        val tower_id = shared.getInt(LoginActivity.TOWER_ID,0)
        val satpam_id = shared.getInt(LoginActivity.SECURITY_ID,0)
        val token = shared.getString(LoginActivity.TOKEN, "").toString()

        var q = Volley.newRequestQueue(this)
        val url = Global.urlWS + "permission/workersdetail"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            Response.Listener {
                Log.d("VOLLEY", it)
                val obj = JSONObject(it)
                if(obj.getString("status")=="success") {
                    val permObj = obj.getJSONObject("data")
                    binding.txtUnitWP.text = permObj.getString("unit_no")
                    binding.txtTenantWP.text = permObj.getString("tenant")
                    binding.txtDescriptionWP.text = permObj.getString("description")
                    binding.txtDurationWP.text = "Mulai: " + permObj.getString("start_date") + "\nSelesai: " + permObj.getString("end_date")
                    binding.txtWorkerNumWP.text = permObj.getString("number_of_worker") + " orang"
                    val workers = permObj.getJSONArray("workers")
                    for (i in 0 until workers.length()) {
                        var workerObj = workers.getJSONObject(i)
                        val worker = Worker(workerObj.getInt("id"), workerObj.getString("worker_name"), workerObj.getString("idcard_number"), false)
                        workersList.add(worker)
                    }
                    updateList()
                }
                else{
                    val builder = AlertDialog.Builder(this)
                    builder.setCancelable(false)
                    builder.setTitle("Terjadi Masalah")
                    builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                    builder.setPositiveButton("OK"){dialog, which->
                        this.finish()
                    }
                    builder.create().show()
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

        binding.btnSaveWP.setOnClickListener {
            if(workersList.size == 0){
                Toast.makeText(this, "Anda Harus Memilih Pekerja yang Masuk Paling Tidak 1 Pekerja", Toast.LENGTH_SHORT).show()
            }
            else {
                var workers_ids = arrayListOf<Int>()
                for (i in 0 until workersList.size) {
                    if (workersList[i].presence == true) {
                        workers_ids.add(workersList[i].id)
                    }
                }

                var q = Volley.newRequestQueue(this)
                val url = Global.urlWS + "permission/addpermits"

                val stringRequest = object : StringRequest(
                    Method.POST, url,
                    Response.Listener {
                        val obj = JSONObject(it)
                        Log.d("VOLLEY", obj.toString())
                        if(obj.getString("status")=="success") {
                            val builder = android.app.AlertDialog.Builder(this)
                            builder.setCancelable(false)
                            builder.setTitle("Tamabah Izin Masuk Berhasil")
                            builder.setMessage("Izin Masuk Telah Berhasil dibuat. Silakan Mengizinkan Pekerja untuk Masuk")
                            builder.setPositiveButton("OK"){dialog, which->
                                this.finish()
                            }
                            builder.create().show()
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
                        else{
                            val builder = android.app.AlertDialog.Builder(this)
                            builder.setCancelable(false)
                            builder.setTitle("Terjadi Masalah")
                            builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                            builder.setPositiveButton("OK"){dialog, which->
                                this.finish()
                            }
                            builder.create().show()
                        }
                    },
                    Response.ErrorListener {
                        Log.d("ERROR VOLLEY", it.message.toString())
                        val builder = android.app.AlertDialog.Builder(this)
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
                        params["tower"] = tower_id.toString()
                        for (i in 0 until workers_ids.size) {
                            params["workers_ids[$i]"] = workers_ids[i].toString()
                        }
                        params["officer"] = satpam_id.toString()
                        params["token"] = token.toString()
                        return params
                    }
                }
                q.add(stringRequest)
            }
        }
    }

    fun updateList() {
        val lm: LinearLayoutManager = LinearLayoutManager(this)
        var recyclerView = binding.recViewWorkerWP
        recyclerView.layoutManager = lm
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = WorkerPermitAdapter(workersList, this)
    }
}