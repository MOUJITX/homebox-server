# HomeBox

A Spring Boot backend application.

## Tech Stack

- Java 17
- Spring Boot 4.0.3
- Gradle 8.14

## Project Structure

```
src/main/java/com/moujitx/homebox/server/
└── HomeBoxApplication.java        # Application entry point
src/main/resources/
└── application.yml                # Application configuration
```

## Getting Started

### Prerequisites

- JDK 17+

### Build & Run

```bash
# Build the project
./gradlew build

# Run the application
./gradlew bootRun
```

The server starts on `http://localhost:8080` by default.
