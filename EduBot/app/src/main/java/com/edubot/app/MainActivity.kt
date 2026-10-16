package com.edubot.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { App() } }
    }
}

@Composable
fun App() {
    val stack = remember { mutableStateListOf("welcome") }
    fun go(r: String) { when (r) {
        "logout" -> { stack.clear(); stack.add("signin") }
        "home" -> { stack.clear(); stack.add("home") }
        else -> stack.add(r) } }
    fun back() { if (stack.size > 1) stack.removeLast() }
    BackHandler(stack.size > 1) { back() }
    when (stack.last()) {
        "welcome" -> WelcomeScreen { go("signin") }
        "signin" -> SignInScreen({ go("home") }, { go("signup") })
        "signup" -> SignUpScreen({ go("home") }, { back() })
        "home" -> HomeScreen(::go)
        "profile" -> ProfileScreen(::back) { go("logout") }
        "history" -> HistoryScreen(::back) { go("grades") }
        "grades" -> GradesScreen(::back)
        "chat" -> ChatScreen(::back)
    }
}
