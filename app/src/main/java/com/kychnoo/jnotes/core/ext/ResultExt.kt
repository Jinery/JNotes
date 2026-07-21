package com.kychnoo.jnotes.core.ext

import kotlin.coroutines.cancellation.CancellationException

inline fun <T> Result<T>.onAsyncFailure(
    action: (Throwable) -> Unit = { it.printStackTrace() }
): Result<T> {
    val exception = exceptionOrNull() ?: return this
    if (exception is CancellationException) throw exception

    action(exception)
    return this
}