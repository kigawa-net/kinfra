# Suggested Commands for kinfra Project

## Build and Test
- `./gradlew build` - Build the entire project
- `./gradlew test` - Run all tests
- `./gradlew :kinfra-infra:build` - Build specific module
- `./gradlew :kinfra-infra:test` - Test specific module

## Development
- `./gradlew :app-cli:run --args="<command>"` - Run CLI application
- `./gradlew :app-web:run` - Run web application

## Deployment
- `kinfra init` - Initialize Terraform
- `kinfra plan` - Plan Terraform changes
- `kinfra apply` - Apply Terraform changes
- `kinfra deploy` - Full deploy (init + plan + apply)

## Utility
- `./gradlew clean` - Clean build artifacts
- `./gradlew shadowJar` - Create fat JAR
- `./gradlew distTar` or `./gradlew distZip` - Create distribution archives

## Testing Specific
- `./gradlew test --tests "TestClass.testMethod"` - Run specific test