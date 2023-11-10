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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenkusecurity.databinding.FragmentPermissionListBinding
import org.json.JSONObject
import java.time.LocalDateTime

class PermissionListFragment : Fragment() {
    private lateinit var binding: FragmentPermissionListBinding
    var permissions:ArrayList<RunningPermission> = ArrayList()
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
        binding = FragmentPermissionListBinding.inflate(layoutInflater)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtTowerNamePL.text = "Tower $tower_name"
        binding.fabScanPermission.setOnClickListener {
            activity?.let {
                val intent = Intent(it, ScannerActivity::class.java)
                intent.putExtra(ScannerActivity.TYPE, "permission")
                it.startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        permissions.clear()
        val q = Volley.newRequestQueue(activity)
        val url = Global.urlWS + "permission/list"

        var stringRequest = object : StringRequest(
            Method.POST, url, Response.Listener {
            val obj = JSONObject(it)
            if(obj.getString("status")=="success") {
                val data = obj.getJSONArray("data")
                for (i in 0 until data.length()) {
                    var permObj = data.getJSONObject(i)
                    val perm = RunningPermission(permObj.getInt("id"), permObj.getString("start_date"), permObj.getString("end_date"), permObj.getString("description"), permObj.getString("unit_no"), permObj.getString("tenant"), permObj.getInt("number_of_worker"), permObj.getInt("workPermitsCount"))
                    permissions.add(perm)
                }
                updateList()
            }
            else if(obj.getString("status")=="empty"){
                binding.txtEmptyPL.visibility = View.VISIBLE
                binding.recViewPermission.visibility = View.INVISIBLE
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
        var recyclerView = binding.recViewPermission
        recyclerView.layoutManager = lm
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = PermissionListAdapter(permissions, this.activity)
    }

    companion object {

    }
}