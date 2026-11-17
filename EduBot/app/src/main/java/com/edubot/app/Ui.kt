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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape


val Blue = Color(0xFF1B6AB5)
val Glass = Color(0xFF1B6AB5)
val Navy = Color(0xFF00115E)
val Deep = Color(0xFF164F87)
val Accent = Color(0xFF65B5F5)
private val shape = RoundedCornerShape(14.dp)
private val cardShape = RoundedCornerShape(18.dp)

@Composable
fun Logo(size: Dp, modifier: Modifier = Modifier) {
    Image(painterResource(R.drawable.bot_logo), "Edu Bot logo", modifier.size(size))
}

@Composable
fun Page(
    bg: Color = Blue,
    scroll: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(bg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxWidth()
                .then(
                    if (scroll) Modifier.verticalScroll(
                        rememberScrollState()
                    ) else Modifier
                ),
            content = content
        )
    }
}

@Composable
fun GlassButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val buttonShape = RoundedCornerShape(16.dp)

    Box(
        modifier
            .fillMaxWidth()
            .clip(buttonShape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.28f),
                        Accent.copy(alpha = 0.55f),
                        Glass.copy(alpha = 0.40f)
                    )
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.65f),
                shape = buttonShape
            )
            .alpha(if (enabled) 1f else 0.5f)
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
            .padding(vertical = 17.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun GlassButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    GlassButton(
        text = text,
        modifier = modifier,
        enabled = true,
        onClick = onClick
    )
}

@Composable
fun Field(value: String, onChange: (String) -> Unit, hint: String, modifier: Modifier = Modifier,
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

@Composable
fun AskEduBotButton(onClick: () -> Unit) {
    val buttonShape = CircleShape

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(buttonShape)
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF0164BB).copy(alpha = 0.75f),
                            Color(0xFF4B98DE).copy(alpha = 0.85f)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.45f),
                            Color.White.copy(alpha = 0.10f),
                            Color.White.copy(alpha = 0.25f)
                        )
                    ),
                    shape = buttonShape
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Ask Edu-Bot",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable fun Loading() { Box(Modifier.fillMaxWidth().padding(16.dp), Alignment.Center) { CircularProgressIndicator(color = Accent, strokeWidth = 3.dp) } }
@Composable fun Title(t: String, size: Int = 22) {
    Text(t, color = Color.White, fontSize = size.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(8.dp))
}

val emailRx = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
