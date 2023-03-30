package com.mad.iti.weather.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mad.iti.weather.R
import com.mad.iti.weather.databinding.DailyItemBinding
import com.mad.iti.weather.model.weather.Daily
import com.mad.iti.weather.utils.viewUtils.textView.setDay
import com.mad.iti.weather.utils.viewUtils.textView.setTemp
import com.mad.iti.weather.viewUtils.setImageFromWeatherIconId2x
import java.util.*
import kotlin.math.roundToInt

class DailyAdapter(var timeZone: TimeZone) : ListAdapter<Daily, DailyAdapter.ViewHolder>(DiffUtils) {
    class ViewHolder(val binding: DailyItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = DailyItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val daily = getItem(position)

        if (position == 0) {
            holder.binding.textViewDay.text = holder.binding.root.context.getString(R.string.today)
        } else {
            holder.binding.textViewDay.setDay(daily.dt,timeZone)
        }

        holder.binding.imageViewWeatherIcon.setImageFromWeatherIconId2x(daily.weather[0].icon)
//        holder.binding.textViewTempDegree.text =
//            buildString {
//                append(daily.temp.day.roundToInt())
//                append(holder.binding.root.context.getString(R.string.degree))
//            }

        holder.binding.textViewTempDegree.setTemp(
            daily.temp.day.roundToInt(),
            holder.itemView.context
        )
    }

    object DiffUtils : DiffUtil.ItemCallback<Daily>() {
        override fun areItemsTheSame(oldItem: Daily, newItem: Daily): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Daily, newItem: Daily): Boolean {
            return oldItem == newItem
        }

    }
}