#pragma once

#ifndef JNOTES_KEYDERIVER_H
#define JNOTES_KEYDERIVER_H

#include <jni.h>

#include <openssl/hkdf.h>
#include <openssl/evp.h>

class KeyDeriver {
public:
    static bool DeriveKey(
            const uint8_t* password,
            size_t passwordLen,
            const uint8_t* salt,
            size_t saltLen,
            uint8_t* outKey,
            size_t keylen
    );
};

#endif //JNOTES_KEYDERIVER_H
