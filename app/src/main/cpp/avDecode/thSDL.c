//-----------------------------------------------------------------------------
// Author      : 朱红波
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#include "thSDL.h"
#include "../include/avDecode.h"
#include "../include/TFun.h"

#include "../include/SDL2/SDL.h"
#include "../include/SDL2/SDL_thread.h"
#ifdef WIN32
#pragma comment (lib, "lib.win32/SDL2/SDL2.lib")
#endif

//-----------------------------------------------------------------------------
#if defined(IS_VIDEOPLAY_SDL)
typedef struct TSDLVideoInfo {
  i32 IsExit;
  H_THREADLOCK Lock;
  TavPicture FrameV;
  i32 ImgWidth;
  i32 ImgHeight;
  HWND iDspHandle;

  SDL_Window* Win;
  //  SDL_Surface* Surface;
  SDL_Renderer* Renderer;
  SDL_Texture* Texture;
  SDL_Rect Rect;
  //SDL_Thread* thRefresh;
  //SDL_Event Event;
}TSDLVideoInfo;
//-----------------------------------------------------------------------------
HANDLE thSDLVideo_Init()
{
  i32 ret;
  TSDLVideoInfo* Info;
  u32 flag = SDL_INIT_TIMER;
#ifdef IS_VIDEOPLAY_SDL
  flag = flag | SDL_INIT_VIDEO;
#endif
#if defined(IS_AUDIOTALK_SDL) || defined(IS_AUDIOPLAY_SDL)
  flag = flag | SDL_INIT_AUDIO;
#endif
  ret = SDL_Init(flag);
  PRINTF("%s(%d) ret:%d\n", __FUNCTION__, __LINE__, ret);
  if(ret != 0) return NULL;

  Info = (TSDLVideoInfo*)malloc(sizeof(TSDLVideoInfo));
  if (!Info) return NULL;
  memset(Info, 0, sizeof(TSDLVideoInfo));
  Info->IsExit = false;
  Info->iDspHandle = (HWND)-1;

  return (HANDLE)Info;
}
//-----------------------------------------------------------------------------
bool thSDLVideo_Free(HANDLE Handle)
{
  TSDLVideoInfo* Info = (TSDLVideoInfo*)Handle;
  if (!Info) return false;
  Info->IsExit = true;

  if (Info->Texture) SDL_DestroyTexture(Info->Texture);
  if (Info->Renderer) SDL_DestroyRenderer(Info->Renderer);
  //if (Info->Surface) SDL_FreeSurface(Info->Surface);
#ifndef WIN32
  if (Info->Win) SDL_DestroyWindow(Info->Win);
#endif
  Info->Texture = NULL;
  Info->Renderer = NULL;
  //Info->Surface = NULL;
  Info->Win = NULL;

  free(Info);

  //SDL_Quit();//不能要
  return true;
}
//-----------------------------------------------------------------------------
bool thSDLVideo_FillMem(HANDLE Handle, TavPicture FrameV, i32 ImgWidth, i32 ImgHeight)
{
  TSDLVideoInfo* Info = (TSDLVideoInfo*)Handle;
  if (!Info) return false;
  if (FrameV.data[0] == NULL) return false;
  if (ImgWidth == 0 || ImgHeight==0) return false;
  Info->FrameV = FrameV;
  Info->ImgWidth = ImgWidth;
  Info->ImgHeight = ImgHeight;
  return true;
}
//-----------------------------------------------------------------------------
bool thSDLVideo_Display(HANDLE Handle, HWND DspHandle, TRect DspRect)
{
  i32 ret;
  bool Result = false;
  TSDLVideoInfo* Info = (TSDLVideoInfo*)Handle;
  if (!Info) return Result;

  if (Info->iDspHandle != DspHandle)
  {
    Info->iDspHandle = DspHandle;

    if (Info->Texture) SDL_DestroyTexture(Info->Texture);
    if (Info->Renderer) SDL_DestroyRenderer(Info->Renderer);
    //if (Info->Surface) SDL_FreeSurface(Info->Surface);
#ifndef WIN32
    if (Info->Win) SDL_DestroyWindow(Info->Win);
#endif
    Info->Texture = NULL;
    Info->Renderer = NULL;
    //Info->Surface = NULL;
    Info->Win = NULL;

#ifdef WIN32
    Info->Win = SDL_CreateWindowFrom(DspHandle);
#else
    Info->Win = SDL_CreateWindow("SDL_Windows", 0, 0, SDL_WINDOWPOS_UNDEFINED, SDL_WINDOWPOS_UNDEFINED, SDL_WINDOW_OPENGL);
#endif
    if (!Info->Win) goto exits;

    Info->Renderer = SDL_CreateRenderer(Info->Win, -1, SDL_RENDERER_ACCELERATED);
    /*
    index         ：打算初始化的渲染设备的索引。设置“-1”则初始化默认的渲染设备。
    flags          ：支持以下值（位于SDL_RendererFlags定义中）
    SDL_RENDERER_SOFTWARE ：使用软件渲染    保持
    SDL_RENDERER_ACCELERATED ：使用硬件加速 缩放
    SDL_RENDERER_PRESENTVSYNC：和显示器的刷新率同步
    SDL_RENDERER_TARGETTEXTURE ：不太懂
    */
    if (!Info->Renderer) goto exits;
    //Info->Surface = SDL_GetWindowSurface(Info->Win);
    //if (!Info->Surface) goto exits;
    Info->Texture = SDL_CreateTexture(
      Info->Renderer,
      SDL_PIXELFORMAT_IYUV,//SDL_PIXELFORMAT_YV12,
      SDL_TEXTUREACCESS_STREAMING,
      Info->ImgWidth,
      Info->ImgHeight);
    /*
    access      ：可以取以下值（定义位于SDL_TextureAccess中）
    SDL_TEXTUREACCESS_STATIC         ：变化极少
    SDL_TEXTUREACCESS_STREAMING        ：变化频繁
    SDL_TEXTUREACCESS_TARGET       ：暂时没有理解
    */
    if (!Info->Texture) goto exits;
    ret = SDL_RenderClear(Info->Renderer);//视频可以不要
  }

  Info->Rect.x = DspRect.left;
  Info->Rect.y = DspRect.top;
  Info->Rect.w = DspRect.right - DspRect.left;
  Info->Rect.h = DspRect.bottom - DspRect.top;

  ret = SDL_UpdateTexture(Info->Texture, NULL, (char*)Info->FrameV.data[0], Info->FrameV.linesize[0]);
  //ret = SDL_RenderClear(Info->Renderer);//视频可以不要
  ret = SDL_RenderCopy(Info->Renderer, Info->Texture, NULL, &Info->Rect);
  SDL_RenderPresent(Info->Renderer);
  return true;

exits:
  if (Info->Texture) SDL_DestroyTexture(Info->Texture);
  if (Info->Renderer) SDL_DestroyRenderer(Info->Renderer);
  //if (Info->Surface) SDL_FreeSurface(Info->Surface);
#ifndef WIN32
  if (Info->Win) SDL_DestroyWindow(Info->Win);
#endif
  Info->Texture = NULL;
  Info->Renderer = NULL;
  //Info->Surface = NULL;
  Info->Win = NULL;
  return false;
}
#endif
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
#if defined(IS_AUDIOPLAY_SDL)

#define SDL_AUDIO_OUT  0

typedef struct TSDLAudioPlayInfo {
  SDL_AudioDeviceID hDevIDPlay;
  SDL_AudioSpec wanted_spec;
  SDL_AudioSpec spec;
  i32 Volume;//音量
  u8* Buf[8192];
  u32 BufLen;

  u8* audio_pos;
  u32 audio_len;
}TSDLAudioPlayInfo;
//-----------------------------------------------------------------------------
void callback_SDLAudioPlay(void* userdata, u8* stream, i32 len);
//-----------------------------------------------------------------------------
HANDLE thSDLAudioPlay_Init()
{
  int ret;
  u32 flag = SDL_INIT_TIMER;
  TSDLAudioPlayInfo* Info = (TSDLAudioPlayInfo*)malloc(sizeof(TSDLAudioPlayInfo));
  memset(Info, 0, sizeof(TSDLAudioPlayInfo));

#ifdef IS_VIDEOPLAY_SDL
  flag = flag | SDL_INIT_VIDEO;
#endif
#if defined(IS_AUDIOTALK_SDL) || defined(IS_AUDIOPLAY_SDL)
  flag = flag | SDL_INIT_AUDIO;
#endif
#if defined(ANDROID)
  SDL_SetMainReady();
#endif
  ret = SDL_Init(flag);
PRINTF("%s(%d) ret:%d\n", __FUNCTION__, __LINE__, ret);
  if(ret != 0) return NULL;
#ifdef WIN32
  ret = SDL_AudioInit("winmm");
#endif
#ifdef ANDROID
  ret = SDL_AudioInit("android");
#endif
  Info->Volume = SDL_MIX_MAXVOLUME;
  return (HANDLE)Info;
}
//-----------------------------------------------------------------------------
bool thSDLAudioPlay_SetFormat(HANDLE audioHandle, i32 nChannels, i32 wBitsPerSample, i32 nSamplesPerSec, i32 AudioPacketSize)
{
  char256 DevName = {0};
  i32 i, iDevCount;
  TSDLAudioPlayInfo* Info = (TSDLAudioPlayInfo*)audioHandle;
  if (!Info) return false;
  
  iDevCount = SDL_GetNumAudioDevices(SDL_AUDIO_OUT);
  for (i = 0; i < iDevCount; i++) SDL_Log(" audio play device #%d: '%s'\n", i, SDL_GetAudioDeviceName(i, SDL_AUDIO_OUT));

  if (iDevCount > 0) strcpy(DevName, SDL_GetAudioDeviceName(0, SDL_AUDIO_OUT));
PRINTF("%s(%d) DevName:%s\n", __FUNCTION__, __LINE__, DevName);
  if (DevName[0] == 0x00) return false;

  if (Info->hDevIDPlay)
  {
    SDL_CloseAudioDevice(Info->hDevIDPlay);
    Info->hDevIDPlay = 0;
  }

  SDL_zero(Info->wanted_spec);
  Info->wanted_spec.freq = nSamplesPerSec;//8000;//44100;
  Info->wanted_spec.format = AUDIO_S16SYS;
  if (wBitsPerSample ==  8) Info->wanted_spec.format = AUDIO_S8;
  if (wBitsPerSample == 16) Info->wanted_spec.format = AUDIO_S16SYS;
  Info->wanted_spec.channels = nChannels;//2;   
  Info->wanted_spec.silence = 0;   
  Info->wanted_spec.samples = AudioPacketSize / 2;//1024;  

  Info->wanted_spec.callback = NULL;//callback_SDLAudioPlay;
  Info->wanted_spec.userdata = NULL;//(void*)audioHandle;//0x5678;
//  Info->wanted_spec.callback = callback_SDLAudioPlay;
//  Info->wanted_spec.userdata = (void*)audioHandle;
  SDL_zero(Info->spec);

  Info->hDevIDPlay = SDL_OpenAudioDevice(DevName, SDL_AUDIO_OUT, &Info->wanted_spec, &Info->spec, SDL_AUDIO_ALLOW_ANY_CHANGE);//0
PRINTF("%s(%d) Info->hDevIDPlay:%d\n", __FUNCTION__, __LINE__, Info->hDevIDPlay);
  //Info->hDevIDPlay = SDL_OpenAudioDevice(NULL, SDL_AUDIO_OUT, &Info->wanted_spec, &Info->spec, SDL_AUDIO_ALLOW_ANY_CHANGE);//0
  if (!Info->hDevIDPlay) return false;
  SDL_PauseAudioDevice(Info->hDevIDPlay, SDL_FALSE);

  return true;
}
//-----------------------------------------------------------------------------
void callback_SDLAudioPlay(void* userdata, u8* stream, i32 len)
{
  TSDLAudioPlayInfo* Info = (TSDLAudioPlayInfo*)userdata;

  SDL_memset(stream, 0, len);
  if (Info->audio_len == 0) return;   
  len = (len > Info->audio_len ? Info->audio_len : len);  

  SDL_MixAudio(stream, Info->audio_pos, len, Info->Volume);//SDL_MIX_MAXVOLUME
  Info->audio_pos = Info->audio_pos + len;
  Info->audio_len = Info->audio_len - len;
}
//-----------------------------------------------------------------------------
bool thSDLAudioPlay_PlayFrame(HANDLE audioHandle, char* Buf, i32 BufLen)
{
  int ret;
  TSDLAudioPlayInfo* Info = (TSDLAudioPlayInfo*)audioHandle;
  if (!Info) return false;

  ret = SDL_QueueAudio(Info->hDevIDPlay, Buf, BufLen);
#if 0
  memcpy(Info->Buf, Buf, BufLen);
  Info->BufLen = BufLen;
  //要加锁 cond....
  Info->audio_len =Info->BufLen;
  Info->audio_pos = Info->Buf;  
#endif
  return true;
}
//-----------------------------------------------------------------------------
bool thSDLAudioPlay_Free(HANDLE audioHandle)
{
  TSDLAudioPlayInfo* Info = (TSDLAudioPlayInfo*)audioHandle;
  if (!Info) return false;
  if (Info->hDevIDPlay)
  {
    SDL_CloseAudioDevice(Info->hDevIDPlay);
    Info->hDevIDPlay = 0;
  }
  free(Info);
  return true;
}
#endif
//-----------------------------------------------------------------------------
#if defined(IS_AUDIOTALK_SDL)

#define SDL_AUDIO_IN   1

typedef struct TSDLAudioTalkInfo {
  SDL_AudioDeviceID hDevIDTalk;
  SDL_AudioSpec wanted_spec;
  SDL_AudioSpec spec;

  TAudioTalkCallBack* callback;
  void* UserCustom;

  u8* Buf[8192];
  u32 BufLen;

}TSDLAudioTalkInfo;
//-----------------------------------------------------------------------------
void callback_SDLAudioTalk(void* userdata, u8* stream, i32 len)
{
  int v4;
  TSDLAudioTalkInfo* Info = (TSDLAudioTalkInfo*)userdata;
  v4 = *(int*)stream;
  SDL_Log("talk:%.8x Len:%d\n", v4, len);

  if (Info->callback)
  {
    Info->callback(Info->UserCustom, stream, len);
  }
#if 0
  SDL_memset(stream, 0, len);
  if (Info->audio_len == 0) return;   
  len = (len > Info->audio_len ? Info->audio_len : len);  

  SDL_MixAudio(stream, Info->audio_pos, len, Info->Volume);//SDL_MIX_MAXVOLUME
  Info->audio_pos = Info->audio_pos + len;
  Info->audio_len = Info->audio_len - len;
#endif
}
//-----------------------------------------------------------------------------
HANDLE thSDLAudioTalk_Init()
{
  int ret;
  u32 flag = SDL_INIT_TIMER;
  TSDLAudioTalkInfo* Info = (TSDLAudioTalkInfo*)malloc(sizeof(TSDLAudioTalkInfo));
  memset(Info, 0, sizeof(TSDLAudioTalkInfo));
#ifdef IS_VIDEOPLAY_SDL
  flag = flag | SDL_INIT_VIDEO;
#endif
#if defined(IS_AUDIOTALK_SDL) || defined(IS_AUDIOPLAY_SDL)
  flag = flag | SDL_INIT_AUDIO;
#endif
#if defined(ANDROID)
  SDL_SetMainReady();
#endif
  ret = SDL_Init(flag);
PRINTF("%s(%d) ret:%d\n", __FUNCTION__, __LINE__, ret);
  if(ret != 0) return NULL;

#ifdef WIN32
  ret = SDL_AudioInit("winmm");
#endif
#ifdef ANDROID
  ret = SDL_AudioInit("android");
#endif

  return (HANDLE)Info;
}
//-----------------------------------------------------------------------------
bool thSDLAudioTalk_SetFormat(HANDLE talkHandle,
                              i32 nChannels,
                              i32 wBitsPerSample,
                              i32 nSamplesPerSec,
                              i32 AudioPacketSize,
                              TAudioTalkCallBack callback,
                              void* UserCustom)
{
  char256 DevName = {0};
  i32 i, iDevCount;
  TSDLAudioTalkInfo* Info = (TSDLAudioTalkInfo*)talkHandle;
  if (!Info) return false;
  
  iDevCount = SDL_GetNumAudioDevices(SDL_AUDIO_IN);
  for (i = 0; i < iDevCount; i++) SDL_Log(" audio capture #%d: '%s'\n", i, SDL_GetAudioDeviceName(i, SDL_AUDIO_IN));

  if (iDevCount > 0) strcpy(DevName, SDL_GetAudioDeviceName(0, SDL_AUDIO_IN));
PRINTF("%s(%d) DevName:%s\n", __FUNCTION__, __LINE__, DevName);
  if (DevName[0] == 0x00) return false;

  if (Info->hDevIDTalk)
  {
    SDL_CloseAudioDevice(Info->hDevIDTalk);
    Info->hDevIDTalk = 0;
  }
PRINTF("%s(%d) ret:%d\n", __FUNCTION__, __LINE__, 0);
  SDL_zero(Info->wanted_spec);
  Info->wanted_spec.freq = nSamplesPerSec;//8000;//44100;
  Info->wanted_spec.format = AUDIO_S16SYS;
  if (wBitsPerSample ==  8) Info->wanted_spec.format = AUDIO_S8;
  if (wBitsPerSample == 16) Info->wanted_spec.format = AUDIO_S16SYS;
  Info->wanted_spec.channels = nChannels;//2;   
  Info->wanted_spec.silence = 0;   
  Info->wanted_spec.samples = AudioPacketSize / 2;//1024;   
  Info->wanted_spec.callback = callback_SDLAudioTalk;
  Info->wanted_spec.userdata = (void*)talkHandle;//0x5678;
PRINTF("%s(%d) ret:%d\n", __FUNCTION__, __LINE__, 0);
  SDL_zero(Info->spec);
PRINTF("%s(%d) ret:%d\n", __FUNCTION__, __LINE__, 0);

#ifdef WIN32
Info->hDevIDTalk = SDL_OpenAudioDevice(DevName, SDL_AUDIO_IN, &Info->wanted_spec, &Info->spec, 0);
#else
  Info->hDevIDTalk = SDL_OpenAudioDevice(DevName, SDL_AUDIO_IN, &Info->wanted_spec, &Info->spec, SDL_AUDIO_ALLOW_ANY_CHANGE);
#endif

PRINTF("%s(%d) Info->hDevIDTalk:%d\n", __FUNCTION__, __LINE__, Info->hDevIDTalk);
  if (!Info->hDevIDTalk) return false;
PRINTF("%s(%d) ret:%d\n", __FUNCTION__, __LINE__, 0);
  SDL_PauseAudioDevice(Info->hDevIDTalk, SDL_FALSE);
  //int Len = SDL_DequeueAudio(Info->hDevIDTalk, Buf, sizeof(Buf));回调为空时
  Info->callback = callback;
  Info->UserCustom = UserCustom;
PRINTF("%s(%d) ret:%d\n", __FUNCTION__, __LINE__, 0);
  return true;
}
//-----------------------------------------------------------------------------
bool thSDLAudioTalk_Free(HANDLE talkHandle)
{
  TSDLAudioTalkInfo* Info = (TSDLAudioTalkInfo*)talkHandle;
  if (!Info) return false;
  if (Info->hDevIDTalk)
  {
    SDL_ClearQueuedAudio(Info->hDevIDTalk);
    SDL_CloseAudioDevice(Info->hDevIDTalk);
    Info->hDevIDTalk = 0;
  }
  free(Info);
  return true;
}
#endif
//-----------------------------------------------------------------------------
