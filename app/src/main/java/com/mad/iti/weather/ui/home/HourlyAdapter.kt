package com.mad.iti.weather.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mad.iti.weather.databinding.HourlyItemBinding
import com.mad.iti.weather.model.weather.Hourly
import com.mad.iti.weather.utils.viewUtils.textView.setTime
import com.mad.iti.weather.viewUtils.setImageFromWeatherIconId2x

class HourlyAdapter : ListAdapter<Hourly, HourlyAdapter.ViewHolder>(DiffUtils) {
    class ViewHolder(val binding: HourlyItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = HourlyItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val hourly = getItem(position)
        holder.binding.textViewTime.setTime(hourly.dt)
        holder.binding.imageViewWeatherIcon.setImageFromWeatherIconId2x(hourly.weather[0].icon)
        holder.binding.textViewTempDegree.text = hourly.temp.toString()
    }

    object DiffUtils : DiffUtil.ItemCallback<Hourly>() {
        override fun areItemsTheSame(oldItem: Hourly, newItem: Hourly): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Hourly, newItem: Hourly): Boolean {
            return oldItem == newItem
        }

    }
}