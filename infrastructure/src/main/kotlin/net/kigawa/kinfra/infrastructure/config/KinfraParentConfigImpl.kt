package net.kigawa.kinfra.infrastructure.config

import com.charleskorn.kaml.Yaml
import net.kigawa.kinfra.model.conf.*
import java.io.File
import java.nio.file.Path

class KinfraParentConfigImpl(
    private var kinfraParentConfigScheme: KinfraParentConfigScheme,
    val file: File,
): KinfraParentConfig {
    override val projectName: String
        get() = kinfraParentConfigScheme.projectName
    override val description: String?
        get() = kinfraParentConfigScheme.description
    override val terraform: TerraformSettings?
        get() = kinfraParentConfigScheme.terraform
    override val subProjects: List<SubProject>
        get() = kinfraParentConfigScheme.subProjects.map { SubProject.fromString(it) }
    override val bitwarden: BitwardenSettings?
        get() = kinfraParentConfigScheme.bitwarden
    override val update: UpdateSettings?
        get() = kinfraParentConfigScheme.update
    override val filePath: Path
        get() = file.toPath()

    override fun toData(): KinfraParentConfigData {
        return KinfraParentConfigData(
            projectName = projectName,
            description = description,
            terraform = terraform,
            subProjects = subProjects,
            bitwarden = bitwarden,
            update = update,
        )
    }

    override fun saveData(updatedConfig: KinfraParentConfigData) {
        kinfraParentConfigScheme = KinfraParentConfigScheme.from(updatedConfig)
        file.writeText(
            Yaml.default.encodeToString(
                KinfraParentConfigScheme.serializer(), kinfraParentConfigScheme
            )
        )
    }
}