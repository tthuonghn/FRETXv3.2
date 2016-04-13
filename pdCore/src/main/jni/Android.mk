LOCAL_CPPFLAGS  += -std=c++11
LOCAL_LDLIBS += -latomic
include $(call all-subdir-makefiles)

LOCAL_PATH := $(call my-dir)

#---------------------------------------------------------------

include $(CLEAR_VARS)
LOCAL_MODULE := pd
LOCAL_EXPORT_C_INCLUDES := ../../pdCore/jni/libpd/pure-data/src
LOCAL_SRC_FILES := ../../pdCore/libs/$(TARGET_ARCH_ABI)/libpd.so
ifneq ($(MAKECMDGOALS),clean)
    include $(PREBUILT_SHARED_LIBRARY)
endif
