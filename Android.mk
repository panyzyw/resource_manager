LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)

	include $(CLEAR_VARS)
	LOCAL_MODULE := libpl_droidsonroids_gif
	LOCAL_SRC_FILES_32 := libs/armeabi-v7a/libpl_droidsonroids_gif.so
	LOCAL_MULTILIB := 32
	LOCAL_MODULE_CLASS := SHARED_LIBRARIES
	LOCAL_MODULE_SUFFIX := .so
	include $(BUILD_PREBUILT)
	
	include $(CLEAR_VARS)
	LOCAL_MODULE := libpl_droidsonroids_gif_surface
	LOCAL_SRC_FILES_32 := libs/armeabi-v7a/libpl_droidsonroids_gif_surface.so
	LOCAL_MULTILIB := 32
	LOCAL_MODULE_CLASS := SHARED_LIBRARIES
	LOCAL_MODULE_SUFFIX := .so
	include $(BUILD_PREBUILT)

	
	include $(CLEAR_VARS)
	LOCAL_MODULE := libffmpeg
	LOCAL_SRC_FILES_32 := libs/armeabi-v7a/libffmpeg.so
	LOCAL_MULTILIB := 32
	LOCAL_MODULE_CLASS := SHARED_LIBRARIES
	LOCAL_MODULE_SUFFIX := .so
	include $(BUILD_PREBUILT)

	
	include $(CLEAR_VARS)
	LOCAL_MODULE := libOMX.9
	LOCAL_SRC_FILES_32 := libs/armeabi-v7a/libOMX.9.so
	LOCAL_MULTILIB := 32
	LOCAL_MODULE_CLASS := SHARED_LIBRARIES
	LOCAL_MODULE_SUFFIX := .so
	include $(BUILD_PREBUILT)

	
	include $(CLEAR_VARS)
	LOCAL_MODULE := libOMX.11
	LOCAL_SRC_FILES_32 := libs/armeabi-v7a/libOMX.11.so
	LOCAL_MULTILIB := 32
	LOCAL_MODULE_CLASS := SHARED_LIBRARIES
	LOCAL_MODULE_SUFFIX := .so
	include $(BUILD_PREBUILT)

	
	include $(CLEAR_VARS)
	LOCAL_MODULE := libOMX.14
	LOCAL_SRC_FILES_32 := libs/armeabi-v7a/libOMX.14.so
	LOCAL_MULTILIB := 32
	LOCAL_MODULE_CLASS := SHARED_LIBRARIES
	LOCAL_MODULE_SUFFIX := .so
	include $(BUILD_PREBUILT)

	
	include $(CLEAR_VARS)
	LOCAL_MODULE := libOMX.18
	LOCAL_SRC_FILES_32 := libs/armeabi-v7a/libOMX.18.so
	LOCAL_MULTILIB := 32
	LOCAL_MODULE_CLASS := SHARED_LIBRARIES
	LOCAL_MODULE_SUFFIX := .so
	include $(BUILD_PREBUILT)

	
	include $(CLEAR_VARS)
	LOCAL_MODULE := libstlport_shared
	LOCAL_SRC_FILES_32 := libs/armeabi-v7a/libstlport_shared.so
	LOCAL_MULTILIB := 32
	LOCAL_MODULE_CLASS := SHARED_LIBRARIES
	LOCAL_MODULE_SUFFIX := .so
	include $(BUILD_PREBUILT)

	
	include $(CLEAR_VARS)
	LOCAL_MODULE := libvao.0
	LOCAL_SRC_FILES_32 := libs/armeabi-v7a/libvao.0.so
	LOCAL_MULTILIB := 32
	LOCAL_MODULE_CLASS := SHARED_LIBRARIES
	LOCAL_MODULE_SUFFIX := .so
	include $(BUILD_PREBUILT)

	
	include $(CLEAR_VARS)
	LOCAL_MODULE := libvplayer
	LOCAL_SRC_FILES_32 := libs/armeabi-v7a/libvplayer.so
	LOCAL_MULTILIB := 32
	LOCAL_MODULE_CLASS := SHARED_LIBRARIES
	LOCAL_MODULE_SUFFIX := .so
	include $(BUILD_PREBUILT)

	
	include $(CLEAR_VARS)
	LOCAL_MODULE := libvscanner
	LOCAL_SRC_FILES_32 := libs/armeabi-v7a/libvscanner.so
	LOCAL_MULTILIB := 32
	LOCAL_MODULE_CLASS := SHARED_LIBRARIES
	LOCAL_MODULE_SUFFIX := .so
	include $(BUILD_PREBUILT)

	
	include $(CLEAR_VARS)
	LOCAL_MODULE := libvvo.0
	LOCAL_SRC_FILES_32 := libs/armeabi-v7a/libvvo.0.so
	LOCAL_MULTILIB := 32
	LOCAL_MODULE_CLASS := SHARED_LIBRARIES
	LOCAL_MODULE_SUFFIX := .so
	include $(BUILD_PREBUILT)

	
	include $(CLEAR_VARS)
	LOCAL_MODULE := libvvo.7
	LOCAL_SRC_FILES_32 := libs/armeabi-v7a/libvvo.7.so
	LOCAL_MULTILIB := 32
	LOCAL_MODULE_CLASS := SHARED_LIBRARIES
	LOCAL_MODULE_SUFFIX := .so
	include $(BUILD_PREBUILT)

	
	include $(CLEAR_VARS)
	LOCAL_MODULE := libvvo.8
	LOCAL_SRC_FILES_32 := libs/armeabi-v7a/libvvo.8.so
	LOCAL_MULTILIB := 32
	LOCAL_MODULE_CLASS := SHARED_LIBRARIES
	LOCAL_MODULE_SUFFIX := .so
	include $(BUILD_PREBUILT)

	
	include $(CLEAR_VARS)
	LOCAL_MODULE := libvvo.9
	LOCAL_SRC_FILES_32 := libs/armeabi-v7a/libvvo.9.so
	LOCAL_MULTILIB := 32
	LOCAL_MODULE_CLASS := SHARED_LIBRARIES
	LOCAL_MODULE_SUFFIX := .so
	include $(BUILD_PREBUILT)

	
	include $(CLEAR_VARS)
	LOCAL_MODULE := libvvo.j
	LOCAL_SRC_FILES_32 := libs/armeabi-v7a/libvvo.j.so
	LOCAL_MULTILIB := 32
	LOCAL_MODULE_CLASS := SHARED_LIBRARIES
	LOCAL_MODULE_SUFFIX := .so
	include $(BUILD_PREBUILT)


include $(CLEAR_VARS)
LOCAL_JNI_SHARED_LIBRARIES +=libpl_droidsonroids_gif_surface libpl_droidsonroids_gif libffmpeg libOMX.9 libOMX.11 libOMX.14 libOMX.18 libstlport_shared libvao.0 libvplayer  libvscanner libvvo.0 libvvo.7 libvvo.8 libvvo.9 libvvo.j
LOCAL_MODULE_TAGS := optional
LOCAL_CERTIFICATE := platform

LOCAL_PACKAGE_NAME := YYDRobotResourceManager
LOCAL_SRC_FILES := $(call all-java-files-under, src)
LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
 
LOCAL_STATIC_JAVA_LIBRARIES := \
	android-support-v4 \
	libresourceamnager1 \
	libresourceamnager2 \
	libresourceamnager3 \
	libresourceamnager4 

LOCAL_PROGUARD_ENABLED := disabled

include $(BUILD_PACKAGE)	


include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := \
	libresourceamnager1:libs/picasso-2.5.2.jar \
	libresourceamnager2:libs/nineoldandroidslibrary.jar \
	libresourceamnager3:libs/classes.jar \
	libresourceamnager4:libs/initactivity.jar


include $(BUILD_MULTI_PREBUILT)