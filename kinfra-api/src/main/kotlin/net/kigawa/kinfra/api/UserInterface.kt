package net.kigawa.kinfra.api

interface UserInterface {
    suspend fun askStrLineQuestion(question: String): String
}