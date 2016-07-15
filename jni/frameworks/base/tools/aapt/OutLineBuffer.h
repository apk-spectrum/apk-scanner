#ifndef __OUTPUT_LINE_BUFFER_H
#define __OUTPUT_LINE_BUFFER_H

#include <sstream>
#include <jni.h>

#define printf(...) do { \
    int __len__ = snprintf(NULL, 0, __VA_ARGS__); \
    char buf[__len__+1]; snprintf(buf, __len__+1, __VA_ARGS__); \
    OutLineBuffer::appendStringStream(buf); \
} while(0)

class OutLineBuffer {
public :
    OutLineBuffer(JNIEnv* env);
    ~OutLineBuffer();
    
    static void appendStringStream(char* stubLine);
    jobjectArray toArray();
    
    OutLineBuffer& operator<<(char* str);
    //friend std::ostringstream& operator<<(std::ostringstream &ss, const char*);
    
private :
    void append( char* stubLine );
    void addStringArrayList( const char* line );
    void flush_line();

    std::ostringstream line_stringstream;
    
    JNIEnv* env;
    
    //jclass      java_lang_String;
    //jmethodID   java_lang_String_;
    //jclass      java_util_ArrayList;
    jmethodID   java_util_ArrayList_add;
    
    jobject outputArrayList;
};

extern thread_local OutLineBuffer* pOutLineBuffer;

#endif // __OUTPUT_LINE_BUFFER_H
