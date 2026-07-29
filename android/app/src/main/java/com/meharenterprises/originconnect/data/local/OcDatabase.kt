package com.meharenterprises.originconnect.data.local
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [OcConversationEntity::class, OcContactEntity::class],
    version = 1,
    exportSchema = false
)
abstract class OcDatabase : RoomDatabase() {
    abstract fun conversationDao(): OcConversationDao
    abstract fun contactDao(): OcContactDao
}
