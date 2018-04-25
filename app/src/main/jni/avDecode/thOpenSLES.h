//-----------------------------------------------------------------------------
// Author      : ÷Ï∫Ï≤®
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#ifndef thOpenSLES_h
#define thOpenSLES_h

#include "../include/libthSDK.h"

#ifdef __cplusplus
extern "C"
{
#endif

//-----------------------------------------------------------------------------
HANDLE thOpenSLESPlay_Init();
bool thOpenSLESPlay_SetFormat(HANDLE audioHandle, i32 nChannels, i32 wBitsPerSample, i32 nSamplesPerSec, i32 AudioPacketSize);
bool thOpenSLESPlay_FillMem(HANDLE audioHandle, char* Buf, i32 BufLen);
bool thOpenSLESPlay_Free(HANDLE audioHandle);

#ifdef __cplusplus
}
#endif

#endif
