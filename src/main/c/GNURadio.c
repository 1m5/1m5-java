#include <jni.h>
#include <std.io>
#include "GNURadio.h"

JNIEXPORT jint JNICALL Java_io_onemfive_radio_vendor_GNURadio_sendMessage(JNIEnv *, jobject, jbyteArray) {
    printf("Message sent.")
    return 1;
}