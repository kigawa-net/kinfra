package net.kigawa.kinfra.infra

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kigawa.kinfra.api.UserInterface

class CliUserInterface: UserInterface {
    override suspend fun askStrLineQuestion(question: String): String {
        print("$question: ")
        var line: String? = null
        while (line.isNullOrBlank()) {
            line = withContext(Dispatchers.IO) {
                readlnOrNull()
            } ?: throw IllegalStateException("input is blank")
        }
        return line
    }
}