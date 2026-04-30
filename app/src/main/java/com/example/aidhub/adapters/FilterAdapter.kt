package com.example.aidhub.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.data.dataStractures.Skill
import com.example.aidhub.databinding.ItemFilterBinding



class FilterAdapter(private val onFilterSelected: (String) -> Unit) :
    RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {

    private val filters = mutableListOf("All").apply { addAll(Skill.getAllDisplayNames()) }
    private var selectedPosition = 0


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val binding = ItemFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FilterViewHolder(binding)
    }



    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val filter = filters[position]
        holder.bind(filter)
    }

    override fun getItemCount(): Int {
        return filters.size
    }

    inner class FilterViewHolder(private val binding: ItemFilterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(filter: String) {

            binding.cardLayout.isSelected = (position == selectedPosition)
            binding.txtFilter.text = filter

            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition

                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                onFilterSelected(filter)
            }
        }
    }

}