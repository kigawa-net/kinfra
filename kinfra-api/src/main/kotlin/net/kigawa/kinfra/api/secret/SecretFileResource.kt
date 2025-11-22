package net.kigawa.kinfra.api.secret

import net.kigawa.kinfra.api.fs.FileResource
import net.kigawa.kinfra.api.resource.KinfraResource

interface SecretFileResource: KinfraResource {
    val fileResource: FileResource
}