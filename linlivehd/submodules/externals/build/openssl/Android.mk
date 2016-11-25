LOCAL_PATH := $(call my-dir)

subdirs := $(addprefix $(LOCAL_PATH)/,$(addsuffix /Android.mk, \
		crypto \
		ssl \
	))

include $(subdirs)

# static library
# =====================================================

include $(CLEAR_VARS)
LOCAL_SRC_FILES:=
LOCAL_C_INCLUDES:=
LOCAL_WHOLE_STATIC_LIBRARIES += libcrypto libssl
LOCAL_MODULE:= libopenssl
include $(BUILD_STATIC_LIBRARY)