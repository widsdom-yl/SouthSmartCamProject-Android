//-----------------------------------------------------------------------------
// Author      : 朱红波
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#include "../include/avDecode.h"
#include "thmmSystem.h"
#include <windows.h>
#include <mmsystem.h>
//-----------------------------------------------------------------------------
#define WAV_MAX_BUF_SIZE  2048
#define WAVOUT_BUF_COUNT     8
#define WAVIN_BUF_COUNT      3
//-----------------------------------------------------------------------------
typedef struct TWaveOutInfo {
  HWAVEOUT hDevWavOut;
  WAVEFORMATEX WavFormat;
  int Idx;
  WAVEHDR WavOutHdr[WAVOUT_BUF_COUNT];
  char tmpBuf[WAV_MAX_BUF_SIZE][WAVOUT_BUF_COUNT];
  bool IsOpenWaveOut;
}TWaveOutInfo;
//-----------------------------------------------------------------------------
typedef struct TWaveInInfo {
  HWAVEIN hDevWavIn;
  WAVEFORMATEX WavFormat;
  int Idx;
  WAVEHDR WavInHdr[WAVIN_BUF_COUNT];
  char tmpBuf[WAV_MAX_BUF_SIZE][WAVIN_BUF_COUNT];
  bool IsOpenWaveIn;

  TAudioTalkCallBack* callback;
  void* UserCustom;
}TWaveInInfo;
//-----------------------------------------------------------------------------
HANDLE thWaveOut_Init()
{
//  int i;
  TWaveOutInfo* Info = (TWaveOutInfo*)malloc(sizeof(TWaveOutInfo));
  if (!Info) return NULL;
  memset(Info, 0, sizeof(TWaveOutInfo));

  return Info;
}
//-----------------------------------------------------------------------------
void STDCALL callback_WaveOutAudio(HWAVEOUT hDevWavOut, u32 uMsg, u32 UserCustom, u32 dwParam1, u32 dwParam2)//回调函数
{
  u32 ret;
  TWaveOutInfo* Info = (TWaveOutInfo*)UserCustom;

  if (hDevWavOut != Info->hDevWavOut) return;

  if (uMsg == MM_WOM_DONE)
  {
    ret = waveOutUnprepareHeader(hDevWavOut, (WAVEHDR*)dwParam1, sizeof(WAVEHDR));
  }
}
//-----------------------------------------------------------------------------
bool OpenWaveOut(HANDLE audioHandle)
{
  u32 ret;
  TWaveOutInfo* Info = (TWaveOutInfo*)audioHandle;
  if (!Info) return false;
  if (Info->IsOpenWaveOut) return true;

  ret = waveOutGetNumDevs();
  if (ret == 0) return false;
  ret = waveOutOpen(&Info->hDevWavOut,
    0,
    &Info->WavFormat,
    (u32)callback_WaveOutAudio,
    (u32)Info,
    CALLBACK_FUNCTION | WAVE_MAPPED
    );//  or WAVE_MAPPED

  if (ret != 0) return false;
  Info->IsOpenWaveOut = (Info->hDevWavOut > 0);
  return Info->IsOpenWaveOut;
}
//-----------------------------------------------------------------------------
bool CloseWaveOut(HANDLE audioHandle)
{
  u32 ret;
  TWaveOutInfo* Info = (TWaveOutInfo*)audioHandle;
  if (!Info) return false;
  if (Info->IsOpenWaveOut)
  {
    ret = waveOutReset(Info->hDevWavOut);
    if (ret != 0) return false;
    ret = waveOutClose(Info->hDevWavOut);
    if (ret != 0) return false;
    Info->hDevWavOut = NULL;
    Info->IsOpenWaveOut = false;
  }
  return true;
}
//-----------------------------------------------------------------------------
bool thWaveOut_SetFormat(HANDLE audioHandle, i32 nChannels, i32 wBitsPerSample, i32 nSamplesPerSec, i32 AudioPacketSize)
{
  bool ret;
  WAVEFORMATEX* fmt;
  TWaveOutInfo* Info = (TWaveOutInfo*)audioHandle;
  if (!Info) return false;
  fmt = &Info->WavFormat;
  if (fmt->nChannels == nChannels && fmt->wBitsPerSample == wBitsPerSample && fmt->nSamplesPerSec == nSamplesPerSec) return true;

  ret = CloseWaveOut(audioHandle);
  if (!ret) return false;

  Info->WavFormat.nChannels = nChannels;
  Info->WavFormat.wBitsPerSample = wBitsPerSample;
  Info->WavFormat.nSamplesPerSec = nSamplesPerSec;
  Info->WavFormat.wFormatTag = WAVE_FORMAT_PCM;
  Info->WavFormat.nBlockAlign = (Info->WavFormat.wBitsPerSample / 8)*Info->WavFormat.nChannels;
  Info->WavFormat.nAvgBytesPerSec = Info->WavFormat.wBitsPerSample * Info->WavFormat.nBlockAlign;
  Info->WavFormat.cbSize = sizeof(WAVEFORMATEX);

  ret = OpenWaveOut(audioHandle);
  if (!ret) return false;
  return true;
}
//-----------------------------------------------------------------------------
bool thWaveOut_PlayFrame(HANDLE audioHandle, char* Buf, i32 BufLen)
{
  u32 ret;
  TWaveOutInfo* Info = (TWaveOutInfo*)audioHandle;
  if (!Info) return false;
  if (Info->Idx >= WAVOUT_BUF_COUNT) Info->Idx = 0;
  if (Info->IsOpenWaveOut && BufLen > WAV_MAX_BUF_SIZE) return false;

  if (Info->WavOutHdr[Info->Idx].dwFlags >= 2)
  {
    ret = waveOutUnprepareHeader(Info->hDevWavOut, &Info->WavOutHdr[Info->Idx], sizeof(WAVEHDR));
    Info->WavOutHdr[Info->Idx].dwFlags = 0;
    return false;
  }

  Info->WavOutHdr[Info->Idx].lpData = Info->tmpBuf[Info->Idx];
  Info->WavOutHdr[Info->Idx].dwBufferLength = BufLen;
  Info->WavOutHdr[Info->Idx].dwBytesRecorded = BufLen;

  Info->WavOutHdr[Info->Idx].dwFlags = 0;
  memcpy(Info->WavOutHdr[Info->Idx].lpData ,Buf, BufLen);
  

  ret = waveOutPrepareHeader(Info->hDevWavOut, &Info->WavOutHdr[Info->Idx], sizeof(WAVEHDR));
  if (ret != MMSYSERR_NOERROR)
  {
    ret = waveOutUnprepareHeader(Info->hDevWavOut, &Info->WavOutHdr[Info->Idx], sizeof(WAVEHDR));
    Info->WavOutHdr[Info->Idx].dwFlags = 0;
    return false;
  }

  ret = waveOutWrite(Info->hDevWavOut, &Info->WavOutHdr[Info->Idx], sizeof(WAVEHDR));
  if (ret != MMSYSERR_NOERROR)
  {
    ret = waveOutUnprepareHeader(Info->hDevWavOut, &Info->WavOutHdr[Info->Idx], sizeof(WAVEHDR));
    Info->WavOutHdr[Info->Idx].dwFlags = 0;
    return false;
  }

  Info->Idx = (Info->Idx+1) % WAVOUT_BUF_COUNT;
  return true;
}
//-----------------------------------------------------------------------------
bool thWaveOut_Free(HANDLE audioHandle)
{
  TWaveOutInfo* Info = (TWaveOutInfo*)audioHandle;
  if (!Info) return false;

  CloseWaveOut(audioHandle);
  free(Info);
  return false;
}
//-----------------------------------------------------------------------------
HANDLE thWaveIn_Init()
{
  TWaveInInfo* Info = (TWaveInInfo*)malloc(sizeof(TWaveInInfo));
  if (!Info) return NULL;
  memset(Info, 0, sizeof(TWaveInInfo));

  return Info;
}
//-----------------------------------------------------------------------------
void STDCALL callback_WaveInAudio(HWAVEOUT hDevWavIn, u32 uMsg, u32 UserCustom, u32 dwParam1, u32 dwParam2)//回调函数
{
  u32 ret;
  WAVEHDR* hdr;
  TWaveInInfo* Info = (TWaveInInfo*)UserCustom;
  
  if (uMsg != MM_WIM_DATA) return;
  if (hDevWavIn != Info->hDevWavIn) return;

  hdr = (WAVEHDR*)dwParam2;
  if (!hdr) return;

  if (Info->callback)
  {
    Info->callback(Info->UserCustom, hdr->lpData, hdr->dwBytesRecorded);
  }

  hdr->dwFlags = 0;
  hdr->dwBytesRecorded = 0;
  hdr->dwBufferLength = WAV_MAX_BUF_SIZE;
  ret = waveInPrepareHeader(Info->hDevWavIn, hdr, sizeof(WAVEHDR));
  ret = waveInAddBuffer(Info->hDevWavIn, hdr, sizeof(WAVEHDR));
}
//-----------------------------------------------------------------------------
bool OpenWaveIn(HANDLE talkHandle)
{
  u32 ret, i;
  TWaveInInfo* Info = (TWaveInInfo*)talkHandle;
  if (!Info) return false;
  if (Info->IsOpenWaveIn) return true;

  ret = waveOutGetNumDevs();
  if (ret == 0) return false;
  ret = waveInOpen(&Info->hDevWavIn,
    0,
    &Info->WavFormat,
    (u32)callback_WaveInAudio,
    (u32)Info,
    CALLBACK_FUNCTION | WAVE_MAPPED
    );//  or WAVE_MAPPED

  for (i=0; i<WAVIN_BUF_COUNT; i++)
  {
    Info->WavInHdr[i].lpData = malloc(WAV_MAX_BUF_SIZE);
    Info->WavInHdr[i].dwBufferLength = WAV_MAX_BUF_SIZE;//BufInSize;
    Info->WavInHdr[i].dwBytesRecorded = Info->WavFormat.nAvgBytesPerSec;
    Info->WavInHdr[i].dwFlags = 0;
    ret = waveInPrepareHeader(Info->hDevWavIn, &Info->WavInHdr[i], sizeof(WAVEHDR));
    ret = waveInAddBuffer(Info->hDevWavIn, &Info->WavInHdr[i], sizeof(WAVEHDR));
  }
  ret = waveInStart(Info->hDevWavIn);

  Info->IsOpenWaveIn = (Info->hDevWavIn > 0);
  return Info->IsOpenWaveIn;
}
//-----------------------------------------------------------------------------
bool CloseWaveIn(HANDLE talkHandle)
{
  u32 ret;
  TWaveInInfo* Info = (TWaveInInfo*)talkHandle;
  if (!Info) return false;
  if (Info->IsOpenWaveIn)
  {
    ret = waveInStop(Info->hDevWavIn);
    if (ret != 0) return false;
    ret = waveInClose(Info->hDevWavIn);
    if (ret != 0) return false;
    Info->hDevWavIn = NULL;
    Info->IsOpenWaveIn = false;
  }
  return true;
}
//-----------------------------------------------------------------------------
bool thWaveIn_SetFormat(HANDLE talkHandle,
                        i32 nChannels, i32 wBitsPerSample, i32 nSamplesPerSec,
                        i32 AudioPacketSize,
                        TAudioTalkCallBack callback,
                        void* UserCustom)
{
  bool ret;
  WAVEFORMATEX* fmt;
  TWaveInInfo* Info = (TWaveInInfo*)talkHandle;
  if (!Info) return false;
  fmt = &Info->WavFormat;
  if (fmt->nChannels == nChannels && fmt->wBitsPerSample == wBitsPerSample && fmt->nSamplesPerSec == nSamplesPerSec) return true;

  ret = CloseWaveIn(talkHandle);
  if (!ret) return false;

  Info->callback = callback;
  Info->UserCustom = UserCustom;

  Info->WavFormat.nChannels = nChannels;
  Info->WavFormat.wBitsPerSample = wBitsPerSample;
  Info->WavFormat.nSamplesPerSec = nSamplesPerSec;
  Info->WavFormat.wFormatTag = WAVE_FORMAT_PCM;
  Info->WavFormat.nBlockAlign = (Info->WavFormat.wBitsPerSample / 8)*Info->WavFormat.nChannels;
  Info->WavFormat.nAvgBytesPerSec = Info->WavFormat.wBitsPerSample * Info->WavFormat.nBlockAlign;
  Info->WavFormat.cbSize = sizeof(WAVEFORMATEX);

  ret = OpenWaveIn(talkHandle);
  if (!ret) return false;
  return true;
}
//-----------------------------------------------------------------------------
bool thWaveIn_Free(HANDLE talkHandle)
{
  TWaveInInfo* Info = (TWaveInInfo*)talkHandle;
  if (!Info) return false;

  CloseWaveIn(talkHandle);
  free(Info);
  return true;
}
//-----------------------------------------------------------------------------