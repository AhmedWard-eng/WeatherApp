package com.mad.iti.weather.ui.favorite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mad.iti.weather.databinding.FavItemBinding
import com.mad.iti.weather.language.getLanguageLocale
import com.mad.iti.weather.model.entities.FavWeatherData
import com.mad.iti.weather.utils.locationUtils.formatAddressToCity
import com.mad.iti.weather.utils.locationUtils.formatAddressToCountry
import com.mad.iti.weather.utils.locationUtils.getAddress
import java.util.*

class FavAdapter(val onClickListener: OnClickListener) : ListAdapter<FavWeatherData, FavAdapter.ViewHolder>(DiffUtils) {
    class ViewHolder(val binding: FavItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = FavItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val favWeatherData = getItem(position)

        val context = holder.binding.root.context
        getAddress(
            context, favWeatherData.lon, favWeatherData.lat, Locale(getLanguageLocale())
        ) { address ->
            holder.binding.textViewCity.text = address?.let { it1 -> formatAddressToCity(it1) }

            holder.binding.textViewCountry.text = address?.let { it1 -> formatAddressToCountry(it1) }
        }

        holder.binding.imageViewDelete.setOnClickListener{
            onClickListener.onRemoveClick(favWeatherData)
        }

        holder.itemView.setOnClickListener {
            onClickListener.onItemClick(favWeatherData)
        }
    }



    object DiffUtils : DiffUtil.ItemCallback<FavWeatherData>() {
        override fun areItemsTheSame(oldItem: FavWeatherData, newItem: FavWeatherData): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FavWeatherData, newItem: FavWeatherData): Boolean {
            return oldItem == newItem
        }

    }

    class OnClickListener(val removeClickListener : (FavWeatherData) -> Unit,val itemClickListener : (FavWeatherData) -> Unit){
        fun onRemoveClick(favWeatherData: FavWeatherData) = removeClickListener(favWeatherData)
        fun onItemClick(favWeatherData: FavWeatherData) = itemClickListener(favWeatherData)
    }
}