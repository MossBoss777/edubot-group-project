package com.edubot.app

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(onStart: () -> Unit) = Page {
    Spacer(Modifier.windowInsetsPadding(WindowInsets.statusBars))
    Title("Welcome to Edu Bot", 38)
    Spacer(Modifier.height(16.dp))
    Text(
        text =" Your smart mobile AI student assistant",
        color = Color.White.copy(0.85f),
        fontSize = 26.sp,
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.weight(1f))
    Logo(
        300.dp,
        Modifier.align(Alignment.CenterHorizontally)
    )
    Spacer(Modifier.weight(1f))
    GetStartedButton(onClick = onStart)
}

@Composable
fun GetStartedButton(onClick: () -> Unit) {
    val circleShape = CircleShape

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(CircleShape)
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
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape = CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.weight(1f))

        Text(
            text = "Get Started",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(circleShape)
                .background(Color.White.copy(alpha = 0.08f))
                .border(
                    width = 1.dp,
                    color = Color.White.copy(alpha = 0.65f),
                    shape = circleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.arrow_right),
                contentDescription = "Continue",
                modifier = Modifier.size(40.dp)
            )
        }
    }
}

@Composable
fun SignInScreen(onDone: () -> Unit, onSignUp: () -> Unit) {
    val ctx = LocalContext.current; val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }; var pw by remember { mutableStateOf("") }
    var eErr by remember { mutableStateOf<String?>(null) }; var pErr by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    fun toast(m: String) = Toast.makeText(ctx, m, Toast.LENGTH_LONG).show()
    Page(scroll = true) {
        Spacer(Modifier.height(16.dp))
        Logo(110.dp, Modifier.align(Alignment.CenterHorizontally))
        Title("Sign In to your Edu Bot account")
        Spacer(Modifier.height(12.dp))
        Field(
            value = email,
            onValueChange = { email = it },
            placeholder = "Enter email",
            icon = R.drawable.profile,
            error = eErr
        )

        Spacer(Modifier.height(12.dp))

        Field(
            value = pw,
            onValueChange = { pw = it },
            placeholder = "Enter password",
            icon = R.drawable.lock,
            password = true,
            error = pErr
        )
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
        Text(
            "New to Edu Bot?",
            color = Color.White,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp))
        Text(
            "Sign Up",
            color = Navy,
            modifier = Modifier.align(Alignment.CenterHorizontally).clickable(onClick = onSignUp))
    }
}

@Composable
fun Field(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: Int,
    modifier: Modifier = Modifier,
    password: Boolean = false,
    error: String? = null
) {
    val shape = RoundedCornerShape(18.dp)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(62.dp)
                .clip(shape)
                .background(Color.White.copy(alpha = 0.10f))
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.35f),
                    shape
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )

            Spacer(Modifier.width(12.dp))

            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp
                ),
                visualTransformation = if (password) {
                    PasswordVisualTransformation()
                } else {
                    VisualTransformation.None
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (password) {
                        KeyboardType.Password
                    } else {
                        KeyboardType.Email
                    },
                    imeAction = ImeAction.Next
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                color = Color.White.copy(alpha = 0.65f),
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        if (error != null) {
            Text(
                text = error,
                color = Color(0xFFFFB4AB),
                fontSize = 12.sp,
                modifier = Modifier.padding(
                    start = 8.dp,
                    top = 4.dp
                )
            )
        }
    }
}

private val termsText = """
Welcome to EduBot! By creating an account and using our application, you agree to be bound by these Terms and Conditions:

1. Acceptance of Terms
By registering or using EduBot, you agree to comply with and be bound by these terms. If you do not agree, please do not use the application.

2. Account Responsibility
- You must provide accurate registration details.
- You are responsible for keeping your password secure.
- You are responsible for all activities under your account.

3. Acceptable Use
- EduBot is designed for educational assistance and study management.
- You must not upload illegal, harmful, or copyright-infringing content.
- Automated scraping or misuse of service infrastructure is strictly prohibited.

4. Intellectual Property
All content, features, designs, and AI features within EduBot are protected by intellectual property laws.

5. Limitation of Liability
EduBot is provided "as is". We are not liable for any indirect, incidental, or consequential damages resulting from app usage.

6. Updates to Terms
We may update these terms periodically. Continued use of EduBot signifies acceptance of any changes.
""".trimIndent()

private val privacyText = """
At EduBot, we respect your privacy and are committed to protecting the personal information you provide when using our application.

1. Information We Collect
- Full Name.
- Email Address.
- Year of study.
- Timetable information entered by user.
- Information needed to generate quizzes, summaries, and study plans.

2. How We Use Your Information
Information collected is used to:
- Identify and track users.
- Personalise the application experience.
- Power AI study assistance and manage your academic schedule.

3. What to Avoid Sharing
- Do not upload sensitive, personal, or confidential documents.

4. Our Promise
- To ensure the security and privacy of your information.
- To not sell user information to third parties.
- To respect user privacy rights.

By registering and using EduBot, you acknowledge that you have read and understood this Privacy Policy.
""".trimIndent()

@Composable
private fun PolicyDialog(
    title: String,
    content: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = content,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Accent, fontWeight = FontWeight.SemiBold)
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Navy,
        titleContentColor = Color.White,
        textContentColor = Color.White.copy(0.9f)
    )
}

@Composable
fun SignUpScreen(onDone: () -> Unit, onSignIn: () -> Unit) {
    val ctx = LocalContext.current; val scope = rememberCoroutineScope()
    var f by remember { mutableStateOf("") }; var l by remember { mutableStateOf("") }; var y by remember { mutableStateOf("") }
    var e by remember { mutableStateOf("") }; var p by remember { mutableStateOf("") }; var c by remember { mutableStateOf("") }
    var agree by remember { mutableStateOf(false) }; var err by remember { mutableStateOf<String?>(null) }; var busy by remember { mutableStateOf(false) }
    var showTerms by remember { mutableStateOf(false) }; var showPrivacy by remember { mutableStateOf(false) }

    Page(scroll = true) {
        Logo(80.dp, Modifier.align(Alignment.CenterHorizontally)); Title("Let's sign up to Edu Bot")
        Row { Field(f, { f = it }, "First name", Modifier.weight(1f)); Spacer(Modifier.width(8.dp)); Field(y, { y = it.filter(Char::isDigit).take(1) }, "Year", Modifier.width(90.dp)) }
        Field(l, { l = it }, "Enter last name"); Field(e, { e = it }, "Enter email")
        Field(p, { p = it }, "Enter password", password = true); Field(c, { c = it }, "Confirm password", password = true)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(agree, { agree = it })
            val agreementText = buildAnnotatedString {
                append("I agree with the ")
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "TERMS",
                        styles = TextLinkStyles(style = SpanStyle(color = Navy, fontWeight = FontWeight.SemiBold)),
                        linkInteractionListener = { showTerms = true }
                    )
                ) {
                    append("Terms and Conditions")
                }
                append(" and ")
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "PRIVACY",
                        styles = TextLinkStyles(style = SpanStyle(color = Navy, fontWeight = FontWeight.SemiBold)),
                        linkInteractionListener = { showPrivacy = true }
                    )
                ) {
                    append("Privacy Policy")
                }
            }
            Text(
                text = agreementText,
                style = TextStyle(color = Color.White, fontSize = 13.sp),
                modifier = Modifier.weight(1f)
            )
        }

        if (showTerms) {
            PolicyDialog(
                title = "Terms and Conditions",
                content = termsText,
                onDismiss = { showTerms = false }
            )
        }

        if (showPrivacy) {
            PolicyDialog(
                title = "Privacy Policy",
                content = privacyText,
                onDismiss = { showPrivacy = false }
            )
        }

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
