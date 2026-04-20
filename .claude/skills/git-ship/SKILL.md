---
name: git-ship
description: "Ship a completed ChatUp feature branch end-to-end: run validation, sync with dev, commit, push, and open a PR. Use when: shipping a finished feature, sending code for review, committing and pushing a completed change, open a PR."
argument-hint: "Optional: PR description notes"
allowed-tools: Bash(git status), Bash(git diff --stat), Bash(git diff), Bash(git log --oneline *), Bash(git add *), Bash(git commit -m *), Bash(git rev-parse --abbrev-ref HEAD), Bash(git rev-parse --abbrev-ref --symbolic-full-name @{u}), Bash(git fetch origin), Bash(git rebase origin/dev), Bash(git push -u origin *), Bash(git push), Bash(gh pr create *), Bash(gh pr view *), Bash(./gradlew lint), Bash(./gradlew build)
---

# Git Ship — ChatUp

Ships the current `chatup/*` feature branch to remote and opens a PR targeting `dev`.
Run each step sequentially — do not skip steps.

The optional argument is: `$ARGUMENTS`

---

## Step 1 — Verify the branch

```bash
git rev-parse --abbrev-ref HEAD
```

- If branch is `dev` or `main`, **stop** — direct pushes not allowed
- Confirm branch follows `chatup/...` convention

---

## Step 2 — Run validation

```bash
./gradlew lint
./gradlew build
```

Both must pass before committing. Fix all lint errors. Build must succeed with no Kotlin compilation errors.

**If either fails, stop and fix before continuing.**

---

## Step 3 — Sync with dev

```bash
git fetch origin
git rebase origin/dev
```

If rebase has conflicts, **stop and report to developer**. Never auto-resolve conflicts.

---

## Step 4 — Commit all changes

Follow `.claude/skills/git-commit/SKILL.md` in full:

1. `git status` and `git diff --stat`
2. If working tree is clean, skip to Step 5
3. Stage specific files — never `git add .`
4. Commit with correct format

**If commit fails, stop.**

---

## Step 5 — Push the branch

Follow `.claude/skills/git-push/SKILL.md` in full:

1. Show last 5 commits so user can confirm
2. Verify `[claude]` prefix on all commits — note any that don't
3. Push:
   - No upstream → `git push -u origin <branch>`
   - Upstream set → `git push`
4. Never force-push without explicit user confirmation

**If push fails, stop.**

---

## Step 6 — Open a PR to dev

```bash
gh pr create --base dev --title "[claude] Title" --body "<body>"
```

### PR Title
```
[claude] Title Case Summary
```

### PR Body
```markdown
## Implementation Complete

### Summary
<One or two sentences describing what was built and why.>

### What Was Done
1. **Data Layer** — <item or "Not touched">
2. **Repository** — <item or "Not touched">
3. **ViewModel** — <item or "Not touched">
4. **UI** — <item or "Not touched">
5. **DI / Hilt** — <item or "Not touched">

### Manual Test Steps
1. <Step 1>
2. <Step 2>

### Notes / Tradeoffs
- <TODOs? New dependencies? Breaking changes?>
```

After PR is created, display the PR URL to the user.

---

## Completion checks

- [ ] Branch is not `dev` or `main`
- [ ] `./gradlew lint` passes
- [ ] `./gradlew build` passes
- [ ] Synced with `origin/dev` via rebase — no conflicts
- [ ] Specific files staged only — no `git add .`
- [ ] Commit follows `[claude]` format
- [ ] No hooks skipped
- [ ] Push succeeded
- [ ] PR created targeting `dev` with template filled
- [ ] PR URL displayed to user
