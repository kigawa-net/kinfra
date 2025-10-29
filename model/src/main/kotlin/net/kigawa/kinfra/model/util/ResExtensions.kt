package net.kigawa.kinfra.model.util

import net.kigawa.kodel.err.ActionException
import net.kigawa.kodel.err.Res

/**
 * Resから終了コードを取得するヘルパー関数
 */
fun Res<Int, ActionException>.exitCode(): Int = when (this) {
    is Res.Ok -> value
    is Res.Err -> err.exitCode
}

/**
 * Resが成功かどうかを判定するヘルパー関数
 */
fun Res<Int, ActionException>.isSuccess(): Boolean = this is Res.Ok

/**
 * Resが失敗かどうかを判定するヘルパー関数
 */
fun Res<Int, ActionException>.isFailure(): Boolean = this is Res.Err

/**
 * Resからメッセージを取得するヘルパー関数
 */
fun Res<Int, ActionException>.message(): String? = when (this) {
    is Res.Ok -> null
    is Res.Err -> err.message
}
