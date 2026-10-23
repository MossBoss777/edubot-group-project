# Edu Bot: AI Academic Assistant (Android)

Edu Bot is a mobile AI study assistant for South African university students. It turns lecture material into summaries, practice quizzes and study plans, answers academic questions in a chat, and keeps track of grades and class times.

Built for **ITMDA3-B34 (Eduvos, Midrand Campus), Deliverable 4: Implementation**, based on the research project *Evaluating the Effectiveness of AI-Powered Chatbots as Academic Learning Assistants for University Students*.

**Group 8:** Moeen Butt, Lesedi Matabologa, Phemelo Kago Padisho, Mohammad Saleh

---



## Features

| Feature | Description |
|---|---|
| **Authentication** | Sign up, sign in, sign out, forgot/reset password, update profile, delete account |
| **Note summaries** | Paste notes or attach a PDF and get a structured summary: headed sections, bullet points and a key-terms table |
| **Quiz generation** | Five multiple-choice questions with tappable answers, instant right/wrong feedback, explanations and a final score |
| **Study plans** | A day-by-day table (Day, Topic, Activity, Hours) built from the exam date and hours available |
| **Chat assistant** | A dedicated chat page for questions, explanations, code help and translation |
| **Timetable** | The student describes their classes in plain text, the AI converts them into entries, and the Home card shows the next class |
| **Modules and grades** | Log, view, edit and delete marks per module with an automatic percentage |
| **History** | View and delete saved summaries, quizzes and study plans |
| **File attachments** | Attach PDFs (up to 10 MB) or `.txt` files |

### CRUD modules
- **Grades:** create, read, update and delete (table `grades`)
- **Timetable:** create (by prompt), read, delete (table `timetable`)
- **AI history:** create (automatic), read, delete (table `sessions`)

---

## Tech stack

| Layer | Technology |
|---|---|
| Mobile app | Kotlin, Jetpack Compose (Material 3), Android Studio |
| Backend | Supabase (PostgreSQL, Auth, PostgREST, Edge Functions) |
| AI | Google Gemini API, called only from a Supabase Edge Function |
| Networking | OkHttp |
| Navigation | Single activity with a simple route back-stack |


The app never talks to the AI directly. It calls the `ai-assist` Edge Function, which removes common personal identifiers, adds the task-specific instructions, and returns JSON that the app renders as tables and cards. This means the AI provider can be changed without touching the app code.

---

## Project structure

```
EduBot/
├── app/src/main/java/com/edubot/app/
│   ├── MainActivity.kt   App entry point and navigation
│   ├── Api.kt            Supabase client: auth, CRUD helpers, AI calls
│   ├── Ui.kt             Theme, shared components (buttons, fields, cards, logo)
│   ├── Auth.kt           Welcome, Sign In, Sign Up screens
│   ├── Home.kt           Home screen: tools and tiles
│   ├── Chat.kt           Reusable chat area and the Chat page
│   ├── Format.kt         Response templates (summary, quiz, plan) and text cleaning
│   ├── Timetable.kt      Timetable card and prompt-based entry
│   ├── Grades.kt         Modules and grades (CRUD)
│   ├── History.kt        Saved AI results (read, delete)
│   └── Profile.kt        Profile, settings, account actions
├── app/src/main/res/     App icon and logo
├── supabase/functions/ai-assist/index.ts   Edge Function (Gemini)
├── supabase_setup.sql    Core tables, policies, delete-account function
├── supabase_update.sql   Timetable table and admin statistics view
└── local.properties.example
```

---

## Database

| Table | Purpose |
|---|---|
| `sessions` | Every AI interaction (type, input, output) |
| `grades` | Module marks: online test /10, assignment /40, exam /100 |
| `timetable` | Weekly classes (module, day, time, venue) |
| `auth.users` | Managed by Supabase Auth; profile details are stored in user metadata |

Row-Level Security is enabled on every table, so a student can only access their own rows.

---

## Setup

### Prerequisites
- Android Studio (latest stable) with an emulator or a physical Android device
- A free [Supabase](https://supabase.com) account
- A free [Google AI Studio](https://aistudio.google.com) API key

### 1. Clone and open
```
git clone <your-repository-url>
```
In Android Studio choose **File, then Open**, and select the folder that directly contains `settings.gradle.kts`.

### 2. Create the Supabase backend
1. Create a new Supabase project.
2. Open **SQL Editor** and run `supabase_setup.sql`, then `supabase_update.sql`.
3. (Optional, easier testing) Under **Authentication, Providers, Email**, turn off "Confirm email".

### 3. Deploy the AI function
1. Go to **Edge Functions**, create a function named exactly `ai-assist`, and paste in `supabase/functions/ai-assist/index.ts`.
2. Go to **Edge Functions, Secrets** and add `GEMINI_API_KEY` with your Gemini key.

### 4. Configure the app
Copy `local.properties.example` to `local.properties` and fill in your values:
```
sdk.dir=<path to your Android SDK>
SUPABASE_URL=https://YOUR-PROJECT-ID.supabase.co
SUPABASE_KEY=<your anon public key>
```
Use the Project URL and the legacy `anon` key from **Project Settings, API**. Never commit `local.properties`; confirm it is listed in `.gitignore`.

### 5. Run
Sync Gradle, choose an emulator (API 34 or similar), and press **Run**. If you change `local.properties`, use **Build, Clean Project** then **Rebuild Project**.

---

## Security and privacy

- **Authentication:** Supabase Auth stores passwords hashed; every request carries a signed JWT.
- **Data isolation:** Row-Level Security policies (`auth.uid() = user_id`) restrict every table to its owner.
- **Secrets:** the Gemini key exists only as a Supabase secret. The app contains only the public anon key.
- **Transport:** all traffic uses HTTPS.
- **Privacy:** the Edge Function removes e-mail addresses, phone numbers and student numbers from text before it is sent to the AI. Names written in free text are not detected.
- **Admin:** administration is done by the project owner in the Supabase dashboard. The `admin_usage_stats` view shows anonymised usage counts (requests per day and type, active students) with no identities or content, and is not readable by the app or students.

---


## Academic context

This project forms part of a research study on the effectiveness of AI chatbots as academic learning assistants in South African higher education, covering three structured use cases: note summarisation, quiz generation and personalised study planning.
