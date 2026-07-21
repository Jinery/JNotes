package com.kychnoo.jnotes.provider

import androidx.annotation.StringRes


interface ResourceProvider {
    fun getString(@StringRes resId: Int): String
    fun getString(@StringRes resId: Int, vararg args: Any): String
}