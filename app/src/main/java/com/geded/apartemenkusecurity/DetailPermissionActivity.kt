package com.geded.apartemenkusecurity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.geded.apartemenkusecurity.databinding.ActivityDetailPermissionBinding

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
    }
}