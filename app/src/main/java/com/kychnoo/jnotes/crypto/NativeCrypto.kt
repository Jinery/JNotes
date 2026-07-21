package com.kychnoo.jnotes.crypto

import android.util.Base64
import com.kychnoo.jnotes.domain.exception.CryptoException

object NativeCrypto {
    // Methods for calling from CPP.
    @JvmStatic private external fun random(size: Int): ByteArray?
    @JvmStatic private external fun encrypt(password: ByteArray, plainText: ByteArray): ByteArray?
    @JvmStatic private external fun decrypt(password: ByteArray, encryptedData: ByteArray): ByteArray?
    @JvmStatic private external fun sha256(data: ByteArray): ByteArray?
    @JvmStatic private external fun hmacSha256(key: ByteArray, data: ByteArray): ByteArray?

    fun sRandom(size: Int): ByteArray = random(size) ?: throw CryptoException("Random Generation Failed")
    fun encryptData(password: String, plainText: ByteArray): ByteArray
        = encrypt(password.toByteArrayWithCharsets(), plainText) ?: throw CryptoException("Encryption Failed")
    fun decryptData(password: String, encryptedData: ByteArray): ByteArray
        = decrypt(password.toByteArrayWithCharsets(), encryptedData) ?: throw CryptoException("Decrypt Failed - Wrong password or corrupted data")

    fun encryptString(password: String, text: String): String {
        val encrypted = encryptData(password, text.toByteArrayWithCharsets())
        return Base64.encodeToString(encrypted, Base64.NO_WRAP)
    }
    fun decryptString(password: String, base64: String): String {
        val encryptedText = Base64.decode(base64, Base64.NO_WRAP)
        return String(decryptData(password, encryptedText), Charsets.UTF_8)
    }

    fun genSha256(data: ByteArray): ByteArray = sha256(data) ?: throw CryptoException("SHA-256 failed")
    fun genHmacSha256(key: ByteArray, data: ByteArray): ByteArray = hmacSha256(key, data)
        ?: throw CryptoException("HMAC failed")

    private fun String.toByteArrayWithCharsets() = this.toByteArray(Charsets.UTF_8)
}