CLAUDE.md — ChatUp
AI-Assisted Development Workflow
> This document defines how Claude Code should work in this repository.
> Claude Code works interactively with the developer — changes are reviewed in real time before committing.

---

## Project Overview

ChatUp is an Android real-time chat application built as a portfolio showcase project.
Demonstrates production-grade Android architecture: clean MVVM, Firebase backend, and modern Kotlin patterns.

Key features: one-on-one messaging, group chats, Google Sign-In, typing indicators, message delivery/seen status, user profiles.

Portfolio goal: Show senior-level Android architecture decisions — clean separation of concerns, testable code, idiomatic Kotlin.

---

## Tech Stack

| Layer              | Technology                              |
|--------------------|-----------------------------------------|
| Language           | Kotlin 2.0.21                           |
| Min SDK            | 26 / Target SDK 36                      |
| Architecture       | MVVM + Repository + Use Cases           |
| UI                 | XML layouts + ViewBinding               |
| Async              | Kotlin Coroutines + Flow                |
| Dependency Injection | Hilt                                  |
| Backend            | Firebase Firestore + Firebase Auth      |
| Image loading      | Glide 4.16.0                            |
| Auth               | Firebase Auth (email/password + Google) |
| Build system       | Gradle 8.13.2 (KTS)                     |
| Version control    | GitHub                                  |

---

## Architecture (STRICT)

### Layer Flow

```
Activity / Fragment  →  ViewModel  →  Use Case  →  Repository  →  Firebase Data Source
```

### Rules

- **Activities/Fragments**: UI only — observe LiveData/StateFlow, dispatch user events. No business logic.
- **ViewModels**: Hold and transform UI state. Call use cases. Never reference Android Context directly (use Application if needed).
- **Use Cases**: Single-responsibility business logic. Optional layer — only add when logic is non-trivial.
- **Repositories**: Abstract the data source. Return `Flow<Result<T>>` or `suspend fun`. No Firebase SDK imports outside the data layer.
- **Data Sources**: All Firebase/Firestore calls live here. No business logic.
- **Models**: Pure Kotlin data classes in `data/model/`. No Android imports.

### What NOT to do

- No direct Firebase calls in ViewModels or Fragments
- No singleton `FirebaseManager` mixing all operations
- No callbacks — use coroutines/Flow instead
- No hard-wiring dependencies — use Hilt injection
- No logic in Activities/Fragments beyond UI handling

---

## Package Structure

```
com.example.chatup/
├── data/
│   ├── model/          ← Pure Kotlin data classes (User, Message, Conversation)
│   ├── source/         ← Firebase data sources (ChatDataSource, AuthDataSource, etc.)
│   └── repository/     ← Repository implementations
├── domain/
│   ├── repository/     ← Repository interfaces
│   └── usecase/        ← Use cases (optional, add when needed)
├── ui/
│   ├── auth/           ← Login, Register screens (Activity + ViewModel)
│   ├── chat/           ← ChatActivity + ChatViewModel
│   ├── conversations/  ← ConversationListFragment + ViewModel
│   ├── group/          ← GroupChat screen
│   ├── profile/        ← ProfileActivity/Fragment + ViewModel
│   ├── search/         ← SearchActivity + ViewModel
│   └── settings/       ← SettingsActivity
├── di/                 ← Hilt modules (AppModule, FirebaseModule, etc.)
└── util/               ← Extensions, constants, helpers
```

---

## How Claude Code Works Here

- Read existing code before making any changes
- Show what will change before significant refactors
- Ask for clarification when a task is ambiguous
- Keep scope tight — only implement what is discussed
- Never add Gradle dependencies without explaining why and getting approval
- Never touch `google-services.json` or any secrets
- Never push or open PRs without explicit developer instruction
- Never rename packages/classes without confirming — affects all import statements

---

## Ticket / Task Tracking

Tickets are tracked in `TICKETS.md` at the project root — same workflow as Linear but kept local.

**Format:**
```
## CU-<N> — Title

- Status: todo | in-progress | done
- Priority: high | medium | low
- Description: <what and why>
- Acceptance Criteria:
  - [ ] AC1
  - [ ] AC2
```

When starting a ticket: update status to `in-progress`.
When done: update status to `done` and reference the branch/PR.

---

## Branch Naming

```
<type>/<short-description>
```

| Type | When |
|------|------|
| `feat` | New feature or screen |
| `fix` | Bug fix |
| `chore` | Config, deps, build — no src change |
| `refactor` | Restructure without behaviour change |
| `docs` | Documentation only |
| `test` | Tests only |
| `perf` | Performance improvement |
| `spike` | Time-boxed investigation |

Examples:
```
feat/chat-repository
fix/typing-indicator-memory-leak
chore/hilt-setup
refactor/firebase-data-source-split
```

---

## Commit Rules

All commits must start with `[claude] (MODEL_NAME)`.
MODEL_NAME must reflect the actual Claude model running the session — never hardcode a model name.

Examples of valid model names: `claude-sonnet-4-6`, `claude-opus-4-6`, `claude-haiku-4-5`

**First commit on branch:**
```
[claude] (claude-sonnet-4-6) Title Case Description
```

**Subsequent commits:**
```
[claude] (claude-sonnet-4-6) [<type>] imperative description
```

Types: `feat` `fix` `style` `refactor` `chore` `docs` `perf` `test`

Rules:
- Imperative tense always
- Under 72 characters
- Never include Co-Authored-By footer
- Never `git add .` or `git add -A`
- Never `--no-verify`

---

## Pull Request Rules

Target branch is always `dev`. Feature branches merge into `dev`; `dev` is periodically merged into `main`.

**PR Title:**
```
[claude] Title Case Summary
```

**PR Body Template (REQUIRED):**
```
## Implementation Complete

### Summary
<One or two sentences.>

### What Was Done
1. **Data Layer** — <item or "Not touched">
2. **Repository** — <item or "Not touched">
3. **ViewModel** — <item or "Not touched">
4. **UI** — <item or "Not touched">
5. **DI / Hilt** — <item or "Not touched">

### Manual Test Steps
1.
2.

### Notes / Tradeoffs
- <Placeholders? TODOs? New dependencies? Breaking changes?>
```

---

## Validation Commands

```bash
./gradlew lint          # Must pass — fix all errors before committing
./gradlew build         # Must succeed — no Kotlin compilation errors
./gradlew test          # Unit tests must pass
```

---

## Claude Code Restrictions

**Must NOT:**
- Add Gradle dependencies without approval
- Hardcode API keys, secrets, or Firebase config values
- Push or open PRs without explicit instruction
- Take destructive actions (delete files, rename packages) without confirmation
- Modify `google-services.json`
- Skip lint or build validation before committing
- Introduce callbacks where coroutines/Flow should be used

---

## Pre-Merge Checklist

- [ ] `./gradlew lint` passes
- [ ] `./gradlew build` passes
- [ ] Architecture layer rules respected (no Firebase in ViewModel, etc.)
- [ ] Hilt injection used — no manual instantiation of dependencies
- [ ] Coroutines/Flow used — no raw callbacks
- [ ] No hardcoded strings (use `strings.xml`)
- [ ] No secrets in code
- [ ] New dependencies approved
- [ ] Commits follow `[claude]` format
- [ ] PR template filled out

---

## Key Architecture Rules — Never Violate

- Firebase SDK only in the `data/source/` layer — never leak into ViewModel or UI
- ViewModels must be injected via Hilt `@HiltViewModel` — never instantiated manually
- All async work via coroutines/Flow — no `.addOnSuccessListener` callbacks in ViewModel or above
- Repository interfaces live in `domain/` — implementations in `data/` — this enables testability
- UI state exposed as `StateFlow<UiState>` or `LiveData` from ViewModel — never raw mutable fields
