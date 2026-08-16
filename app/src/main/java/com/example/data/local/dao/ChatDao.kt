package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatSessionEntity
import com.example.data.local.entity.TokenUsageEntity
import com.example.data.local.entity.VoiceLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    // --- Sessions ---
    @Query("""
        SELECT * FROM chat_sessions 
        WHERE isArchived = :isArchived 
        AND (:searchQuery = '' OR title LIKE '%' || :searchQuery || '%')
        ORDER BY isPinned DESC, updatedAt DESC
    """)
    fun getSessions(isArchived: Boolean, searchQuery: String = ""): Flow<List<ChatSessionEntity>>

    @Query("SELECT * FROM chat_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSessionById(sessionId: Long): ChatSessionEntity?

    @Query("SELECT * FROM chat_sessions ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLatestSession(): ChatSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSessionEntity): Long

    @Update
    suspend fun updateSession(session: ChatSessionEntity)

    @Query("UPDATE chat_sessions SET isPinned = :isPinned WHERE id = :sessionId")
    suspend fun setPinned(sessionId: Long, isPinned: Boolean)

    @Query("UPDATE chat_sessions SET isArchived = :isArchived WHERE id = :sessionId")
    suspend fun setArchived(sessionId: Long, isArchived: Boolean)

    @Query("UPDATE chat_sessions SET title = :newTitle, updatedAt = :updatedAt WHERE id = :sessionId")
    suspend fun updateTitle(sessionId: Long, newTitle: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSession(sessionId: Long)

    // --- Messages ---
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: Long): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getMessagesList(sessionId: Long): List<ChatMessageEntity>

    @Query("""
        SELECT * FROM chat_messages 
        WHERE content LIKE '%' || :query || '%' 
        ORDER BY timestamp DESC LIMIT 50
    """)
    fun searchMessages(query: String): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Update
    suspend fun updateMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: Long)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun clearMessagesForSession(sessionId: Long)

    @Query("SELECT COUNT(*) FROM chat_messages")
    fun getTotalMessageCount(): Flow<Int>

    // --- Token Usage Analytics ---
    @Query("SELECT * FROM token_usage_stats ORDER BY timestamp DESC LIMIT 30")
    fun getRecentTokenStats(): Flow<List<TokenUsageEntity>>

    @Query("SELECT * FROM token_usage_stats WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getTokenStatForDate(dateKey: String): TokenUsageEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTokenStat(stat: TokenUsageEntity)

    @Query("SELECT SUM(promptTokens) FROM token_usage_stats")
    fun getTotalPromptTokens(): Flow<Int?>

    @Query("SELECT SUM(responseTokens) FROM token_usage_stats")
    fun getTotalResponseTokens(): Flow<Int?>

    // --- Voice Transcript Logs ---
    @Query("SELECT * FROM voice_transcripts ORDER BY timestamp DESC")
    fun getAllVoiceLogs(): Flow<List<VoiceLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoiceLog(log: VoiceLogEntity): Long

    @Query("DELETE FROM voice_transcripts WHERE id = :logId")
    suspend fun deleteVoiceLog(logId: Long)

    @Query("DELETE FROM voice_transcripts")
    suspend fun clearAllVoiceLogs()
}
