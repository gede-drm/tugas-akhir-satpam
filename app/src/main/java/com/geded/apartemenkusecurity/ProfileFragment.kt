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
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.geded.apartemenkusecurity.databinding.FragmentPermissionListBinding
import com.geded.apartemenkusecurity.databinding.FragmentProfileBinding
import org.json.JSONObject

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    var security_name = ""
    var username = ""
    var token = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var shared: SharedPreferences = requireActivity().getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
        security_name = shared.getString(LoginActivity.NAME.toString(),"").toString()
        username = shared.getString(LoginActivity.USERNAME.toString(),"").toString()
        token = shared.getString(LoginActivity.TOKEN.toString(),"").toString()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(layoutInflater)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtSecurityName.setText(security_name)
        binding.btnLogout.setOnClickListener {
            var shared: SharedPreferences = requireActivity().getSharedPreferences(Global.sharedFile, Context.MODE_PRIVATE)
            var editor: SharedPreferences.Editor = shared.edit()

            val q = Volley.newRequestQueue(activity)
            val url = Global.urlGeneralWS + "cleartoken"

            var stringRequest = object : StringRequest(
                Method.POST, url, Response.Listener {
                    val obj = JSONObject(it)
                    if(obj.getString("status")=="success") {
                        editor.putString(LoginActivity.USERNAME, "")
                        editor.putString(LoginActivity.NAME, "")
                        editor.putInt(LoginActivity.SECURITY_ID, 0)
                        editor.putString(LoginActivity.TOWER, "")
                        editor.putInt(LoginActivity.TOWER_ID, 0)
                        editor.putString(LoginActivity.TOKEN, "")
                        editor.apply()

                        activity?.let{ fragmentActivity ->
                            val intent = Intent(fragmentActivity, LoginActivity::class.java)
                            fragmentActivity.startActivity(intent)
                            fragmentActivity.finish()
                        }
                    }
                    else{
                        val builder = AlertDialog.Builder(activity)
                        builder.setCancelable(false)
                        builder.setTitle("Terjadi Masalah")
                        builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                        builder.setPositiveButton("OK"){dialog, which->
                        }
                        builder.create().show()
                    }
                },
                Response.ErrorListener {
                    val builder = AlertDialog.Builder(activity)
                    builder.setCancelable(false)
                    builder.setTitle("Terjadi Masalah")
                    builder.setMessage("Terdapat Masalah Jaringan\nSilakan Coba Lagi Nanti.")
                    builder.setPositiveButton("OK"){dialog, which->
                    }
                    builder.create().show()
                }){
                override fun getParams(): MutableMap<String, String> {
                    val params = HashMap<String, String>()
                    params["username"] = username.toString()
                    params["token"] = token.toString()
                    return params
                }
            }
            stringRequest.setShouldCache(false)
            q.add(stringRequest)
        }
    }

    companion object {

    }
}