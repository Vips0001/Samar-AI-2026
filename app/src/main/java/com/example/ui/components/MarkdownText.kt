package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.HapticsHelper

@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val context = LocalContext.current
    val elements = parseMarkdown(markdown)

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        elements.forEach { element ->
            when (element) {
                is MarkdownElement.Header -> {
                    val fontSize = when (element.level) {
                        1 -> 20.sp
                        2 -> 18.sp
                        else -> 16.sp
                    }
                    Text(
                        text = buildAnnotatedInlineText(element.text, textColor),
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }

                is MarkdownElement.CodeBlock -> {
                    CodeBlockCard(
                        language = element.language,
                        code = element.code,
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Code", element.code))
                            HapticsHelper.performClick(context = context)
                            Toast.makeText(context, "Code copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                is MarkdownElement.BulletItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "• ",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Text(
                            text = buildAnnotatedInlineText(element.text, textColor),
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                }

                is MarkdownElement.NumberedItem -> {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 6.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${element.number}. ",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = buildAnnotatedInlineText(element.text, textColor),
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor
                        )
                    }
                }

                is MarkdownElement.BlockQuote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(24.dp)
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = buildAnnotatedInlineText(element.text, textColor),
                            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
                            color = textColor.copy(alpha = 0.9f)
                        )
                    }
                }

                is MarkdownElement.Divider -> {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                }

                is MarkdownElement.Paragraph -> {
                    Text(
                        text = buildAnnotatedInlineText(element.text, textColor),
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun CodeBlockCard(
    language: String,
    code: String,
    onCopy: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F172A))
            .border(1.dp, Color(0xFF334155), RoundedCornerShape(10.dp))
    ) {
        // Code Block Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1E293B))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = language.ifBlank { "code" }.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF94A3B8),
                fontWeight = FontWeight.SemiBold
            )
            IconButton(
                onClick = onCopy,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy code",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Code Content
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(12.dp)
        ) {
            Text(
                text = code,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.5.sp,
                color = Color(0xFFE2E8F0),
                lineHeight = 18.sp
            )
        }
    }
}

private sealed class MarkdownElement {
    data class Header(val level: Int, val text: String) : MarkdownElement()
    data class CodeBlock(val language: String, val code: String) : MarkdownElement()
    data class BulletItem(val text: String) : MarkdownElement()
    data class NumberedItem(val number: String, val text: String) : MarkdownElement()
    data class BlockQuote(val text: String) : MarkdownElement()
    object Divider : MarkdownElement()
    data class Paragraph(val text: String) : MarkdownElement()
}

private fun parseMarkdown(text: String): List<MarkdownElement> {
    val elements = mutableListOf<MarkdownElement>()
    val lines = text.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // Code block
        if (line.trimStart().startsWith("```")) {
            val language = line.trimStart().removePrefix("```").trim()
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            elements.add(MarkdownElement.CodeBlock(language, codeLines.joinToString("\n")))
            i++
            continue
        }

        // Headers
        if (line.startsWith("# ")) {
            elements.add(MarkdownElement.Header(1, line.removePrefix("# ").trim()))
            i++
            continue
        } else if (line.startsWith("## ")) {
            elements.add(MarkdownElement.Header(2, line.removePrefix("## ").trim()))
            i++
            continue
        } else if (line.startsWith("### ")) {
            elements.add(MarkdownElement.Header(3, line.removePrefix("### ").trim()))
            i++
            continue
        }

        // Divider
        if (line.trim() == "---" || line.trim() == "***" || line.trim() == "___") {
            elements.add(MarkdownElement.Divider)
            i++
            continue
        }

        // Blockquote
        if (line.startsWith("> ")) {
            elements.add(MarkdownElement.BlockQuote(line.removePrefix("> ").trim()))
            i++
            continue
        }

        // Bullet point
        if (line.trimStart().startsWith("* ") || line.trimStart().startsWith("- ") || line.trimStart().startsWith("• ")) {
            val clean = line.trimStart().replace(Regex("^[\\*\\-•]\\s+"), "")
            elements.add(MarkdownElement.BulletItem(clean))
            i++
            continue
        }

        // Numbered list
        val numberedMatch = Regex("^(\\d+)\\.\\s+(.*)").find(line.trimStart())
        if (numberedMatch != null) {
            val num = numberedMatch.groupValues[1]
            val content = numberedMatch.groupValues[2]
            elements.add(MarkdownElement.NumberedItem(num, content))
            i++
            continue
        }

        // Normal paragraph
        if (line.isNotBlank()) {
            elements.add(MarkdownElement.Paragraph(line))
        }

        i++
    }

    return elements
}

private fun buildAnnotatedInlineText(text: String, defaultColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var currentIndex = 0
        // Parse bold **text**, italic *text*, inline `code`
        val regex = Regex("(\\*{2}(.+?)\\*{2})|(\\*(.+?)\\*)|(`(.+?)`)")
        val matches = regex.findAll(text)

        matches.forEach { matchResult ->
            val range = matchResult.range
            if (range.first > currentIndex) {
                append(text.substring(currentIndex, range.first))
            }

            val fullMatch = matchResult.value
            when {
                fullMatch.startsWith("**") && fullMatch.endsWith("**") -> {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = defaultColor)) {
                        append(fullMatch.removeSurrounding("**"))
                    }
                }
                fullMatch.startsWith("`") && fullMatch.endsWith("`") -> {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color(0x33888888),
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    ) {
                        append(" ${fullMatch.removeSurrounding("`")} ")
                    }
                }
                fullMatch.startsWith("*") && fullMatch.endsWith("*") -> {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic, color = defaultColor)) {
                        append(fullMatch.removeSurrounding("*"))
                    }
                }
                else -> {
                    append(fullMatch)
                }
            }
            currentIndex = range.last + 1
        }

        if (currentIndex < text.length) {
            append(text.substring(currentIndex))
        }
    }
}
