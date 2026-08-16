package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "token_usage_stats")
data class TokenUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateKey: String, // e.g. "2026-08-15" or day string
    val dayOfWeek: String, // "Mon", "Tue", etc.
    val timestamp: Long = System.currentTimeMillis(),
    val promptTokens: Int = 0,
    val responseTokens: Int = 0,
    val messageCount: Int = 1,
    val imageGenCount: Int = 0,
    val videoGenCount: Int = 0
)
