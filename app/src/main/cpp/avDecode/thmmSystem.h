//-----------------------------------------------------------------------------
// Author      : ÷Ï∫Ï≤®
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#ifndef thmmSystem_h
#define thmmSystem_h

#include "../include/avDecode.h"

#ifdef __cplusplus
extern "C"
{
#endif

  //-----------------------------------------------------------------------------
  EXPORT HANDLE thWaveOut_Init();
  EXPORT bool thWaveOut_SetFormat(HANDLE audioHandle, i32 nChannels, i32 wBitsPerSample, i32 nSamplesPerSec, i32 AudioPacketSize);
  EXPORT bool thWaveOut_PlayFrame(HANDLE audioHandle, char* Buf, i32 BufLen);
  EXPORT bool thWaveOut_Free(HANDLE audioHandle);

  EXPORT HANDLE thWaveIn_Init();
  EXPORT bool thWaveIn_SetFormat(
    HANDLE talkHandle,
    i32 nChannels, i32 wBitsPerSample, i32 nSamplesPerSec,
    i32 AudioPacketSize,
    TAudioTalkCallBack callback,
    void* UserCustom);
  EXPORT bool thWaveIn_Free(HANDLE talkHandle);

#ifdef __cplusplus
}
#endif

#endif
