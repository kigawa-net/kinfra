package net.kigawa.kinfra.infrastructure.file

import net.kigawa.kinfra.model.conf.HomeDirGetter
import java.nio.file.Path
import kotlin.io.path.Path

/**
 * システムプロパティを使用したデフォルトのHomeDirGetter実装
 */
class SystemHomeDirGetter: HomeDirGetter {
    override fun getHomeDir(): Path {
        return System.getProperty("user.home")?.let { Path(it) }
            ?: throw IllegalStateException("user.home system property is not set")
    }
}
