# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Tech Stack

- Java 17
- Spring Boot 4.0.3
- Gradle 8.14 (wrapper included)

## Build Commands

- `./gradlew build` — compile, test, and package
- `./gradlew bootRun` — run the application locally
- `./gradlew test` — run tests only

## Project Layout

- `src/main/java/com/moujitx/homebox/server/` — application source code
- `src/main/resources/application.yml` — configuration
- `src/test/java/com/moujitx/homebox/server/` — tests

## Rules

- After every task, immediately update `README.md` and `CLAUDE.md` if the changes warrant documentation updates (e.g. new features, changed commands, altered architecture, new dependencies, updated setup steps).
- Commit changes only after all steps are approved by the user. For large modifications containing multiple small tasks or features, commit at each small task/feature boundary rather than one big commit at the end.
- Do not edit or create tests unless explicitly noted.
- When API usage changes, update the API usage doc (`docs/api.md`) and the Postman collection file (`docs/homebox.postman_collection.json`). If either file does not exist, create it.
