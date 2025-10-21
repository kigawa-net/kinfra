package net.kigawa.kinfra.model.conf

import net.kigawa.kinfra.model.SubProject

data class KinfraParentConfigData(
    val projectName: String,
    val description: String? = null,
    val terraform: TerraformSettings? = null,
    val subProjects: List<SubProject> = emptyList(),
    val bitwarden: BitwardenSettings? = null,
    val update: UpdateSettings? = null
)
