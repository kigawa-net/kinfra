package net.kigawa.kinfra.model.conf

/**
 * サブプロジェクト情報
 * 
 * @param name サブプロジェクト名
 * @param path サブプロジェクトのパス（省略時はnameと同じ）
 */
data class SubProject(
    val name: String,
    val path: String = name
) {
    /**
     * 文字列表現を返す（YAMLシリアライズ用）
     */
    override fun toString(): String {
        return if (path == name) {
            name
        } else {
            "$name:$path"
        }
    }
    
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
                SubProject(parts[0].trim(), parts[1].trim())
            } else {
                SubProject(input.trim())
            }
        }
    }
}