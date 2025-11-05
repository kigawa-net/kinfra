package net.kigawa.kinfra.infrastructure.dep

import net.kigawa.kodel.api.dep.DepContext
import net.kigawa.kodel.api.dep.DepsBase

class RootDeps(
    depContext: DepContext<KinfraDepScope>,
) : DepsBase<KinfraDepScope>(depContext)
