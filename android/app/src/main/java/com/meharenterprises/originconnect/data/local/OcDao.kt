package com.meharenterprises.originconnect.data.local
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OcConversationDao {
    @Query("SELECT * FROM oc_conversations ORDER BY lastMessageAt DESC")
    suspend fun getAll(): List<OcConversationEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<OcConversationEntity>)
    @Query("DELETE FROM oc_conversations WHERE id = :id")
    suspend fun delete(id: String)
}

@Dao
interface OcContactDao {
    @Query("SELECT * FROM oc_contacts ORDER BY COALESCE(localName, serverName) ASC")
    suspend fun getAll(): List<OcContactEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<OcContactEntity>)
}
