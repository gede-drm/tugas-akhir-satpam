package com.geded.apartemenkusecurity

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.geded.apartemenkusecurity.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val fragments: ArrayList<Fragment> = ArrayList()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

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
}