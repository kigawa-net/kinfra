package net.kigawa.kinfra.infrastructure.dep

import net.kigawa.kodel.api.dep.context.DepScope

interface InfraScope<S : InfraScope<S>> : DepScope<S>
