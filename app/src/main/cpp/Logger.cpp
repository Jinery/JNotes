#include <android/log.h>

#define TAG "JNotes_CPP"

#define LOGI(fmt, ...) __android_log_print(ANDROID_LOG_INFO, TAG, "[%s:%d] " fmt, __FILE_NAME__, __LINE__, ##__VA_ARGS__)
#define LOGW(fmt, ...) __android_log_print(ANDROID_LOG_WARN, TAG, "[%s:%d] " fmt, __FILE_NAME__, __LINE__, ##__VA_ARGS__)
#define LOGE(fmt, ...) __android_log_print(ANDROID_LOG_ERROR, TAG, "[%s:%d] *** ERROR *** " fmt, __FILE_NAME__, __LINE__, ##__VA_ARGS__)

#ifdef DEBUG_BUILD
#define LOGD(fmt, ...) __android_log_print(ANDROID_LOG_DEBUG, TAG, "[%s:%d] " fmt, __FILE_NAME__, __LINE__, ##__VA_ARGS__)
#else
#define LOGD(fmt, ...) // Remove all debug logs in release.
#endif