---
name: implement-ticket
description: "Implement a ChatUp ticket end-to-end: look up the ticket in TICKETS.md, implement the feature following ChatUp MVVM standards, then update the ticket status. Use when asked to implement a ticket, work on a ticket, or start a ticket (e.g. 'Implement ticket CU-03')."
argument-hint: "Ticket ID, e.g. CU-03"
allowed-tools: Read, Edit, Write, Glob, Grep, Bash(git checkout dev), Bash(git pull origin dev), Bash(git checkout -b *), Bash(git branch --show-current), Bash(./gradlew lint), Bash(./gradlew build)
---

# Ticket Implementation — ChatUp

Use this skill to implement a ticket end-to-end, following all ChatUp MVVM architecture rules from `CLAUDE.md`.

The ticket to implement is: `$ARGUMENTS`

---

## Step 1 — Look up the ticket

1. Read `TICKETS.md` and find the ticket matching the given ID
2. Identify ticket type: **feature / bug / chore / refactor / spike**
3. Note the **layer**: data | domain | ui | di | all
4. Note the **module**: auth | chat | conversations | group | profile | search | settings | shared
5. Note acceptance criteria — these are the definition of done
6. If ticket touches **Hilt DI modules or interfaces**, confirm scope with developer before starting
7. Note any dependencies on other tickets

---

## Step 2 — Mark ticket as in-progress

Edit `TICKETS.md`: change `Status: todo` → `Status: in-progress` for this ticket.

---

## Step 3 — Create the feature branch

```bash
git checkout dev
git pull origin dev
git checkout -b chatup/<short-description>
git branch --show-current
```

---

## Step 4 — Plan the implementation

Read existing code in the relevant area before making any changes. Identify which files need to change:

| Layer | Location |
|-------|----------|
| Data model | `data/model/` |
| Firebase data source | `data/source/` |
| Repository impl | `data/repository/` |
| Repository interface | `domain/repository/` |
| Use case | `domain/usecase/` |
| ViewModel | `ui/<module>/` |
| Activity / Fragment | `ui/<module>/` |
| Hilt module | `di/` |
| Utilities | `util/` |

Check for existing classes before creating new ones.

---

## Step 5 — Implement the feature

Follow all architecture rules from `CLAUDE.md` strictly:

### Architecture rules (STRICT)
- **Firebase SDK only in `data/source/`** — never in ViewModel, Repository interface, or UI
- **ViewModels use `@HiltViewModel` + `@Inject constructor`** — never instantiated manually
- **All async work via coroutines/Flow** — no `.addOnSuccessListener` callbacks above the data layer
- **Repository interface in `domain/`** — implementation in `data/`
- **UI state exposed as `StateFlow<UiState>` or `LiveData`** — never raw mutable fields on ViewModel
- **Activities/Fragments: UI only** — observe state, dispatch events, no business logic

### State coverage
Every screen must handle:
- [ ] Loading state
- [ ] Error state
- [ ] Empty state (where applicable)

### Flow/coroutine conventions
- Firestore listeners → `callbackFlow { }` in data source
- One-shot operations → `suspend fun` returning `Result<T>`
- ViewModels launch in `viewModelScope`
- Repositories return `Flow<Result<T>>` or `suspend fun`

### Decision points

| Condition | Action |
|-----------|--------|
| New Gradle dependency needed | Stop — explain why, get approval |
| Touches authentication flow | Confirm with developer first |
| Firestore data model change | Flag for developer review |
| New Hilt module needed | Create in `di/` following existing module pattern |

---

## Step 6 — Run validation

```bash
./gradlew lint
```
Fix all errors. Warnings acceptable but note them.

```bash
./gradlew build
```
Must succeed with no Kotlin compilation errors.

**Both must pass before committing.**

---

## Step 7 — Update ticket status to done

Edit `TICKETS.md`:
- Change `Status: in-progress` → `Status: done`
- Add a line: `Branch: chatup/<branch-name>`

---

## Completion checks

- [ ] Ticket fetched from TICKETS.md and all acceptance criteria addressed
- [ ] Ticket marked `in-progress` before starting, `done` after finishing
- [ ] Feature branch created from `dev` following `chatup/...` convention
- [ ] Architecture layering correct — Firebase only in data source
- [ ] ViewModels use `@HiltViewModel` and `@Inject`
- [ ] Coroutines/Flow used — no raw Firebase callbacks above data layer
- [ ] Loading, error, and empty states handled in UI
- [ ] `./gradlew lint` passes with no errors
- [ ] `./gradlew build` succeeds
- [ ] Branch name added to ticket entry in TICKETS.md
