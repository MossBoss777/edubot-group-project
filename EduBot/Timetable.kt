package com.edubot.app

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.json.JSONObject

private val dayNames = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

/** Minutes from now until the next weekly occurrence of a class (huge number if the data can't be parsed). */
private fun minutesUntil(day: String, time: String): Int = runCatching {
    val c = java.util.Calendar.getInstance()
    val today = (c.get(java.util.Calendar.DAY_OF_WEEK) + 5) % 7            // Monday = 0 ... Sunday = 6
    val nowMin = c.get(java.util.Calendar.HOUR_OF_DAY) * 60 + c.get(java.util.Calendar.MINUTE)
    val (h, m) = time.trim().split(":").map { it.trim().toInt() }
    val d = dayNames.indexOf(day.trim()); require(d >= 0)
    var diff = (d - today) * 1440 + h * 60 + m - nowMin
    if (diff < 0) diff += 7 * 1440
    diff
}.getOrDefault(Int.MAX_VALUE)

/** Home card: shows the next class. The "+" lets the student describe their timetable in a prompt; the AI turns it into rows. */
@Composable
fun TimetableCard() {
    val ctx = LocalContext.current; val scope = rememberCoroutineScope()
    var rows by remember { mutableStateOf<List<JSONObject>>(emptyList()) }
    var dialog by remember { mutableStateOf(false) }; var prompt by remember { mutableStateOf("") }; var busy by remember { mutableStateOf(false) }
    fun toast(m: String) = Toast.makeText(ctx, m, Toast.LENGTH_LONG).show()
    fun load() { scope.launch { runCatching { Api.list("timetable", "order=created_at.asc") }.onSuccess { rows = it.objs() } } }
    LaunchedEffect(Unit) { load() }
    val next = rows.minByOrNull { minutesUntil(it.optString("day"), it.optString("time")) }

    Card(Modifier.padding(top = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("📅  Timetable", color = Color.White, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("+", color = Color.White, fontSize = 26.sp, modifier = Modifier.clickable { dialog = true }.padding(horizontal = 8.dp)) }
        Text(if (next == null) "No classes yet. Tap + and describe your timetable."
             else "Next class: ${next.optString("module")}\n${next.optString("day")} ${next.optString("time")}   ${next.optString("venue")}",
            color = Color.White.copy(0.85f), fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
    }
    if (dialog) AlertDialog(onDismissRequest = { dialog = false }, title = { Text("Your timetable") },
        text = { Column(Modifier.verticalScroll(rememberScrollState())) {
            Text("Describe your classes, e.g. \"Databases Monday 10:00 Room B2, Networks Wednesday 14:00 Lab 3\"", fontSize = 13.sp)
            OutlinedTextField(prompt, { prompt = it }, Modifier.fillMaxWidth().padding(vertical = 8.dp), minLines = 2)
            if (busy) CircularProgressIndicator(Modifier.padding(8.dp))
            rows.forEach { r -> Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${r.optString("module")} - ${r.optString("day")} ${r.optString("time")} ${r.optString("venue")}", Modifier.weight(1f), fontSize = 13.sp)
                Text("🗑", Modifier.clickable { scope.launch { runCatching { Api.delete("timetable", r.getString("id")) }.onSuccess { load() }.onFailure { toast(it.message ?: "Error") } } }.padding(8.dp)) } } } },
        confirmButton = { TextButton(enabled = !busy, onClick = {
            if (prompt.isBlank()) toast("Describe at least one class first") else scope.launch {
                busy = true
                runCatching {
                    val j = parseJson(Api.ai("timetable", prompt)) ?: error("Could not understand that timetable, please rephrase")
                    val list = j.optJSONArray("classes")?.objs() ?: emptyList()
                    check(list.isNotEmpty()) { "No classes found in your message" }
                    list.forEach { c -> Api.insert("timetable", JSONObject().put("module", c.optString("module")).put("day", c.optString("day")).put("time", c.optString("time")).put("venue", c.optString("venue"))) }
                    prompt = ""; load(); toast("${list.size} class(es) added")
                }.onFailure { toast(it.message ?: "Error") }
                busy = false } }) { Text("Add") } },
        dismissButton = { TextButton({ dialog = false }) { Text("Close") } })
}
