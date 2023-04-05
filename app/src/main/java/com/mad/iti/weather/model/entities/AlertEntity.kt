package com.mad.iti.weather.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "AlertEntity")
data class AlertEntity(
    @PrimaryKey @ColumnInfo(name = "entryid") var id: String = UUID.randomUUID().toString(),
    val start: Long, val end: Long,
    val kind: String,
    val lon: Double,
    val lat: Double,
)

object AlertKind {
    const val NOTIFICATION = "NOTIFICATION"
    const val ALARM = "ALARM"
}
