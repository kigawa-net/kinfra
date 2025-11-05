package net.kigawa.kodel.api.entrypoint

data class EntrypointName(
    val raw: String,
) {
    init {
        require(raw.isNotBlank()) { "entrypoint name cannot be blank" }
        require(!raw.contains(" ")) { "entrypoint name cannot contain space" }
        raw.forEach {
            if (it == '-') return@forEach
            if (it.isDigit()) return@forEach
            require(it.isLowerCase()) { "entrypoint name must be lowercase" }
            require(it.isLetterOrDigit()) {
                "entrypoint name can contain only lowercase letters, digits, underscore and hyphen"
            }
        }
    }
}
