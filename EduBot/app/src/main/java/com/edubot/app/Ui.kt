package com.edubot.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Purple = Color(0xFF57368F); val Glass = Color(0xFF7A4FA8); val Navy = Color(0xFF1B2A5C); val Deep = Color(0xFF2B2B6B)
val Accent = Color(0xFF8B63D9)
private val shape = RoundedCornerShape(14.dp)
private val cardShape = RoundedCornerShape(18.dp)

@Composable
fun Logo(size: Dp, modifier: Modifier = Modifier) {
    Image(painterResource(R.drawable.bot_logo), "Edu Bot logo", modifier.size(size))
}

@Composable
fun Page(bg: Color = Purple, onBack: (() -> Unit)? = null, scroll: Boolean = false, content: @Composable ColumnScope.() -> Unit) {
    val brush = Brush.verticalGradient(listOf(bg, lerp(bg, Color.Black, 0.4f)))
    Column(Modifier.fillMaxSize().background(brush).statusBarsPadding().navigationBarsPadding().imePadding()
        .padding(horizontal = 20.dp, vertical = 16.dp)) {
        Column(Modifier.weight(1f).fillMaxWidth().let { if (scroll) it.verticalScroll(rememberScrollState()) else it }, content = content)
        if (onBack != null) GlassButton("Home", Modifier.padding(top = 10.dp), onBack)
    }
}

@Composable
fun GlassButton(text: String, modifier: Modifier = Modifier, enabled: Boolean = true, onClick: () -> Unit) {
    Box(modifier.fillMaxWidth().alpha(if (enabled) 1f else 0.5f).clip(shape)
        .background(Brush.horizontalGradient(listOf(Accent, Glass)))
        .border(1.dp, Color.White.copy(0.35f), shape).clickable(enabled = enabled, onClick = onClick).padding(vertical = 15.dp),
        contentAlignment = Alignment.Center) {
        Text(text, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}

@Composable
fun GlassButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    GlassButton(text, modifier, true, onClick)
}

@Composable
fun Field(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier.fillMaxWidth(),
          password: Boolean = false, error: String? = null) {
    Column(modifier.padding(vertical = 5.dp)) {
        TextField(value, onChange, Modifier.fillMaxWidth().border(1.dp, if (error != null) Color(0xFFFFB4AB) else Color.White.copy(0.25f), shape),
            singleLine = true, isError = error != null, shape = shape,
            placeholder = { Text(hint, color = Color.White.copy(0.6f)) },
            visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
            colors = TextFieldDefaults.colors(focusedContainerColor = Color.White.copy(0.14f), unfocusedContainerColor = Color.White.copy(0.10f),
                errorContainerColor = Color.White.copy(0.10f), focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                errorTextColor = Color.White, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent, cursorColor = Color.White))
        if (error != null) Text(error, color = Color(0xFFFFB4AB), fontSize = 12.sp, modifier = Modifier.padding(start = 6.dp, top = 2.dp))
    }
}

@Composable
fun Card(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().clip(cardShape).background(Color.White.copy(0.10f)).border(1.dp, Color.White.copy(0.18f), cardShape)
        .let { if (onClick != null) it.clickable(onClick = onClick) else it }.padding(16.dp), content = content)
}

@Composable fun Loading() { Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) { CircularProgressIndicator(color = Color.White, strokeWidth = 3.dp) } }
@Composable fun Title(t: String, size: Int = 22) {
    Text(t, color = Color.White, fontSize = size.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(8.dp))
}

val emailRx = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
