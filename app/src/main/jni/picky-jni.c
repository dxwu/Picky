#include <jni.h>
#include <fcntl.h>
#include <stdio.h>
#include <string.h>
#include <unistd.h>
#include <errno.h>

#include "binder_filter.h"

int fd = -1;

// returns positive on success, negative on fail
int setup_dev_perm() {
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

JNIEXPORT jint JNICALL
Java_edu_dartmouth_dwu_picky_Policy_nativeInitPolicyPersistFile(JNIEnv *env, jclass type) {

    if (popen("su -c \"if [ ! -f /data/local/tmp/bf.policy ]; then touch /data/local/tmp/bf.policy; chmod 777 /data/local/tmp/bf.policy; fi\"", "r") == NULL) {
        return -1;
    }

    return 0;
}

JNIEXPORT jstring JNICALL
Java_edu_dartmouth_dwu_picky_Policy_nativeReadPolicy(JNIEnv *env, jclass type) {

    int sizeRead = 1024;
    char* returnValue = (char*) malloc(sizeRead+1);

    if (fd < 0) {
        setup_dev_perm();
    }

    strcpy(returnValue, "empty");

    // ssize_t read(int fd, void *buf, size_t count);
    int len = read(fd, returnValue, sizeRead);
    returnValue[len] = '\0';

    free(returnValue);
    return (*env)->NewStringUTF(env, returnValue);
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
        setup_dev_perm();
    }

    strcpy(ret, "nativeWriteUserFilter:\n");

    struct bf_user_filter user_filter;
    user_filter.action = (int) action;
    user_filter.uid = (int) uid;
    user_filter.message = (char*) message;
    user_filter.data = (char*) data;
    user_filter.context = 0;

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

// overloaded with int value
JNIEXPORT jint JNICALL
Java_edu_dartmouth_dwu_picky_Policy_nativeWriteContextFilterLine__IILjava_lang_String_2Ljava_lang_String_2III(
        JNIEnv *env, jclass type, jint action, jint uid, jstring message_, jstring data_,
        jint context, jint contextType, jint contextIntValue) {
    const char *message = (*env)->GetStringUTFChars(env, message_, 0);
    const char *data = (*env)->GetStringUTFChars(env, data_, 0);

    if (fd < 0) {
        setup_dev_perm();
    }

    struct bf_user_filter user_filter;
    user_filter.action = (int) action;
    user_filter.uid = (int) uid;
    user_filter.message = (char*) message;
    user_filter.data = (char*) data;
    user_filter.context = (int) context;
    user_filter.context_type = (int) contextType;
    user_filter.context_int_value = (int) contextIntValue;

    int write_len = write(fd, &user_filter, sizeof(user_filter));

    (*env)->ReleaseStringUTFChars(env, message_, message);
    (*env)->ReleaseStringUTFChars(env, data_, data);

    return write_len;
}

// overloaded with string value
JNIEXPORT jint JNICALL
Java_edu_dartmouth_dwu_picky_Policy_nativeWriteContextFilterLine__IILjava_lang_String_2Ljava_lang_String_2IILjava_lang_String_2(
        JNIEnv *env, jclass type, jint action, jint uid, jstring message_, jstring data_,
        jint context, jint contextType, jstring contextStringValue_) {
    const char *message = (*env)->GetStringUTFChars(env, message_, 0);
    const char *data = (*env)->GetStringUTFChars(env, data_, 0);
    const char *contextStringValue = (*env)->GetStringUTFChars(env, contextStringValue_, 0);

    struct bf_user_filter user_filter;
    user_filter.action = (int) action;
    user_filter.uid = (int) uid;
    user_filter.message = (char*) message;
    user_filter.data = (char*) data;
    user_filter.context = (int) context;
    user_filter.context_type = (int) contextType;
    user_filter.context_string_value = (char*) contextStringValue;

    int write_len = write(fd, &user_filter, sizeof(user_filter));

    (*env)->ReleaseStringUTFChars(env, message_, message);
    (*env)->ReleaseStringUTFChars(env, data_, data);
    (*env)->ReleaseStringUTFChars(env, contextStringValue_, contextStringValue);

    return write_len;
}