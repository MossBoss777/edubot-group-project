package com.edubot.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject


object Api {
    private val http = OkHttpClient.Builder().readTimeout(java.time.Duration.ofSeconds(60)).build()
    private val JSON = "application/json".toMediaType()
    var token: String? = null
    var user: JSONObject? = null
    val userId get() = user?.optString("id") ?: ""
    val meta get() = user?.optJSONObject("user_metadata") ?: JSONObject()


    suspend fun call(method: String, path: String, body: JSONObject? = null, authed: Boolean = true): String =
        withContext(Dispatchers.IO) {
            val rb = if (method == "GET" || method == "DELETE") null else (body?.toString() ?: "{}").toRequestBody(JSON)
            val req = Request.Builder().url(BuildConfig.SUPABASE_URL + path)
                .header("apikey", BuildConfig.SUPABASE_KEY)
                .header("Authorization", "Bearer " + (token.takeIf { authed } ?: BuildConfig.SUPABASE_KEY))
                .header("Prefer", "return=representation").method(method, rb).build()
            http.newCall(req).execute().use { r ->
                val txt = r.body?.string() ?: ""
                if (!r.isSuccessful) {
                    val j = runCatching { JSONObject(txt) }.getOrNull()
                    throw Exception(j?.optString("msg")?.ifBlank { null } ?: j?.optString("message")?.ifBlank { null }
                        ?: j?.optString("error_description")?.ifBlank { null } ?: "Request failed (${r.code})")
                }
                txt
            }
        }

    private fun setSession(j: JSONObject) { token = j.optString("access_token").ifBlank { null }; user = j.optJSONObject("user") ?: j }

    suspend fun signIn(email: String, pw: String) =
        setSession(JSONObject(call("POST", "/auth/v1/token?grant_type=password",
            JSONObject().put("email", email).put("password", pw), false)))


    suspend fun signUp(email: String, pw: String, first: String, last: String, year: String): Boolean {
        val j = JSONObject(call("POST", "/auth/v1/signup", JSONObject().put("email", email).put("password", pw)
            .put("data", JSONObject().put("first_name", first).put("last_name", last).put("year", year)), false))
        if (j.has("access_token")) { setSession(j); return true }
        return false
    }

    suspend fun resetPassword(email: String) { call("POST", "/auth/v1/recover", JSONObject().put("email", email), false) }
    suspend fun updateInfo(first: String, last: String, year: String) {
        user = JSONObject(call("PUT", "/auth/v1/user", JSONObject().put("data",
            JSONObject().put("first_name", first).put("last_name", last).put("year", year))))
    }
    suspend fun deleteAccount() { call("POST", "/rest/v1/rpc/delete_me"); signOut() }
    fun signOut() { token = null; user = null }

    suspend fun list(table: String, query: String = "order=created_at.desc") = JSONArray(call("GET", "/rest/v1/$table?$query"))
    suspend fun insert(table: String, row: JSONObject) { call("POST", "/rest/v1/$table", row.put("user_id", userId)) }
    suspend fun update(table: String, id: String, row: JSONObject) { call("PATCH", "/rest/v1/$table?id=eq.$id", row) }
    suspend fun delete(table: String, id: String) { call("DELETE", "/rest/v1/$table?id=eq.$id") }

    suspend fun ai(type: String, text: String, pdfBase64: String? = null) =
        JSONObject(call("POST", "/functions/v1/ai-assist", JSONObject().put("type", type).put("text", text)
            .apply { if (pdfBase64 != null) put("pdf", pdfBase64) })).optString("output")
}

fun JSONArray.objs(): List<JSONObject> = (0 until length()).map { getJSONObject(it) }
