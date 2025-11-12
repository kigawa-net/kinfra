package net.kigawa.kinfra.api.resource

import net.kigawa.kinfra.api.HashValue
import net.kigawa.kinfra.api.Hasher
import net.kigawa.kinfra.api.KinfraContext
import kotlin.io.path.Path
import kotlin.io.path.pathString

class FilePathResource(
    strPath: String,
): KinfraResource {
    val path = Path(strPath)

    init {
        require(strPath.isNotEmpty()) { "path is empty" }
        require(!strPath.endsWith("/")) { "path must not end with /" }
    }

    override fun hash(hasher: Hasher, ctx: KinfraContext): HashValue {
        return hasher.hash(path.pathString)
    }
}