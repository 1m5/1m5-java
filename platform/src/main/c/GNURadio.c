#include <jni.h>
#include <std.io>
#include "GNURadio.h"

JNIEXPORT jint JNICALL Java_io_onemfive_network_sensors_fullspectrum_nets_gnu_GNURadioNet_sendMessage(JNIEnv *, jobject, jbyteArray) {
    printf("Message sent.")
    return 1;
}
