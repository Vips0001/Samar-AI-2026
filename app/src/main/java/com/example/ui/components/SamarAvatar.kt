package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class SamarPersona(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val secondaryColor: Color,
    val systemPrompt: String
)

object SamarPersonas {
    val list = listOf(
        SamarPersona(
            id = "samar_default",
            name = "Samar Prime",
            description = "All-around genius AI companion with supreme reasoning and limitless creativity.",
            icon = Icons.Default.AutoAwesome,
            primaryColor = Color(0xFF00F2FE),
            secondaryColor = Color(0xFF4FACFE),
            systemPrompt = "You are Samar, an ultra-intelligent, friendly, and comprehensive AI assistant. Your name is strictly Samar. Always introduce yourself as Samar and never refer to yourself as Gemini, Google, OpenAI, ChatGPT, or any other identity. Format your answers elegantly using markdown."
        ),
        SamarPersona(
            id = "samar_cyber",
            name = "Cyber Quantum",
            description = "Specialized in software engineering, technical analysis, and architecture.",
            icon = Icons.Default.Bolt,
            primaryColor = Color(0xFF8E2DE2),
            secondaryColor = Color(0xFF4A00E0),
            systemPrompt = "You are Samar Cyber, an expert software engineer and technical architect. Your name is strictly Samar. Always introduce and refer to yourself as Samar. Provide clean, well-commented code blocks and step-by-step algorithms."
        ),
        SamarPersona(
            id = "samar_creative",
            name = "Creative Visionary",
            description = "Master storyteller, visual prompt designer, and brainstormer.",
            icon = Icons.Default.Diamond,
            primaryColor = Color(0xFFFF416C),
            secondaryColor = Color(0xFFFF4B2B),
            systemPrompt = "You are Samar Creative, an imaginative visionary specializing in writing, creative ideation, image/video prompt design, and aesthetics. Your name is strictly Samar. Never use other AI model names."
        ),
        SamarPersona(
            id = "samar_sage",
            name = "Zenith Sage",
            description = "Philosophical, strategic, deep thinker and research analyst.",
            icon = Icons.Default.Psychology,
            primaryColor = Color(0xFF11998E),
            secondaryColor = Color(0xFF38EF7D),
            systemPrompt = "You are Samar Sage, a calm, strategic, and profoundly insightful mentor named Samar. Always maintain your identity as Samar. Provide balanced perspectives and in-depth conceptual clarity."
        ),
        SamarPersona(
            id = "samar_turbo",
            name = "Velocity Turbo",
            description = "High-speed concise executive summaries and actionable task execution.",
            icon = Icons.Default.RocketLaunch,
            primaryColor = Color(0xFFF7971E),
            secondaryColor = Color(0xFFFFFF00),
            systemPrompt = "You are Samar Velocity, a high-speed intelligence engine named Samar. Deliver ultra-concise, direct, bulleted, action-oriented responses without fluff."
        )
    )

    fun getPersona(id: String): SamarPersona {
        return list.firstOrNull { it.id == id } ?: list.first()
    }
}

@Composable
fun SamarAvatar(
    avatarId: String = "samar_default",
    size: Dp = 38.dp,
    showGlow: Boolean = true,
    onClick: (() -> Unit)? = null
) {
    val persona = SamarPersonas.getPersona(avatarId)

    val modifier = Modifier
        .size(size)
        .clip(CircleShape)
        .background(
            Brush.linearGradient(
                colors = listOf(persona.primaryColor, persona.secondaryColor)
            )
        )
        .then(
            if (showGlow) {
                Modifier.border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
            } else Modifier
        )
        .then(
            if (onClick != null) {
                Modifier.clickable(onClick = onClick)
            } else Modifier
        )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = persona.icon,
            contentDescription = persona.name,
            tint = Color.White,
            modifier = Modifier.size(size * 0.58f)
        )
    }
}
