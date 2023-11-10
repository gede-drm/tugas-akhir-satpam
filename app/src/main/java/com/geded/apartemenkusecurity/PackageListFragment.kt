package com.geded.apartemenkusecurity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenkusecurity.databinding.ActivityMainBinding
import com.geded.apartemenkusecurity.databinding.FragmentPackageListBinding
import org.json.JSONObject

class PackageListFragment : Fragment() {
    private lateinit var binding: FragmentPackageListBinding
    var packages:ArrayList<PendingPackage> = ArrayList()
    var tower_name = ""
    var tower_id = 0
    var token = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var shared: SharedPreferences = requireActivity().getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        tower_name = shared.getString(LoginActivity.TOWER.toString(),"").toString()
        tower_id = shared.getInt(LoginActivity.TOWER_ID,0)
        token = shared.getString(LoginActivity.TOKEN, "").toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentPackageListBinding.inflate(layoutInflater)
        val view = binding.root
        return view
    }

    override fun onResume() {
        super.onResume()
        updateAPI()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtTowerName.text = "Tower $tower_name"
        binding.fabAddPackage.setOnClickListener {
            activity?.let {
                val intent = Intent(it, AddPackageActivity::class.java)
                it.startActivity(intent)
            }
        }
        binding.fabScanPackage.setOnClickListener {
            activity?.let {
                val intent = Intent(it, ScannerActivity::class.java)
                intent.putExtra(ScannerActivity.TYPE, "package")
                it.startActivity(intent)
            }
        }
        binding.refreshLayoutPackage.setOnRefreshListener {
            binding.recViewPackageList.isVisible = false
            updateAPI()
        }
    }

    fun updateAPI(){
        packages.clear()
        val q = Volley.newRequestQueue(activity)
        val url = Global.urlWS + "package/pendinglist"

        var stringRequest = object : StringRequest(Method.POST, url, Response.Listener {
            val obj = JSONObject(it)
            if(obj.getString("status")=="success") {
                val data = obj.getJSONArray("data")
                for (i in 0 until data.length()) {
                    var pkgObj = data.getJSONObject(i)
                    val pkg = PendingPackage(pkgObj.getInt("id"), pkgObj.getString("receive_date"), pkgObj.getString("photo_url"), pkgObj.getString("unit_no"))
                    packages.add(pkg)
                }
                updateList()
            }
            else if(obj.getString("status")=="empty"){
                binding.txtEmpty.visibility = View.VISIBLE
                binding.refreshLayoutPackage.isRefreshing = false
                binding.recViewPackageList.visibility = View.INVISIBLE
            }
        },
            Response.ErrorListener {
                val builder = AlertDialog.Builder(activity)
                builder.setCancelable(false)
                builder.setTitle("Terjadi Masalah")
                builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                builder.setPositiveButton("OK"){dialog, which->
                    activity?.finish()
                    System.exit(0)
                }
                builder.create().show()
            }){
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["tower"] = tower_id.toString()
                params["token"] = token.toString()
                return params
            }
        }
        stringRequest.setShouldCache(false)
        q.add(stringRequest)
    }
    fun updateList() {
        val lm: LinearLayoutManager = LinearLayoutManager(activity)
        var recyclerView = binding.recViewPackageList
        recyclerView.layoutManager = lm
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = PendingPackageAdapter(packages, this.activity)
        recyclerView.isVisible = true
        binding.txtEmpty.visibility = View.GONE
        binding.refreshLayoutPackage.isRefreshing = false
    }

    companion object {

    }
}