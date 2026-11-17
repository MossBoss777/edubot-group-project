package com.edubot.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun ModeScreen(mode: String, onBack: () -> Unit) {
    val (title, placeholder, welcomeMsg) = when (mode) {
        "summary" -> Triple(
            "Note Summary",
            "Paste notes summary...",
            "Hi ${Api.meta.optString("first_name", "there")}. Attach a PDF/txt file or paste your notes below, and I will create a structured summary with key terms for you."
        )
        "quiz" -> Triple(
            "Quiz Generation",
            "Notes for your quiz...",
            "Hi ${Api.meta.optString("first_name", "there")}, What topic or study material would you like a practice quiz on?"
        )
        "study_plan" -> Triple(
            "Study Plan",
            "Describe your exam and timeframe...",
            "Hi ${Api.meta.optString("first_name", "there")}, Tell me about your upcoming exams, subjects, and available time, and I will build a custom study plan for you."
        )
        else -> Triple(
            "Edu Bot Mode",
            "Type a message...",
            "Hi ${Api.meta.optString("first_name", "there")}! How can I help you today?"
        )
    }

    Page(scroll = false) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Image(
                painter = painterResource(R.drawable.cross),
                contentDescription = "Close mode",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable { onBack() }
            )
        }

        ChatArea(mode, placeholder) {
            BotBubble(welcomeMsg)
        }
    }
}

@Composable fun SummaryScreen(onBack: () -> Unit) = ModeScreen("summary", onBack)
@Composable fun QuizScreen(onBack: () -> Unit) = ModeScreen("quiz", onBack)
@Composable fun StudyPlanScreen(onBack: () -> Unit) = ModeScreen("study_plan", onBack)
