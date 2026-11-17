package com.edubot.app

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val privacyText = """
At EduBot, we respect your privacy and are committed to protecting the personal information you provide when using our application.

1. Information We Collect
- Full Name.
- Email Address.
- Year of study.
- Timetable information entered by user.
- Information needed to generate quizzes, summaries, and study plans.

2. How We Use Your Information
Information collected is used to:
- Identify and track users.
- Personalise the application experience.
- Power AI study assistance and manage your academic schedule.

3. What to Avoid Sharing
- Do not upload sensitive, personal, or confidential documents.

4. Our Promise
- To ensure the security and privacy of your information.
- To not sell user information to third parties.
- To respect user privacy rights.

By registering and using EduBot, you acknowledge that you have read and understood this Privacy Policy.
""".trimIndent()

private val aboutUsText = """
Welcome to Edu Bot!

Edu Bot is your personal, AI-powered mobile study assistant designed to empower students throughout their academic journey.

Key Features:
- Smart AI Chat: Instant help with concepts, assignments, and study materials.
- Automated Summaries & Quizzes: Upload your lecture notes or PDFs to create quick summaries and practice quizzes.
- Timetable Management: Organise your class schedule and never miss an upcoming lecture or room location.
- Academic Tracking: Log grades, monitor your performance, and keep tabs on study goals.

Our Mission:
We aim to make learning more accessible, interactive, and structured for every student through cutting-edge mobile AI tools.

Version 1.0.0
Created by EduBot Team: Mohammad Saleh, Lesedi Matabologa, Moeen Butt, Phemelo Padisho for students everywhere.
""".trimIndent()

@Composable
private fun InfoDialog(
    title: String,
    content: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = content,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Accent, fontWeight = FontWeight.SemiBold)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Navy,
        titleContentColor = Color.White,
        textContentColor = Color.White.copy(0.9f)
    )
}

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onHome: () -> Unit
) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    var settings by remember { mutableStateOf(false) }
    var dlg by remember { mutableStateOf("") }
    var showPrivacy by remember { mutableStateOf(false) }
    var showAboutUs by remember { mutableStateOf(false) }
    var f by remember { mutableStateOf(Api.meta.optString("first_name")) }
    var l by remember { mutableStateOf(Api.meta.optString("last_name")) }
    var y by remember { mutableStateOf(Api.meta.optString("year")) }

    fun toast(m: String) =
        Toast.makeText(ctx, m, Toast.LENGTH_LONG).show()

    Page(bg = Deep, scroll = true) {
        if (!settings) {
            // Header: Profile title and close icon
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Profile",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Image(
                    painter = painterResource(R.drawable.cross),
                    contentDescription = "Close profile",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable {
                            onHome()
                        }
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4B98DE))
                            .border(
                                2.dp,
                                Color.White.copy(alpha = 0.65f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = f.trim().firstOrNull()
                                ?.uppercaseChar()?.toString() ?: "?",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "$f $l".trim().ifBlank { "Student" },
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Year: $y",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            GlassButton("Settings") {
                settings = true
            }

            Spacer(Modifier.height(20.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Title("Support And Information")

                    val supportItems = listOf(
                        "Feedback",
                        "Rate Application",
                        "About Us",
                        "Privacy Policy"
                    )

                    supportItems.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { item ->
                                Box(Modifier.weight(1f)) {
                                    GlassButton(
                                        text = item,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        when (item) {
                                            "Privacy Policy" -> showPrivacy = true
                                            "About Us" -> showAboutUs = true
                                            else -> toast("$item: coming soon")
                                        }
                                    }
                                }
                            }

                            if (rowItems.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        } else {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Settings",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Image(
                    painter = painterResource(R.drawable.back),
                    contentDescription = "Back to Profile",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable {
                            settings = false
                        }
                        .padding(8.dp)
                )
            }

            GlassButton("Reset password", Modifier.padding(vertical = 6.dp)) {
                scope.launch {
                    runCatching {
                        Api.resetPassword(Api.user!!.optString("email"))
                    }.onSuccess {
                        toast("Reset link sent to your email")
                    }.onFailure {
                        toast(it.message ?: "Error")
                    }
                }
            }

            GlassButton("Update Information", Modifier.padding(vertical = 6.dp)) {
                dlg = "update"
            }

            GlassButton("Account deletion", Modifier.padding(vertical = 6.dp)) {
                dlg = "delete"
            }
        }

        Spacer(Modifier.weight(1f))

        GlassButton(
            text = "Sign Out",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Api.signOut()
            onLogout()
        }
    }

    if (showPrivacy) {
        InfoDialog(
            title = "Privacy Policy",
            content = privacyText,
            onDismiss = { showPrivacy = false }
        )
    }

    if (showAboutUs) {
        InfoDialog(
            title = "About Us",
            content = aboutUsText,
            onDismiss = { showAboutUs = false }
        )
    }

    if (dlg == "update") {
        AlertDialog(
            onDismissRequest = { dlg = "" },
            title = { Text("Update information") },
            text = {
                Column {
                    OutlinedTextField(
                        value = f,
                        onValueChange = { f = it },
                        label = { Text("First name") }
                    )
                    OutlinedTextField(
                        value = l,
                        onValueChange = { l = it },
                        label = { Text("Last name") }
                    )
                    OutlinedTextField(
                        value = y,
                        onValueChange = {
                            y = it.filter(Char::isDigit).take(1)
                        },
                        label = { Text("Year of study") }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (f.isBlank() || l.isBlank() || y.isBlank()) {
                            toast("All fields are required")
                        } else {
                            scope.launch {
                                runCatching {
                                    Api.updateInfo(f.trim(), l.trim(), y)
                                }.onSuccess {
                                    toast("Updated")
                                    dlg = ""
                                }.onFailure {
                                    toast(it.message ?: "Error")
                                }
                            }
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { dlg = "" }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (dlg == "delete") {
        AlertDialog(
            onDismissRequest = { dlg = "" },
            title = { Text("Delete account?") },
            text = {
                Text("This permanently deletes your account and all your data.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            runCatching {
                                Api.deleteAccount()
                            }.onSuccess {
                                onLogout()
                            }.onFailure {
                                toast(it.message ?: "Error")
                            }
                        }
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { dlg = "" }) {
                    Text("Cancel")
                }
            }
        )
    }
}
