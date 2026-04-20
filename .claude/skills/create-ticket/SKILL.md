---
name: create-ticket
description: "Create a ChatUp ticket in TICKETS.md end-to-end: classify type, draft title and description from the canonical template, then append to TICKETS.md. Use when asked to create, open, or add a ticket (e.g. 'Create a ticket for …')."
argument-hint: "Short description of what the ticket is for"
allowed-tools: Read, Edit, Bash(grep -o "CU-[0-9]*" TICKETS.md)
---

# Create Ticket — ChatUp

Use this skill to create a well-structured ticket in `TICKETS.md` following ChatUp conventions.

The request is: `$ARGUMENTS`

---

## Step 1 — Classify the ticket

| Type | When | Example title |
|------|------|---------------|
| `feature` | New screen, component, or capability | `Implement ChatRepository with Flow` |
| `bug` | Something broken or incorrect | `Fix typing indicator not clearing on exit` |
| `chore` | Technical work, no user-visible change | `Add Hilt modules and DI setup` |
| `refactor` | Restructure without behaviour change | `Migrate FirebaseManager to data source layer` |
| `spike` | Time-boxed investigation | `Spike: Evaluate Paging 3 for message history` |

---

## Step 2 — Determine the next CU number

1. Read `TICKETS.md`
2. Scan for all occurrences of `CU-(\d+)` in ticket headings
3. Take the highest number found — call it `MAX`
4. Next ticket number is `MAX + 1`

If no CU-prefixed tickets found, start from `CU-1`.

---

## Step 3 — Draft the ticket

### Title format
- Feature/Refactor: `CU-<N> <verb> <subject>` — e.g. `CU-03 Implement ChatRepository`
- Bug: `CU-<N> Fix <what's broken>` — e.g. `CU-07 Fix typing indicator memory leak`
- Chore: `CU-<N> <technical task>` — e.g. `CU-02 Add Hilt DI setup`
- Spike: `CU-<N> Spike: <question>` — e.g. `CU-09 Spike: Evaluate Paging 3 for chat`

### Priority mapping
| User says | Priority |
|-----------|----------|
| critical / urgent / blocking | high |
| important / should fix | medium |
| nice-to-have / low / later | low |
| (default) | medium |

### Feature / Refactor template

```markdown
## CU-<N> — <Title>

- Status: todo
- Priority: <high | medium | low>
- Type: <feature | refactor>
- Layer: <data | domain | ui | di | all>

### Context
<Why does this work need to happen? What problem does it solve?>

### Acceptance Criteria
- [ ] AC1: <Specific, testable outcome>
- [ ] AC2: <Specific, testable outcome>

### Scope
In scope:
- <What is included>

Out of scope:
- <What is explicitly not included>

### Technical Notes
- Module: <auth | chat | conversations | group | profile | search | settings | shared>
- New Gradle dependency: <Yes — name and reason | No>
- Breaking change: <Yes — describe | No>
- Dependencies: <Other CU ticket IDs or "none">

### Edge Cases
- [ ] Loading state handled
- [ ] Error state handled
- [ ] Empty state handled

---
```

### Bug template

```markdown
## CU-<N> — <Title>

- Status: todo
- Priority: <high | medium | low>
- Type: bug

### What Happens
<Actual behaviour>

### What Should Happen
<Expected behaviour>

### Steps to Reproduce
1. <Step 1>
2. <Step 2>

### Environment
- Device: <e.g. Pixel 7, Samsung Galaxy S23>
- Android version: <e.g. Android 14>
- Screen: <e.g. ChatActivity>

### Logs / Screenshots
<Paste logcat errors or "Not yet captured">

---
```

### Chore / Spike template

```markdown
## CU-<N> — <Title>

- Status: todo
- Priority: <high | medium | low>
- Type: <chore | spike>

### Context
<Why is this work needed? What does it enable?>

### Done When
- [ ] <Concrete, verifiable outcome>
- [ ] <Concrete, verifiable outcome>

### Technical Notes
- <Relevant details, constraints>
- New Gradle dependency: <Yes — name and reason | No>

---
```

---

## Step 4 — Append to TICKETS.md

Read `TICKETS.md`, then append the drafted ticket block above the trailing comment (or at the end if none).

---

## Step 5 — Report back

```
✅ Ticket created: CU-<N> — <Title>
Type: <type>
Priority: <priority>
Suggested branch: chatup/<short-description>
```

---

## Rules

- Never create duplicate tickets — check existing titles before creating
- One ticket per concern — never bundle multiple unrelated issues
- Always use the correct template for the ticket type
- Layer field must match one of: data | domain | ui | di | all
