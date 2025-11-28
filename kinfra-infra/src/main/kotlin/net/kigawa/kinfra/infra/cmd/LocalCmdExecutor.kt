package net.kigawa.kinfra.infra.cmd

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import net.kigawa.kinfra.api.process.CmdExecutor
import net.kigawa.kinfra.api.process.ProcessConfig
import net.kigawa.kinfra.api.process.ProcessRes
import net.kigawa.kodel.api.log.Logger
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter

class LocalCmdExecutor(
    val logger: Logger,
): CmdExecutor {
    override suspend fun <SI, SO, SE> execute(processConfig: ProcessConfig<SI, SO, SE>): ProcessRes<SI, SO, SE> {
        logger.debug("execute: ${processConfig.cmd.raw}")
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
        return coroutineScope {

            // 標準出力の読み取り
            val outRes = async(Dispatchers.IO) {
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    processConfig.stdout(WrapperReader(reader))
                }
            }

            // 標準エラー出力の読み取り
            val errRes = async(Dispatchers.IO) {
                BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                    processConfig.stderr(WrapperReader(reader))
                }
            }
            // 標準入力への書き込み
            val inRes = async(Dispatchers.IO) {
                processConfig.stdin.let { stdin ->
                    OutputStreamWriter(process.outputStream).use { writer ->
                        WrapperWriter(writer).stdin()
                    }
                }
            }
            // プロセスの終了を待機
            val exitCode = process.waitFor()

            ProcessRes(
                exitCode = exitCode,
                outputRes = outRes.await(),
                errRes = errRes.await(),
                inputRes = inRes.await()
            )
        }
    }
}