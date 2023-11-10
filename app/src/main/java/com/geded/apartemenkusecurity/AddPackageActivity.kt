package com.geded.apartemenkusecurity

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenkusecurity.databinding.ActivityAddPackageBinding
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date


class AddPackageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddPackageBinding
    val REQUEST_IMAGE_CAPTURE = 1
    var units:ArrayList<Unit> = ArrayList()
    var uriImageBase64 = ""
    private lateinit var photoFile:File
    var vFilename = ""
    var imagePath = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPackageBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        var shared: SharedPreferences = getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        val tower_id = shared.getInt(LoginActivity.TOWER_ID,0)
        val satpam_id = shared.getInt(LoginActivity.SECURITY_ID,0)
        val token = shared.getString(LoginActivity.TOKEN, "").toString()

//        var q = Volley.newRequestQueue(this)
//        val url = Global.urlGeneralWS + "unitbytower"
//
//        val stringRequest = object : StringRequest(
//            Method.POST, url,
//            Response.Listener {
//                val obj = JSONObject(it)
//                if(obj.getString("status")=="success") {
//                    val data = obj.getJSONArray("data")
//                    for (i in 0 until data.length()) {
//                        var unitObj = data.getJSONObject(i)
//                        val unit = Unit(unitObj.getInt("id"), unitObj.getString("unit_no"))
//                        units.add(unit)
//                    }
//                    val unitSpinnerAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, units)
//                    unitSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                    binding.spinnerUnit.adapter = unitSpinnerAdapter
//                }
//                else{
//                    binding.spinnerUnit.isEnabled = false
//                }
//            },
//            Response.ErrorListener {
//                val builder = AlertDialog.Builder(this)
//                builder.setCancelable(false)
//                builder.setTitle("Terjadi Masalah")
//                builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
//                builder.setPositiveButton("OK"){dialog, which->
//                    this.finish()
//                }
//                builder.create().show()
//            }
//            )
//        {
//            override fun getParams(): MutableMap<String, String> {
//                val params = HashMap<String, String>()
//                params["tower"] = tower_id.toString()
//                return params
//            }
//        }
//        q.add(stringRequest)

        binding.btnTakePhoto.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_IMAGE_CAPTURE)
            }
            else{
                takePicture()
            }
        }
        binding.btnSavePackage.setOnClickListener {
            val unit = binding.txtUnitNo.text.toString()
            val sender = binding.txtSender.text.toString()
            val recipient = binding.txtRecipient.text.toString()
            val detail = binding.txtDetail.text.toString()

            if(unit != "" && sender != "" && recipient != "" && detail != ""){
                if(uriImageBase64 != ""){
                    val description = "Nama Pengirim: $sender\nNama Penerima: $recipient\nDetail Paket: $detail"
                    var q = Volley.newRequestQueue(this)
                    val url = Global.urlWS + "package/entry"

                    val stringRequest = object : StringRequest(
                        Method.POST, url,
                        Response.Listener {
                            val obj = JSONObject(it)
                            if(obj.getString("status")=="success") {
                                val builder = AlertDialog.Builder(this)
                                builder.setCancelable(false)
                                builder.setTitle("Paket Masuk Tersimpan")
                                builder.setMessage("Data Paket Masuk Telah Tersimpan.")
                                builder.setPositiveButton("OK"){dialog, which->
                                    this.finish()
                                }
                                builder.create().show()
                            }
                            else if(obj.getString("status") == "othertower"){
                                val builder = AlertDialog.Builder(this)
                                builder.setCancelable(false)
                                builder.setTitle("Terjadi Kesalahan")
                                builder.setMessage("Unit Berada di Tower Tempat Anda Tidak Sedang Bertugas.")
                                builder.setPositiveButton("OK"){dialog, which->
                                }
                                builder.create().show()
                            }
                            else if(obj.getString("status") == "notfound"){
                                val builder = AlertDialog.Builder(this)
                                builder.setCancelable(false)
                                builder.setTitle("Terjadi Kesalahan")
                                builder.setMessage("Unit Tidak Ditemukan.\nPastikan Anda Telah Memasukkan Unit dengan Format yang Sesuai")
                                builder.setPositiveButton("OK"){dialog, which->
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
                            params["description"] = description
                            params["tower"] = tower_id.toString()
                            params["officer"] = satpam_id.toString()
                            params["unit"] = unit
                            params["image"] = uriImageBase64
                            params["token"] = token.toString()
                            return params
                        }
                    }
                    val retryPolicy = DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    stringRequest.setRetryPolicy(retryPolicy);
                    q.add(stringRequest)
                }
                else{
                    Toast.makeText(
                        this,
                        "Terjadi Kesalahan dalam Pengambilan Foto, Silakan Ambil Foto Ulang",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            else{
                Toast.makeText(
                    this,
                    "Unit/Pengirim/Penerima/Detail Paket Tidak Boleh Kosong!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    fun takePicture() {
        val i = Intent()
        i.action = MediaStore.ACTION_IMAGE_CAPTURE
        photoFile = createImageFile()
        // Continue only if the File was successfully created
        if (photoFile != null) {
            val photoURI = FileProvider.getUriForFile(
                this,
                "com.geded.apartemenkusecurity.fileprovider",
                photoFile!!
            )
            i.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(i, REQUEST_IMAGE_CAPTURE)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            imagePath = absolutePath
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_IMAGE_CAPTURE -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                )
                else
                    Toast.makeText(
                        this,
                        "Anda harus memperbolehkan akses ke kamera",
                        Toast.LENGTH_LONG
                    ).show()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
                if (requestCode == REQUEST_IMAGE_CAPTURE) {
                    val imageBitmap = BitmapFactory.decodeFile(photoFile!!.absolutePath)
                    binding.imgPackagePhoto.setImageBitmap(imageBitmap)
                    uriImageBase64 = getImageUriFromBitmap(imageBitmap)
                }
        }
    }

    fun getImageUriFromBitmap(bitmap: Bitmap): String{
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, bytes)

        val byteImagePhoto = bytes.toByteArray()
        val encodedImage = "data:image/jpeg;base64," + Base64.encodeToString(byteImagePhoto, Base64.DEFAULT)
        return encodedImage
    }
}