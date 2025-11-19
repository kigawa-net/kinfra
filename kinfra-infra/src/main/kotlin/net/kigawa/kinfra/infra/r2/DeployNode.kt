package net.kigawa.kinfra.infra.r2

data class DeployNode(
    var record: DeployRecord?,
    val children: MutableMap<String, DeployNode>,
)