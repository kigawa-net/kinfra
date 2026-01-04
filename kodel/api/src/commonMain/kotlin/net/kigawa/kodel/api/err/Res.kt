package net.kigawa.kodel.api.err

/**
 * 結果を表すシールドインターフェース。
 * 成功（Ok）またはエラー（Err）を表す。
 *
 * @param T 成功時の値の型
 * @param E エラーの型
 */
sealed interface Res<out T, out E: Throwable> {

    /**
     * 成功を表すクラス。
     *
     * @param value 成功時の値
     */
    @Suppress("unused")
    class Ok<out T, out E: Throwable>(val value: T): Res<T, E> {
        fun <F: Throwable> convertType(): Ok<T, F> = Ok(value)
        fun <F> mapValue(block: (T) -> F): Ok<F, E> = Ok(block(value))
    }

    /**
     * エラーを表すクラス。
     *
     * @param err エラー
     */
    @Suppress("unused")
    class Err<out T, out E: Throwable>(val err: E): Res<T, E> {
        fun <U> convertType(): Err<U, E> = Err(err)
        fun <U, F: Throwable> mapErr(block: (E) -> F): Err<U, F> = Err(block(err))
    }
}

inline fun <T, E: Throwable, U> Res<T, E>.onOk(block: (T) -> U): Res<U, E> = when (val res = this) {
    is Res.Err<T, E> -> Res.Err(res.err)
    is Res.Ok<T, E> -> Res.Ok(block(res.value))
}

fun <T, E: Throwable> Res<Res<T, E>, E>.flat(): Res<T, E> = when (val res = this) {
    is Res.Err<Res<T, E>, E> -> res.convertType()
    is Res.Ok<Res<T, E>, E> -> res.value
}

fun <T, E: Throwable> Res<Res<T, E>?, E>.flatNullable(): Res<T, E>? = when (val res = this) {
    is Res.Err<Res<T, E>?, E> -> res.convertType()
    is Res.Ok<Res<T, E>?, E> -> res.value
}

@Suppress("unused")
fun <T, E: Throwable, U> Res<T, E>.flatOk(block: (T) -> Res<U, E>): Res<U, E> = onOk(block).flat()

@Suppress("unused")
inline fun <T, E: Throwable, U> Res<T, E>.flatNullableOk(block: (T) -> Res<U, E>?): Res<U, E>? =
    onOk(block).flatNullable()
