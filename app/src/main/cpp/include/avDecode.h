//-----------------------------------------------------------------------------
// Author      : 朱红波
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#ifndef avDecode_h
#define avDecode_h

#include "cm_types.h"

#ifdef WIN32
#define IS_VIDEOPLAY_DDRAW//#define IS_VIDEOPLAY_SDL
#define IS_AUDIOPLAY_SDL
#define IS_AUDIOTALK_SDL
//#define IS_AUDIOPLAY_MMSYSTEM
//#define IS_AUDIOTALK_MMSYSTEM
#endif

#ifdef ANDROID
#define IS_VIDEOPLAY_OPENGL
#define IS_AUDIOPLAY_SLES
#define IS_AUDIOTALK_SLES
//#define IS_VIDEOPLAY_SDL
//#define IS_AUDIOPLAY_SDL
//#define IS_AUDIOTALK_SDL
#endif

#ifdef IPHONE
//#define IS_VIDEOPLAY_OPENGL
#define IS_VIDEOPLAY_SDL
#define IS_AUDIOPLAY_SDL
#define IS_AUDIOTALK_SDL
#endif

#ifndef EXPORT
#ifdef WIN32
#define EXPORT __declspec(dllexport)
#else
#define EXPORT
#endif
#endif

#ifdef __cplusplus
extern "C"
{
#endif 
  //-----------------------------------------------------------------------------
#define FMT_YUV420P       0
#define FMT_BGR24         3
#define FMT_BGR32        30

  typedef enum TMediaType {
    CODEC_NONE  =       0,//AV_CODEC_ID_NONE
    CODEC_MJPEG =       8,//AV_CODEC_ID_MJPEG
    CODEC_MPEG4 =      13,//AV_CODEC_ID_MPEG4
    CODEC_H264  =      28,//AV_CODEC_ID_H264
    CODEC_H265  =     174,//AV_CODEC_ID_H265 AV_CODEC_ID_HEVC
    CODEC_PCM   = 0x10000,//AV_CODEC_ID_PCM_S16LE
    CODEC_G711  = 0x10007,//AV_CODEC_ID_PCM_ALAW
    CODEC_AAC   = 0x15002,//AV_CODEC_ID_AAC
  }TMediaType;

//#define AV_PIX_FMT_RGB565   44
//#define AV_PIX_FMT_YUV420P   0

  typedef struct TencParam {
    int VideoType;                   //MJPEG=8, MPEG4=13  H264=28 H265=174 AAC=0x15002=86018
    int FrameRate;                   //帧率 1-30 MAX:PAL 25 NTSC 30
    int IPInterval;                  //IP帧间隔 1-120 default 30
    int BitRate;                     //码流 64K 128K 256K 512K 1024K 1536K 2048K 2560K 3072K

    char* yuvbuf;                    //必须先申请

    int encWidth;                    //宽 720 360 180 704 352 176 640 320 160
    int encHeight;                   //高 480 240 120 576 288 144 
    char* encBuf;                    //必须先申请
    int encBufSize;                  //指定encBuf 大小
    int encLen;                      //解码后数据大小
    int IsKeyFrame;                  //是否I帧
    int ForceKeyFrame;               //为1时，强制编码成I帧

  }TencParam;

  typedef struct TavPicture {
    unsigned char* data[8];
    int linesize[8];
  } TavPicture;

  //-----------------------------------------------------------------------------
  EXPORT int g711_decode(char* dstBuf, const unsigned char* srcBuf, int srcBufLen);
  EXPORT int g711_encode(unsigned char* dstBuf, const char* srcBuf, int srcBufLen);
  EXPORT int pcm_DB(unsigned char* buf, int size);
  //-----------------------------------------------------------------------------
  EXPORT int thImgConvertFill(TavPicture* pFrame, char* Buf, int fmt, int Width, int Height);
  EXPORT bool thImgConvertScale(char* d, int dfmt, int dw, int dh, char* s, int sfmt, int sw, int sh, int IsFlip);
  EXPORT bool thImgConvertScale1(TavPicture* dst, int dfmt, int dw, int dh, TavPicture* src, int sfmt, int sw, int sh, int IsFlip);
  EXPORT bool thImgConvertScale2(char* d, int dfmt, int dw, int dh, TavPicture* src, int sfmt, int sw, int sh, int IsFlip);
  //-----------------------------------------------------------------------------
  EXPORT HANDLE thDecodeVideoInit(int VideoType);
  EXPORT bool thDecodeVideoFrame(HANDLE decHandle, char* encBuf, int encLen, int* decWidth, int* decHeight, TavPicture* avPicture);
  EXPORT bool thDecodeVideoSaveToJpg(HANDLE decHandle, char* FileName_Jpg);
  EXPORT bool thDecodeVideoFree(HANDLE decHandle);
  //-----------------------------------------------------------------------------
  EXPORT HANDLE thEncodeVideoInit(TencParam* Param);
  EXPORT bool thEncodeVideoFrame(HANDLE encHandle, TencParam* Param);
  EXPORT bool thEncodeVideoFree(HANDLE encHandle);
  //-----------------------------------------------------------------------------
  EXPORT HANDLE thRecordStart(char* FileName, TMediaType VideoType, int Width, int Height, int FrameRate, TMediaType AudioType, int nChannels, int nSamplesPerSec, int wBitsPerSample);
  EXPORT bool thRecordWriteVideo(HANDLE RecHandle, char* Buf, int Len, i64 pts);//pts = -1 为自动计算
  EXPORT bool thRecordWriteAudio(HANDLE RecHandle, char* Buf, int Len, i64 pts);//pts = -1 为自动计算 Len只能为2048，不然会丢包
  EXPORT bool thRecordIsRec(HANDLE RecHandle);
  EXPORT bool thRecordStop(HANDLE RecHandle);
  //-----------------------------------------------------------------------------
  EXPORT HANDLE thRender_Init(int iDDrawMode/*0=VIDEOMEMORY 1=SYSTEMMEMORY*/);
  EXPORT bool thRender_Free(HANDLE videoHandle);
  EXPORT bool thRender_FillMem(HANDLE videoHandle, TavPicture v, i32 ImgWidth, i32 ImgHeight);
  EXPORT bool thRender_Display(HANDLE videoHandle, HWND DspHandle, TRect DspRect);

  EXPORT HANDLE thAudioPlay_Init();
  EXPORT bool thAudioPlay_SetFormat(HANDLE audioHandle, i32 nChannels, i32 wBitsPerSample, i32 nSamplesPerSec, i32 AudioPacketSize);
  EXPORT bool thAudioPlay_PlayFrame(HANDLE audioHandle, char* Buf, i32 BufLen);
  EXPORT bool thAudioPlay_Free(HANDLE audioHandle);

  typedef void(TAudioTalkCallBack)(void* UserCustom, char* Buf, i32 Len);
  EXPORT HANDLE thAudioTalk_Init();
  EXPORT bool thAudioTalk_SetFormat(
    HANDLE talkHandle,
    i32 nChannels, i32 wBitsPerSample, i32 nSamplesPerSec,
    i32 AudioPacketSize,
    TAudioTalkCallBack callback,
    void* UserCustom
    );
  EXPORT bool thAudioTalk_Free(HANDLE talkHandle);

#ifdef __cplusplus
}
#endif 

#endif
