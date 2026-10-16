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
fun GradesScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current; val scope = rememberCoroutineScope()
    var rows by remember { mutableStateOf<List<JSONObject>>(emptyList()) }; var loading by remember { mutableStateOf(true) }
    var open by remember { mutableStateOf<String?>(null) }; var editing by remember { mutableStateOf<JSONObject?>(null) }; var adding by remember { mutableStateOf(false) }
    fun toast(m: String) = Toast.makeText(ctx, m, Toast.LENGTH_SHORT).show()
    fun load() = scope.launch { loading = true; runCatching { Api.list("grades") }.onSuccess { rows = it.objs() }.onFailure { toast(it.message ?: "Load failed") }; loading = false }
    LaunchedEffect(Unit) { load() }
    Page(onBack = onBack) {
        Card(onClick = { adding = true }) { Row { Text("Modules", color = Color.White, modifier = Modifier.weight(1f)); Text("+", color = Color.White) } }
        if (loading) Loading()
        else if (rows.isEmpty()) Text("No modules yet. Tap + to log a grade.", color = Color.White, modifier = Modifier.padding(16.dp))
        LazyColumn { items(rows) { g ->
            val pct = ((g.optDouble("online_test") + g.optDouble("assignment") + g.optDouble("exam")) / 150 * 100).toInt()
            Card(Modifier.padding(top = 8.dp), onClick = { open = if (open == g.getString("id")) null else g.getString("id") }) {
                Row { Text(g.getString("module_name"), color = Color.White, modifier = Modifier.weight(1f)); Text("$pct%", color = Color.White) }
                if (open == g.getString("id")) {
                    listOf("Online Test" to "online_test" to 10, "Assignment" to "assignment" to 40, "Exam" to "exam" to 100).forEach { (a, max) ->
                        Card(Modifier.padding(top = 6.dp)) { Row { Text(a.first, color = Color.White, modifier = Modifier.weight(1f)); Text("${g.optDouble(a.second).toInt()}/$max", color = Color.White) } } }
                    Row { TextButton({ editing = g }) { Text("Edit", color = Color.White) }
                        TextButton({ scope.launch { runCatching { Api.delete("grades", g.getString("id")) }.onSuccess { toast("Deleted"); load() }.onFailure { toast(it.message ?: "Error") } } }) { Text("Delete", color = Color(0xFFFFB4AB)) } }
                } } } }
    }
    if (adding || editing != null) GradeDialog(editing, { adding = false; editing = null }) { name, t, a, e ->
        scope.launch {
            val row = JSONObject().put("module_name", name).put("online_test", t).put("assignment", a).put("exam", e)
            runCatching { editing?.let { Api.update("grades", it.getString("id"), row) } ?: Api.insert("grades", row) }
                .onSuccess { toast("Saved"); adding = false; editing = null; load() }.onFailure { toast(it.message ?: "Error") } } }
}


@Composable
fun GradeDialog(g: JSONObject?, onClose: () -> Unit, onSave: (String, Double, Double, Double) -> Unit) {
    var n by remember { mutableStateOf(g?.optString("module_name") ?: "") }
    var t by remember { mutableStateOf(g?.optDouble("online_test")?.toString() ?: "") }
    var a by remember { mutableStateOf(g?.optDouble("assignment")?.toString() ?: "") }
    var e by remember { mutableStateOf(g?.optDouble("exam")?.toString() ?: "") }; var err by remember { mutableStateOf<String?>(null) }
    AlertDialog(onDismissRequest = onClose, title = { Text(if (g == null) "Log Grade" else "Edit Grade") },
        text = { Column { OutlinedTextField(n, { n = it }, label = { Text("Module Name") }, singleLine = true)
            OutlinedTextField(t, { t = it }, label = { Text("Online Test /10") }, singleLine = true)
            OutlinedTextField(a, { a = it }, label = { Text("Assignment /40") }, singleLine = true)
            OutlinedTextField(e, { e = it }, label = { Text("Exam /100") }, singleLine = true)
            err?.let { Text(it, color = MaterialTheme.colorScheme.error) } } },
        confirmButton = { TextButton({
            val tv = t.toDoubleOrNull(); val av = a.toDoubleOrNull(); val ev = e.toDoubleOrNull()
            err = when { n.isBlank() -> "Module name required"; tv == null || tv !in 0.0..10.0 -> "Test must be 0-10"
                av == null || av !in 0.0..40.0 -> "Assignment must be 0-40"; ev == null || ev !in 0.0..100.0 -> "Exam must be 0-100"; else -> null }
            if (err == null) onSave(n.trim(), tv!!, av!!, ev!!) }) { Text("Log Grade") } },
        dismissButton = { TextButton(onClose) { Text("Cancel") } })
}
