#include <jni.h>
#include <fcntl.h>
#include <stdio.h>
#include <string.h>
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
Java_edu_dartmouth_dwu_picky_Policy_nativeSetUpPermissions(JNIEnv *env, jclass type) {


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


JNIEXPORT jstring JNICALL
Java_edu_dartmouth_dwu_picky_Policy_nativeWriteFilterLine(JNIEnv *env, jclass type, jint action,
                                                          jint uid, jstring message_,
                                                          jstring data_) {
    const char *message = (*env)->GetStringUTFChars(env, message_, 0);
    const char *data = (*env)->GetStringUTFChars(env, data_, 0);

    char ret[1024];
    char str[128];

    if (fd < 0) {
        setup_already = 0;
        setup_dev_perm();
    }

    strcpy(ret, "nativeWriteUserFilter:\n");

    struct bf_user_filter user_filter;
    user_filter.action = (int) action;
    user_filter.uid = (int) uid;
    user_filter.message = (char*) message;
    user_filter.data = (char*) data;

    // size_t write(int fildes, const void *buf, size_t nbytes);
    int write_len = write(fd, &user_filter, sizeof(user_filter));

    strcat(ret, "Opened driver, fd: ");
    sprintf(str, "%d", fd);
    strcat(ret, str);
    strcat(ret, "\n");
    strcat(ret, "writelen: ");
    sprintf(str, "%d", write_len);
    strcat(ret, str);

    (*env)->ReleaseStringUTFChars(env, message_, message);
    (*env)->ReleaseStringUTFChars(env, data_, data);

    return (*env)->NewStringUTF(env, ret);
}

JNIEXPORT jstring JNICALL
Java_edu_dartmouth_dwu_picky_Policy_nativeReadPolicy(JNIEnv *env, jclass type) {

    int sizeRead = 1024;
    char* returnValue = (char*) malloc(sizeRead+1);

    if (fd < 0) {
        setup_already = 0;
        setup_dev_perm();
    }

    strcpy(returnValue, "empty");

    // ssize_t read(int fd, void *buf, size_t count);
    int len = read(fd, returnValue, sizeRead);
    returnValue[len] = '\0';

    return (*env)->NewStringUTF(env, returnValue);
}