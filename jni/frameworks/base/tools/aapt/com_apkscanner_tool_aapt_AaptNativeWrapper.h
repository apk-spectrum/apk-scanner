/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_apkscanner_tool_aapt_AaptNativeWrapper */

#ifndef _Included_com_apkscanner_tool_aapt_AaptNativeWrapper
#define _Included_com_apkscanner_tool_aapt_AaptNativeWrapper
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_apkscanner_tool_aapt_AaptNativeWrapper
 * Method:    run
 * Signature: ([Ljava/lang/String;)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_com_apkscanner_tool_aapt_AaptNativeWrapper_run
  (JNIEnv *, jclass, jobjectArray);

/*
 * Class:     com_apkscanner_tool_aapt_AaptNativeWrapper
 * Method:    getResTable
 * Signature: (Ljava/lang/String;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_com_apkscanner_tool_aapt_AaptNativeWrapper_getResTable
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_apkscanner_tool_aapt_AaptNativeWrapper
 * Method:    realeaseResTable
 * Signature: (Ljava/lang/Object;)V
 */
JNIEXPORT void JNICALL Java_com_apkscanner_tool_aapt_AaptNativeWrapper_realeaseResTable
  (JNIEnv *, jclass, jobject);

/*
 * Class:     com_apkscanner_tool_aapt_AaptNativeWrapper
 * Method:    getResourceName
 * Signature: (Ljava/lang/Object;I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_apkscanner_tool_aapt_AaptNativeWrapper_getResourceName
  (JNIEnv *, jclass, jobject, jint);

#ifdef __cplusplus
}
#endif
#endif