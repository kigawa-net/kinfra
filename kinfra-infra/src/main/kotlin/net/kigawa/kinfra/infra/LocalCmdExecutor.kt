package net.kigawa.kinfra.infra

import net.kigawa.kinfra.api.process.CmdExecutor
import net.kigawa.kinfra.api.process.ProcessConfig
import net.kigawa.kinfra.api.process.ProcessRes

class LocalCmdExecutor: CmdExecutor {
    override fun <SI, SO, SE> execute(processConfig: ProcessConfig<SI, SO, SE>): ProcessRes<SI, SO, SE> {
        val processBuilder = ProcessBuilder(processConfig.cmd.raw)

        // 作業ディレクトリの設定
        processConfig.workingDirectory?.let {
            processBuilder.directory(it)
        }

        // 環境変数の設定
        processConfig.environment?.let { env ->
            processBuilder.environment().putAll(env)
        }

        // エラー出力をリダイレクトするかどうか
        processBuilder.redirectErrorStream(false)

        // プロセスの起動
        val process = processBuilder.start()

        // 標準入力への書き込み
        processConfig.stdin?.let { stdin ->
            OutputStreamWriter(process.outputStream).use { writer ->
                processConfig.stdinSerializer(stdin, writer)
                writer.flush()
            }
        }

        // 標準出力の読み取り
        val stdout = BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            processConfig.stdoutDeserializer(reader)
        }

        // 標準エラー出力の読み取り
        val stderr = BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
            processConfig.stderrDeserializer(reader)
        }

        // プロセスの終了を待機
        val exitCode = process.waitFor()

        return ProcessRes(
            exitCode = exitCode,
            stdout = stdout,
            stderr = stderr,
            stdin = processConfig.stdin
        )
    }
}