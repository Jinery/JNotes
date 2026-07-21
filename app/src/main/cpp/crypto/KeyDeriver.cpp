#include "KeyDeriver.h"

bool KeyDeriver::DeriveKey(const uint8_t *password, size_t passwordLen, const uint8_t *salt, size_t saltLen, uint8_t *outKey, size_t keylen) {
    return HKDF(outKey, keylen, EVP_sha256(),
            password, passwordLen,
            salt, saltLen,
            reinterpret_cast<const uint8_t*>("jnotes-v1"), 9) == 1;
}
