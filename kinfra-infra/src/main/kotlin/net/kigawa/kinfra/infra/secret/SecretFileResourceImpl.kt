package net.kigawa.kinfra.infra.secret

import net.kigawa.kinfra.api.ctx.KinfraContext
import net.kigawa.kinfra.api.hash.HashSrc
import net.kigawa.kinfra.api.resource.DirPathResource
import net.kigawa.kinfra.api.resource.FilePathResource
import net.kigawa.kinfra.api.resource.FileResource
import net.kigawa.kinfra.api.resource.NewFileResource
import net.kigawa.kinfra.api.secret.SecretFileResource
import net.kigawa.kinfra.model.BitwardenSecret

class SecretFileResourceImpl(
    val secret: BitwardenSecret,
    val secretDir: DirPathResource,
    ctx: KinfraContext,
    filePathResource: FilePathResource,
): FileResource(filePathResource, ctx), SecretFileResource {


    override suspend fun hashSrc(): HashSrc {
        return HashSrc.resource(secret, secretDir)
    }

    override suspend fun asFileResource(): FileResource {
        return NewFileResource(
            secret.value,
            FilePathResource(secretDir.path.join(secret.key)),
            ctx
        ).createFile()
    }
}