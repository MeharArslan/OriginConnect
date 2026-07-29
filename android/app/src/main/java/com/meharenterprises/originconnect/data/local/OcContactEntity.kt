package com.meharenterprises.originconnect.data.local
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "oc_contacts")
data class OcContactEntity(
    @PrimaryKey val userId: String,
    val phone: String,
    val serverName: String,
    val localName: String?,
    val photoUrl: String?,
    val about: String?
)
