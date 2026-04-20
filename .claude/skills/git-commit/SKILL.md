---
name: git-commit
description: 'Commit message formatting for ChatUp. Use when: committing changes, writing a commit message, staging and committing files, first commit on a branch, subsequent commits, git commit -m.'
argument-hint: 'Optional: commit type (feat|fix|docs|style|refactor|test|chore|perf|ci|build|revert) — omit for first commit on branch'
---

# Git Commit Format — ChatUp

All commits made with Claude Code assistance must start with the `[claude]` prefix. This is a strict project rule from CLAUDE.md.

## First Commit on a Branch

Mirror the branch description and use Title Case with model name:

```
[claude] (MODEL_NAME) <Title Case Description>
```

⚠️ Replace `MODEL_NAME` with the **actual Claude model running this session** — never hardcode a model name.
Common values: `claude-sonnet-4-6`, `claude-opus-4-6`, `claude-haiku-4-5`

Example (branch `chatup/hilt-setup`, running claude-sonnet-4-6):
```
[claude] (claude-sonnet-4-6) Hilt Setup and DI Module Configuration
```

## Subsequent Commits

```
[claude] (MODEL_NAME) [<type>] <imperative description>
```

Examples:
```
[claude] (claude-sonnet-4-6) [feat] Add ChatRepository with Flow-based message stream
[claude] (claude-sonnet-4-6) [fix] Fix typing indicator not clearing on screen exit
[claude] (claude-sonnet-4-6) [refactor] Migrate FirebaseManager to data source layer
[claude] (claude-sonnet-4-6) [chore] Add Hilt dependency to build.gradle.kts
```

## Commit Types

| Type | When |
|------|------|
| `feat` | New feature or screen |
| `fix` | Bug fix |
| `docs` | Documentation only |
| `style` | XML layout, no logic change |
| `refactor` | Restructure, no behaviour change |
| `test` | Tests only |
| `chore` | Config, deps, non-src changes |
| `perf` | Performance improvement |
| `ci` | CI/CD changes |
| `build` | Build system or dependency changes |
| `revert` | Reverts a previous commit |

## Multi-Commit Strategy

When changes span multiple concerns, split into focused commits:

1. Run `git status` and `git diff --stat` to survey all changed files
2. Group by feature cohesion — files changed for the same reason belong together
3. Order: foundational first (DI modules, models, interfaces), then data layer, then ViewModel, then UI
4. Stage each group explicitly with `git add <files>` — never `git add .` when splitting

## Rules

- Prefix is always `[claude] (MODEL_NAME)` — use the actual active model, never hardcode
- Never include `Co-Authored-By` footer
- Imperative mood: "Add feature" not "Added feature"
- Subject line under 72 characters — no body, no bullet points
- First commit on branch = no type prefix; all subsequent = type prefix required
- Never skip hooks (`--no-verify`) unless explicitly instructed
- Stage specific files by name — avoid `git add -A` or `git add .`
