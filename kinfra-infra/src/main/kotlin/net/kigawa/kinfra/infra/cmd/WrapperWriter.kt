package net.kigawa.kinfra.infra.cmd

import net.kigawa.kinfra.api.io.Writer
import java.io.OutputStreamWriter

class WrapperWriter(
    val outPutStreamWriter: OutputStreamWriter,
): Writer {
    override fun write(str: String) {
        outPutStreamWriter.write(str)
        outPutStreamWriter.flush()
    }
}