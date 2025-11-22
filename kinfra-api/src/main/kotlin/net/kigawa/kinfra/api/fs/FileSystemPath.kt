package net.kigawa.kinfra.api.fs

class FileSystemPath(
    val strPath: String,
) {
    init {
        require(strPath.isNotBlank()) { "path is blank" }
    }

    fun join(path: String) = FileSystemPath("$strPath/$path")
    override fun toString(): String {
        return strPath
    }
}