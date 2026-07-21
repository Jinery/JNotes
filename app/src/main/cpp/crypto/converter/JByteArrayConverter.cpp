#include "JByteArrayConverter.h"

std::vector<uint8_t> JByteArrayConverter::JByteArrayToVector(JNIEnv *env, jbyteArray arr) {
    if (!arr) return {  };
    jsize len = env->GetArrayLength(arr);
    std::vector<uint8_t> vec(len);
    env->GetByteArrayRegion(arr, 0, len, reinterpret_cast<jbyte*>(vec.data()));
    return vec;
}

jbyteArray JByteArrayConverter::VectorToJByteArray(JNIEnv *env, const std::vector<uint8_t> &vec) {
    if (vec.empty()) return env->NewByteArray(0);
    jbyteArray arr = env->NewByteArray(vec.size());
    env->SetByteArrayRegion(arr, 0, vec.size(), reinterpret_cast<const jbyte*>(vec.data()));
    return arr;
}
