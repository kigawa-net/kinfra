package net.kigawa.kinfra.infra.dep

import net.kigawa.kodel.api.dep.context.DepScope

interface InfraScope<S : InfraScope<S>> : DepScope<S>
