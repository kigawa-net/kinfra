package net.kigawa.kinfra.infra

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kigawa.kinfra.api.UserInterface

class CliUserInterface: UserInterface {
    override suspend fun askStrLineQuestion(question: String): String {
        println("$question: ")
        System.out.flush()
        var line: String? = null
        while (line.isNullOrBlank()) {
            line = withContext(Dispatchers.IO) {
                readlnOrNull()
            } ?: throw IllegalStateException("input is blank")
        }
        return line
    }
}