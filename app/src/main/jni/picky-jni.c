#include <jni.h>
#include <fcntl.h>
#include <stdio.h>
#include <unistd.h>
#include <errno.h>

#include "binder_filter.h"

int setup_already = 0;
int fd = -1;

// returns positive on success, negative on fail
int setup_dev_perm() {
    if (setup_already == 1) {
        if (fd >= 0) {
            return fd;
        } else {
            return -4;
        }
    }

    // enable binderfilter
    if (popen("su -c \"echo 1 > /sys/module/binder_filter/parameters/filter_enable\"", "r") == NULL) {
        return -1;
    }

    // chmod binderfilter
    if (popen("su -c chmod 666 /dev/binderfilter", "r") == NULL) {
        return -2;
    }

    // change SELinux policy to allow our driver (take on type of binder driver)
    if (popen("su -c chcon u:object_r:binder_device:s0 /dev/binderfilter", "r") == NULL) {
        return -3;
    }

    setup_already = 1;

    fd = open("/dev/binderfilter", O_RDWR);
    if (fd >= 0) {
        return fd;
    } else {
        return -4;
    }
}


JNIEXPORT jstring JNICALL
Java_edu_dartmouth_dwu_picky_MainActivity_nativeWriteUserFilter(JNIEnv *env, jobject instance,
                                                                jobjectArray intents,
                                                                jint level_noBT,
                                                                jint level_withBT) {
    char ret[300];
    char str[50];

    if (fd < 0) {
        setup_already = 0;
        setup_dev_perm();
    }

    strcpy(ret, "nativeWriteUserFilter:\n");

    struct bf_user_filter user_filter;
    user_filter.level_value_no_BT = (int) level_noBT;
    user_filter.level_value_with_BT = (int) level_withBT;

    int intents_length = (*env)->GetArrayLength(env, intents);
    char* c_intents[intents_length];

    for (int i=0; i<intents_length; i++) {
        jstring string = (jstring) (*env)->GetObjectArrayElement(env, intents, i);
        char *rawString = (char*) ((*env)->GetStringUTFChars(env, string, 0));
        c_intents[i] = rawString;

        sprintf(str, "\t%s\n", c_intents[i]);
        strcat(ret, str);
    }

    user_filter.intents = c_intents;
    user_filter.intents_len = intents_length;

    // size_t write(int fildes, const void *buf, size_t nbytes);
    int write_len = write(fd, &user_filter, sizeof(user_filter));

    for (int i=0; i<intents_length; i++) {
        jstring string = (jstring) (*env)->GetObjectArrayElement(env, intents, i);
        (*env)->ReleaseStringUTFChars(env, string, c_intents[i]);
    }

    strcat(ret, "Opened driver, fd: ");
    sprintf(str, "%d", fd);
    strcat(ret, str);
    strcat(ret, "\n");
    strcat(ret, "writelen: ");
    sprintf(str, "%d", write_len);
    strcat(ret, str);
    return (*env)->NewStringUTF(env, ret);
}


JNIEXPORT jstring JNICALL
Java_edu_dartmouth_dwu_picky_MainActivity_nativeSetUpPermissions(JNIEnv *env, jobject instance) {

    int r = setup_dev_perm();
    if (r == -1) {
        return (*env)->NewStringUTF(env, "filter_enable failed");
    } else if (r == -2) {
        return (*env)->NewStringUTF(env, "chmod failed");
    } else if (r == -3) {
        return (*env)->NewStringUTF(env, "chcon failed");
    } else if (r == -4) {
        return (*env)->NewStringUTF(env, "open driver failed");
    }

    return (*env)->NewStringUTF(env, "Setup success");
}