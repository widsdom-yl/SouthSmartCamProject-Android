//-----------------------------------------------------------------------------
// Author      : ÷Ï∫Ï≤®
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#ifndef thOpenGL_h
#define thOpenGL_h

#include "../include/avDecode.h"

#ifdef __cplusplus
extern "C"
{
#endif
  //-----------------------------------------------------------------------------
  HANDLE thOpenGLVideo_Init();
  bool thOpenGLVideo_FillMem(HANDLE Handle, TavPicture FrameV420, i32 ImgWidth, i32 ImgHeight);
  bool thOpenGLVideo_Display(HANDLE Handle, HWND DspHandle, TRect dspRect);
  bool thOpenGLVideo_DisplayEnd(HANDLE Handle);
  bool thOpenGLVideo_Free(HANDLE Handle);


  HANDLE thSLESAudioPlay_Init();
  bool thSLESAudioPlay_SetFormat(HANDLE audioHandle, i32 nChannels, i32 wBitsPerSample, i32 nSamplesPerSec, i32 AudioPacketSize);
  bool thSLESAudioPlay_PlayFrame(HANDLE audioHandle, char* Buf, i32 BufLen);
  bool thSLESAudioPlay_Free(HANDLE audioHandle);

  HANDLE thSLESAudioTalk_Init();
  bool thSLESAudioTalk_SetFormat(
    HANDLE talkHandle,
    i32 nChannels, i32 wBitsPerSample, i32 nSamplesPerSec,
    i32 AudioPacketSize,
    TAudioTalkCallBack callback,
    void* UserCustom);
  bool thSLESAudioTalk_Free(HANDLE talkHandle);

#ifdef __cplusplus
}
#endif

#endif
