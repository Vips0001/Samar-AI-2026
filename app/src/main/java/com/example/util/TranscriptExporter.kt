package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.ChatSessionEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExportFormat(val extension: String, val mimeType: String, val label: String) {
    JSON("json", "application/json", "JSON Data Format (.json)"),
    MARKDOWN("md", "text/markdown", "Markdown Document (.md)"),
    PDF("pdf", "application/pdf", "PDF Document (.pdf)"),
    PLAIN_TEXT("txt", "text/plain", "Plain Text (.txt)")
}

object TranscriptExporter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    private val dateOnlyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun exportThread(
        context: Context,
        session: ChatSessionEntity,
        messages: List<ChatMessageEntity>,
        format: ExportFormat
    ): File {
        return when (format) {
            ExportFormat.JSON -> exportAsJson(context, session, messages)
            ExportFormat.MARKDOWN -> exportAsMarkdown(context, session, messages)
            ExportFormat.PDF -> exportAsPdf(context, session, messages)
            ExportFormat.PLAIN_TEXT -> exportAsPlainText(context, session, messages)
        }
    }

    fun exportAsJson(
        context: Context,
        session: ChatSessionEntity,
        messages: List<ChatMessageEntity>
    ): File {
        val fileName = "Samar_Thread_${session.id}_${System.currentTimeMillis()}.json"
        val file = File(context.cacheDir, fileName)

        val rootObj = JSONObject()
        rootObj.put("appName", "Samar AI")
        rootObj.put("exportTimestamp", System.currentTimeMillis())
        rootObj.put("exportDate", dateFormat.format(Date()))

        val sessionObj = JSONObject()
        sessionObj.put("sessionId", session.id)
        sessionObj.put("title", session.title)
        sessionObj.put("createdAt", dateFormat.format(Date(session.createdAt)))
        sessionObj.put("updatedAt", dateFormat.format(Date(session.updatedAt)))
        sessionObj.put("isPinned", session.isPinned)
        rootObj.put("session", sessionObj)

        val messagesArray = JSONArray()
        messages.forEach { msg ->
            val msgObj = JSONObject()
            msgObj.put("id", msg.id)
            msgObj.put("sender", if (msg.sender == "USER") "User" else "Samar AI")
            msgObj.put("role", msg.sender)
            msgObj.put("content", msg.content)
            msgObj.put("messageType", msg.messageType)
            msgObj.put("modelUsed", msg.modelUsed)
            msgObj.put("timestamp", msg.timestamp)
            msgObj.put("formattedTime", dateFormat.format(Date(msg.timestamp)))
            if (!msg.mediaFileName.isNullOrEmpty()) {
                msgObj.put("attachmentName", msg.mediaFileName)
                msgObj.put("attachmentMimeType", msg.mediaMimeType)
            }
            if (msg.promptTokens > 0 || msg.candidateTokens > 0) {
                val tokenObj = JSONObject()
                tokenObj.put("promptTokens", msg.promptTokens)
                tokenObj.put("responseTokens", msg.candidateTokens)
                msgObj.put("tokens", tokenObj)
            }
            messagesArray.put(msgObj)
        }
        rootObj.put("messagesCount", messages.size)
        rootObj.put("messages", messagesArray)

        FileOutputStream(file).use { out ->
            out.write(rootObj.toString(2).toByteArray())
        }

        return file
    }

    fun exportAsMarkdown(
        context: Context,
        session: ChatSessionEntity,
        messages: List<ChatMessageEntity>
    ): File {
        val fileName = "Samar_Thread_${session.id}_${System.currentTimeMillis()}.md"
        val file = File(context.cacheDir, fileName)

        val sb = StringBuilder()
        sb.append("# ${session.title}\n\n")
        sb.append("> **Exported by Samar AI** • ${dateFormat.format(Date())}\n")
        sb.append("> Total Messages: ${messages.size} | Session ID: #${session.id}\n\n")
        sb.append("---\n\n")

        messages.forEach { msg ->
            val isUser = msg.sender == "USER"
            val senderLabel = if (isUser) "👤 **You (User)**" else "✨ **Samar AI** (`${msg.modelUsed}`)"
            val time = dateFormat.format(Date(msg.timestamp))

            sb.append("### $senderLabel\n")
            sb.append("*$time*\n\n")

            if (!msg.mediaFileName.isNullOrEmpty()) {
                sb.append("📎 *Attached: ${msg.mediaFileName}*\n\n")
            }

            if (msg.messageType == "IMAGE_GEN") {
                sb.append("🎨 **AI Image Generation Prompt:**\n")
                sb.append("```\n${msg.content}\n```\n\n")
            } else if (msg.messageType == "VIDEO_GEN") {
                sb.append("🎬 **AI Video Generation Prompt:**\n")
                sb.append("```\n${msg.content}\n```\n\n")
            } else {
                sb.append("${msg.content}\n\n")
            }

            sb.append("---\n\n")
        }

        FileOutputStream(file).use { out ->
            out.write(sb.toString().toByteArray())
        }

        return file
    }

    fun exportAsPlainText(
        context: Context,
        session: ChatSessionEntity,
        messages: List<ChatMessageEntity>
    ): File {
        val fileName = "Samar_Thread_${session.id}_${System.currentTimeMillis()}.txt"
        val file = File(context.cacheDir, fileName)

        val sb = StringBuilder()
        sb.append("========================================\n")
        sb.append("SAMAR AI CHAT TRANSCRIPT\n")
        sb.append("Title: ${session.title}\n")
        sb.append("Date: ${dateFormat.format(Date(session.createdAt))}\n")
        sb.append("Rights & Powered by Samar AI\n")
        sb.append("========================================\n\n")

        messages.forEach { msg ->
            val senderLabel = if (msg.sender == "USER") "User" else "Samar AI"
            val time = dateFormat.format(Date(msg.timestamp))
            sb.append("[$time] $senderLabel:\n")
            sb.append("${msg.content}\n")
            if (msg.mediaFileName != null) {
                sb.append("[Attachment: ${msg.mediaFileName}]\n")
            }
            sb.append("\n----------------------------------------\n\n")
        }

        FileOutputStream(file).use { out ->
            out.write(sb.toString().toByteArray())
        }

        return file
    }

    fun exportAsPdf(
        context: Context,
        session: ChatSessionEntity,
        messages: List<ChatMessageEntity>
    ): File {
        val fileName = "Samar_Thread_${session.id}_${System.currentTimeMillis()}.pdf"
        val file = File(context.cacheDir, fileName)

        val pdfDocument = PdfDocument()
        val pageWidth = 595 // Standard A4 points
        val pageHeight = 842
        var pageNumber = 1

        val margin = 40f
        val contentWidth = pageWidth - (margin * 2)

        val headerPaint = Paint().apply {
            color = Color.rgb(30, 27, 75)
            isAntiAlias = true
        }

        val titlePaint = Paint().apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val subTitlePaint = Paint().apply {
            color = Color.rgb(200, 220, 255)
            textSize = 10f
            isAntiAlias = true
        }

        val userHeaderPaint = Paint().apply {
            color = Color.rgb(79, 70, 229)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val aiHeaderPaint = Paint().apply {
            color = Color.rgb(13, 148, 136)
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 10f
            isAntiAlias = true
        }

        val bgBubblePaintUser = Paint().apply {
            color = Color.rgb(241, 245, 249)
            isAntiAlias = true
        }

        val bgBubblePaintAi = Paint().apply {
            color = Color.rgb(240, 253, 250)
            isAntiAlias = true
        }

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        fun drawHeader(c: Canvas) {
            c.drawRect(0f, 0f, pageWidth.toFloat(), 70f, headerPaint)
            c.drawText("SAMAR AI • Chat Transcript", margin, 35f, titlePaint)
            c.drawText("Topic: ${session.title}  |  Exported: ${dateFormat.format(Date())}", margin, 55f, subTitlePaint)
        }

        drawHeader(canvas)
        var currentY = 90f

        messages.forEach { msg ->
            val isUser = msg.sender == "USER"
            val senderName = if (isUser) "You (User)" else "Samar AI"
            val time = dateFormat.format(Date(msg.timestamp))

            val lines = breakTextIntoLines(msg.content, textPaint, contentWidth - 20f)
            val bubbleHeight = (lines.size * 14f) + 36f

            if (currentY + bubbleHeight > pageHeight - 50f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                drawHeader(canvas)
                currentY = 90f
            }

            val bubblePaint = if (isUser) bgBubblePaintUser else bgBubblePaintAi
            canvas.drawRoundRect(
                margin,
                currentY,
                pageWidth - margin,
                currentY + bubbleHeight,
                8f,
                8f,
                bubblePaint
            )

            val headerColor = if (isUser) userHeaderPaint else aiHeaderPaint
            canvas.drawText("$senderName  •  $time", margin + 10f, currentY + 16f, headerColor)

            var lineY = currentY + 32f
            lines.forEach { line ->
                canvas.drawText(line, margin + 10f, lineY, textPaint)
                lineY += 14f
            }

            currentY += bubbleHeight + 12f
        }

        pdfDocument.finishPage(page)

        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()

        return file
    }

    private fun breakTextIntoLines(text: String, paint: Paint, maxWidth: Float): List<String> {
        val result = mutableListOf<String>()
        val paragraphs = text.split("\n")
        for (para in paragraphs) {
            if (para.isEmpty()) {
                result.add("")
                continue
            }
            val words = para.split(" ")
            var currentLine = ""
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                val measuredWidth = paint.measureText(testLine)
                if (measuredWidth > maxWidth) {
                    if (currentLine.isNotEmpty()) {
                        result.add(currentLine)
                    }
                    currentLine = word
                } else {
                    currentLine = testLine
                }
            }
            if (currentLine.isNotEmpty()) {
                result.add(currentLine)
            }
        }
        return result
    }

    fun shareFile(context: Context, file: File, mimeType: String, title: String) {
        val authority = "${context.packageName}.fileprovider"
        val contentUri: Uri = FileProvider.getUriForFile(context, authority, file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Share Samar Transcript via")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }
}
