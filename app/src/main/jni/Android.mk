####
####include $(call all-subdir-makefiles)
####
#   

LOCAL_PATH        := $(call my-dir)
include $(CLEAR_VARS)



LOCAL_MODULE := libthSDK

LOCAL_ALLOW_UNDEFINED_SYMBOLS := false
LOCAL_ALLOW_UNDEFINED_SYMBOLS := false
LOCAL_DISABLE_FATAL_LINKER_WARNINGS=true

LOCAL_CFLAGS += -DGL_GLEXT_PROTOTYPES
LOCAL_CFLAGS += -D__STDC_CONSTANT_MACROS=1
LOCAL_CPPFLAGS += -fexceptions -frtti

#LOCAL_SRC_FILES := libAVAPIs.so libIOTCAPIs.so liblSDL2.so
LOCAL_SRC_FILES := \
  android/jniMain.c \
  common/av_Queue.c common/List.c common/skt.c common/TFun.c cJSON/cJSON.c \
  avDecode/audio_g711.c \
  thSDK/thPlay.c thSDK/axdllEx.c \
  avDecode/thSDL.c avDecode/thOpenGL_SLES.c avDecode/thRec.c \
  avDecode/thImg.c avDecode/thVideoDec.c \
  avDecode/thRender.c \
\
  avDecode/libjpeg/myJpeg.c \
  avDecode/libjpeg/jcapimin.c avDecode/libjpeg/jcapistd.c avDecode/libjpeg/jccoefct.c \
  avDecode/libjpeg/jccolor.c avDecode/libjpeg/jcdctmgr.c avDecode/libjpeg/jchuff.c \
  avDecode/libjpeg/jcinit.c avDecode/libjpeg/jcmainct.c avDecode/libjpeg/jcmarker.c \
  avDecode/libjpeg/jcmaster.c avDecode/libjpeg/jcomapi.c avDecode/libjpeg/jcparam.c \
  avDecode/libjpeg/jcphuff.c avDecode/libjpeg/jcprepct.c avDecode/libjpeg/jcsample.c \
  avDecode/libjpeg/jctrans.c avDecode/libjpeg/jdapimin.c avDecode/libjpeg/jdapistd.c \
  avDecode/libjpeg/jdatadst.c avDecode/libjpeg/jdatasrc.c avDecode/libjpeg/jdcoefct.c \
  avDecode/libjpeg/jdcolor.c avDecode/libjpeg/jddctmgr.c avDecode/libjpeg/jdhuff.c \
  avDecode/libjpeg/jdinput.c avDecode/libjpeg/jdmainct.c avDecode/libjpeg/jdmarker.c \
  avDecode/libjpeg/jdmaster.c avDecode/libjpeg/jdmerge.c avDecode/libjpeg/jdphuff.c \
  avDecode/libjpeg/jdpostct.c avDecode/libjpeg/jdsample.c avDecode/libjpeg/jdtrans.c \
  avDecode/libjpeg/jerror.c avDecode/libjpeg/jfdctflt.c avDecode/libjpeg/jfdctfst.c \
  avDecode/libjpeg/jfdctint.c avDecode/libjpeg/jidctflt.c avDecode/libjpeg/jidctfst.c \
  avDecode/libjpeg/jidctint.c avDecode/libjpeg/jidctred.c avDecode/libjpeg/jquant1.c \
  avDecode/libjpeg/jquant2.c avDecode/libjpeg/jutils.c avDecode/libjpeg/jmemmgr.c \
  avDecode/libjpeg/jmemansi.c \
  \

LOCAL_C_INCLUDES := \
  $(LOCAL_PATH)/include/ffmpeg 

LOCAL_LDLIBS :=   \
  -llog -lz -lGLESv1_CM -ldl -L$(LOCAL_PATH) \
  -lavformat -lavcodec -lavdevice -lavfilter -lavutil -lswscale -lswresample -lAVAPIs -lIOTCAPIs -lSmartConnection \
  -lvo-aacenc -lSDL2 -Llib.android

LOCAL_LDLIBS    += -lOpenSLES
LOCAL_LDLIBS    += -landroid

include $(BUILD_SHARED_LIBRARY)

