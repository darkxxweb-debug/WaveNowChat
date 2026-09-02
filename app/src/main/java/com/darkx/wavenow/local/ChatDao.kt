package com.darkx.wavenow.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(chats: List<ChatEntity>)

    @Query("SELECT * FROM chats ORDER BY lastMessageTime DESC")
    fun observeChats(): Flow<List<ChatEntity>>

    @Query("SELECT * FROM chats WHERE type = 'group'")
    fun observeGroups(): Flow<List<ChatEntity>>

    @Query("UPDATE chats SET isFavourite = :fav WHERE id = :chatId")
    suspend fun setFavourite(chatId: String, fav: Boolean)
}
