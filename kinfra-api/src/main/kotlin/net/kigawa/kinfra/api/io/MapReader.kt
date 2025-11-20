package net.kigawa.kinfra.api.io

class MapReader<F, T>(
    val translater: suspend (F) -> T,
    val reader: Reader<F>,
): Reader<T> {
    override suspend fun read(): T {
        return translater.invoke(reader.read())
    }

    override suspend fun hasNext(): Boolean {
        return reader.hasNext()
    }
}