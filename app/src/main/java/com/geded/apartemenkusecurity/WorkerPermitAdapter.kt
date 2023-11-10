package com.geded.apartemenkusecurity

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.geded.apartemenkusecurity.databinding.LayoutWorkerWpBinding

class WorkerPermitAdapter(val workers:ArrayList<Worker>, val context: FragmentActivity?):
RecyclerView.Adapter<WorkerPermitAdapter.WorkerPermitViewHolder>() {
    class WorkerPermitViewHolder(val binding: LayoutWorkerWpBinding): RecyclerView.ViewHolder(binding.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkerPermitViewHolder {
        val binding = LayoutWorkerWpBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WorkerPermitViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return workers.size
    }

    override fun onBindViewHolder(holder: WorkerPermitViewHolder, position: Int) {
        holder.binding.checkWorker.text = "Nama: " + workers[position].worker_name + "\nNo. ID/NIK: " + workers[position].idcard_number.toString()
        holder.binding.checkWorker.setOnCheckedChangeListener { buttonView, isChecked ->
            workers[position].presence = holder.binding.checkWorker.isChecked
        }
    }
}