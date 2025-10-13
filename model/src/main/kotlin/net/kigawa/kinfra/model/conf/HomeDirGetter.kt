package net.kigawa.kinfra.model.conf

/**
 * ホームディレクトリを取得するインターフェース
 *
 * テスト時に異なる実装を注入できるようにするための抽象化
 */
interface HomeDirGetter {
    /**
     * ユーザーのホームディレクトリパスを取得
     * @return ホームディレクトリの絶対パス
     */
    fun getHomeDir(): String
}

/**
 * システムプロパティを使用したデフォルトのHomeDirGetter実装
 */
class SystemHomeDirGetter : HomeDirGetter {
    override fun getHomeDir(): String {
        return System.getProperty("user.home")
            ?: throw IllegalStateException("user.home system property is not set")
    }
}
