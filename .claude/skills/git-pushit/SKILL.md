---
name: git-pushit
description: 'Combined stage + commit + push workflow for ChatUp. Use when: committing and pushing in one step, git pushit, ship changes, stage + commit + push.'
argument-hint: 'Optional: commit type (feat|fix|docs|style|refactor|test|chore|perf) — omit for first commit on branch'
allowed-tools: Bash(git status), Bash(git diff --stat), Bash(git diff), Bash(git log --oneline -5), Bash(git add *), Bash(git commit -m *), Bash(git rev-parse --abbrev-ref HEAD), Bash(git rev-parse --abbrev-ref --symbolic-full-name @{u}), Bash(git push -u origin *), Bash(git push)
---

# Git Pushit — Stage + Commit + Push — ChatUp

Combined workflow. Runs commit step and, only on success, the push step.

The optional argument is: `$ARGUMENTS`

---

## Step 1 — Follow git-commit skill

Follow `.claude/skills/git-commit/SKILL.md` in full:

1. Run `git status` and `git diff --stat` to survey all changed files
2. Group by feature cohesion — stage specific files by name, never `git add .`
3. Draft commit message:
   - **First commit on branch** → `[claude] (MODEL_NAME) Title Case Description`
   - **Subsequent commit** → `[claude] (MODEL_NAME) [<type>] imperative description`
4. Commit. Never `--no-verify`.

**If commit fails, stop. Do not proceed to push.**

---

## Step 2 — Follow git-push skill

Only after commit succeeds, follow `.claude/skills/git-push/SKILL.md` in full:

1. Verify branch is not `main` — stop and warn if so
2. Show last 5 commits so user can confirm
3. Check for upstream:
   - No upstream → `git push -u origin <branch>`
   - Upstream exists → `git push`
4. On success: display output, remind user PRs target `main`
5. On failure: diagnose error, never force-push without confirmation

---

## Completion checks

- [ ] Specific files staged — no `git add .` or `git add -A`
- [ ] Commit message starts with `[claude]` in correct format
- [ ] No hooks skipped
- [ ] Branch is not `main`
- [ ] Recent commits shown before push
- [ ] Push succeeded and output displayed
- [ ] User reminded PR targets `main`
