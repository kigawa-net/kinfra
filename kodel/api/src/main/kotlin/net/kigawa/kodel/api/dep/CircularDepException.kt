package net.kigawa.kodel.api.dep

import kotlin.reflect.KClass

class CircularDepException(
    val depClasses: List<KClass<*>>,
): IllegalStateException(
    "circular dependency ${depClasses.joinToString(" ->", transform = { it.qualifiedName ?: "unknown" })}"
) {
}