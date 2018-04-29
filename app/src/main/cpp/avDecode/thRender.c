//-----------------------------------------------------------------------------
// Author      : ÷Ï∫Ï≤®
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#include "../include/avDecode.h"
#include "thOpenGL_SLES.h"

//-----------------------------------------------------------------------------
HANDLE thRender_Init(int iDDrawMode/*0=VIDEOMEMORY 1=SYSTEMMEMORY*/)
{
#ifdef IS_VIDEOPLAY_DDRAW
  return thDDraw_Init(iDDrawMode);
#endif
#ifdef IS_VIDEOPLAY_SDL
  return thSDLVideo_Init();
#endif
#ifdef IS_VIDEOPLAY_OPENGL
  return thOpenGLVideo_Init();
#endif
}
//-----------------------------------------------------------------------------
bool thRender_Free(HANDLE videoHandle)
{
#ifdef IS_VIDEOPLAY_DDRAW
  return thDDraw_Free(videoHandle);
#endif
#ifdef IS_VIDEOPLAY_SDL
  return thSDLVideo_Free(videoHandle);
#endif
#ifdef IS_VIDEOPLAY_OPENGL
  return thOpenGLVideo_Free(videoHandle);
#endif
}
//-----------------------------------------------------------------------------
bool thRender_FillMem(HANDLE videoHandle, TavPicture FrameV, i32 ImgWidth, i32 ImgHeight)
{
#ifdef IS_VIDEOPLAY_DDRAW
  return thDDraw_FillMem(videoHandle, FrameV, ImgWidth, ImgHeight);
#endif
#ifdef IS_VIDEOPLAY_SDL
  return thSDLVideo_FillMem(videoHandle, FrameV, ImgWidth, ImgHeight);
#endif
#ifdef IS_VIDEOPLAY_OPENGL
  return thOpenGLVideo_FillMem(videoHandle, FrameV, ImgWidth, ImgHeight);
#endif
}
//-----------------------------------------------------------------------------
bool thRender_Display(HANDLE videoHandle, HWND DspHandle, TRect DspRect)
{
#ifdef IS_VIDEOPLAY_DDRAW
  return thDDraw_Display(videoHandle, DspHandle, DspRect);
#endif
#ifdef IS_VIDEOPLAY_SDL
  return thSDLVideo_Display(videoHandle, DspHandle, DspRect);
#endif
#ifdef IS_VIDEOPLAY_OPENGL
  return thOpenGLVideo_Display(videoHandle, DspHandle, DspRect);
#endif
}
//-----------------------------------------------------------------------------
//*****************************************************************************
//-----------------------------------------------------------------------------
HANDLE thAudioPlay_Init()
{
#ifdef IS_AUDIOPLAY_MMSYSTEM
  return thWaveOut_Init();
#endif
#ifdef IS_AUDIOPLAY_SDL
  return thSDLAudioPlay_Init();
#endif
#ifdef IS_AUDIOPLAY_SLES
  return thSLESAudioPlay_Init();
#endif
}
//-----------------------------------------------------------------------------
bool thAudioPlay_SetFormat(HANDLE audioHandle, i32 nChannels, i32 wBitsPerSample, i32 nSamplesPerSec, i32 AudioPacketSize)
{
#ifdef IS_AUDIOPLAY_MMSYSTEM
  return thWaveOut_SetFormat(audioHandle, nChannels, wBitsPerSample, nSamplesPerSec, AudioPacketSize);
#endif
#ifdef IS_AUDIOPLAY_SDL
  return thSDLAudioPlay_SetFormat(audioHandle, nChannels, wBitsPerSample, nSamplesPerSec, AudioPacketSize);
#endif
#ifdef IS_AUDIOPLAY_SLES
  return thSLESAudioPlay_SetFormat(audioHandle, nChannels, wBitsPerSample, nSamplesPerSec, AudioPacketSize);
#endif
}
//-----------------------------------------------------------------------------
bool thAudioPlay_PlayFrame(HANDLE audioHandle, char* Buf, i32 BufLen)
{
#ifdef IS_AUDIOPLAY_MMSYSTEM
  return thWaveOut_PlayFrame(audioHandle, Buf, BufLen);
#endif
#ifdef IS_AUDIOPLAY_SDL
  return thSDLAudioPlay_PlayFrame(audioHandle, Buf, BufLen);
#endif
#ifdef IS_AUDIOPLAY_SLES
  return thSLESAudioPlay_PlayFrame(audioHandle, Buf, BufLen);
#endif
}
//-----------------------------------------------------------------------------
bool thAudioPlay_Free(HANDLE audioHandle)
{
#ifdef IS_AUDIOPLAY_MMSYSTEM
  return thWaveOut_Free(audioHandle);
#endif
#ifdef IS_AUDIOPLAY_SDL
  return thSDLAudioPlay_Free(audioHandle);
#endif
#ifdef IS_AUDIOPLAY_SLES
  return thSLESAudioPlay_Free(audioHandle);
#endif
}
//-----------------------------------------------------------------------------
//*****************************************************************************
//-----------------------------------------------------------------------------
HANDLE thAudioTalk_Init()
{
#ifdef IS_AUDIOTALK_MMSYSTEM
  return thWaveIn_Init();
#endif
#ifdef IS_AUDIOTALK_SDL
  return thSDLAudioTalk_Init();
#endif
#ifdef IS_AUDIOTALK_SLES
  return thSLESAudioTalk_Init();
#endif
}
//-----------------------------------------------------------------------------
bool thAudioTalk_SetFormat(HANDLE talkHandle,
                           i32 nChannels, i32 wBitsPerSample, i32 nSamplesPerSec,
                           i32 AudioPacketSize,
                           TAudioTalkCallBack callback,
                           void* UserCustom)
{
#ifdef IS_AUDIOTALK_MMSYSTEM
  return thWaveIn_SetFormat(talkHandle, nChannels, wBitsPerSample, nSamplesPerSec, AudioPacketSize, callback, UserCustom);
#endif
#ifdef IS_AUDIOTALK_SDL
  return thSDLAudioTalk_SetFormat(talkHandle, nChannels, wBitsPerSample, nSamplesPerSec, AudioPacketSize, callback, UserCustom);
#endif
#ifdef IS_AUDIOTALK_SLES
  return thSLESAudioTalk_SetFormat(talkHandle, nChannels, wBitsPerSample, nSamplesPerSec, AudioPacketSize, callback, UserCustom);
#endif
}
//-----------------------------------------------------------------------------
bool thAudioTalk_Free(HANDLE talkHandle)
{
#ifdef IS_AUDIOTALK_MMSYSTEM
  return thWaveIn_Free(talkHandle);
#endif
#ifdef IS_AUDIOTALK_SDL
  return thSDLAudioTalk_Free(talkHandle);
#endif
#ifdef IS_AUDIOTALK_SLES
  return thSLESAudioTalk_Free(talkHandle);
#endif
}
