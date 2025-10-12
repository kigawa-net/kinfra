package net.kigawa.kinfra.util

import java.util.Properties

object VersionUtil {
    private var cachedVersion: String? = null

    fun getVersion(): String {
        if (cachedVersion != null) {
            return cachedVersion!!
        }

        cachedVersion = try {
            val properties = Properties()
            val inputStream = VersionUtil::class.java.classLoader
                .getResourceAsStream("version.properties")

            if (inputStream != null) {
                properties.load(inputStream)
                properties.getProperty("version", "dev")
            } else {
                "dev"
            }
        } catch (e: Exception) {
            "dev"
        }

        return cachedVersion!!
    }
}
