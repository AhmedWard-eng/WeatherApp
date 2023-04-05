package com.mad.iti.weather.ui.favorite

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mad.iti.weather.databinding.FavItemBinding
import com.mad.iti.weather.language.getLanguageLocale
import com.mad.iti.weather.model.entities.FavWeatherEntity
import com.mad.iti.weather.utils.locationUtils.formatAddressToCity
import com.mad.iti.weather.utils.locationUtils.formatAddressToCountry
import com.mad.iti.weather.utils.locationUtils.getAddress
import java.util.*

class FavAdapter(val onClickListener: OnClickListener) : ListAdapter<FavWeatherEntity, FavAdapter.ViewHolder>(DiffUtils) {
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



    object DiffUtils : DiffUtil.ItemCallback<FavWeatherEntity>() {
        override fun areItemsTheSame(oldItem: FavWeatherEntity, newItem: FavWeatherEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FavWeatherEntity, newItem: FavWeatherEntity): Boolean {
            return oldItem == newItem
        }

    }

    class OnClickListener(val removeClickListener : (FavWeatherEntity) -> Unit, val itemClickListener : (FavWeatherEntity) -> Unit){
        fun onRemoveClick(favWeatherEntity: FavWeatherEntity) = removeClickListener(favWeatherEntity)
        fun onItemClick(favWeatherEntity: FavWeatherEntity) = itemClickListener(favWeatherEntity)
    }
}