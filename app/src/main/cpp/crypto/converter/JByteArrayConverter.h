#pragma once
#ifndef JNOTES_JBYTEARRAYCONVERTER_H
#define JNOTES_JBYTEARRAYCONVERTER_H

#include <jni.h>
#include <vector>


class JByteArrayConverter {
public:
    static std::vector<uint8_t> JByteArrayToVector(JNIEnv* env, jbyteArray arr);
    static jbyteArray VectorToJByteArray(JNIEnv* env, const std::vector<uint8_t>& vec);
};

#endif //JNOTES_JBYTEARRAYCONVERTER_H
