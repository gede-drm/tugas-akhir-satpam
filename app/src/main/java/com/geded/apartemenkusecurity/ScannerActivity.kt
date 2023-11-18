package com.geded.apartemenkusecurity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.geded.apartemenkusecurity.databinding.ActivityScannerBinding
import com.google.zxing.BarcodeFormat
import org.json.JSONObject

class ScannerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityScannerBinding
    private lateinit var codeScanner: CodeScanner
    companion object{
        val TYPE = "TYPE"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val scanType = intent.getStringExtra(TYPE)
        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        val tower_id = shared.getInt(LoginActivity.TOWER_ID,0)
        val satpam_id = shared.getInt(LoginActivity.SECURITY_ID,0)
        val token = shared.getString(LoginActivity.TOKEN, "").toString()

        val scannerView = binding.scannerView
        codeScanner = CodeScanner(this, scannerView)

        codeScanner.camera = CodeScanner.CAMERA_BACK
        codeScanner.formats = arrayListOf(BarcodeFormat.QR_CODE)

        codeScanner.autoFocusMode = AutoFocusMode.SAFE
        codeScanner.scanMode = ScanMode.SINGLE
        codeScanner.isAutoFocusEnabled = false

        codeScanner.decodeCallback = DecodeCallback { code->
            runOnUiThread {
                if(scanType == "package"){
                    var q = Volley.newRequestQueue(this)
                    val url = Global.urlWS + "package/collection"

                    val stringRequest = object : StringRequest(
                        Method.POST, url,
                        Response.Listener {
                            val obj = JSONObject(it)
                            if(obj.getString("status")=="success") {
                                val builder = AlertDialog.Builder(this)
                                builder.setCancelable(false)
                                builder.setTitle("Pengambilan Paket Berhasil")
                                builder.setMessage("Pengambilan Paket Milik Unit " + obj.getString("unit_no") + " Berhasil dilakukan!")
                                builder.setPositiveButton("OK"){dialog, which->
                                    this.finish()
                                }
                                builder.create().show()
                            }
                            else if(obj.getString("status") == "othertower"){
                                val builder = AlertDialog.Builder(this)
                                builder.setCancelable(false)
                                builder.setTitle("Terjadi Kesalahan")
                                builder.setMessage("Paket ini Milik Unit yang Berada di Tower Tempat Anda Sedang Tidak Bertugas.")
                                builder.setPositiveButton("OK"){dialog, which->
                                    this.finish()
                                }
                                builder.create().show()
                            }
                            else if(obj.getString("status") == "picked"){
                                val builder = AlertDialog.Builder(this)
                                builder.setCancelable(false)
                                builder.setTitle("Terjadi Kesalahan")
                                builder.setMessage("Paket Telah diambil!\nApabila Terdapat Ketidaktepatan, Hubungi Manajemen.")
                                builder.setPositiveButton("OK"){dialog, which->
                                    this.finish()
                                }
                                builder.create().show()
                            }
                            else if(obj.getString("status") == "notfound"){
                                val builder = AlertDialog.Builder(this)
                                builder.setCancelable(false)
                                builder.setTitle("Terjadi Kesalahan")
                                builder.setMessage("Paket Tidak Terdaftar!\nPastikan Kode QR yang discan benar.")
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
                            params["code"] = code.text
                            params["tower"] = tower_id.toString()
                            params["officer"] = satpam_id.toString()
                            params["token"] = token.toString()
                            return params
                        }
                    }
                    val retryPolicy = DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    stringRequest.setRetryPolicy(retryPolicy);
                    q.add(stringRequest)
                }
                else if(scanType == "permission"){
                    var q = Volley.newRequestQueue(this)
                    val url = Global.urlWS + "permission/scan"

                    val stringRequest = object : StringRequest(
                        Method.POST, url,
                        Response.Listener {
                            val obj = JSONObject(it)
                            if(obj.getString("status")=="success") {
                                val permission_id = obj.getString("id")
                                val builder = AlertDialog.Builder(this)
                                builder.setCancelable(false)
                                builder.setTitle("Perizinan ditemukan")
                                builder.setMessage("Silakan Pilih Pekerja yang Masuk")
                                builder.setPositiveButton("OK"){dialog, which->
                                    val intent = Intent(this, WorkerPermitsActivity::class.java)
                                    intent.putExtra(WorkerPermitsActivity.PERMISSION_PERMIT_ID, permission_id)
                                    startActivity(intent)
                                    this.finish()
                                }
                                builder.create().show()
                            }
                            else if(obj.getString("status") == "permitted"){
                                val builder = AlertDialog.Builder(this)
                                builder.setCancelable(false)
                                builder.setTitle("Terjadi Kesalahan")
                                builder.setMessage("Perizinan ini Sedang Berlangsung dan Telah diperbolehkan untuk Masuk.")
                                builder.setPositiveButton("OK"){dialog, which->
                                    this.finish()
                                }
                                builder.create().show()
                            }
                            else if(obj.getString("status") == "othertower"){
                                val builder = AlertDialog.Builder(this)
                                builder.setCancelable(false)
                                builder.setTitle("Terjadi Kesalahan")
                                builder.setMessage("Perizinan ini Milik Unit yang Berada di Tower Tempat Anda Sedang Tidak Bertugas.")
                                builder.setPositiveButton("OK"){dialog, which->
                                    this.finish()
                                }
                                builder.create().show()
                            }
                            else if(obj.getString("status") == "notfound"){
                                val builder = AlertDialog.Builder(this)
                                builder.setCancelable(false)
                                builder.setTitle("Terjadi Kesalahan")
                                builder.setMessage("Perizinan Tidak Terdaftar!\nPastikan Kode QR yang discan benar.")
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
                            params["code"] = code.text
                            params["tower"] = tower_id.toString()
                            params["officer"] = satpam_id.toString()
                            params["token"] = token.toString()
                            return params
                        }
                    }
                    val retryPolicy = DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    stringRequest.setRetryPolicy(retryPolicy);
                    q.add(stringRequest)
                }
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }

        scannerView.setOnClickListener {
            codeScanner.startPreview()
        }
    }

    override fun onResume() {
        super.onResume()
        codeScanner.startPreview()
    }

    override fun onPause() {
        codeScanner.releaseResources()
        super.onPause()
    }
}