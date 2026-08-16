package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.SamarApplication
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatSessionEntity
import com.example.data.local.entity.TokenUsageEntity
import com.example.data.local.entity.VoiceLogEntity
import com.example.data.repository.ChatRepository
import com.example.data.sync.FirestoreBackupManager
import com.example.ui.components.SamarPersonas
import com.example.ui.theme.AppThemeMode
import com.example.ui.theme.ChatFontSize
import com.example.util.ExportFormat
import com.example.util.HapticsHelper
import com.example.util.MediaHelper
import com.example.util.SpeechToTextManager
import com.example.util.TextToSpeechManager
import com.example.util.TranscriptExporter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChatRepository = (application as SamarApplication).repository
    val speechManager = SpeechToTextManager(application)
    val ttsManager = TextToSpeechManager(application)
    val firestoreBackupManager = FirestoreBackupManager(
        application,
        (application as SamarApplication).database.chatDao()
    )

    // UI Navigation & Tab States
    val isArchivedTab = MutableStateFlow(false)
    val searchQuery = MutableStateFlow("")

    // Active Session
    val currentSessionId = MutableStateFlow<Long?>(null)
    val currentSession = MutableStateFlow<ChatSessionEntity?>(null)

    // Input States
    val inputPrompt = MutableStateFlow("")
    val selectedAttachmentUri = MutableStateFlow<Uri?>(null)
    val selectedAttachmentName = MutableStateFlow<String?>(null)
    val selectedAttachmentMime = MutableStateFlow<String?>(null)
    val activeAiMode = MutableStateFlow("TEXT") // "TEXT", "IMAGE_GEN", "VIDEO_GEN"

    val isGenerating = MutableStateFlow(false)
    val errorMessage = MutableStateFlow<String?>(null)

    // Settings & Theme
    val themeMode = MutableStateFlow(AppThemeMode.SAMAR_BRANDED)
    val customAccentColor = MutableStateFlow(Color(0xFF00F2FE))
    val fontSize = MutableStateFlow(ChatFontSize.MEDIUM)
    val customApiKey = MutableStateFlow("")
    val globalAvatarId = MutableStateFlow("samar_default")

    // Reactive Sessions list based on search and archive filter
    @OptIn(ExperimentalCoroutinesApi::class)
    val sessions: StateFlow<List<ChatSessionEntity>> = combine(
        isArchivedTab,
        searchQuery
    ) { archived, query ->
        Pair(archived, query)
    }.flatMapLatest { (archived, query) ->
        repository.getSessions(isArchived = archived, searchQuery = query)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Reactive Messages for current active session
    @OptIn(ExperimentalCoroutinesApi::class)
    val messages: StateFlow<List<ChatMessageEntity>> = currentSessionId
        .flatMapLatest { sessionId ->
            if (sessionId != null) {
                repository.getMessages(sessionId)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Voice Transcripts Log
    val voiceLogs: StateFlow<List<VoiceLogEntity>> = repository.getAllVoiceLogs()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Analytics / Token usage
    val recentTokenStats: StateFlow<List<TokenUsageEntity>> = repository.getRecentTokenStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalMessages: StateFlow<Int> = repository.getTotalMessageCount()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val totalPromptTokens: StateFlow<Int?> = repository.getTotalPromptTokens()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    val totalResponseTokens: StateFlow<Int?> = repository.getTotalResponseTokens()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    init {
        // Initialize with default session if none exists
        viewModelScope.launch {
            val existing = repository.getSessionById(1)
            if (existing != null) {
                currentSessionId.value = existing.id
                currentSession.value = existing
            } else {
                createNewSession("Welcome to Samar AI")
            }
        }
    }

    fun createNewSession(title: String = "New Conversation") {
        viewModelScope.launch {
            val persona = SamarPersonas.getPersona(globalAvatarId.value)
            val newId = repository.createNewSession(
                title = title,
                persona = persona.name,
                avatarId = globalAvatarId.value
            )
            currentSessionId.value = newId
            currentSession.value = repository.getSessionById(newId)
            inputPrompt.value = ""
            clearAttachment()

            // Insert friendly greeting message from Samar
            repository.insertMessage(
                ChatMessageEntity(
                    sessionId = newId,
                    sender = "AI",
                    content = "Hello! I am **Samar**, your limitless modern AI companion. 🚀\n\nI can assist you with:\n* 💡 Intelligent problem-solving, analysis and coding\n* 🎨 Limitless **Samar Visual Studio** image creation\n* 🎬 Cinematic **Samar Motion** video simulation\n* 🎙️ **Voice-to-Text** queries\n* 📎 Multimodal photo, document & file analysis\n\nHow can I help you today?",
                    messageType = "TEXT",
                    modelUsed = "Samar 3.5 Turbo"
                )
            )
        }
    }

    fun selectSession(session: ChatSessionEntity) {
        currentSessionId.value = session.id
        currentSession.value = session
        inputPrompt.value = ""
        clearAttachment()
        ttsManager.stop()
        HapticsHelper.performClick(context = getApplication())
    }

    fun togglePinSession(session: ChatSessionEntity) {
        viewModelScope.launch {
            repository.togglePinSession(session.id, !session.isPinned)
            HapticsHelper.performClick(context = getApplication())
        }
    }

    fun toggleArchiveSession(session: ChatSessionEntity) {
        viewModelScope.launch {
            repository.toggleArchiveSession(session.id, !session.isArchived)
            HapticsHelper.performClick(context = getApplication())
        }
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (currentSessionId.value == sessionId) {
                val latest = repository.getLatestSession()
                if (latest != null) {
                    selectSession(latest)
                } else {
                    createNewSession()
                }
            }
            HapticsHelper.performClick(context = getApplication())
        }
    }

    fun renameSession(sessionId: Long, newTitle: String) {
        viewModelScope.launch {
            repository.updateSessionTitle(sessionId, newTitle)
            if (currentSessionId.value == sessionId) {
                currentSession.value = repository.getSessionById(sessionId)
            }
            HapticsHelper.performClick(context = getApplication())
        }
    }

    fun setAttachment(uri: Uri) {
        selectedAttachmentUri.value = uri
        selectedAttachmentName.value = MediaHelper.getFileName(getApplication(), uri)
        selectedAttachmentMime.value = getApplication<Application>().contentResolver.getType(uri) ?: "image/jpeg"
        HapticsHelper.performClick(context = getApplication())
    }

    fun clearAttachment() {
        selectedAttachmentUri.value = null
        selectedAttachmentName.value = null
        selectedAttachmentMime.value = null
    }

    fun toggleSpeechRecognition() {
        HapticsHelper.performClick(context = getApplication())
        if (speechManager.isListening.value) {
            speechManager.stopListening()
        } else {
            speechManager.startListening { resultText ->
                if (resultText.isNotBlank()) {
                    inputPrompt.value = if (inputPrompt.value.isBlank()) resultText else "${inputPrompt.value} $resultText"
                    // Save to voice transcript logs
                    viewModelScope.launch {
                        repository.saveVoiceLog(resultText, currentSessionId.value)
                    }
                }
            }
        }
    }

    fun deleteVoiceLog(logId: Long) {
        viewModelScope.launch {
            repository.deleteVoiceLog(logId)
            HapticsHelper.performClick(context = getApplication())
        }
    }

    fun clearAllVoiceLogs() {
        viewModelScope.launch {
            repository.clearAllVoiceLogs()
            HapticsHelper.performClick(context = getApplication())
        }
    }

    fun speakAiMessage(messageId: Long, text: String) {
        ttsManager.speak(messageId, text)
        HapticsHelper.performClick(context = getApplication())
    }

    fun stopSpeaking() {
        ttsManager.stop()
    }

    fun triggerCloudBackup() {
        viewModelScope.launch {
            firestoreBackupManager.performBackup()
            HapticsHelper.performClick(context = getApplication())
        }
    }

    fun sendCurrentPrompt() {
        val prompt = inputPrompt.value.trim()
        val attachmentUri = selectedAttachmentUri.value
        val attachmentName = selectedAttachmentName.value
        val attachmentMime = selectedAttachmentMime.value
        val mode = activeAiMode.value

        if (prompt.isEmpty() && attachmentUri == null) return

        val sessionId = currentSessionId.value ?: return

        HapticsHelper.performSuccess(context = getApplication())
        inputPrompt.value = ""
        clearAttachment()
        activeAiMode.value = "TEXT"

        viewModelScope.launch {
            isGenerating.value = true

            when (mode) {
                "IMAGE_GEN" -> {
                    // 1. User Image Prompt Message
                    repository.insertMessage(
                        ChatMessageEntity(
                            sessionId = sessionId,
                            sender = "USER",
                            content = prompt,
                            messageType = "IMAGE_GEN"
                        )
                    )

                    // 2. AI Generating Placeholder
                    val aiMsgId = repository.insertMessage(
                        ChatMessageEntity(
                            sessionId = sessionId,
                            sender = "AI",
                            content = prompt,
                            messageType = "IMAGE_GEN",
                            isGenerating = true,
                            modelUsed = "Samar Visual Studio"
                        )
                    )

                    // 3. Generate Image Call
                    val result = repository.generateAiImage(
                        prompt = prompt,
                        customApiKey = customApiKey.value.takeIf { it.isNotBlank() }
                    )

                    result.onSuccess { imagePath ->
                        repository.updateMessage(
                            ChatMessageEntity(
                                id = aiMsgId,
                                sessionId = sessionId,
                                sender = "AI",
                                content = prompt,
                                messageType = "IMAGE_GEN",
                                mediaUri = imagePath,
                                isGenerating = false,
                                modelUsed = "Samar Visual Studio"
                            )
                        )
                        HapticsHelper.performSuccess(context = getApplication())
                    }.onFailure { error ->
                        repository.updateMessage(
                            ChatMessageEntity(
                                id = aiMsgId,
                                sessionId = sessionId,
                                sender = "AI",
                                content = "Image Generation Failed: ${error.localizedMessage}",
                                messageType = "TEXT",
                                isGenerating = false
                            )
                        )
                    }
                }

                "VIDEO_GEN" -> {
                    // 1. User Video Prompt Message
                    repository.insertMessage(
                        ChatMessageEntity(
                            sessionId = sessionId,
                            sender = "USER",
                            content = prompt,
                            messageType = "VIDEO_GEN"
                        )
                    )

                    // 2. AI Video Generating Placeholder
                    val aiMsgId = repository.insertMessage(
                        ChatMessageEntity(
                            sessionId = sessionId,
                            sender = "AI",
                            content = prompt,
                            messageType = "VIDEO_GEN",
                            isGenerating = true,
                            generationStatus = "Rendering cinematic frames with Samar Motion...",
                            modelUsed = "Samar Motion 1080p"
                        )
                    )

                    // 3. Generate Video Simulation & Storyboard
                    val result = repository.generateAiVideo(
                        prompt = prompt,
                        customApiKey = customApiKey.value.takeIf { it.isNotBlank() }
                    )

                    result.onSuccess { desc ->
                        repository.updateMessage(
                            ChatMessageEntity(
                                id = aiMsgId,
                                sessionId = sessionId,
                                sender = "AI",
                                content = prompt,
                                messageType = "VIDEO_GEN",
                                isGenerating = false,
                                generationStatus = desc,
                                modelUsed = "Samar Motion 1080p"
                            )
                        )
                        HapticsHelper.performSuccess(context = getApplication())
                    }.onFailure { error ->
                        repository.updateMessage(
                            ChatMessageEntity(
                                id = aiMsgId,
                                sessionId = sessionId,
                                sender = "AI",
                                content = "Video Rendering Notice: ${error.localizedMessage}",
                                messageType = "TEXT",
                                isGenerating = false
                            )
                        )
                    }
                }

                else -> {
                    // Standard / Multimodal Text Message
                    var base64Data: String? = null
                    var mimeType: String? = null

                    if (attachmentUri != null) {
                        val encoded = MediaHelper.uriToBase64(getApplication(), attachmentUri)
                        if (encoded != null) {
                            mimeType = encoded.first
                            base64Data = encoded.second
                        }
                    }

                    // 1. Insert User Message
                    repository.insertMessage(
                        ChatMessageEntity(
                            sessionId = sessionId,
                            sender = "USER",
                            content = prompt,
                            messageType = "TEXT",
                            mediaUri = attachmentUri?.toString(),
                            mediaFileName = attachmentName,
                            mediaMimeType = attachmentMime
                        )
                    )

                    // 2. Insert AI Generating State Message
                    val aiMsgId = repository.insertMessage(
                        ChatMessageEntity(
                            sessionId = sessionId,
                            sender = "AI",
                            content = "",
                            isGenerating = true,
                            modelUsed = "Samar 3.5 Turbo"
                        )
                    )

                    val persona = SamarPersonas.getPersona(globalAvatarId.value)

                    // 3. Send to Gemini API (Samar Model)
                    val result = repository.sendChatMessage(
                        sessionId = sessionId,
                        userPrompt = prompt,
                        attachmentBase64 = base64Data,
                        attachmentMimeType = mimeType,
                        customApiKey = customApiKey.value.takeIf { it.isNotBlank() },
                        systemInstructionText = persona.systemPrompt
                    )

                    result.onSuccess { reply ->
                        repository.updateMessage(
                            ChatMessageEntity(
                                id = aiMsgId,
                                sessionId = sessionId,
                                sender = "AI",
                                content = reply,
                                isGenerating = false,
                                modelUsed = "Samar 3.5 Turbo"
                            )
                        )
                        HapticsHelper.performSuccess(context = getApplication())

                        // Auto-generate title if this is the first exchange
                        val msgCount = repository.getMessagesList(sessionId).size
                        if (msgCount <= 3) {
                            val autoTitle = repository.generateSessionTitle(prompt, customApiKey.value.takeIf { it.isNotBlank() })
                            repository.updateSessionTitle(sessionId, autoTitle)
                            currentSession.value = repository.getSessionById(sessionId)
                        }
                    }.onFailure { error ->
                        repository.updateMessage(
                            ChatMessageEntity(
                                id = aiMsgId,
                                sessionId = sessionId,
                                sender = "AI",
                                content = "Error: ${error.localizedMessage ?: "Failed to generate response. Please check your network and API key."}",
                                isGenerating = false
                            )
                        )
                    }
                }
            }

            isGenerating.value = false
        }
    }

    fun exportThreadInFormat(format: ExportFormat) {
        val session = currentSession.value ?: return
        viewModelScope.launch {
            val msgs = repository.getMessagesList(session.id)
            if (msgs.isEmpty()) {
                Toast.makeText(getApplication(), "No messages to export", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val exportedFile = TranscriptExporter.exportThread(getApplication(), session, msgs, format)
            TranscriptExporter.shareFile(
                context = getApplication(),
                file = exportedFile,
                mimeType = format.mimeType,
                title = "Samar AI Transcript - ${session.title}"
            )
            HapticsHelper.performClick(context = getApplication())
        }
    }

    fun setTheme(mode: AppThemeMode) {
        themeMode.value = mode
        HapticsHelper.performClick(context = getApplication())
    }

    fun setCustomAccentColor(color: Color) {
        customAccentColor.value = color
        HapticsHelper.performClick(context = getApplication())
    }

    fun setChatFontSize(size: ChatFontSize) {
        fontSize.value = size
        HapticsHelper.performClick(context = getApplication())
    }

    fun setAvatar(avatarId: String) {
        globalAvatarId.value = avatarId
        HapticsHelper.performClick(context = getApplication())
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.stopListening()
        ttsManager.shutdown()
    }
}
