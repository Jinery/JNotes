#pragma once
#ifndef JNOTES_CRYPTER_H
#define JNOTES_CRYPTER_H

#include <jni.h>
#include <vector>
#include <openssl/evp.h>
#include <openssl/aes.h>

#include "../Logger.cpp"

class Crypter {
public:
    static constexpr int kAesKeySize = 32, kAesIvSize = 12;
    static constexpr int kSaltSize = 32;
    static constexpr int kAesTagSize = 16;
public:
    static std::vector<uint8_t> AesGcmEncrypt(const std::vector<uint8_t>& plainText,
            const uint8_t* key,
            const uint8_t* iv,
            const uint8_t* aad, size_t aadLen);
    static std::vector<uint8_t> AesGcmDecrypt(const std::vector<uint8_t>& cipherTextAndTag,
            const uint8_t* key,
            const uint8_t* iv,
            const uint8_t* aad, size_t aadLen);
    static void SecureZero(void* ptr, size_t len);
};


#endif //JNOTES_CRYPTER_H
