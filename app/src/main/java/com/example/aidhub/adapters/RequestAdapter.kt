package com.example.aidhub.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.data.dataStractures.Request
import com.example.aidhub.databinding.ItemRequestBinding
import com.example.aidhub.utilities.ChipBuilder
import com.example.aidhub.utilities.convertMillisToDate

class RequestAdapter(private val requests: List<Request?>, private val onRequestClicked: (Request) -> Unit) :
    RecyclerView.Adapter<RequestAdapter.RequestViewHolder>() {




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestViewHolder {
        val binding = ItemRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RequestViewHolder(binding)
    }



    override fun onBindViewHolder(holder: RequestViewHolder, position: Int) {
        val request = requests[position]
        holder.bind(request!!)
    }

    override fun getItemCount(): Int {
        return requests.size
    }

    inner class RequestViewHolder(private val binding: ItemRequestBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(request: Request) {

            binding.txtTitle.text = request.title
            binding.txtDescription.text = request.content

            val date = convertMillisToDate(request.timestamp)
            binding.txtDate.text = date
            binding.root.setOnClickListener { onRequestClicked(request) }

            val statusChip = ChipBuilder.createStatusChip(binding.root.context, request.status)
            binding.chipGroupStatus.removeAllViews()
            binding.chipGroupStatus.addView(statusChip)
        }
    }
}