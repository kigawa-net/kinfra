package net.kigawa.kinfra.infra.cmd

import net.kigawa.kinfra.api.process.CmdExecutor
import net.kigawa.kinfra.api.process.ProcessConfig
import net.kigawa.kinfra.api.process.ProcessRes
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class LocalCmdExecutor: CmdExecutor {
    override suspend fun <SI, SO, SE> execute(processConfig: ProcessConfig<SI, SO, SE>): ProcessRes<SI, SO, SE> {
        val processBuilder = ProcessBuilder(processConfig.cmd.raw)

        // 作業ディレクトリの設定
        processConfig.workingDir?.let {
            processBuilder.directory(File(it.strPath))
        }

        // 環境変数の設定
        processConfig.env.let { env ->
            processBuilder.environment().putAll(env)
        }

        // エラー出力をリダイレクトするかどうか
        processBuilder.redirectErrorStream(false)

        // プロセスの起動
        val process = processBuilder.start()

        // 標準入力への書き込み
        val inRes = processConfig.stdin.let { stdin ->
            OutputStreamWriter(process.outputStream).use { writer ->
                WrapperWriter(writer).stdin()
            }
        }

        // 標準出力の読み取り
        val outRes = BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            processConfig.stdout(WrapperReader(reader))
        }

        // 標準エラー出力の読み取り
        val errRes = BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
            processConfig.stderr(WrapperReader(reader))
        }

        // プロセスの終了を待機
        val exitCode = process.waitFor()

        return ProcessRes(
            exitCode = exitCode,
            outputRes = outRes,
            errRes = errRes,
            inputRes = inRes
        )
    }
}