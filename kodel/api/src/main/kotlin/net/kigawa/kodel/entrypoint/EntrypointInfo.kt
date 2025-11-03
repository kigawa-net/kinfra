package net.kigawa.kodel.entrypoint

class EntrypointInfo (
    val name: EntrypointName,
    val aliases: List<EntrypointName>,
    val description: String,
){
    constructor(name: String, aliases: List<String>, description: String):
        this(EntrypointName(name), aliases.map(::EntrypointName), description)
}