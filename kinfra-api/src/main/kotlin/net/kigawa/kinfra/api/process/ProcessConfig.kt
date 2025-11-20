package net.kigawa.kinfra.api.process

import net.kigawa.kinfra.api.io.FileSystemPath
import net.kigawa.kinfra.api.io.Reader
import net.kigawa.kinfra.api.io.Writer

data class ProcessConfig<SI, SO, SE>(
    val cmd: Cmd,
    val stdin: suspend Writer.() -> SI,
    val stdout: suspend Reader<String>.() -> SO,
    val stderr: suspend Reader<String>.() -> SE,
    val workingDir: FileSystemPath?,
    val env: Map<String, String>,
) {
    companion object {
        fun create(
            cmd: Cmd,
        ): ProcessConfig<Unit, Unit, Unit> {
            return ProcessConfig(
                cmd, { }, {}, { forEach { println(it) } },
                null, emptyMap()
            )
        }
    }

    fun <R> stdin(stdin: suspend Writer.() -> R): ProcessConfig<R, SO, SE> {
        return ProcessConfig(cmd, stdin, stdout, stderr, workingDir, env)
    }

    fun <R> stdout(stdout: suspend Reader<String>.() -> R): ProcessConfig<SI, R, SE> {
        return ProcessConfig(cmd, stdin, stdout, stderr, workingDir, env)
    }

    fun <R> stderr(stderr: suspend Reader<String>.() -> R): ProcessConfig<SI, SO, R> {
        return ProcessConfig(cmd, stdin, stdout, stderr, workingDir, env)
    }

}