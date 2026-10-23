package com.edubot.app

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.json.JSONArray
import org.json.JSONObject

/** Removes markdown symbols (##, **, *, backticks) so plain text looks clean. */
fun clean(s: String): String = s.replace(Regex("(?m)^\\s{0,3}#{1,6}\\s*"), "").replace("**", "").replace("__", "").replace("`", "")
    .replace(Regex("(?m)^\\s*[*-]\\s+"), "• ").replace("*", "").trim()

/** Extracts the first JSON object from the AI text (tolerates ```json fences). Null if there is none. */
fun parseJson(s: String): JSONObject? = runCatching {
    val a = s.indexOf('{'); val b = s.lastIndexOf('}')
    if (a < 0 || b <= a) null else JSONObject(s.substring(a, b + 1))
}.getOrNull()

/** Short text for the History list. */
fun preview(output: String): String {
    val j = parseJson(output)
    return if (j != null && j.has("title")) clean(j.optString("title")) else clean(output).take(160)
}

private fun JSONArray?.strs(): List<String> = if (this == null) emptyList() else (0 until length()).map { optString(it) }
private fun correctIndex(q: JSONObject) = "ABCD".indexOf(q.optString("answer").trim().uppercase().take(1))
private val Lilac = Color(0xFFD0BCFF)

/** Bot reply: mascot avatar + a formatted response. */
@Composable
fun BotReply(text: String) {
    Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.Top) {
        Logo(30.dp); Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) { ResponseView(text) }
    }
}

/** Chooses the template from the shape of the JSON: quiz, study plan table, summary sections, or plain text. */
@Composable
fun ResponseView(output: String) {
    val j = remember(output) { parseJson(output) }
    when {
        j != null && j.has("questions") -> QuizView(j)
        j != null && j.has("rows") -> PlanView(j)
        j != null && j.has("sections") -> SummaryView(j)
        else -> Text(clean(output), color = Color.White, fontSize = 15.sp,
            modifier = Modifier.clip(RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)).background(Color.White.copy(0.12f)).padding(12.dp))
    }
}

@Composable
private fun Panel(content: @Composable ColumnScope.() -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color.White.copy(0.12f)).padding(14.dp), content = content)
}

/** Template: title, overview, headed sections with bullet points, key-terms table. */
@Composable
private fun SummaryView(j: JSONObject) {
    val terms = j.optJSONArray("key_terms")?.objs() ?: emptyList()
    Panel {
        Text(clean(j.optString("title", "Summary")), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        if (j.optString("overview").isNotBlank()) Text(clean(j.optString("overview")), color = Color.White.copy(0.85f), fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
        (j.optJSONArray("sections")?.objs() ?: emptyList()).forEach { s ->
            Text(clean(s.optString("heading")), color = Lilac, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp))
            s.optJSONArray("points").strs().forEach { Text("•  ${clean(it)}", color = Color.White.copy(0.95f), fontSize = 14.sp, modifier = Modifier.padding(top = 3.dp)) }
        }
        if (terms.isNotEmpty()) {
            Text("Key terms", color = Lilac, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Accent).padding(8.dp)) {
                Text("Term", Modifier.weight(0.3f), Color.White, 12.sp, fontWeight = FontWeight.Bold); Text("Definition", Modifier.weight(0.7f), Color.White, 12.sp, fontWeight = FontWeight.Bold) }
            terms.forEachIndexed { n, t ->
                Row(Modifier.fillMaxWidth().background(if (n % 2 == 0) Color.White.copy(0.06f) else Color.Transparent).padding(8.dp)) {
                    Text(clean(t.optString("term")), Modifier.weight(0.3f).padding(end = 4.dp), Color.White, 12.sp, fontWeight = FontWeight.SemiBold)
                    Text(clean(t.optString("definition")), Modifier.weight(0.7f), Color.White, 12.sp) } }
        }
    }
}

/** Template: study plan as a table with a heading row (Day | Topic | Activity | Hrs). */
@Composable
private fun PlanView(j: JSONObject) {
    val rows = j.optJSONArray("rows")?.objs() ?: emptyList(); val w = listOf(0.17f, 0.30f, 0.41f, 0.12f)
    Panel {
        Text(clean(j.optString("title", "Study Plan")), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        if (j.optString("summary").isNotBlank()) Text(clean(j.optString("summary")), color = Color.White.copy(0.85f), fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(Accent).padding(8.dp)) {
            listOf("Day", "Topic", "Activity", "Hrs").forEachIndexed { i, h -> Text(h, Modifier.weight(w[i]), Color.White, 12.sp, fontWeight = FontWeight.Bold) } }
        rows.forEachIndexed { n, r ->
            Row(Modifier.fillMaxWidth().background(if (n % 2 == 0) Color.White.copy(0.06f) else Color.Transparent).padding(8.dp)) {
                listOf(r.optString("day"), r.optString("topic"), r.optString("activity"), r.optString("hours")).forEachIndexed { i, t ->
                    Text(clean(t), Modifier.weight(w[i]).padding(end = 4.dp), Color.White, 12.sp) } } }
    }
}

/** Template: numbered questions with tappable options, instant right/wrong colouring, explanation and score. */
@Composable
private fun QuizView(j: JSONObject) {
    val qs = j.optJSONArray("questions")?.objs() ?: emptyList(); val picked = remember { mutableStateMapOf<Int, Int>() }
    Panel {
        Text(clean(j.optString("title", "Quiz")), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("Tap an answer to check it", color = Color.White.copy(0.7f), fontSize = 12.sp)
        qs.forEachIndexed { qi, q ->
            val correct = correctIndex(q); val p = picked[qi]
            Text("${qi + 1}. ${clean(q.optString("question"))}", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 14.dp))
            q.optJSONArray("options").strs().map { it.replace(Regex("^[A-Da-d][).:\\-]\\s+"), "") }.forEachIndexed { oi, o ->
                val bg = when { p == null -> Color.White.copy(0.08f); oi == correct -> Color(0xFF2E7D32).copy(0.75f); oi == p -> Color(0xFFC62828).copy(0.75f); else -> Color.White.copy(0.08f) }
                Text("${"ABCDEF".getOrElse(oi) { '?' }}.  ${clean(o)}", color = Color.White, fontSize = 14.sp,
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp).clip(RoundedCornerShape(10.dp)).background(bg).clickable(enabled = p == null) { picked[qi] = oi }.padding(10.dp)) }
            if (p != null) Text((if (p == correct) "✔ Correct. " else "✘ Incorrect. ") + clean(q.optString("explanation")), color = Color.White.copy(0.85f), fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
        }
        if (qs.isNotEmpty() && picked.size == qs.size)
            Text("Score: ${qs.indices.count { picked[it] == correctIndex(qs[it]) }} / ${qs.size}", color = Lilac, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 14.dp))
    }
}
