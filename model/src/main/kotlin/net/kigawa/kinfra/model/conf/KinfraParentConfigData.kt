package net.kigawa.kinfra.model.conf

data class KinfraParentConfigData(
    override val projectName: String,
    override val description: String? = null,
    override val terraform: TerraformSettings? = null,
    override val subProjects: List<String> = emptyList(),
    override val bitwarden: BitwardenSettings? = null,
    override val update: UpdateSettings? = null
) : KinfraParentConfig
