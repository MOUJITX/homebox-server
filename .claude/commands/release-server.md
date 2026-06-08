---
allowed-tools: Bash(cd:*), Bash(git tag:*), Bash(git log:*), Bash(git push:*), AskUserQuestion
description: Tag a new server version and push to origin
---

Release a new version tag for the **server** repository.

Follow these steps:

1. Run `git tag --sort=-v:refname | head -5` to find the latest version tag (e.g. `v0.5.1`).
2. Parse the current major, minor, and patch numbers from the latest tag.
3. Ask the user which version level to bump — **major** (`X.0.0`), **minor** (`x.Y.0`), or **patch** (`x.y.Z`) — using the question tool. Default suggestion: patch.
4. Compute the new version string (e.g. current `v0.5.1` + patch → `v0.5.2`).
5. Confirm with the user: "Create tag `vX.Y.Z` and push to origin?" before proceeding.
6. Create the annotated tag: `git tag -a vX.Y.Z -m "Release vX.Y.Z"`.
7. Push the tag to origin: `git push origin vX.Y.Z`.
8. Print the new tag name and confirm success.
