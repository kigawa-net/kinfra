# ColorLogger Usage

## Overview

`ColorLogger` is a console logger with category-based color coding for CLI output. It works with the `LogCategory` enum from the model module to provide consistent, colorful terminal output.

## Available Log Categories

- **INFO** - General information messages (cyan)
- **SUCCESS** - Success messages (green)
- **WARNING** - Warning messages (yellow)
- **ERROR** - Error messages (red)
- **COMMAND** - Command execution output (blue)
- **DEBUG** - Debug messages (magenta)

## Usage Examples

### Basic Usage

```kotlin
import net.kigawa.kinfra.util.ColorLogger

// Log different types of messages
ColorLogger.info("Starting operation...")
ColorLogger.success("Operation completed successfully")
ColorLogger.warning("This is a warning message")
ColorLogger.error("An error occurred")
ColorLogger.command("Running: terraform init")
ColorLogger.debug("Debug information")
```

### Using with LogCategory

```kotlin
import net.kigawa.kinfra.model.LogCategory
import net.kigawa.kinfra.util.ColorLogger

// Direct category specification
ColorLogger.log("Custom message", LogCategory.INFO)
```

### Migration from AnsiColors

**Before:**
```kotlin
println("${AnsiColors.YELLOW}Warning:${AnsiColors.RESET} Failed to pull from git repository")
println("${AnsiColors.BLUE}Working directory:${AnsiColors.RESET} ${config.workingDirectory}")
println("${AnsiColors.GREEN}✓${AnsiColors.RESET} Operation successful")
```

**After:**
```kotlin
ColorLogger.warning("Warning: Failed to pull from git repository")
ColorLogger.info("Working directory: ${config.workingDirectory}")
ColorLogger.success("✓ Operation successful")
```

## Benefits

1. **Consistent Colors**: All log messages use predefined colors based on their category
2. **Cleaner Code**: No need for manual ANSI color codes and reset sequences
3. **Type Safety**: Using `LogCategory` enum ensures valid categories
4. **Better Readability**: Intent is clear from method names (info, success, warning, etc.)

## AnsiColors Helper Methods

The `AnsiColors` object also provides category-aware helper methods:

```kotlin
import net.kigawa.kinfra.util.AnsiColors
import net.kigawa.kinfra.model.LogCategory

// Colorize text based on category
val coloredText = AnsiColors.colorize("Message", LogCategory.SUCCESS)

// Get color code for a category
val color = AnsiColors.getColorForCategory(LogCategory.ERROR)
```
