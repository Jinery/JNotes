#include "Crypter.h"

void Crypter::SecureZero(void *ptr, size_t len) {
    volatile auto* pat = (volatile unsigned char*)ptr;
    while (len--) *pat++ = 0;
}

std::vector<uint8_t> Crypter::AesGcmEncrypt(const std::vector<uint8_t> &plainText, const uint8_t *key, const uint8_t *iv, const uint8_t *aad, size_t aadLen) {
    LOGD("Encrypting data with AES-GCM, size: %zu", plainText.size());
    std::vector<uint8_t> cipherText(plainText.size());
    std::vector<uint8_t> tag(kAesTagSize);
    EVP_CIPHER_CTX* ctx = EVP_CIPHER_CTX_new();

    if (!ctx) return {  };
    int len, cipherTextLen;
    if (EVP_EncryptInit_ex(ctx, EVP_aes_256_gcm(), nullptr, key, iv) != 1) goto fail;
    if (aad && aadLen > 0) {
        if (EVP_EncryptUpdate(ctx, nullptr, &len, aad, aadLen) != 1) goto fail;
    }

    if (EVP_EncryptUpdate(ctx, cipherText.data(), &len, plainText.data(), plainText.size()) != 1) goto fail;
    cipherTextLen = len;
    if (EVP_EncryptFinal_ex(ctx, cipherText.data() + len, &len) != 1) goto fail;
    cipherTextLen += len;
    if (EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_GET_TAG, kAesTagSize, tag.data()) != 1) goto fail;
    EVP_CIPHER_CTX_free(ctx);
    cipherText.resize(cipherTextLen);
    cipherText.insert(cipherText.end(), tag.begin(), tag.end());
    return cipherText;
fail:
    EVP_CIPHER_CTX_free(ctx);
    return {  };
}

std::vector<uint8_t> Crypter::AesGcmDecrypt(const std::vector<uint8_t> &cipherTextAndTag, const uint8_t *key, const uint8_t *iv, const uint8_t *aad, size_t aadLen) {
    if (cipherTextAndTag.size() < kAesTagSize) return {  };
    size_t ctLen = cipherTextAndTag.size() - kAesTagSize;
    std::vector<uint8_t> plainText(ctLen);
    EVP_CIPHER_CTX* ctx = EVP_CIPHER_CTX_new();

    if (!ctx) return {  };
    int len, plainTextLen;
    if (EVP_DecryptInit_ex(ctx, EVP_aes_256_gcm(), nullptr, key, iv) != 1) goto fail;
    if (aad && aadLen > 0) {
        if (EVP_DecryptUpdate(ctx, nullptr, &len, aad, aadLen) != 1) goto fail;
    }
    if (EVP_CIPHER_CTX_ctrl(ctx, EVP_CTRL_GCM_SET_TAG, kAesTagSize,
            const_cast<uint8_t*>(cipherTextAndTag.data() + ctLen)) != 1) goto fail;
    if (EVP_DecryptUpdate(ctx, plainText.data(), &len, cipherTextAndTag.data(), ctLen) != 1) goto fail;
    plainTextLen = len;
    if (EVP_DecryptFinal_ex(ctx, plainText.data() + len, &len) != 1) {
        LOGE("AES-GCM tag verification FAILED");
        goto fail;
    }
    plainTextLen += len;
    EVP_CIPHER_CTX_free(ctx);
    plainText.resize(plainTextLen);
    return plainText;
fail:
    EVP_CIPHER_CTX_free(ctx);
    return {  };
}
