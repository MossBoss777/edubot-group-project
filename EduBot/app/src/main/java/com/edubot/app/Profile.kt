package com.edubot.app

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(onBack: () -> Unit, onLogout: () -> Unit) {
    val ctx = LocalContext.current; val scope = rememberCoroutineScope()
    var settings by remember { mutableStateOf(false) }; var dlg by remember { mutableStateOf("") }
    var f by remember { mutableStateOf(Api.meta.optString("first_name")) }; var l by remember { mutableStateOf(Api.meta.optString("last_name")) }
    var y by remember { mutableStateOf(Api.meta.optString("year")) }
    fun toast(m: String) = Toast.makeText(ctx, m, Toast.LENGTH_LONG).show()
    Page(bg = Deep, onBack = if (settings) ({ settings = false }) else onBack) {
        if (!settings) {
            Text("My Profile", color = Color.White, fontSize = 20.sp)
            Card(Modifier.padding(vertical = 12.dp)) { Text("${Api.meta.optString("first_name")} ${Api.meta.optString("last_name")}\nYear ${Api.meta.optString("year")}", color = Color.White, fontSize = 20.sp) }
            GlassButton("Settings") { settings = true }
            Card(Modifier.padding(top = 24.dp)) { Title("Support And Information")
                listOf("Feedback", "Rate Application", "About Us", "Privacy Policy").forEach { GlassButton(it, Modifier.padding(vertical = 3.dp)) { toast("$it: coming soon") } } }
            Spacer(Modifier.height(12.dp)); GlassButton("Sign Out") { Api.signOut(); onLogout() }
        } else {
            Title("Settings", 26)
            GlassButton("Reset password", Modifier.padding(6.dp)) { scope.launch { runCatching { Api.resetPassword(Api.user!!.optString("email")) }.onSuccess { toast("Reset link sent to your email") }.onFailure { toast(it.message ?: "Error") } } }
            GlassButton("Update Information", Modifier.padding(6.dp)) { dlg = "update" }
            GlassButton("Account deletion", Modifier.padding(6.dp)) { dlg = "delete" }
        }
    }
    if (dlg == "update") AlertDialog({ dlg = "" }, title = { Text("Update information") },
        text = { Column { OutlinedTextField(f, { f = it }, label = { Text("First name") }); OutlinedTextField(l, { l = it }, label = { Text("Last name") }); OutlinedTextField(y, { y = it.filter(Char::isDigit).take(1) }, label = { Text("Year of study") }) } },
        confirmButton = { TextButton({ if (f.isBlank() || l.isBlank() || y.isBlank()) toast("All fields are required") else scope.launch {
            runCatching { Api.updateInfo(f.trim(), l.trim(), y) }.onSuccess { toast("Updated"); dlg = "" }.onFailure { toast(it.message ?: "Error") } } }) { Text("Save") } },
        dismissButton = { TextButton({ dlg = "" }) { Text("Cancel") } })
    if (dlg == "delete") AlertDialog({ dlg = "" }, title = { Text("Delete account?") }, text = { Text("This permanently deletes your account and all your data.") },
        confirmButton = { TextButton({ scope.launch { runCatching { Api.deleteAccount() }.onSuccess { onLogout() }.onFailure { toast(it.message ?: "Error") } } }) { Text("Delete", color = Color.Red) } },
        dismissButton = { TextButton({ dlg = "" }) { Text("Cancel") } })
}
