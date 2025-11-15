package net.kigawa.kinfra.api.io

class FileSystemPath(
    val strPath: String
) {
    init {
        require(strPath.isNotBlank()) {"path is blank"}
    }

    override fun toString(): String {
        return strPath
    }
}