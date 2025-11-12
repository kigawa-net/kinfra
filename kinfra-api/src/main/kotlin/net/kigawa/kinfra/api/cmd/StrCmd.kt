package net.kigawa.kinfra.api.cmd

class StrCmd(cmd: List<String>): Cmd {
    override val raw: List<String> = cmd
}