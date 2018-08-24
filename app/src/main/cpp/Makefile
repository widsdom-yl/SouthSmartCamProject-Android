#android/jniMain.c \

FILESRC = \
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
  -Iinclude -Iinclude/ffmpeg \
  \
  -lavformat -lavcodec -lavdevice -lavfilter -lavutil -lswscale -lswresample -lAVAPIs -lIOTCAPIs -lSmartConnection \
  -lvo-aacenc -lSDL2 -Llinux/lib

all:
	gcc -o aaa $(FILESRC)
 
#-----------------------------------------------------------------------------
