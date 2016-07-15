#include <stdio.h>
#include <stdlib.h>

#include <androidfw/ResourceTypes.h>

#include "Main.h"

#include "com_apkscanner_tool_aapt_AaptNativeWrapper.h"
#include "JniCharacterSet.h"
#include "OutLineBuffer.h"

extern int main(int argc, char* const argv[]);

static jstring gEncodingCharaset;

JNIEXPORT jobjectArray JNICALL Java_com_apkscanner_tool_aapt_AaptNativeWrapper_run
  (JNIEnv *env, jclass /*thiz*/, jobjectArray params)
{
    char prog[8] = "libaapt";
    jsize paramCnt = 0;
    
    if(params != NULL) {
        paramCnt = env->GetArrayLength(params);
    }
    if(paramCnt < 0) paramCnt = 0;

	char* buf[paramCnt+1];
	buf[0] = prog;

    jstring encoding = gEncodingCharaset;
    jclass java_lang_String = NULL;
    jmethodID java_lang_String_getBytes = NULL;

    if(encoding != NULL) {
        java_lang_String = env->FindClass("java/lang/String");
        if(java_lang_String != NULL) {
            java_lang_String_getBytes = env->GetMethodID(java_lang_String, "getBytes", "(Ljava/lang/String;)[B");
        }
        if(java_lang_String_getBytes == NULL) {
            encoding = NULL;
            if(java_lang_String != NULL) {
                env->DeleteLocalRef(java_lang_String);
                java_lang_String = NULL;
            }
        }
    }

    for(int i = 0; i < paramCnt; i++) {
        jstring param = static_cast<jstring>(env->GetObjectArrayElement(params, i));
        if(param == NULL) {
            buf[i+1] = NULL;
            fprintf(stderr, "params[%d] is NULL\n", i);
            continue;
        }

        if(encoding != NULL) {
            jbyteArray bytes = static_cast<jbyteArray>(env->CallObjectMethod(param, java_lang_String_getBytes, gEncodingCharaset));
            // check UnsupportedEncodingException
            if(env->ExceptionCheck()) {
                fprintf(stderr, "UnsupportedEncodingException occurred\n");
                env->ExceptionClear();
                
                jmethodID java_lang_String_getBytes_ = env->GetMethodID(java_lang_String, "getBytes", "()[B");
                if(java_lang_String_getBytes_ != NULL) {
                    bytes = static_cast<jbyteArray>(env->CallObjectMethod(param, java_lang_String_getBytes_));
                }
            }
            if(bytes != NULL) {
                buf[i+1] = jbyteArray2cstr(env, bytes);
                env->DeleteLocalRef(bytes);
            } else {
                buf[i+1] = NULL;
                fprintf(stderr, "Failure: encoding buf[%d] is NULL\n", i);
            }
        } else {
            const char *jchar = env->GetStringUTFChars(param, 0);
            buf[i+1] = (char *)malloc(4096);
            sprintf(buf[i+1], "%s", (char*)jchar);
            env->ReleaseStringUTFChars(param, jchar);
        }

        env->DeleteLocalRef(param);
    }

    jobjectArray stringArray = NULL;
    {
        OutLineBuffer olb(env);
    	main(paramCnt+1,(char**)buf);
    	stringArray = olb.toArray();
    }

	for(int i = 0; i < paramCnt; i++) {
	    if(buf[i+1] != NULL) {
	        free((void*)buf[i+1]);
	    }
    }

	fflush(stdout);
	fflush(stderr);
    
	return stringArray;
}

JNIEXPORT jobject JNICALL Java_com_apkscanner_tool_aapt_AaptNativeWrapper_getResTable
  (JNIEnv *, jclass /*thiz*/, jstring)
{
    return NULL;
}

JNIEXPORT void JNICALL Java_com_apkscanner_tool_aapt_AaptNativeWrapper_realeaseResTable
  (JNIEnv *, jclass /*thiz*/, jobject)
{
    return;
}

JNIEXPORT jstring JNICALL Java_com_apkscanner_tool_aapt_AaptNativeWrapper_getResourceName
  (JNIEnv *, jclass /*thiz*/, jobject, jint)
{
    const char* filename = "C:\\framework-res.apk";
    int resID = 0x0104030a;

    AssetManager assets;
    int32_t assetsCookie;
    if (!assets.addAssetPath(String8(filename), &assetsCookie)) {
        fprintf(stderr, "ERROR: dump failed because assets could not be loaded : %s\n", filename);
        return NULL;
    }
    
    // Make a dummy config for retrieving resources...  we need to supply
    // non-default values for some configs so that we can retrieve resources
    // in the app that don't have a default.  The most important of these is
    // the API version because key resources like icons will have an implicit
    // version if they are using newer config types like density.
    ResTable_config config;
    memset(&config, 0, sizeof(ResTable_config));
    config.language[0] = 'e';
    config.language[1] = 'n';
    config.country[0] = 'U';
    config.country[1] = 'S';
    config.orientation = ResTable_config::ORIENTATION_PORT;
    config.density = ResTable_config::DENSITY_MEDIUM;
    config.sdkVersion = 10000; // Very high.
    config.screenWidthDp = 320;
    config.screenHeightDp = 480;
    config.smallestScreenWidthDp = 320;
    config.screenLayout |= ResTable_config::SCREENSIZE_NORMAL;
    assets.setConfiguration(config);

    const ResTable& res = assets.getResources(false);
    if (res.getError() != NO_ERROR) {
        fprintf(stderr, "ERROR: dump failed because the resource table is invalid/corrupt.\n");
        return NULL;
    }
    fprintf(stderr, "Success load file : %s\n", filename);
    
    
    android::ResTable::resource_name rname;
    if(res.getResourceName(resID, false, &rname)) {
        printf("resName %s\n", String8(rname.name, rname.nameLen).string());
    }


    android::Res_value resValue;


    

    Vector<String8> locales;
    res.getLocales(&locales);

    String8 label;
    const size_t NL = locales.size();
    for (size_t i=0; i<NL; i++) {
        const char* localeStr =  locales[i].string();
        assets.setLocale(localeStr != NULL ? localeStr : "");
            
            
                res.getResource(resID, &resValue, false, 0, NULL, NULL);
    
    size_t len;
    const android::Res_value* value2 = &resValue;
    const char16_t* str = res.valueToString(value2, 0, NULL, &len);
    String8 result = str ? String8(str, len) : String8();
        
    //printf("getResource %d - %s\n", size, result.string());
    
                printf("application-label-%s:'%s'\n", localeStr, result.string());
            /*
        String8 llabel = AaptXml::getResolvedAttribute(res, tree, LABEL_ATTR, &error);
        if (llabel != "") {
            if (localeStr == NULL || strlen(localeStr) == 0) {
                label = llabel;
                printf("application-label:'%s'\n",
                        ResTable::normalizeForOutput(llabel.string()).string());
            } else {
                if (label == "") {
                    label = llabel;
                }
                printf("application-label-%s:'%s'\n", localeStr,
                       ResTable::normalizeForOutput(llabel.string()).string());
            }
        }
        */
    }




    
    fflush(stdout);
    fflush(stderr);

    return NULL;
}



//static JNINativeMethod sMethod[] = {
    /* name, signature, funcPtr */
//    {"Java_com_apkscanner_tool_aapt_AaptNativeWrapper_run", "([Ljava/lang/String;)[Ljava/lang/String;", (jobjectArray*)Java_com_apkscanner_tool_aapt_AaptNativeWrapper_run}
//};

/*
int jniRegisterNativMethod(JNIEnv* env, const char* className, const JNINativeMethod* gMethods, int numMethods ) {
    jclass clazz;
 
    clazz = env->FindClass(className);
 
    if(clazz == NULL){
        return -1;
    }
    if(env->RegisterNatives(clazz, gMethods, numMethods) < 0){
        return -1;
    }
    return 0;
}
*/

jint JNI_OnLoad(JavaVM* jvm, void* /*reserved*/) {
    JNIEnv* env = NULL;
    jint result = -1;

    if(jvm->GetEnv((void**) &env, JNI_VERSION_1_6) != JNI_OK){
        return result;
    }
    
    jstring encodingCharaset = getEncodingCharacterSet(env);
    if(encodingCharaset != NULL) {
        gEncodingCharaset = static_cast<jstring>(env->NewGlobalRef(encodingCharaset));
        env->DeleteLocalRef(encodingCharaset);
    }
    fflush(stderr);
 
    //jniRegisterNativMethod(env, "com/example/jniedu/day2/ByteJniTestActivity", sMethod, NELEM(sMethod));
 
    return JNI_VERSION_1_6;
}

void JNI_OnUnload(JavaVM *jvm, void* /*reserved*/)
{
    JNIEnv *env;

    if (jvm->GetEnv((void **)&env, JNI_VERSION_1_6)) {
        return; 
    }
    
    if(gEncodingCharaset != NULL) {
        env->DeleteWeakGlobalRef(gEncodingCharaset);
    }

    return; 
}
