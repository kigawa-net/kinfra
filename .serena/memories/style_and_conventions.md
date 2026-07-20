# Code Style and Conventions for kinfra

## Language
- **Primary Language**: Kotlin (JVM)
- **Version**: Kotlin 2.2.0, JDK 21

## Formatting
- **Indentation**: 4 spaces
- **Naming Conventions**:
  - Classes/Interfaces: PascalCase
  - Methods/Variables: camelCase
  - Constants: UPPER_SNAKE_CASE
- **Imports**: Alphabetically ordered, no wildcard imports
- **Error Handling**: Use Res<T, E> (Result type)
- **Dependency Injection**: Use Koin
- **Logging**: Use Logger interface
- **Output**: Use AnsiColors for colored output

## Architecture
- **Pattern**: Clean Architecture with multi-module structure
- **Modules**:
  - model - Domain models and interfaces
  - action - Business logic interfaces
  - infrastructure - Implementation layer
  - app-cli - CLI application
  - app-web - Web application

## Serialization
- Use kotlinx.serialization for JSON
- Use Gson for dynamic JSON generation
- Use kaml for YAML

## Testing
- Use JUnit 5
- Test naming: describe what the test does

## Git
- Follow conventional commit messages
- Use feature branches