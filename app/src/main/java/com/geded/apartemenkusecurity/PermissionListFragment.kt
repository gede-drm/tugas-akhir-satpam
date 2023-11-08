package com.geded.apartemenkusecurity

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.geded.apartemenkusecurity.databinding.FragmentPackageListBinding
import com.geded.apartemenkusecurity.databinding.FragmentPermissionListBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [PermissionListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PermissionListFragment : Fragment() {
    private lateinit var binding: FragmentPermissionListBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        binding.fabScanPermission.setOnClickListener {
            activity?.let {
                val intent = Intent(it, ScannerActivity::class.java)
                intent.putExtra(ScannerActivity.TYPE, "permission")
                it.startActivity(intent)
            }
        }
    }

    companion object {

    }
}