---
name: git-push
description: 'Use when: pushing the current branch to remote, publishing local commits, or asked to push code. Checks for upstream remote and sets one if missing. Never force-pushes without explicit confirmation.'
argument-hint: 'Optional: --force-with-lease (only with explicit user confirmation)'
allowed-tools: Bash(git rev-parse --abbrev-ref HEAD), Bash(git rev-parse --abbrev-ref --symbolic-full-name @{u}), Bash(git push -u origin *), Bash(git push), Bash(git log --oneline -5), Bash(git status)
---

# Git Push — ChatUp

Push the current `chatup/*` branch to `origin`, following ChatUp branch conventions.

The optional argument is: `$ARGUMENTS`

---

## Step 1 — Verify the current branch

```bash
git rev-parse --abbrev-ref HEAD
```

- If branch is `main`, **stop and warn the user** — direct pushes to main are not allowed
- If branch follows `chatup/...` convention, proceed
- If branch name doesn't follow convention, note it but still proceed

---

## Step 2 — Show recent commits

```bash
git log --oneline -5
```

Display to user so they can confirm what is being pushed. Verify commits follow `[claude]` prefix. Note any that don't.

---

## Step 3 — Check for upstream remote

```bash
git rev-parse --abbrev-ref --symbolic-full-name @{u} 2>/dev/null
```

- Empty → no upstream set → Step 4a
- Shows remote → Step 4b

---

## Step 4a — Push and set upstream (first push)

```bash
git push -u origin $(git rev-parse --abbrev-ref HEAD)
```

---

## Step 4b — Push (upstream already set)

```bash
git push
```

---

## Step 5 — Confirm or report failure

On **success**: show push output and remind user that PRs target `main`.

On **failure**:

| Error | Cause | Fix |
|-------|-------|-----|
| `rejected … non-fast-forward` | Remote has newer commits | `git pull --rebase origin main` then push |
| `remote: Permission denied` | SSH/token issue | Check GitHub token scopes |
| `error: failed to push some refs` | Force push needed after rebase | Ask user confirmation before `--force-with-lease` |

> ⚠️ Never force-push without explicit user confirmation. Never force-push to `main`.

---

## Completion checks

- [ ] Branch is not `main`
- [ ] Recent commits shown to user
- [ ] Commits follow `[claude]` convention (note any that don't)
- [ ] Push succeeded and output displayed
- [ ] User reminded PR should target `main`
