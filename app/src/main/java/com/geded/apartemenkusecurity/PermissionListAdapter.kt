package com.geded.apartemenkusecurity

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.geded.apartemenkusecurity.databinding.LayoutPermissionListBinding

class PermissionListAdapter(val runningPermissions:ArrayList<RunningPermission>, val context: FragmentActivity?):
    RecyclerView.Adapter<PermissionListAdapter.PermissionListViewHolder>() {
    class PermissionListViewHolder(val binding: LayoutPermissionListBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PermissionListViewHolder {
        val binding = LayoutPermissionListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PermissionListAdapter.PermissionListViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return runningPermissions.size
    }

    override fun onBindViewHolder(holder: PermissionListViewHolder, position: Int) {
        with(holder.binding) {
            txtUnitNoPL.setText(runningPermissions [position].unit_no)
            txtWorkerNumPL.setText("Jumlah Pekerja Masuk: " + runningPermissions[position].workPermitsCount.toString())
            txtTenantPL.setText("Tenant: " + runningPermissions[position].tenant)
        }
        holder.binding.btnDetailPermission.setOnClickListener {
            val intent = Intent(this.context, DetailPermissionActivity::class.java)
            intent.putExtra(
                DetailPermissionActivity.PERMISSION_ID, runningPermissions[position].id.toString()
            )
            context?.startActivity(intent)
        }
    }
}