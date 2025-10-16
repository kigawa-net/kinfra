package net.kigawa.kinfra.model.conf

import java.nio.file.Path

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
    fun getHomeDir(): Path
}
