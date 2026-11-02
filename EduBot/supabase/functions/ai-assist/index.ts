const rules = "Reply with ONLY valid JSON, no markdown, no code fences. Use plain text inside values: never use asterisks, hashes or other markdown symbols. ";
const prompts: Record<string, string> = {
  summary: rules + `Summarise the content in exactly this shape: {"title":"...","overview":"one or two sentences","sections":[{"heading":"...","points":["...","..."]}],"key_terms":[{"term":"...","definition":"..."}]}. Use 3-6 sections and up to 8 key terms.`,
  quiz: rules + `Create 5 multiple-choice questions from the content in exactly this shape: {"title":"...","questions":[{"question":"...","options":["...","...","...","..."],"answer":"B","explanation":"..."}]}. Each question has exactly 4 options WITHOUT letter prefixes; "answer" is one letter A-D.`,
  study_plan: rules + `Create a day-by-day study plan in exactly this shape: {"title":"...","summary":"one sentence","rows":[{"day":"Day 1","topic":"...","activity":"...","hours":2}]}. Respect the exam date and daily hours given by the student; if missing assume 7 days at 2 hours per day. Include revision and a practice test near the end.`,
  timetable: rules + `Extract the weekly classes the student describes in exactly this shape: {"classes":[{"module":"...","day":"Monday","time":"10:00","venue":"..."}]}. "day" is the full English weekday name, "time" is 24-hour HH:mm, "venue" may be an empty string.`,
  chat: "You are Edu Bot, a helpful academic assistant for South African university students. Answer clearly in plain text. Do not use markdown symbols such as #, ** or backticks; use short paragraphs and simple numbered lines instead.",
};

const MODELS = ["gpt-4o-mini", "gpt-4.1-mini", "gpt-5-mini"];
const reply = (output: string) => new Response(JSON.stringify({ output }), { headers: { "content-type": "application/json" } });

const anonymise = (t: string) => t.replace(/[\w.+-]+@[\w-]+\.[\w.-]+/g, "[email]").replace(/\b(?:\+?\d[\s-]?){9,13}\b/g, "[phone]").replace(/\bEDUV\d+\b/gi, "[student no.]");

Deno.serve(async (req) => {
  const key = Deno.env.get("OPENAI_API_KEY");
  if (!key) return reply("Server error: OPENAI_API_KEY secret is not set in Supabase.");
  const { type, text: raw, pdf } = await req.json(); 
  const text = anonymise(String(raw ?? ""));
  const system = prompts[type] ?? prompts.chat;
  try {
    const content: unknown[] = [{ type: "text", text }];
    if (pdf) content.push({ type: "file", file: { filename: "document.pdf", file_data: `data:application/pdf;base64,${pdf}` } });
    let lastError = "no response";
    for (const model of MODELS) {
      const r = await fetch("https://api.openai.com/v1/chat/completions", {
        method: "POST", signal: AbortSignal.timeout(70000),
        headers: { Authorization: `Bearer ${key}`, "content-type": "application/json" },
        body: JSON.stringify({ model, messages: [{ role: "system", content: system }, { role: "user", content }],
          ...(type === "chat" ? {} : { response_format: { type: "json_object" } }) }),
      });
      const j = await r.json();
      const out = j.choices?.[0]?.message?.content;
      if (out) return reply(out);
      lastError = `[${r.status}] ${j.error?.message ?? "no response"}`;
      if (r.status !== 404 && j.error?.code !== "model_not_found") break;
    }
    return reply("OpenAI error " + lastError);
  } catch (_e) {
    return reply("The AI service did not answer in time. Please try again.");
  }
});
