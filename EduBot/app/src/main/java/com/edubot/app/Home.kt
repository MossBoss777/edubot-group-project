package com.edubot.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Home: Summary / Quiz / Plan tools run here; the Chat tile opens the separate chat page. */
@Composable
fun HomeScreen(go: (String) -> Unit) {
    val tiles = listOf(Triple("📝", "Summary", "summary"), Triple("❓", "Quiz", "quiz"), Triple("📅", "Plan", "study_plan"), Triple("💬", "Chat", "chat"))
    val hints = mapOf("summary" to "Paste notes or attach a PDF to summarise", "quiz" to "Paste material or attach a PDF for a quiz",
        "study_plan" to "Subject, exam date and daily study hours")
    var mode by remember { mutableStateOf("summary") }; var menu by remember { mutableStateOf(false) }
    Page {
        // Header: profile avatar, title, menu
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(Accent).border(1.dp, Color.White.copy(0.5f), CircleShape).clickable { go("profile") }, Alignment.Center) {
                Text(Api.meta.optString("first_name", "?").take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            Spacer(Modifier.weight(1f)); Text("EDU BOT", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 3.sp); Spacer(Modifier.weight(1f))
            Box { Text("☰", color = Color.White, fontSize = 28.sp, modifier = Modifier.clickable { menu = true })
                DropdownMenu(menu, { menu = false }) {
                    DropdownMenuItem({ Text("History") }, { menu = false; go("history") })
                    DropdownMenuItem({ Text("Modules & Grades") }, { menu = false; go("grades") })
                    DropdownMenuItem({ Text("Profile") }, { menu = false; go("profile") })
                    DropdownMenuItem({ Text("Sign out") }, { menu = false; Api.signOut(); go("logout") }) } }
        }
        Spacer(Modifier.height(14.dp))
        // Greeting card with the mascot
        Card { Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Hey ${Api.meta.optString("first_name", "there")} 👋", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("How may I assist you today?", color = Color.White.copy(0.85f), fontSize = 15.sp) }
            Logo(64.dp) } }
        // Tiles: the first three select a tool, "Chat" navigates to its own page
        Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            tiles.forEach { (icon, label, m) ->
                val sel = mode == m; val s = RoundedCornerShape(14.dp)
                Column(Modifier.weight(1f).clip(s).background(if (sel) Accent else Color.White.copy(0.10f))
                    .border(1.dp, Color.White.copy(if (sel) 0.7f else 0.2f), s).clickable { if (m == "chat") go("chat") else mode = m }.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(icon, fontSize = 20.sp); Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.Center) } } }
        Text(hints[mode]!!, color = Color.White.copy(0.7f), fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp, start = 4.dp))
        ChatArea(mode, "Message") {
            Card(Modifier.padding(top = 12.dp)) {
                Text("📅  Timetable", color = Color.White, fontWeight = FontWeight.SemiBold)
                Text("Your next class will be displayed here.", color = Color.White.copy(0.8f), fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp)) } }
    }
}
