---
name: audit
description: "Use when: 'audit the codebase', 'scan for bugs', 'full project review', 'architecture review', 'check code quality', 'find issues', or wants a comprehensive health check of the ChatUp Android codebase. Covers bugs, architecture violations, MVVM layering, DI, coroutines/Flow, security, Kotlin health, and dead code."
version: 0.1.0
---

# Full Codebase Audit — ChatUp Android

Scan the entire project — not a single file, not a single layer. Every meaningful Kotlin file, every layer. Produce a prioritised report of everything wrong, risky, or drifting from standard.

Be thorough. Be blunt. Do not soften findings.

---

## Step 1 — Map the full codebase

Read every `.kt` file under `app/src/main/`. Do not skip files because they look simple. Also read `app/build.gradle.kts` and `CLAUDE.md`.

---

## Step 2 — Seven audit sections

### Section 1: Bug Hunt

Look for defects that cause incorrect behaviour or runtime errors:

- Firebase listeners registered but never removed — memory leaks / `IllegalStateException`
- `!!` (non-null assertion) on values that could be null — crash risk
- Coroutine jobs launched in `GlobalScope` — not tied to lifecycle
- `viewModelScope` not used in ViewModel — leaking coroutines
- Firestore `DocumentSnapshot` fields accessed without null checks
- Missing `try/catch` around Firebase operations that can throw
- `LiveData` or `StateFlow` updated from background thread without `postValue` / correct dispatcher
- Fragment `view` accessed after `onDestroyView` — crash on back navigation
- `RecyclerView.Adapter` `notifyDataSetChanged()` called excessively instead of `DiffUtil`
- Unhandled `Exception` in `catch` blocks (swallowed silently)

### Section 2: Architecture Violations (MVVM)

**Layer purity — flag any of these:**
- Firebase SDK import (`com.google.firebase`) in a ViewModel, Fragment, or Activity
- Business logic (conditionals, transformations) inside a Fragment or Activity
- Repository interface defined in `data/` instead of `domain/`
- ViewModel instantiated with `ViewModelProvider(this)` instead of Hilt injection
- Data class with Android imports (`Context`, `View`, etc.)
- `FirebaseManager` singleton still in use — should be replaced by data sources + repositories

**Data flow:**
- Firebase callbacks (`.addOnSuccessListener`) used above the data source layer
- Raw `MutableLiveData` or `MutableStateFlow` exposed publicly from ViewModel
- Fragment accessing ViewModel of another Fragment directly (should go through shared ViewModel or navArgs)

**Async:**
- `.addOnSuccessListener` / `.addOnFailureListener` callbacks in ViewModel or Repository — must be coroutines/Flow
- `runBlocking` used outside of tests
- `Dispatchers.Main` hardcoded — should be injected or use `viewModelScope`

### Section 3: Dependency Injection (Hilt)

- Classes that should be injected but are manually instantiated (`= SomeClass()` in ViewModel or Repository)
- Missing `@HiltViewModel` on ViewModels
- Missing `@Inject constructor` on classes that Hilt should provide
- Hilt modules not using `@Provides` / `@Binds` correctly
- Singleton scope misuse — classes that hold mutable state but aren't `@Singleton`
- Context injected where `@ApplicationContext` qualifier is missing

### Section 4: Security Risks

- Firebase Auth `currentUser` used without null check — could access data as unauthenticated user
- Firestore rules bypassed by using service account credentials client-side
- User input (chat messages, usernames) not sanitised before writing to Firestore
- `google-services.json` values hardcoded anywhere in source files
- Sensitive data (tokens, passwords) logged with `Log.d` or `Log.e`
- Missing authentication check before performing Firestore operations in a repository

### Section 5: Kotlin Health

- Every `!!` operator — list file and line, assess crash probability
- `as SomeType` unchecked casts — potential `ClassCastException`
- Functions returning `Any?` or `Unit?` where a typed result is expected
- `lateinit var` used where `by lazy` or constructor injection is more appropriate
- Mutable `var` properties on data classes that should be `val`
- String concatenation in loops instead of `StringBuilder`
- `when` expressions missing `else` branch on non-sealed types
- `// TODO` markers — list each with file and line

### Section 6: Coroutines and Flow

- `Flow` collected with `.collect {}` in a ViewModel instead of `stateIn` / `shareIn`
- Missing `flowOn(Dispatchers.IO)` for Firestore/network operations
- Cold flows recreated on every collection instead of being shared
- `Channel` used where `StateFlow` or `SharedFlow` is more appropriate
- Exception handling missing in Flow — uncaught exceptions cancel the flow silently
- `launchIn` called without proper scope binding

### Section 7: Dead Code and Technical Debt

- Unused imports
- Commented-out code blocks
- `Log.d` / `Log.e` / `println` left in production code
- Package named `mananger` (typo — should be `manager`)
- Duplicate logic written in multiple files
- Classes defined but never instantiated or injected
- Resources (strings, drawables) declared in XML but unused in code
- `TODO()` stubs that throw `NotImplementedError` at runtime

---

## Step 3 — Output the report

```
# ChatUp Codebase Audit
Date: <today>
Files scanned: <N>

---

## 🔴 Blockers — Fix Before Next Merge
<numbered list: file, line if known, exact problem, why it matters>

## 🟡 Warnings — Fix This Sprint
<numbered list: same format>

## 🟢 Suggestions — Technical Debt to Track
<numbered list: same format>

---

## Architecture Health: <score /10>
<What is solid, what is drifting, specific files causing drift>

## Security Health: <score /10>
<What is safe, what is exposed, specific risks>

## Kotlin / Coroutines Health: <score /10>
<Overall code quality, worst offenders>

## DI / Hilt Health: <score /10>
<How complete and correct the DI wiring is>

---

## Priority Action List
1. <Most critical fix>
2. <Second>
...up to 10 items

---

## What Is Solid
<Things done well — specific, not generic praise>
```

---

## Audit rules

- Read every `.kt` file before scoring anything
- Never report false positives — only flag issues found in actual code
- Cite file paths and line numbers wherever possible
- Suggest targeted fixes, not full rewrites
- Do not pad the report — if a section is clean, say so in one line
- Blockers are non-negotiable: crash risks, security issues, memory leaks, or architecture violations that make the code untestable
