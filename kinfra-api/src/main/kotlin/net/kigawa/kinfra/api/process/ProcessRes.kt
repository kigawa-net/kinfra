package net.kigawa.kinfra.api.process

interface ProcessRes<SI,SO,SE> {
    val exitCode: Any
    val inputRes: SI
    val outputRes: SO
    val errRes: SE
}