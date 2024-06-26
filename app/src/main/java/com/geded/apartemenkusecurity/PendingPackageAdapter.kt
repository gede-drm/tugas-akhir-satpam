package com.geded.apartemenkusecurity

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.geded.apartemenkusecurity.databinding.LayoutPendingPackageListBinding
import com.squareup.picasso.Picasso

class PendingPackageAdapter(val pendingPackages:ArrayList<PendingPackage>, val context:FragmentActivity?):RecyclerView.Adapter<PendingPackageAdapter.PendingPackageViewHolder>() {
class PendingPackageViewHolder(val binding:LayoutPendingPackageListBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PendingPackageViewHolder {
        val binding = LayoutPendingPackageListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PendingPackageViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return pendingPackages.size
    }

    override fun onBindViewHolder(holder: PendingPackageViewHolder, position: Int) {
        val url =pendingPackages[position].photo_url
        with(holder.binding){
            Picasso.get().load(url).resize(200, 134).into(imgPackage)
            txtReceiveDate.text = pendingPackages[position].receive_date
            txtUnitNo.text = pendingPackages[position].unit_no
        }
        holder.binding.btnDetail.setOnClickListener {
            val intent = Intent(this.context, DetailPackageActivity::class.java)
            intent.putExtra(
                DetailPackageActivity.PACKAGE_ID, pendingPackages[position].id.toString()
            )
            context?.startActivity(intent)
        }
    }
}