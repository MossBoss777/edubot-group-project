package com.edubot.app

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Reusable conversation area: message bubbles, optional PDF attachment and the message bar.
 * [mode] is the AI task (summary | quiz | study_plan | chat). Every exchange is saved to `sessions`.
 * [emptyContent] is shown while there are no messages yet.
 */
@Composable
fun ColumnScope.ChatArea(mode: String, placeholder: String = "Message", emptyContent: @Composable () -> Unit = {}) {
    val ctx = LocalContext.current; val scope = rememberCoroutineScope(); val listState = rememberLazyListState()
    var input by remember { mutableStateOf("") }
    val msgs = remember { mutableStateListOf<Pair<String, String>>() }; var busy by remember { mutableStateOf(false) }
    // Attached PDF as (file name, base64 data); sent to the AI together with the message
    var pdf by remember { mutableStateOf<Pair<String, String>?>(null) }
    // File picker: PDFs become an attachment, plain-text files are loaded into the message box
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) runCatching {
            val type = ctx.contentResolver.getType(uri) ?: ""
            val bytes = ctx.contentResolver.openInputStream(uri)!!.use { it.readBytes() }
            when {
                type == "application/pdf" -> {
                    check(bytes.size <= 10_000_000) { "PDF is larger than 10 MB" }
                    pdf = (uri.lastPathSegment?.substringAfterLast('/') ?: "document.pdf") to android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                    Toast.makeText(ctx, "PDF attached - press send", Toast.LENGTH_SHORT).show()
                }
                type.startsWith("text/") -> { input = String(bytes).take(20000); Toast.makeText(ctx, "File loaded - press send", Toast.LENGTH_SHORT).show() }
                else -> error("Only PDF or .txt files are supported")
            }
        }.onFailure { Toast.makeText(ctx, it.message ?: "Could not read file", Toast.LENGTH_LONG).show() }
    }
    LaunchedEffect(msgs.size) { if (msgs.isNotEmpty()) listState.animateScrollToItem(msgs.lastIndex) }

    /** Validates the input, calls the AI, stores the result and shows it (or the error) in the chat. */
    fun send() {
        val attached = pdf
        val q = input.trim().ifEmpty { if (attached != null) "Please process the attached document." else "" }
        if (q.isEmpty()) { Toast.makeText(ctx, "Type a message or attach a file first", Toast.LENGTH_SHORT).show(); return }
        scope.launch {
            busy = true
            runCatching {
                val out = Api.ai(mode, q, attached?.second)
                val label = if (attached != null) "📎 ${attached.first}\n$q" else q
                Api.insert("sessions", org.json.JSONObject().put("session_type", mode).put("input_text", label).put("ai_output", out))
                msgs.add(label to out); input = ""; pdf = null
            }.onFailure { msgs.add(q to "⚠ Error: " + (it.message ?: "AI request failed")) }
            busy = false
        }
    }

    if (msgs.isEmpty()) emptyContent()
    LazyColumn(Modifier.weight(1f), state = listState) { items(msgs) { (q, a) ->
        Box(Modifier.fillMaxWidth().padding(top = 12.dp), contentAlignment = Alignment.CenterEnd) {
            Text(if (q.length > 300) q.take(300) + "…" else q, color = Color.White, fontSize = 15.sp,
                modifier = Modifier.widthIn(max = 290.dp).clip(RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)).background(Accent).padding(12.dp)) }
        BotBubble(a) } }
    if (busy) Loading()
    pdf?.let { Text("📎 ${it.first}   ✕", color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(4.dp).clickable { pdf = null }) }
    // Message bar: attach, text, send
    val bar = RoundedCornerShape(28.dp)
    Row(Modifier.fillMaxWidth().clip(bar).background(Color.White.copy(0.12f)).border(1.dp, Color.White.copy(0.25f), bar).padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text("📎", fontSize = 22.sp, modifier = Modifier.padding(8.dp).clickable { picker.launch("*/*") })
        TextField(input, { input = it }, Modifier.weight(1f), maxLines = 4, placeholder = { Text(placeholder, color = Color.White.copy(0.6f)) },
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                focusedTextColor = Color.White, unfocusedTextColor = Color.White, focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent, cursorColor = Color.White))
        Box(Modifier.size(44.dp).clip(CircleShape).background(Accent).clickable(enabled = !busy) { send() }, Alignment.Center) {
            Text("➤", color = Color.White, fontSize = 18.sp) }
    }
}

/** Bot reply bubble with the mascot avatar. */
@Composable
fun BotBubble(text: String) {
    Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.Top) {
        Logo(30.dp); Spacer(Modifier.width(8.dp))
        Text(text, color = Color.White, fontSize = 15.sp, modifier = Modifier.weight(1f, fill = false)
            .clip(RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)).background(Color.White.copy(0.12f)).padding(12.dp))
    }
}

/** Dedicated chat page: free conversation with the Edu Bot assistant. */
@Composable
fun ChatScreen(onBack: () -> Unit) {
    Page(onBack = onBack) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 8.dp)) {
            Logo(48.dp); Spacer(Modifier.width(12.dp))
            Column { Text("Edu Bot Assistant", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                Text("Ask me anything about your studies", color = Color.White.copy(0.75f), fontSize = 13.sp) } }
        ChatArea("chat", "Ask Edu Bot...") {
            BotBubble("Hi ${Api.meta.optString("first_name", "there")}! 👋 I can explain concepts, help with code, translate text or answer research questions. What would you like to know?") }
    }
}
