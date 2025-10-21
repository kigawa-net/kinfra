package net.kigawa.kinfra.model

import java.nio.file.Path

/**
 * サブプロジェクト情報
 */
interface SubProject {
    val name: String
    val path: String

    companion object {
        /**
         * 文字列からSubProjectをパース
         *
         * @param input "name" または "name:path" 形式の文字列
         * @return SubProjectインスタンス
         */
        fun fromString(input: String): SubProject {
            return if (':' in input) {
                val parts = input.split(':', limit = 2)
                SubProjectImpl(parts[0].trim(), parts[1].trim())
            } else {
                SubProjectImpl(input.trim())
            }
        }
    }
}
