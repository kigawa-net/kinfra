package net.kigawa.kinfra.infrastructure.process

import net.kigawa.kinfra.model.err.ActionException
import net.kigawa.kinfra.model.err.Res
import java.io.File
import java.io.IOException

/**
 * 外部プロセスの実行を担当
 */
interface ProcessExecutor {
    fun execute(
        args: Array<String>,
        workingDir: File? = null,
        environment: Map<String, String> = emptyMap(),
        quiet: Boolean = true
    ): Res<Int, ActionException>

    fun executeWithOutput(
        args: Array<String>,
        workingDir: File? = null,
        environment: Map<String, String> = emptyMap()
    ): ExecutionResult

    fun checkInstalled(command: String): Boolean
}

data class ExecutionResult(
    val exitCode: Int,
    val output: String,
    val error: String = ""
)

class ProcessExecutorImpl : ProcessExecutor {
    override fun execute(
        args: Array<String>,
        workingDir: File?,
        environment: Map<String, String>,
        quiet: Boolean
    ): Res<Int, ActionException> {
        return try {
            val processBuilder = ProcessBuilder(*args)

            if (workingDir != null) {
                processBuilder.directory(workingDir)
            }

            environment.forEach { (key, value) ->
                processBuilder.environment()[key] = value
            }

            if (quiet) {
                processBuilder.redirectOutput(ProcessBuilder.Redirect.DISCARD)
                processBuilder.redirectError(ProcessBuilder.Redirect.DISCARD)
            } else {
                processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE)
                processBuilder.redirectError(ProcessBuilder.Redirect.PIPE)
            }

            val process = processBuilder.start()
            val output = if (quiet) "" else process.inputStream.bufferedReader().readText()
            val error = if (quiet) "" else process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            if (!quiet) {
                // quietでない場合、outputを出力
                print(output)
                if (error.isNotBlank()) {
                    print(error)
                }
            }

            if (exitCode == 0) {
                Res.Ok(exitCode)
            } else {
                // エラーの場合はより詳細な情報を含める
                val errorMessage = buildString {
                    appendLine("Command failed with exit code $exitCode")
                    appendLine("Command: ${args.joinToString(" ")}")
                    if (workingDir != null) {
                        appendLine("Working directory: $workingDir")
                    }
                    if (error.isNotBlank()) {
                        appendLine("Error output:")
                        appendLine(error)
                    } else if (output.isNotBlank()) {
                        appendLine("Output:")
                        appendLine(output)
                    }
                }
                Res.Err(ActionException(exitCode, errorMessage))
            }
        } catch (e: IOException) {
            Res.Err(ActionException(1, "Error executing command: ${e.message}"))
        }
    }

    override fun executeWithOutput(
        args: Array<String>,
        workingDir: File?,
        environment: Map<String, String>
    ): ExecutionResult {
        return try {
            val processBuilder = ProcessBuilder(*args)

            if (workingDir != null) {
                processBuilder.directory(workingDir)
            }

            environment.forEach { (key, value) ->
                processBuilder.environment()[key] = value
            }

            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText()
            val error = process.errorStream.bufferedReader().readText()
            val exitCode = process.waitFor()

            ExecutionResult(exitCode, output, error)
        } catch (e: IOException) {
            ExecutionResult(1, "", "Error executing command: ${e.message}")
        }
    }

    override fun checkInstalled(command: String): Boolean {
        return try {
            val process = ProcessBuilder(command, "version")
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start()
            process.waitFor() == 0
        } catch (_: Exception) {
            false
        }
    }
}