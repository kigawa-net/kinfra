# Code Style and Conventions

## Language and Setup
- **Language**: Kotlin (JVM)
- **Indentation**: 4 spaces
- **Encoding**: UTF-8

## Naming Conventions
- **Classes/Interfaces**: PascalCase (e.g., `TerraformService`)
- **Methods/Variables**: camelCase (e.g., `runCommand`)
- **Constants**: UPPER_SNAKE_CASE (e.g., `DEFAULT_TIMEOUT`)

## Imports
- Alphabetical order
- No wildcard imports

## Error Handling
- Use `Res<T, E>` type (Result-like monad)
- Exceptions are thrown for config loading, handled by callers

## Dependency Injection
- Use Koin for DI
- Modules: AppModule, InfrastructureModule, BitwardenModule, ActionsModule, WebModule

## Logging
- Use `Logger` interface
- Log levels: DEBUG, INFO, WARN, ERROR
- Default level: INFO

## Output
- Use `AnsiColors` for colored console output
- Examples: `${AnsiColors.RED}`, `${AnsiColors.GREEN}`

## Serialization
- JSON: Gson
- YAML: kotlinx-serialization + kaml
- Models must have `@Serializable` annotation

## Architecture Patterns
- Layered architecture: model → action → infrastructure → app-*
- Commands registered in DI modules
- Terraform commands auto-redirect to SDK versions if BWS_ACCESS_TOKEN is set