# When Task is Completed

After implementing or modifying code:

1. **Build**: Run `./gradlew build` to ensure compilation
2. **Test**: Run `./gradlew test` to verify functionality
3. **Lint**: ktlint is temporarily disabled, but check manually
4. **Documentation**: Update relevant docs if needed
5. **Commit**: Use conventional commit messages

For new features:
- Add tests
- Update README if user-facing
- Update AGENTS.md for tool changes

For bug fixes:
- Add regression tests
- Update changelog if applicable