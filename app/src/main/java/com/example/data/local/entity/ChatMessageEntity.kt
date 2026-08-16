package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = ChatSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: Long,
    val sender: String, // "USER" or "AI" or "SYSTEM"
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val messageType: String = "TEXT", // "TEXT", "IMAGE_GEN", "VIDEO_GEN", "MULTIMODAL", "FILE"
    val mediaUri: String? = null,
    val mediaMimeType: String? = null,
    val mediaFileName: String? = null,
    val promptTokens: Int = 0,
    val candidateTokens: Int = 0,
    val modelUsed: String = "Samar 3.5 Turbo",
    val isGenerating: Boolean = false,
    val generationStatus: String? = null // e.g. "Generating video frames...", "Completed", "Failed"
)
