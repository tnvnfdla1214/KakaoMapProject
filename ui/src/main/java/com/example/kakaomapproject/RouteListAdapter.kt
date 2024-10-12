package com.example.kakaomapproject

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.data.response.OriginDestination
import com.example.kakaomapproject.databinding.ItemLocationBinding

class LocationListAdapter(
    private val routes: List<OriginDestination>,
    private val onItemClicked: (OriginDestination) -> Unit
) : RecyclerView.Adapter<LocationListAdapter.RouteViewHolder>() {

    class RouteViewHolder(
        private val binding: ItemLocationBinding,
        private val onItemClicked: (OriginDestination) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(location: OriginDestination) {
            binding.textOrigin.text = location.origin
            binding.textDestination.text = location.destination

            binding.root.setOnClickListener {
                onItemClicked(location)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLocationBinding.inflate(inflater, parent, false)
        return RouteViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        holder.bind(routes[position])
    }

    override fun getItemCount(): Int = routes.size
}