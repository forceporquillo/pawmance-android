package dev.apes.pawmance.data.conversation

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.apes.pawmance.model.Conversations
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationsDao {

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  fun insertMessage(conversations: Conversations)

  @Query("SELECT * FROM conversations WHERE recipientId=:petId ORDER BY timestamp DESC")
  fun getConversation(petId: String): Flow<List<Conversations>>

  @Query("SELECT * FROM conversations ORDER BY timestamp DESC")
  fun getAllConversations(): Flow<List<Conversations>>
}