//-----------------------------------------------------------------------------
// Author      : 朱红波
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#ifndef avDecode_h
#define avDecode_h

#include <windows.h>

#define IS_MP_INIT

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

  typedef enum TMsgID
  {
    Msg_ReadOver     = WM_USER + 0x1000,
    Msg_PlayOver     = WM_USER + 0x1001,
    Msg_Video        = WM_USER + 0x1002,
    Msg_Audio        = WM_USER + 0x1003,
    Msg_HasVideoData = WM_USER + 0x1004,
    Msg_Connect      = WM_USER + 0x1005,
    Msg_DisConn      = WM_USER + 0x1006,
    Msg_ReConn       = WM_USER + 0x1007,
    Msg_Load         = WM_USER + 0x1008,
    Msg_Unload       = WM_USER + 0x1009,
  }TMsgID;

  typedef enum TStreamStatus
  {
    st_ReadOver,
    st_PlayOver,
    st_Video,
    st_Audio, 
  } TStreamStatus;

  typedef struct TavFormatInfo//sizeof 624
  {
    //VIDEO
    int VideoCodecID;
    int FrameRate;
    int BitRate;
    int Gop;
    int Width;
    int Height;
    //AUDIO
    int AudioCodecID;
    int nSamplesPerSec; //采样率
    int nChannels; //单声道=0 立体声=1
    int wBitsPerSample; //8, 16

    HANDLE VideoHandle;

    LONGLONG Duration;
    LONGLONG TimeStamp;

    unsigned char* videoBuf;
    int videoBufLen;
    i64 videoPts;
    i64 audioPts;
    unsigned char* audioBuf;
    int audioBufLen;

    int NaluType;//5 6 7 8 4
    unsigned char Buf_extradataV[256];
    int Len_extradataV;
    unsigned char Buf_extradataA[256];
    int Len_extradataA;

    int Reserved;//88
  } TavFormatInfo;

  typedef enum TPlayStatus
  {
    PS_None  = 0,  //空
    PS_Play  = 1,  //播放
    PS_Pause = 2,  //暂停
    PS_Stop  = 3,  //停止
    PS_Seek  = 4,  //seek
    PS_Close = 5,  //关闭
  } TPlayStatus;

  typedef enum TVideoShowModel
  {
    vs_Stretched,  //拉伸到显示区域
    vs_LetterBox,  //按比例缩放到显示区域
  } TVideoShowModel;

  typedef void(* TavCallback)(HANDLE Handle, TavFormatInfo* avFormatInfo, TStreamStatus Status, const void* UserData);

#ifdef IS_MP_INIT
  EXPORT HANDLE MP_Init();
  EXPORT int MP_LoadVideo(HANDLE Handle, char* FileName, int rtsp_any0_udp1_tcp2, HWND PlayHandle, int iConnTimeOut/*ms*/, bool IsProcessVideo, bool IsProcessAudio);//Wnd为窗口显示句柄,为0不显示
  EXPORT int MP_UnloadVideo(HANDLE Handle, DWORD sleepmsClose = 300);
  EXPORT int MP_Free(HANDLE Handle);
#else
  EXPORT HANDLE MP_LoadVideo(char* FileName, int rtsp_any0_udp1_tcp2, HWND PlayHandle, int iConnTimeOut/*ms*/, bool IsProcessVideo, bool IsProcessAudio);//Wnd为窗口显示句柄,为0不显示
  EXPORT int MP_UnloadVideo(HANDLE Handle, DWORD sleepmsClose = 300);
#endif
  EXPORT int MP_SetCallback(HANDLE Handle, TavCallback callback, const void* UserData);
  EXPORT int MP_SetRect(HANDLE Handle, HWND PlayHandle, int x, int y, int w, int h);
  EXPORT int MP_IsPauseDecode(HANDLE Handle, int IsPauseDecode);
  EXPORT int MP_IsEof(HANDLE Handle);

  EXPORT int MP_Play(HANDLE Handle);
  EXPORT int MP_Stop(HANDLE Handle);
  EXPORT int MP_Pause(HANDLE Handle);

  //获取当前位置
  EXPORT int MP_GetCurrentPosition(HANDLE Handle, LONGLONG* pos, LONGLONG* total); //pos total单位ms
  //获取当前播放状态
  EXPORT int MP_GetCurrentPlayStatus(HANDLE Handle, int* PlayStatus);
  //定位
  EXPORT int MP_SeekToPosition(HANDLE Handle, LONGLONG pos); //单位ms
  //设置播放速率
  EXPORT int MP_SetPlaySpeed(HANDLE Handle, int speed); //100% = 100, 200% = 200, 50% = 50

  //设置音量 0~100
  EXPORT int MP_SetVolume(HANDLE Handle, int Vol100);
  EXPORT int MP_GetVolume(HANDLE Handle, int* Vol100);
  EXPORT int MP_SetMute(HANDLE Handle, int bMute);

  EXPORT int MP_GetVideoShowModel(HANDLE Handle, int* lpVideoShowModel);
  EXPORT int MP_SetVideoShowModel(HANDLE Handle, int VideoShowModel);

  EXPORT int MP_Snapshot(HANDLE Handle, char* FileName, int TimeOut);

  EXPORT unsigned int MP_BufLenTotal(HANDLE Handle);

  EXPORT int MP_SetColor(HANDLE Handle, int iBrightness, int iContrast, int iSaturation);
  EXPORT int MP_GetColor(HANDLE Handle, int* iBrightness, int* iContrast, int* iSaturation);
  
#ifdef __cplusplus
}
#endif 

#endif
