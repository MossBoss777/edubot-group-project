package com.edubot.app

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(onStart: () -> Unit) = Page {
    Spacer(Modifier.weight(1f))
    Logo(200.dp, Modifier.align(Alignment.CenterHorizontally))
    Spacer(Modifier.height(16.dp))
    Title("Welcome to Edu Bot", 30)
    Text("Your smart mobile AI student assistant", color = Color.White.copy(0.85f), fontSize = 17.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.height(24.dp))
    Text("📝 Summarise notes   ❓ Practice quizzes   📅 Study plans", color = Color.White.copy(0.7f), fontSize = 13.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center, modifier = Modifier.fillMaxWidth())
    Spacer(Modifier.weight(1f))
    GlassButton("Get Started   ➜", onClick = onStart)
}

@Composable
fun SignInScreen(onDone: () -> Unit, onSignUp: () -> Unit) {
    val ctx = LocalContext.current; val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }; var pw by remember { mutableStateOf("") }
    var eErr by remember { mutableStateOf<String?>(null) }; var pErr by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    fun toast(m: String) = Toast.makeText(ctx, m, Toast.LENGTH_LONG).show()
    Page(scroll = true) {
        Spacer(Modifier.height(16.dp)); Logo(110.dp, Modifier.align(Alignment.CenterHorizontally)); Title("Sign In to your Edu Bot account"); Spacer(Modifier.height(12.dp))
        Field(email, { email = it }, "Enter email", error = eErr); Field(pw, { pw = it }, "Enter password", password = true, error = pErr)
        Text("Forgot password?", color = Navy, modifier = Modifier.align(Alignment.CenterHorizontally).padding(8.dp).clickable {
            if (!emailRx.matches(email)) eErr = "Enter your email above first" else scope.launch {
                runCatching { Api.resetPassword(email) }.onSuccess { toast("Reset link sent to $email") }.onFailure { toast(it.message ?: "Error") } } })
        if (busy) Loading()
        Spacer(Modifier.weight(1f))
        GlassButton("Sign In", enabled = !busy) {
            eErr = if (emailRx.matches(email)) null else "Enter a valid email"; pErr = if (pw.length >= 6) null else "Password must be 6+ characters"
            if (eErr == null && pErr == null) scope.launch {
                busy = true
                runCatching { Api.signIn(email.trim(), pw) }.onSuccess { onDone() }.onFailure { toast(it.message ?: "Sign in failed") }
                busy = false } }
        Text("New to Edu Bot?", color = Color.White, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp))
        Text("Sign Up", color = Navy, modifier = Modifier.align(Alignment.CenterHorizontally).clickable(onClick = onSignUp))
    }
}

@Composable
fun SignUpScreen(onDone: () -> Unit, onSignIn: () -> Unit) {
    val ctx = LocalContext.current; val scope = rememberCoroutineScope()
    var f by remember { mutableStateOf("") }; var l by remember { mutableStateOf("") }; var y by remember { mutableStateOf("") }
    var e by remember { mutableStateOf("") }; var p by remember { mutableStateOf("") }; var c by remember { mutableStateOf("") }
    var agree by remember { mutableStateOf(false) }; var err by remember { mutableStateOf<String?>(null) }; var busy by remember { mutableStateOf(false) }
    Page(scroll = true) {
        Logo(80.dp, Modifier.align(Alignment.CenterHorizontally)); Title("Let's sign up to Edu Bot")
        Row { Field(f, { f = it }, "First name", Modifier.weight(1f)); Spacer(Modifier.width(8.dp)); Field(y, { y = it.filter(Char::isDigit).take(1) }, "Year", Modifier.width(90.dp)) }
        Field(l, { l = it }, "Enter last name"); Field(e, { e = it }, "Enter email")
        Field(p, { p = it }, "Enter password", password = true); Field(c, { c = it }, "Confirm password", password = true)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(agree, { agree = it }); Text("I agree with the Terms of Conditions and Privacy Policy", color = Color.White, fontSize = 12.sp) }
        err?.let { Text(it, color = Color(0xFFFFB4AB)) }
        if (busy) Loading()
        Spacer(Modifier.weight(1f))
        GlassButton("Sign Up", enabled = !busy) {
            err = when {
                f.isBlank() || l.isBlank() || y.isBlank() -> "Please fill in your name and year of study"
                !emailRx.matches(e) -> "Enter a valid email"; p.length < 6 -> "Password must be 6+ characters"
                p != c -> "Passwords do not match"; !agree -> "Please accept the Terms and Privacy Policy"; else -> null }
            if (err == null) scope.launch {
                busy = true
                runCatching { Api.signUp(e.trim(), p, f.trim(), l.trim(), y) }.onSuccess { inNow ->
                    if (inNow) onDone() else { Toast.makeText(ctx, "Account created. Confirm your email, then sign in.", Toast.LENGTH_LONG).show(); onSignIn() }
                }.onFailure { err = it.message }
                busy = false } }
        Text("Already have an account?", color = Color.White, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp))
        Text("Sign In", color = Navy, modifier = Modifier.align(Alignment.CenterHorizontally).clickable(onClick = onSignIn))
    }
}
