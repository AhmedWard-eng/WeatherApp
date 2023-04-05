package com.mad.iti.weather.ui.alert

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mad.iti.weather.databinding.AlarmItemBinding
import com.mad.iti.weather.model.entities.AlertEntity
import com.mad.iti.weather.utils.viewUtils.textView.setDate
import com.mad.iti.weather.utils.viewUtils.textView.setTime

class AlertsAdapter(private val removeClickListener: RemoveClickListener) : ListAdapter<AlertEntity, AlertsAdapter.ViewHolder>(DiffUtils) {
    class ViewHolder(val binding: AlarmItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AlarmItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alertWeatherData = getItem(position)

        holder.binding.imageViewDelete.setOnClickListener{
            removeClickListener.onRemoveClick(alertWeatherData)
        }

        holder.binding.textViewStartTime.setTime(alertWeatherData.start)
        holder.binding.textViewStartDate.setDate(alertWeatherData.start)
        holder.binding.textViewEndDate.setDate(alertWeatherData.end)
        holder.binding.textViewEndTime.setTime(alertWeatherData.end)

    }



    object DiffUtils : DiffUtil.ItemCallback<AlertEntity>() {
        override fun areItemsTheSame(oldItem: AlertEntity, newItem: AlertEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AlertEntity, newItem: AlertEntity): Boolean {
            return oldItem == newItem
        }

    }

    class RemoveClickListener(val removeClickListener : (AlertEntity) -> Unit){
        fun onRemoveClick(alertEntity: AlertEntity) = removeClickListener(alertEntity)
    }
}