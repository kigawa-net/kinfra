package net.kigawa.kinfra.infra.secret

import net.kigawa.kinfra.api.fs.DirResource
import net.kigawa.kinfra.api.fs.FileResource
import net.kigawa.kinfra.api.hash.HashSrc
import net.kigawa.kinfra.api.secret.SecretFileResource
import net.kigawa.kinfra.model.BitwardenSecret

class SecretFileResourceImpl(
    val secret: BitwardenSecret,
    val secretDir: DirResource,
    override val fileResource: FileResource,
): SecretFileResource {


    override suspend fun hashSrc(): HashSrc {
        return HashSrc.resource(secret, secretDir)
    }

}