package com.example.data.sync

import android.content.Context
import android.util.Log
import com.example.data.local.dao.ChatDao
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatSessionEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SyncStatus {
    IDLE,
    SYNCING,
    SUCCESS,
    ERROR
}

class FirestoreBackupManager(
    private val context: Context,
    private val chatDao: ChatDao
) {
    private val tag = "FirestoreBackupManager"
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus

    private val _lastSyncTime = MutableStateFlow<String?>("Never synced")
    val lastSyncTime: StateFlow<String?> = _lastSyncTime

    private val _statusMessage = MutableStateFlow("Ready for cloud backup")
    val statusMessage: StateFlow<String> = _statusMessage

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy h:mm a", Locale.getDefault())

    suspend fun performBackup(): Result<Int> = withContext(Dispatchers.IO) {
        _syncStatus.value = SyncStatus.SYNCING
        _statusMessage.value = "Connecting to Firebase Firestore..."

        try {
            // Fetch latest sessions from local Room
            val sessions = chatDao.getSessionById(1L)?.let { listOf(it) } ?: emptyList()
            // To get all sessions, we can collect or query
            val latestSession = chatDao.getLatestSession()

            var backedUpCount = 0
            if (latestSession != null) {
                val messages = chatDao.getMessagesList(latestSession.id)

                val backupData = hashMapOf(
                    "sessionId" to latestSession.id,
                    "title" to latestSession.title,
                    "createdAt" to latestSession.createdAt,
                    "updatedAt" to latestSession.updatedAt,
                    "backupTimestamp" to System.currentTimeMillis(),
                    "deviceModel" to android.os.Build.MODEL,
                    "messagesCount" to messages.size,
                    "messages" to messages.map { msg ->
                        hashMapOf(
                            "id" to msg.id,
                            "sender" to msg.sender,
                            "content" to msg.content,
                            "messageType" to msg.messageType,
                            "timestamp" to msg.timestamp,
                            "modelUsed" to msg.modelUsed
                        )
                    }
                )

                firestore.collection("samar_chat_backups")
                    .document("session_${latestSession.id}")
                    .set(backupData, SetOptions.merge())

                backedUpCount = messages.size
            }

            val timeStr = dateFormat.format(Date())
            _lastSyncTime.value = timeStr
            _syncStatus.value = SyncStatus.SUCCESS
            _statusMessage.value = "Backup successful • $backedUpCount messages synced"
            Result.success(backedUpCount)
        } catch (e: Exception) {
            Log.w(tag, "Firestore sync note: ${e.message}")
            _syncStatus.value = SyncStatus.ERROR
            _statusMessage.value = "Local offline storage active • ${e.localizedMessage ?: "Sync pending"}"
            Result.failure(e)
        }
    }
}
