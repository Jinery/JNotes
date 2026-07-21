// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("jnotes");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("jnotes")
//      }
//    }

#include <jni.h>
#include <string>
#include <vector>

#include <openssl/rand.h>
#include <openssl/sha.h>
#include <openssl/hmac.h>

#include "crypto/Crypter.h"
#include "crypto/converter/JByteArrayConverter.h"
#include "crypto/KeyDeriver.h"

extern "C" {
    JNIEXPORT jstring JNICALL
    Java_com_kychnoo_jnotes_MainActivityKt_stringFromJNI(JNIEnv *env, jclass cls) {
        std::string helloText = "Android C++";
        return env->NewStringUTF(helloText.c_str());
    }

    JNIEXPORT jbyteArray JNICALL
    Java_com_kychnoo_jnotes_crypto_NativeCrypto_random(JNIEnv* env, jclass, jint size) {
        if (size <= 0 || size > 1024 * 1024) return nullptr;
        std::vector<uint8_t> buf(size);
        if (RAND_bytes(buf.data(), size) != 1) return nullptr;
        return JByteArrayConverter::VectorToJByteArray(env, buf);
    }

    JNIEXPORT jbyteArray JNICALL
    Java_com_kychnoo_jnotes_crypto_NativeCrypto_encrypt(JNIEnv* env, jclass, jbyteArray password, jbyteArray plainText) {
        std::vector<uint8_t> pass = JByteArrayConverter::JByteArrayToVector(env, password);
        std::vector<uint8_t> plnText = JByteArrayConverter::JByteArrayToVector(env, plainText);

        if (pass.empty() || plnText.empty()) return nullptr;
        uint8_t salt[Crypter::kSaltSize];
        uint8_t iv[Crypter::kAesIvSize];

        if (RAND_bytes(salt, Crypter::kSaltSize) != 1) return nullptr;
        if (RAND_bytes(iv, Crypter::kAesIvSize) != 1) return nullptr;
        uint8_t key[Crypter::kAesKeySize];
        if (!KeyDeriver::DeriveKey(pass.data(), pass.size(), salt, Crypter::kSaltSize, key, Crypter::kAesKeySize)) {
            Crypter::SecureZero(key, sizeof(key));
            return nullptr;
        }

        std::vector<uint8_t> cipherText = Crypter::AesGcmEncrypt(plnText, key, iv, salt, Crypter::kSaltSize);
        Crypter::SecureZero(key, sizeof(key));
        if (cipherText.empty()) return nullptr;
        std::vector<uint8_t> result;
        result.reserve(Crypter::kSaltSize + Crypter::kAesIvSize * cipherText.size());
        result.insert(result.end(), salt, salt + Crypter::kSaltSize);
        result.insert(result.end(), iv, iv + Crypter::kAesIvSize);
        result.insert(result.end(), cipherText.begin(), cipherText.end());
        return JByteArrayConverter::VectorToJByteArray(env, result);
    }

    JNIEXPORT jbyteArray JNICALL
    Java_com_kychnoo_jnotes_crypto_NativeCrypto_decrypt(JNIEnv* env, jclass, jbyteArray password, jbyteArray encryptedData) {
        std::vector<uint8_t> pass = JByteArrayConverter::JByteArrayToVector(env, password);
        std::vector<uint8_t> enc = JByteArrayConverter::JByteArrayToVector(env, encryptedData);
        if (pass.empty() || enc.empty()) return nullptr;
        if (enc.size() < Crypter::kSaltSize + Crypter::kAesIvSize + Crypter::kAesTagSize) {
            LOGE("Encrypted data too short"); return nullptr;
        }
        const uint8_t* salt = enc.data();
        const uint8_t* iv = enc.data() + Crypter::kSaltSize;
        size_t ctLen = enc.size() - Crypter::kSaltSize - Crypter::kAesIvSize;
        uint8_t key[Crypter::kAesKeySize];
        if (!KeyDeriver::DeriveKey(pass.data(), pass.size(), salt, Crypter::kSaltSize, key, Crypter::kAesKeySize)) {
            Crypter::SecureZero(key, sizeof(key)); return nullptr;
        }
        std::vector<uint8_t> ciphertext(enc.data() + Crypter::kSaltSize + Crypter::kAesIvSize, enc.data() + enc.size());
        auto pt = Crypter::AesGcmDecrypt(ciphertext, key, iv, salt, Crypter::kSaltSize);
        Crypter::SecureZero(key, sizeof(key));
        return  JByteArrayConverter::VectorToJByteArray(env, pt);
    }

    JNIEXPORT jbyteArray JNICALL
    Java_com_kychnoo_jnotes_crypto_NativeCrypto_sha256(JNIEnv* env, jclass, jbyteArray data) {
        std::vector<uint8_t> nData = JByteArrayConverter::JByteArrayToVector(env, data);
        uint8_t hash[SHA256_DIGEST_LENGTH];
        SHA256(nData.data(), nData.size(), hash);
        return JByteArrayConverter::VectorToJByteArray(env, std::vector<uint8_t>(hash, hash + SHA256_DIGEST_LENGTH));
    }

    JNIEXPORT jbyteArray JNICALL
    Java_com_kychnoo_jnotes_crypto_NativeCrypto_hmacSha256(JNIEnv* env, jclass, jbyteArray key, jbyteArray data) {
        std::vector<uint8_t> nKey = JByteArrayConverter::JByteArrayToVector(env, key);
        std::vector<uint8_t> nData = JByteArrayConverter::JByteArrayToVector(env, data);
        uint8_t hash[EVP_MAX_MD_SIZE];
        unsigned int hashLen = 0;
        HMAC(EVP_sha256(), nKey.data(), nKey.size(), nData.data(), nData.size(), hash, &hashLen);
        return JByteArrayConverter::VectorToJByteArray(env, std::vector<uint8_t>(hash, hash + hashLen));
    }
}

