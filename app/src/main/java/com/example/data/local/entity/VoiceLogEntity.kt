package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voice_transcripts")
data class VoiceLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val transcript: String,
    val timestamp: Long = System.currentTimeMillis(),
    val wordCount: Int = 0,
    val sessionId: Long? = null
)
