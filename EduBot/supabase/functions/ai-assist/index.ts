// Edge Function: forwards the request to Google Gemini and returns { output }. Needs secret GEMINI_API_KEY.
const prompts: Record<string, string> = {
  summary: "Summarise these lecture notes clearly with bullet points.",
  quiz: "Create a 5-question multiple-choice quiz (A-D) with an answer key from this material.",
  study_plan: "Create a day-by-day study plan from this description.",
  chat: "You are Edu Bot, a helpful academic assistant for South African university students.",
};
// Tried in order; if one is overloaded (503/429) we retry, then move on to the next
const models = ["gemini-2.5-flash", "gemini-2.5-flash-lite", "gemini-flash-latest", "gemini-flash-lite-latest", "gemini-2.0-flash"];
const reply = (output: string) => new Response(JSON.stringify({ output }), { headers: { "content-type": "application/json" } });
const sleep = (ms: number) => new Promise((r) => setTimeout(r, ms));

Deno.serve(async (req) => {
  const key = Deno.env.get("GEMINI_API_KEY");
  if (!key) return reply("Server error: GEMINI_API_KEY secret is not set in Supabase.");
  const { type, text, pdf } = await req.json(); // pdf = optional base64 PDF
  const body = JSON.stringify({
    systemInstruction: { parts: [{ text: prompts[type] ?? prompts.chat }] },
    contents: [{ role: "user", parts: pdf ? [{ text }, { inlineData: { mimeType: "application/pdf", data: pdf } }] : [{ text }] }],
  });
  const errors: string[] = []; // one line per failed attempt, shown to the user for diagnosis
  for (const m of models) {
    for (let attempt = 0; attempt < 2; attempt++) {
      const r = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/${m}:generateContent`, {
        method: "POST", headers: { "x-goog-api-key": key, "content-type": "application/json" }, body });
      const j = await r.json();
      const out = j.candidates?.[0]?.content?.parts?.[0]?.text;
      if (out) return reply(out);
      errors.push(`${m} [${r.status}]: ${(j.error?.message ?? "empty response").slice(0, 90)}`);
      if (r.status !== 503 && r.status !== 429) break; // real error: skip retries, try next model
      await sleep(1500);
    }
  }
  return reply("All Gemini models failed:\n" + errors.join("\n"));
});
