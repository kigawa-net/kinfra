package net.kigawa.kodel.entrypoint

data class EntrypointName(
    val raw: String,
) {
    init {
        require(raw.isNotBlank()) { "entrypoint name cannot be blank" }
        require(!raw.contains(" ")) { "entrypoint name cannot contain space" }
        raw.forEach {
            require(it.isLowerCase()) { "entrypoint name must be lowercase" }
            require(it.isLetterOrDigit() || it == '-') {
                "entrypoint name can contain only lowercase letters, digits, underscore and hyphen"
            }
        }
    }
}