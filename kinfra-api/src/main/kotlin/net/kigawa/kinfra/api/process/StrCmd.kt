package net.kigawa.kinfra.api.process

class StrCmd(cmd: List<String>): Cmd {
    override val raw: List<String> = cmd
}