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
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.res.painterResource

@Composable
fun HomeScreen(go: (String) -> Unit) {
    val tiles = listOf(
        "Note Summary" to "summary",
        "Quiz Generation" to "quiz",
        "Study Plan" to "study_plan",
        "Log Grades" to "grades"
    )

    var mode by remember { mutableStateOf<String?>(null) }; var menu by remember { mutableStateOf(false) }
    Page {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(42.dp).clip(CircleShape).background(Accent).border(1.dp, Color.White.copy(0.5f), CircleShape).clickable { go("profile") }, Alignment.Center) {
                Text(Api.meta.optString("first_name", "?").take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp) }
            Spacer(Modifier.weight(1f)); Text("EDU BOT", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold, letterSpacing = 3.sp); Spacer(Modifier.weight(1f))
            Box {
                Icon(painter = painterResource(R.drawable.burger), contentDescription = "Menu", tint = Color.White, modifier = Modifier.size(28.dp).clickable { menu = true })
                DropdownMenu(
                    expanded = menu,
                    onDismissRequest = { menu = false },
                    modifier = Modifier
                        .background(Accent.copy(alpha = 0.95f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                    containerColor = Accent.copy(alpha = 0.95f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    DropdownMenuItem(
                        text = { Text("History", color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Medium) },
                        onClick = { menu = false; go("history") }
                    )
                    HorizontalDivider(color = Color.White.copy(alpha = 0.25f), modifier = Modifier.padding(vertical = 4.dp))
                    DropdownMenuItem(
                        text = { Text("Sign out", color = Color(0xFFFF0000), fontSize = 14.sp, fontWeight = FontWeight.Medium) },
                        onClick = { menu = false; Api.signOut(); go("logout") }
                    )
                }
            }
        }

        Spacer(Modifier.height(30.dp))

        Card { Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Hey ${Api.meta.optString("first_name", "there")},", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("How may I assist you today?", color = Color.White.copy(0.85f), fontSize = 15.sp) }
            Logo(64.dp) } }
       
        Spacer(Modifier.height(16.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            tiles.forEach { (label, m) ->
                val sel = mode == m
                val s = RoundedCornerShape(14.dp)

                Column(
                    modifier = Modifier
                        .width(100.dp)
                        .height(80.dp)
                        .clip(s)
                        .background(
                            if (sel) Accent
                            else Color.White.copy(alpha = 0.10f)
                        )
                        .border(
                            1.dp,
                            Color.White.copy(
                                alpha = if (sel) 0.7f else 0.2f
                            ),
                            s
                        )
                        .clickable { mode = m; go(m) }
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        label,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }

        Spacer(Modifier.height(30.dp))

        TimetableCard()

        Spacer(Modifier.weight(1f))

        AskEduBotButton(
            onClick = { go("chat") }
        )
    }
}
