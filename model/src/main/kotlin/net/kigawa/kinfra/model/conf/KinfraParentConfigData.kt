package net.kigawa.kinfra.model.conf

data class KinfraParentConfigData(
     val projectName: String,
     val description: String? = null,
     val terraform: TerraformSettings? = null,
     val subProjects: List<String> = emptyList(),
     val bitwarden: BitwardenSettings? = null,
     val update: UpdateSettings? = null
)
