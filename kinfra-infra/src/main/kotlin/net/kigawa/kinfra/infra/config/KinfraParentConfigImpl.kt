package net.kigawa.kinfra.infra.config

import net.kigawa.kinfra.infra.SubProjectImpl
import net.kigawa.kinfra.infra.config.script.KinfraParentConfigKtsWriter
import net.kigawa.kinfra.infra.config.script.KinfraParentConfigScript
import net.kigawa.kinfra.infra.config.script.ScriptConfigCache
import net.kigawa.kinfra.infra.config.script.ScriptHost
import net.kigawa.kinfra.model.conf.*
import net.kigawa.kinfra.model.sub.SubProject
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
        get() = kinfraParentConfigScheme.terraform?.toTerraformSettings()
    override val subProjects: List<SubProject>
        get() = kinfraParentConfigScheme.subProjects.map { it.toSubProject() }
    override val bitwarden: BitwardenSettings?
        get() = kinfraParentConfigScheme.bitwarden?.toBitwardenSettings()
    override val update: UpdateSettings?
        get() = kinfraParentConfigScheme.update?.toUpdateSettings()
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
        file.writeText(KinfraParentConfigKtsWriter.render(kinfraParentConfigScheme))
    }

    override fun addSubProject(
        name: String,
        path: String,
    ): SubProject {
        return SubProjectImpl(name, path)
    }

    companion object {
        fun fromFile(file: File): KinfraParentConfigImpl {
            val scheme =
                ScriptConfigCache.loadOrEval(file, KinfraParentConfigScheme.serializer()) {
                    ScriptHost.eval<KinfraParentConfigScript>(file).toScheme()
                }
            return KinfraParentConfigImpl(scheme, file)
        }
    }
}
