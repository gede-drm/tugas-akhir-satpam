package com.geded.apartemenkusecurity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var sharedFile = "com.geded.apartemenkusecurity"
        var shared: SharedPreferences = requireActivity().getSharedPreferences(sharedFile, Context.MODE_PRIVATE)
        tower_name = shared.getString(LoginActivity.TOWER.toString(),"").toString()
        tower_id = shared.getInt(LoginActivity.TOWER_ID,0)
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
                    binding.recViewPackageList.visibility = View.INVISIBLE
                }
            },
        Response.ErrorListener {  }){
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["tower"] = tower_id.toString()
                return params
            }
        }
        q.add(stringRequest)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtTowerName.text = "Tower $tower_name"
        binding.fabAddPackage.setOnClickListener {
            Toast.makeText(activity,"Helo!", Toast.LENGTH_SHORT).show()
        }
    }

    fun updateList() {
        val lm: LinearLayoutManager = LinearLayoutManager(activity)
        var recyclerView = view?.findViewById<RecyclerView>(R.id.recViewPackageList)
        recyclerView?.layoutManager = lm
        recyclerView?.setHasFixedSize(true)
        recyclerView?.adapter = PendingPackageAdapter(packages, this.activity)
    }

    companion object {

    }
}