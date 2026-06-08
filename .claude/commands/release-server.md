---
allowed-tools: Bash(cd:*), Bash(git tag:*), Bash(git log:*), Bash(git push:*), Bash(git status:*), Bash(git add:*), Bash(git commit:*), AskUserQuestion
description: Commit changes, tag a new server version, and push to origin
---

Release a new version for the **server** repository.

Follow these steps:

1. Run `git tag --sort=-v:refname | head -5` to find the latest version tag (e.g. `v0.5.1`).
2. Parse the current major, minor, and patch numbers from the latest tag.
3. Ask the user which version level to bump — **major** (`X.0.0`), **minor** (`x.Y.0`), or **patch** (`x.y.Z`) — using the question tool. Default suggestion: patch.
4. Compute the new version string (e.g. current `v0.5.1` + patch → `v0.5.2`).
5. Run `git status` to check for uncommitted changes.
   - If there are uncommitted changes, stage all changes (`git add -A`), commit with message `chore: release vX.Y.Z`, and confirm with the user.
   - If the working tree is clean, skip this step.
6. Confirm with the user: "Push commits and create tag `vX.Y.Z` on origin?" before proceeding.
7. Push commits to origin: `git push origin`.
8. Create the annotated tag: `git tag -a vX.Y.Z -m "Release vX.Y.Z"`.
9. Push the tag to origin: `git push origin vX.Y.Z`.
10. Print the new tag name and confirm success.
