# Suggested Commands for Development

## Build Commands
- `./gradlew build` - Build the entire project
- `./gradlew :app-cli:shadowJar` - Create CLI JAR
- `./gradlew :app-web:shadowJar` - Create Web JAR

## Test Commands
- `./gradlew test` - Run all tests
- `./gradlew test --tests "net.kigawa.kinfra.TestClass.testMethod"` - Run specific test

## Lint and Format Commands
- `./gradlew ktlintCheck` - Check code style
- `./gradlew ktlintFormat` - Format code

## Run Commands
- `./gradlew :app-cli:run --args="<command>"` - Run CLI with arguments
- `./gradlew :app-web:run` - Run web server

## Utility Commands
- `git status` - Check git status
- `git add .` - Stage all changes
- `git commit -m "message"` - Commit changes
- `git push` - Push to remote

## When Task is Completed
Run: `./gradlew ktlintFormat && ./gradlew ktlintCheck && ./gradlew test && ./gradlew build`