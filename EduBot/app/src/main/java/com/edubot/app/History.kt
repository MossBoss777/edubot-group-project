package com.edubot.app

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.json.JSONObject

@Composable
fun HistoryScreen(onClose: () -> Unit, onGrades: () -> Unit) {
    val ctx = LocalContext.current; val scope = rememberCoroutineScope()
    val tabs = listOf("Generated Quizzes" to "quiz", "Current Study Plans" to "study_plan", "Summarized Notes" to "summary")
    var sel by remember { mutableStateOf<String?>(null) }; var rows by remember { mutableStateOf<List<JSONObject>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    fun load(t: String) = scope.launch { loading = true
        runCatching { Api.list("sessions", "session_type=eq.$t&order=created_at.desc") }.onSuccess { rows = it.objs() }
            .onFailure { Toast.makeText(ctx, it.message ?: "Load failed", Toast.LENGTH_LONG).show() }; loading = false }
    Page(bg = Navy) {
        Text("✕", color = Color.White, fontSize = androidx.compose.ui.unit.TextUnit(26f, androidx.compose.ui.unit.TextUnitType.Sp),
            modifier = Modifier.align(androidx.compose.ui.Alignment.End).padding(8.dp).let { it }.also { }, )
        Card { Title("History")
            tabs.forEach { (label, type) ->
                GlassButton(label, Modifier.padding(vertical = 4.dp)) { sel = if (sel == type) null else type; if (sel != null) load(type) }
                if (sel == type) Card(Modifier.heightIn(max = 300.dp)) {
                    if (loading) Loading() else if (rows.isEmpty()) Text("Nothing saved yet.", color = Color.White)
                    else LazyColumn { items(rows) { r -> Column(Modifier.padding(vertical = 4.dp)) {
                        Text(r.optString("input_text").take(60), color = Color.White); Text(r.optString("ai_output").take(160), color = Color.White.copy(0.7f))
                        TextButton({ scope.launch { runCatching { Api.delete("sessions", r.getString("id")) }.onSuccess { load(type) }
                            .onFailure { Toast.makeText(ctx, it.message ?: "Error", Toast.LENGTH_SHORT).show() } } }) { Text("Delete", color = Color(0xFFFFB4AB)) } } } }
                } }
            GlassButton("Logged Grades", Modifier.padding(vertical = 4.dp), onGrades)
        }
        TextButton(onClose) { Text("Close", color = Color.White) }
    }
}
