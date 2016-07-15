#include "JniCharacterSet.h"

char *jbyteArray2cstr( JNIEnv *env, jbyteArray javaBytes )
{
    if(env == NULL || javaBytes == NULL) return NULL;
    char *nativeStr = NULL;
    jbyte *nativeBytes = env->GetByteArrayElements(javaBytes,0);
    if(nativeBytes == NULL) return NULL;
    size_t len = env->GetArrayLength(javaBytes);
    nativeStr = (char *)malloc(len+1);
    if(nativeStr != NULL) {
        strncpy(nativeStr, (const char*)nativeBytes, len);
        nativeStr[len] = '\0';
    }
    env->ReleaseByteArrayElements(javaBytes, nativeBytes, JNI_ABORT);
    return nativeStr;
}

jbyteArray cstr2jbyteArray( JNIEnv *env, const char *nativeStr)
{
    if(env == NULL || nativeStr == NULL) return NULL;
    jbyteArray javaBytes = NULL;
    int len = strlen(nativeStr);
    javaBytes = env->NewByteArray(len);
    if(javaBytes != NULL) {
        env->SetByteArrayRegion(javaBytes, 0, len, (jbyte *)nativeStr);
    }
    return javaBytes;
}

const char *getNativeCharacterSet() {
    setlocale(LC_ALL, "");
    char* locstr = setlocale(LC_CTYPE, NULL);

    if(!locstr || !*locstr) {
        fprintf(stderr, "Failure : read system character set\n");
        return NULL;
    }

    while(*locstr && *locstr++ != '.');

#ifdef _WIN32
// MS Code Page Identifiers
// https://msdn.microsoft.com/en-us/library/dd317756(v=vs.85).aspx
// MS code to JAVA
/* java available charsets list
    Map map = Charset.availableCharsets();
    Set set = map.entrySet();
    Iterator it = set.iterator();
    while (it.hasNext()) {
        Map.Entry entry = (Map.Entry) it.next();
        String name = (String) entry.getKey();
        Charset chs = (Charset) entry.getValue();
        System.out.println(name);
        Set aliases = chs.aliases();
        for (Iterator it2 = aliases.iterator(); it2.hasNext();) {
            System.out.println("\t" + it2.next());
        }
    }
*/
    if('0' <= *locstr && '9' >= *locstr) {
        int locint = 0;
        sscanf(locstr, "%d", &locint);
        switch(locint) {
            case 949: return "x-windows-949";
            //case 949: return "EUC-KR";
            case 037: return "IBM037";
            case 437: return "IBM437";
            case 500: return "IBM500";
            case 708: return "ASMO-708";
            //case 709: return "";
            //case 710: return "";
            case 720: return "DOS-720";
            case 737: return "x-IBM737";
            case 775: return "IBM775";
            case 850: return "IBM850";
            case 852: return "IBM852";
            case 855: return "IBM855";
            case 857: return "IBM857";
            case 858: return "IBM00858";
            case 860: return "IBM860";
            case 861: return "IBM861";
            case 862: return "IBM862";
            case 863: return "IBM863";
            case 864: return "IBM864";
            case 865: return "IBM865";
            case 866: return "IBM866";
            case 869: return "IBM869";
            case 870: return "IBM870";
            case 874: return "x-windows-874";
            case 875: return "x-IBM875";
            case 932: return "Shift_JIS";
            case 936: return "GB2312";
            case 950: return "Big5";
            case 1026: return "IBM1026";
            case 1047: return "IBM1047";
            case 1140: return "IBM01140";
            case 1141: return "IBM01141";
            case 1142: return "IBM01142";
            case 1143: return "IBM01143";
            case 1144: return "IBM01144";
            case 1145: return "IBM01145";
            case 1146: return "IBM01146";
            case 1147: return "IBM01147";
            case 1148: return "IBM01148";
            case 1149: return "IBM01149";
            case 1200: return "UTF-16";
            case 1201: return "UTF-16BE";
            case 1250: return "windows-1250";
            case 1251: return "windows-1251";
            case 1252: return "windows-1252";
            case 1253: return "windows-1253";
            case 1254: return "windows-1254";
            case 1255: return "windows-1255";
            case 1256: return "windows-1256";
            case 1257: return "windows-1257";
            case 1258: return "windows-1258";
            case 1361: return "x-Johab";      // Korean, ms1361, ksc5601_1992, johab
            case 10000: return "macintosh";
            case 10001: return "x-mac-japanese";
            case 10002: return "x-mac-chinesetrad";
            case 10003: return "x-mac-korean";
            case 10004: return "x-MacArabic";
            case 10005: return "x-MacHebrew";
            case 10006: return "x-MacGreek";
            case 10007: return "x-MacCyrillic";
            case 10008: return "x-mac-chinesesimp";
            case 10010: return "x-MacRomania";
            case 10017: return "x-MacUkraine";
            case 10021: return "x-MacThai";
            case 10029: return "x-MacCentralEurope";
            case 10079: return "x-MacIceland";
            case 10081: return "x-mac-turkish";
            case 10082: return "x-MacCroatian";
            case 12000: return "UTF-32";
            case 12001: return "UTF-32BE";
            case 20000: return "x-EUC-TW";
            case 20001: return "x-cp20001";
            case 20002: return "x_Chinese-Eten";
            case 20003: return "x-cp20003";
            case 20004: return "x-cp20004";
            case 20005: return "x-cp20005";
            case 20105: return "x-IA5";
            case 20106: return "x-IA5-German";
            case 20107: return "x-IA5-Swedish";
            case 20108: return "x-IA5-Norwegian";
            case 20127: return "US-ASCII";
            case 20261: return "x-cp20261";
            case 20269: return "x-cp20269";
            case 20273: return "IBM273";
            case 20277: return "IBM277";
            case 20278: return "IBM278";
            case 20280: return "IBM280";
            case 20284: return "IBM284";
            case 20285: return "IBM285";
            case 20290: return "IBM290";
            case 20297: return "IBM297";
            case 20420: return "IBM420";
            case 20423: return "IBM423";
            case 20424: return "IBM424";
            case 20833: return "x-EBCDIC-KoreanExtended"; // KOREAN
            case 20838: return "IBM-Thai";
            case 20866: return "KOI8-R";
            case 20871: return "IBM871";
            case 20880: return "IBM880";
            case 20905: return "IBM905";
            case 20924: return "IBM00924";
            case 20932: return "EUC-JP";
            case 20936: return "GBK"; //"GB2312"; // ??? x-cp20936
            case 20949: return "x-windows-949";  // KOREAN  x-cp20949
            case 21025: return "x-IBM1025";
            //case 21027: return ""; // (deprecated)
            case 21866: return "KOI8-U";
            case 28591: return "ISO-8859-1";
            case 28592: return "ISO-8859-2";
            case 28593: return "ISO-8859-3";
            case 28594: return "ISO-8859-4";
            case 28595: return "ISO-8859-5";
            case 28596: return "ISO-8859-6";
            case 28597: return "ISO-8859-7";
            case 28598: return "ISO-8859-8";
            case 28599: return "ISO-8859-9";
            case 28603: return "ISO-8859-13";
            case 28605: return "ISO-8859-15";
            case 29001: return "x-Europa";
            case 38598: return "ISO-8859-8"; // "iso-8859-8-i";
            case 50220: return "ISO-2022-JP";
            case 50221: return "ISO-2022-JP";
            case 50222: return "ISO-2022-JP";
            case 50225: return "ISO-2022-KR";
            case 50227: return "ISO-2022-CN";
            //case 50229: return "ISO-2022-CN";
            //case 50930: return ""; // EBCDIC Japanese (Katakana) Extended
            //case 50931: return ""; // EBCDIC US-Canada and Japanese
            //case 50933: return ""; // EBCDIC Korean Extended and Korean
            //case 50935: return ""; // EBCDIC Simplified Chinese Extended and Simplified Chinese
            //case 50936: return ""; // EBCDIC Simplified Chinese
            //case 50937: return ""; // EBCDIC US-Canada and Traditional Chinese
            //case 50939: return ""; // EBCDIC Japanese (Latin) Extended and Japanese
            case 51932: return "EUC-JP";
            case 51936: return "GB2312"; // euc-cn
            case 51949: return "EUC-KR";
            //case 51950: return ""; // EUC Traditional Chinese
            case 52936: return "hz-gb-2312";
            case 54936: return "GB18030";
            case 57002: return "x-iscii-de";
            case 57003: return "x-iscii-be";
            case 57004: return "x-iscii-ta";
            case 57005: return "x-iscii-te";
            case 57006: return "x-iscii-as";
            case 57007: return "x-iscii-or";
            case 57008: return "x-iscii-ka";
            case 57009: return "x-iscii-ma";
            case 57010: return "x-iscii-gu";
            case 57011: return "x-iscii-pa";
            case 65000: return "UTF-7";
            case 65001: return "UTF-8";
            default: 
                fprintf(stderr, "Unknown Character Set Code : %s\n", locstr);
                break;
        }
    }
#endif
    //fprintf(stderr, "Character Set Code : %s\n", locstr);

	return locstr;
}

jstring getJvmCharacterSet(JNIEnv *env) {
    // Java Character Set
    // System.getProperty("file.encoding");
    if(env == NULL) return NULL;
    jstring charset = NULL;
    jclass java_lang_System = env->FindClass("java/lang/System");
    if(java_lang_System != NULL) {
        jmethodID java_lang_System_getProperty = env->GetStaticMethodID(java_lang_System, "getProperty", "(Ljava/lang/String;)Ljava/lang/String;");
        if(java_lang_System_getProperty != NULL) {
            jstring file_encoding = env->NewStringUTF("file.encoding");
            if(file_encoding != NULL) {
                charset = static_cast<jstring>(env->CallStaticObjectMethod(java_lang_System, java_lang_System_getProperty, file_encoding));
                env->DeleteLocalRef(file_encoding);
            }
        }
        env->DeleteLocalRef(java_lang_System);
    }
    return charset;
}

jstring getPosibleCharacterSet(JNIEnv *env, const char *nativ_char_set) {
    jstring charset = NULL;
    
    if(env == NULL || nativ_char_set == NULL) return NULL;
    
    //java.nio.charset.Charset
    jclass java_nio_charset_Charset = env->FindClass("java/nio/charset/Charset");
    if(java_nio_charset_Charset != NULL) {
        jmethodID java_nio_charset_Charset_forName = env->GetStaticMethodID(java_nio_charset_Charset, "forName", "(Ljava/lang/String;)Ljava/nio/charset/Charset;");
        if(java_nio_charset_Charset_forName != NULL) {
            jstring nativeCharset = env->NewStringUTF(nativ_char_set);
            if(nativeCharset != NULL) {
                jobject jCharset = env->CallStaticObjectMethod(java_nio_charset_Charset, java_nio_charset_Charset_forName, nativeCharset);
                if(env->ExceptionCheck()) {
                    fprintf(stderr, "getPosibleCharacterSet() UnsupportedEncodingException occurred : %s\n", nativ_char_set);
                    env->ExceptionClear();
                }
                if(jCharset != NULL) {
                    jmethodID java_nio_charset_Charset_name = env->GetMethodID(java_nio_charset_Charset, "name", "()Ljava/lang/String;");
                    if(java_nio_charset_Charset_name != NULL) {
                        charset = static_cast<jstring>(env->CallObjectMethod(jCharset, java_nio_charset_Charset_name));
                    }
                    env->DeleteLocalRef(jCharset);
                }
                env->DeleteLocalRef(nativeCharset);
            }
        }
        env->DeleteLocalRef(java_nio_charset_Charset);
    }

    return charset;
}

jstring getEncodingCharacterSet(JNIEnv *env) {
    jstring encodingSet = NULL;
    
    if(env == NULL) return NULL;

    const char *nativeCharSet = getNativeCharacterSet();
    if(nativeCharSet != NULL && *nativeCharSet != '\0') {
        jstring jvmCharSet = getJvmCharacterSet(env);
        if(jvmCharSet != NULL) {
            const char *jcharset = env->GetStringUTFChars(jvmCharSet, 0);
            if(jcharset != NULL) {
                if(strcmp(jcharset, nativeCharSet) != 0) {
                    encodingSet = getPosibleCharacterSet(env, nativeCharSet);
                } else {
                    printf("Same character set : %s\n", nativeCharSet);
                }
                env->ReleaseStringUTFChars(jvmCharSet, jcharset);
            } else {
                fprintf(stderr, "Failure : jvmCharSet GetStringUTFChars...\n");
                encodingSet = getPosibleCharacterSet(env, nativeCharSet);
            }
            env->DeleteLocalRef(jvmCharSet);
        } else {
            fprintf(stderr, "Failure : getJvmCharacterSet...\n");
            encodingSet = getPosibleCharacterSet(env, nativeCharSet);
        }
    } else {
        fprintf(stderr, "Failure : getNativeCharacterSet...\n");
    }
    
    if(encodingSet != NULL) {
        const char *encoding = env->GetStringUTFChars(encodingSet, 0);
        if(encoding != NULL) {
            printf("Get encoding character set : %s\n", encoding);
            env->ReleaseStringUTFChars(encodingSet, encoding);
        } else {
            printf("Get encoding character set : unknown\n");
        }
    }

    return encodingSet;
}
