#include "OutLineBuffer.h"

thread_local OutLineBuffer* pOutLineBuffer = NULL;

OutLineBuffer::OutLineBuffer(JNIEnv* env) 
    : env(env), outputArrayList(NULL)
{
    if(env == NULL || pOutLineBuffer != NULL) return;

    //java_lang_String        = env->FindClass("java/lang/String");
    //java_lang_String_       = env->GetMethodID(java_lang_String, "<init>", "([BLjava/lang/String;)V");
    
    jclass java_util_ArrayList = env->FindClass("java/util/ArrayList");
    if(java_util_ArrayList == NULL) return;

    java_util_ArrayList_add = env->GetMethodID(java_util_ArrayList, "add", "(Ljava/lang/Object;)Z");
    
    jmethodID java_util_ArrayList_ = env->GetMethodID(java_util_ArrayList, "<init>", "()V");
    if(java_util_ArrayList_ != NULL) {
        outputArrayList = env->NewObject(java_util_ArrayList, java_util_ArrayList_);
    }
    
    if(outputArrayList != NULL && java_util_ArrayList_add != NULL) {
        pOutLineBuffer = this;
    }
    
    if(java_util_ArrayList != NULL) {
        env->DeleteLocalRef(java_util_ArrayList);
        java_util_ArrayList = NULL;
    }
}

OutLineBuffer::~OutLineBuffer() {
    if(pOutLineBuffer != this) return;

    /*    
    if(java_lang_String != NULL) {
        env->DeleteLocalRef(java_lang_String);
        java_lang_String = NULL;
    }
    */

    if(outputArrayList != NULL) {
        env->DeleteLocalRef(outputArrayList);
        outputArrayList = NULL;
    }
    
    pOutLineBuffer = NULL;
}

//std::ostringstream& operator<<(std::ostringstream &ss, const char*);
OutLineBuffer& OutLineBuffer::operator<<(char* str) {
    OutLineBuffer::appendStringStream(str);
    return *this;
}

void OutLineBuffer::appendStringStream( char* stubLine ) {
    if(pOutLineBuffer != NULL) {
        pOutLineBuffer->append(stubLine);
    } else {
        fprintf(stdout, "%s", stubLine);
    }
}

void OutLineBuffer::append( char* stubLine )
{
    char* str = stubLine;
    while(*stubLine) {
        if(*stubLine == '\n') {
            *stubLine = '\0';
            line_stringstream << str;
            str = stubLine + 1;

            flush_line();
        }
        stubLine++;
    }

    if(str < stubLine) {
        line_stringstream << str;
    }
}

void OutLineBuffer::addStringArrayList( const char* line ) {
    if(line == NULL) return;
    jstring strline = env->NewStringUTF(line);
    /*
    if(::gEncodingCharaset != NULL) {
        strline = (jstring)env->NewObject(java_lang_String, java_lang_String_, cstr2jbyteArray(gEnv, line), ::gEncodingCharaset);
    } else {
        strline = env->NewStringUTF(line);
    }
    */
    if(strline == NULL) return;
    env->CallVoidMethod(outputArrayList, java_util_ArrayList_add, strline);
    env->DeleteLocalRef(strline);
}

void OutLineBuffer::flush_line() {
    std::string str = line_stringstream.str();
    if(str.size() > 0) {
        addStringArrayList(str.c_str());
    }
    line_stringstream.str("");
    line_stringstream.clear();
}

jobjectArray OutLineBuffer::toArray() {
    jobjectArray stringArray = NULL;

    jclass java_util_ArrayList = env->FindClass("java/util/ArrayList");
    if(java_util_ArrayList == NULL) return NULL;

    jmethodID java_util_ArrayList_toArray = env->GetMethodID(java_util_ArrayList, "toArray", "([Ljava/lang/Object;)[Ljava/lang/Object;");
    if(java_util_ArrayList_toArray != NULL) {
        jclass java_lang_String = env->FindClass("java/lang/String");
        if(java_lang_String != NULL) {
            jobjectArray emptyArray = (jobjectArray)env->NewObjectArray(0, java_lang_String, NULL);
            if(emptyArray != NULL) {
                stringArray = static_cast<jobjectArray>(env->CallObjectMethod(outputArrayList, java_util_ArrayList_toArray, emptyArray));
                env->DeleteLocalRef(emptyArray);
            }
            env->DeleteLocalRef(java_lang_String);
        }
        env->DeleteLocalRef(java_util_ArrayList);
    }
    return stringArray;
}