//-----------------------------------------------------------------------------
// Author      : ÷Ï∫Ï≤®
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#ifndef thSDL_h
#define thSDL_h

#include "../include/avDecode.h"

#ifdef __cplusplus
extern "C"
{
#endif
  //-----------------------------------------------------------------------------
  EXPORT HANDLE thSDLVideo_Init();
  EXPORT bool thSDLVideo_FillMem(HANDLE Handle, TavPicture FrameV, i32 ImgWidth, i32 ImgHeight);
  EXPORT bool thSDLVideo_Display(HANDLE Handle, HWND DspHandle, TRect DspRect);
  EXPORT bool thSDLVideo_Free(HANDLE Handle);

  EXPORT HANDLE thSDLAudioPlay_Init();
  EXPORT bool thSDLAudioPlay_SetFormat(HANDLE audioHandle, i32 nChannels, i32 wBitsPerSample, i32 nSamplesPerSec, i32 AudioPacketSize);
  EXPORT bool thSDLAudioPlay_PlayFrame(HANDLE audioHandle, char* Buf, i32 BufLen);
  EXPORT bool thSDLAudioPlay_Free(HANDLE audioHandle);

  EXPORT HANDLE thSDLAudioTalk_Init();
  EXPORT bool thSDLAudioTalk_SetFormat(
    HANDLE talkHandle,
    i32 nChannels, i32 wBitsPerSample, i32 nSamplesPerSec,
    i32 AudioPacketSize,
    TAudioTalkCallBack callback,
    void* UserCustom);
  EXPORT bool thSDLAudioTalk_Free(HANDLE talkHandle);

#ifdef __cplusplus
}
#endif

#endif
