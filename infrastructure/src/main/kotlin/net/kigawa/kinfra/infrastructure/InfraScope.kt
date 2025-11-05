package net.kigawa.kinfra.infrastructure

import net.kigawa.kodel.api.dep.context.DepScope

interface InfraScope<S: InfraScope<S>>: DepScope<S> {
}