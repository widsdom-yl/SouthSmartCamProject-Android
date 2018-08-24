//-----------------------------------------------------------------------------
// Author      : 朱红波
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#include "thOpenGL_SLES.h"
#include "../include/libthSDK.h"
#include "thffmpeg.h"
#include "../include/TFun.h"
#include "../include/avDecode.h"

#ifdef IS_VIDEOPLAY_OPENGL

#include <GLES/gl.h>
#include <GLES/glext.h>

#define TEXTURE_WIDTH 1024
#define TEXTURE_HEIGHT 512

//-----------------------------------------------------------------------------
typedef struct TOpenGLInfo
{
  i32 IsExit;
  H_THREADLOCK Lock;

  TavPicture FrameV420;
  TavPicture FrameV565;
  char rgbBuf565[2592 * 1944 * 2];

  i32 ImgWidth;
  i32 ImgHeight;
  i32 ScreenWidth;
  i32 ScreenHeight;
  GLuint gl_texture;

} TOpenGLInfo;
//-----------------------------------------------------------------------------
HANDLE thOpenGLVideo_Init()
{
  TOpenGLInfo *Info;
  Info = (TOpenGLInfo *) malloc(sizeof(TOpenGLInfo));
  if (!Info) return NULL;
  memset(Info, 0, sizeof(TOpenGLInfo));
  Info->IsExit = false;
  ThreadLockInit(&Info->Lock);
  return (HANDLE) Info;
}
//-----------------------------------------------------------------------------
bool thOpenGLVideo_Free(HANDLE Handle)
{
  TOpenGLInfo *Info = (TOpenGLInfo *) Handle;
  if (!Info) return false;
  Info->IsExit = true;
  ThreadLockFree(&Info->Lock);
  free(Info);
  return true;
}
//-----------------------------------------------------------------------------
bool thOpenGLVideo_FillMem(HANDLE Handle, TavPicture FrameV420, i32 ImgWidth, i32 ImgHeight)
{
  TOpenGLInfo *Info = (TOpenGLInfo *) Handle;
  if (!Info) return false;
  if (FrameV420.data[0] == NULL) return false;
  if (ImgWidth == 0 || ImgHeight == 0) return false;
  Info->FrameV420 = FrameV420;
  Info->ImgWidth = ImgWidth;
  Info->ImgHeight = ImgHeight;
  return true;
}
//-----------------------------------------------------------------------------
bool thOpenGLVideo_Display(HANDLE Handle, HWND DspHandle, TRect dspRect)
{
  TOpenGLInfo *Info = (TOpenGLInfo *) Handle;
  if (!Info) return false;

  Info->ScreenWidth = dspRect.right - dspRect.left;
  Info->ScreenHeight = dspRect.bottom - dspRect.top;
  if (Info->ImgWidth <= 0) return false;
  if (Info->ImgHeight <= 0) return false;
  if (Info->ScreenWidth <= 0) return false;
  if (Info->ScreenHeight <= 0) return false;

  static GLuint s_disable_caps[] = {GL_FOG, GL_LIGHTING, GL_CULL_FACE, GL_ALPHA_TEST, GL_BLEND, GL_COLOR_LOGIC_OP,
                                    GL_DITHER, GL_STENCIL_TEST, GL_DEPTH_TEST, GL_COLOR_MATERIAL, 0};
  int rect[4] = {0, TEXTURE_HEIGHT, TEXTURE_WIDTH, -TEXTURE_HEIGHT};

  glDeleteTextures(1, &Info->gl_texture);
  GLuint *start = s_disable_caps;
  while (*start) glDisable(*start++);
  glEnable(GL_TEXTURE_2D);

  glGenTextures(1, &Info->gl_texture);
  glBindTexture(GL_TEXTURE_2D, Info->gl_texture);
  glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
  glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

  glShadeModel(GL_FLAT);
  glColor4x(0x10000, 0x10000, 0x10000, 0x10000);
  glTexParameteriv(GL_TEXTURE_2D, GL_TEXTURE_CROP_RECT_OES, rect);

  thImgConvertFill(&Info->FrameV565, Info->rgbBuf565, AV_PIX_FMT_RGB565, TEXTURE_WIDTH, TEXTURE_HEIGHT);
  thImgConvertScale1(//only copy ?
    &Info->FrameV565, AV_PIX_FMT_RGB565, TEXTURE_WIDTH,//Play->ImgWidth,
    TEXTURE_HEIGHT,//Play->ImgHeight,
    &Info->FrameV420, AV_PIX_FMT_YUV420P, Info->ImgWidth, Info->ImgHeight, 0);

  glClear(GL_COLOR_BUFFER_BIT);
  glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB, TEXTURE_WIDTH, TEXTURE_HEIGHT, 0, GL_RGB, GL_UNSIGNED_SHORT_5_6_5,
               (char *) Info->FrameV565.data[0]//App.Video[Chl].bufferRGB565
  );

  int left, top, viewWidth, viewHeight;
  if (Info->ScreenHeight > Info->ScreenWidth)
  {
    left = 0;
    viewWidth = Info->ScreenWidth;
    viewHeight = (int) (Info->ImgHeight * 1.0f / Info->ImgWidth * viewWidth);
    top = (Info->ScreenHeight - viewHeight) / 2;
  } else
  {
    top = 0;
    left = 0;
    viewHeight = Info->ScreenHeight;
    viewWidth = Info->ScreenWidth;
  }
  glViewport(0, 0, Info->ScreenWidth, Info->ScreenHeight);
  glDrawTexiOES(left, top, 0, viewWidth, viewHeight);

  return true;

}

#endif
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
#if defined(IS_AUDIOPLAY_SLES) || defined(IS_AUDIOTALK_SLES)

#include <assert.h>
#include <SLES/OpenSLES.h>
#include "SLES/OpenSLES_Android.h"

//-----------------------------------------------------------------------------
typedef struct TAudioQueuePkt
{
  char *buffer;
  int size;
  int WritePos;
  int ReadPos;
} TAudioQueuePkt;

//-----------------------------------------------------------------------------
TAudioQueuePkt *AudioQueue_Create(int bytes)
{
  TAudioQueuePkt *p;
  if ((p = calloc(1, sizeof(TAudioQueuePkt))) == NULL) return NULL;

  p->size = bytes;
  p->WritePos = p->ReadPos = 0;

  if ((p->buffer = calloc(bytes, sizeof(char))) == NULL)
  {
    free(p);
    return NULL;
  }
  return p;
}

//-----------------------------------------------------------------------------
int AudioQueue_Check(TAudioQueuePkt *p, int writeCheck)
{
  int WritePos = p->WritePos, ReadPos = p->ReadPos, size = p->size;
  if (writeCheck)
  {
    if (WritePos > ReadPos) return ReadPos - WritePos + size - 1;
    else if (WritePos < ReadPos) return ReadPos - WritePos - 1;
    else return size - 1;
  } else
  {
    if (WritePos > ReadPos) return WritePos - ReadPos;
    else if (WritePos < ReadPos) return WritePos - ReadPos + size;
    else return 0;
  }
}

//-----------------------------------------------------------------------------
int AudioQueue_Read(TAudioQueuePkt *p, char *out, int bytes)
{
  int remaining;
  int bytesread, size = p->size;
  int i = 0, ReadPos = p->ReadPos;
  char *buffer = p->buffer;
  if ((remaining = AudioQueue_Check(p, 0)) == 0) return 0;

  bytesread = bytes > remaining ? remaining : bytes;
  for (i = 0; i < bytesread; i++)
  {
    out[i] = buffer[ReadPos++];
    if (ReadPos == size) ReadPos = 0;
  }
  p->ReadPos = ReadPos;
  return bytesread;
}

//-----------------------------------------------------------------------------
int AudioQueue_Write(TAudioQueuePkt *p, const char *in, int bytes)
{
  int remaining;
  int byteswrite, size = p->size;
  int i = 0, WritePos = p->WritePos;
  char *buffer = p->buffer;
  if ((remaining = AudioQueue_Check(p, 1)) == 0) return 0;
  byteswrite = bytes > remaining ? remaining : bytes;
  for (i = 0; i < byteswrite; i++)
  {
    buffer[WritePos++] = in[i];
    if (WritePos == size) WritePos = 0;
  }
  p->WritePos = WritePos;
  return byteswrite;
}

//-----------------------------------------------------------------------------
int AudioQueue_Free(TAudioQueuePkt *p)
{
  if (p == NULL) return 0;
  free(p->buffer);
  free(p);
  return 1;
}

//-----------------------------------------------------------------------------
typedef struct TSLESAudioPlayInfo
{
  SLObjectItf SLObjectPlay;
  SLPlayItf SLPlay;
  SLAndroidSimpleBufferQueueItf SLBufferQueuePlay;
  SLEffectSendItf SLEffectSendPlay;
  TAudioQueuePkt *AudioQueuePlayPkt;
  short BufferPlay[2048];
  unsigned BufferPlaySize;
  int _isplay;

} TSLESAudioPlayInfo;
//-----------------------------------------------------------------------------
typedef struct TSLESAudioTalkInfo
{
  SLObjectItf SLObjectTalk;
  SLRecordItf SLTalk;
  SLAndroidSimpleBufferQueueItf SLBufferQueueTalk;
#define AUDIO_COLLECT_SIZE 1000
  char BufferTalk[AUDIO_COLLECT_SIZE * 2];


  TAudioTalkCallBack *callback;
  void *UserCustom;

} TSLESAudioTalkInfo;
//-----------------------------------------------------------------------------
SLObjectItf engineObject;
SLEngineItf engineEngine;
SLObjectItf outputMixObject;

//-----------------------------------------------------------------------------
void callback_AudioQueuePlay(SLAndroidSimpleBufferQueueItf bq, void *context)
{
  TSLESAudioPlayInfo *Info = (TSLESAudioPlayInfo *) context;
  if (!Info) return;

  int len = AudioQueue_Read(Info->AudioQueuePlayPkt, (char *) Info->BufferPlay, Info->BufferPlaySize);
  if (len > 0)
  {
    (*Info->SLBufferQueuePlay)->Enqueue(Info->SLBufferQueuePlay, Info->BufferPlay, Info->BufferPlaySize);
    Info->_isplay = 1;
  } else
  {
    Info->_isplay = 0;
    usleep(1000 * 500);
  }
}

void thSLESCreateEngine()
{
  if (engineObject == NULL)
  {
    SLresult result;
    result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
    if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);
    result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
    if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);
    result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
    if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);

  }
  errors:

  return;
}
//-----------------------------------------------------------------------------
HANDLE thSLESAudioPlay_Init()
{
  PRINTF("========================== thSLESAudioPlay_Init");
  SLresult result;
  TSLESAudioPlayInfo *Info = (TSLESAudioPlayInfo *) malloc(sizeof(TSLESAudioPlayInfo));
  memset(Info, 0, sizeof(TSLESAudioPlayInfo));
  PRINTF("%s(%d) ret:%d\n", __FUNCTION__, __LINE__, 0);


  /* result = slCreateEngine(&engineObject, 0, NULL, 0, NULL, NULL);
   if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);
   result = (*engineObject)->Realize(engineObject, SL_BOOLEAN_FALSE);
   if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);
   result = (*engineObject)->GetInterface(engineObject, SL_IID_ENGINE, &engineEngine);
   if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);*/
  thSLESCreateEngine();
  const SLInterfaceID ids[1] = {SL_IID_ENVIRONMENTALREVERB};
  const SLboolean req[1] = {SL_BOOLEAN_FALSE};
  result = (*engineEngine)->CreateOutputMix(engineEngine, &outputMixObject, 1, ids, req);
  if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);
  result = (*outputMixObject)->Realize(outputMixObject, SL_BOOLEAN_FALSE);
  if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);

  SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
  SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, 1, SL_SAMPLINGRATE_8, SL_PCMSAMPLEFORMAT_FIXED_16,
                                 SL_PCMSAMPLEFORMAT_FIXED_16, SL_SPEAKER_FRONT_CENTER, SL_BYTEORDER_LITTLEENDIAN};
  SLDataSource audioSrc = {&loc_bufq, &format_pcm};

  SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, outputMixObject};
  SLDataSink audioSnk = {&loc_outmix, NULL};

  const SLInterfaceID ids1[2] = {SL_IID_BUFFERQUEUE, SL_IID_EFFECTSEND};
  const SLboolean req1[2] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};
  result = (*engineEngine)->CreateAudioPlayer(engineEngine, &Info->SLObjectPlay, &audioSrc, &audioSnk, 2, ids1, req1);
  if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);
  result = (*Info->SLObjectPlay)->Realize(Info->SLObjectPlay, SL_BOOLEAN_FALSE);
  if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);
  result = (*Info->SLObjectPlay)->GetInterface(Info->SLObjectPlay, SL_IID_PLAY, &Info->SLPlay);
  if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);
  result = (*Info->SLObjectPlay)->GetInterface(Info->SLObjectPlay, SL_IID_BUFFERQUEUE, &Info->SLBufferQueuePlay);
  if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);
  result = (*Info->SLBufferQueuePlay)->RegisterCallback(Info->SLBufferQueuePlay, callback_AudioQueuePlay, Info);
  if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);
  result = (*Info->SLObjectPlay)->GetInterface(Info->SLObjectPlay, SL_IID_EFFECTSEND, &Info->SLEffectSendPlay);
  if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);
  result = (*Info->SLPlay)->SetPlayState(Info->SLPlay, SL_PLAYSTATE_PLAYING);
  if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);
  Info->AudioQueuePlayPkt = AudioQueue_Create(2048 * sizeof(short) * 1000);
  //  Info->BufferPlay = (short*)malloc(sizeof(short) * 2048);
  Info->BufferPlaySize = 2048;
  Info->_isplay = 0;
  return (HANDLE) Info;

  errors:
  free(Info);
  return NULL;
}
//-----------------------------------------------------------------------------
bool thSLESAudioPlay_SetFormat(HANDLE audioHandle, i32 nChannels, i32 wBitsPerSample, i32 nSamplesPerSec,
                               i32 AudioPacketSize)
{
  TSLESAudioPlayInfo *Info = (TSLESAudioPlayInfo *) audioHandle;
  if (!Info) return false;
  PRINTF("%s(%d) ret:%d\n", __FUNCTION__, __LINE__, 0);
  return true;
}
//-----------------------------------------------------------------------------
bool thSLESAudioPlay_PlayFrame(HANDLE audioHandle, char *Buf, i32 BufLen)
{
  int space, len, size;
  TSLESAudioPlayInfo *Info = (TSLESAudioPlayInfo *) audioHandle;
  if (!Info) return false;
  if (Info->_isplay == 0)
  {
    AudioQueue_Write(Info->AudioQueuePlayPkt, Buf, BufLen);
    space = AudioQueue_Check(Info->AudioQueuePlayPkt, 0);
    if (space < 4096) return false;
    len = AudioQueue_Read(Info->AudioQueuePlayPkt, (char *) Info->BufferPlay, Info->BufferPlaySize);
    Info->_isplay = 1;
    (*Info->SLBufferQueuePlay)->Enqueue(Info->SLBufferQueuePlay, Info->BufferPlay, Info->BufferPlaySize);
    return true;
  } else
  {
    space = AudioQueue_Check(Info->AudioQueuePlayPkt, 0);
    size = AudioQueue_Write(Info->AudioQueuePlayPkt, Buf, BufLen);
  }
  return true;
}
//-----------------------------------------------------------------------------
bool thSLESAudioPlay_Free(HANDLE audioHandle)
{
  PRINTF("========================== thSLESAudioPlay_Free");
  TSLESAudioPlayInfo *Info = (TSLESAudioPlayInfo *) audioHandle;
  if (!Info) return false;
  PRINTF("%s(%d) ret:%d\n", __FUNCTION__, __LINE__, 0);
  if (Info->SLObjectPlay != NULL)
  {
    (*Info->SLObjectPlay)->Destroy(Info->SLObjectPlay);
    Info->SLObjectPlay = NULL;
    Info->SLPlay = NULL;
    Info->SLBufferQueuePlay = NULL;
  }

  if (outputMixObject != NULL)
  {
    (*outputMixObject)->Destroy(outputMixObject);
    outputMixObject = NULL;
  }

  if (engineObject != NULL)
  {
    (*engineObject)->Destroy(engineObject);
    engineObject = NULL;
    engineEngine = NULL;
  }
  AudioQueue_Free(Info->AudioQueuePlayPkt);
  Info->AudioQueuePlayPkt = NULL;
  return true;
}

//-----------------------------------------------------------------------------
void callback_AudioQueueTalk(SLAndroidSimpleBufferQueueItf bq, void *context)
{
  TSLESAudioTalkInfo *Info = (TSLESAudioTalkInfo *) context;
  if (!Info) return;
  (*Info->SLBufferQueueTalk)->Enqueue(Info->SLBufferQueueTalk, Info->BufferTalk, AUDIO_COLLECT_SIZE);
  if (Info->callback)
  {
    Info->callback(Info->UserCustom, (char *) Info->BufferTalk, AUDIO_COLLECT_SIZE);
  }
}
//-----------------------------------------------------------------------------
HANDLE thSLESAudioTalk_Init()
{
  thSLESCreateEngine();
  PRINTF("========================== talk init");
  SLresult result;
  TSLESAudioTalkInfo *Info = (TSLESAudioTalkInfo *) malloc(sizeof(TSLESAudioTalkInfo));
  memset(Info, 0, sizeof(TSLESAudioTalkInfo));

  SLDataLocator_IODevice loc_dev = {SL_DATALOCATOR_IODEVICE, SL_IODEVICE_AUDIOINPUT, SL_DEFAULTDEVICEID_AUDIOINPUT,
                                    NULL};
  SLDataSource audioSrc = {&loc_dev, NULL};

  SLDataLocator_AndroidSimpleBufferQueue loc_bq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
  SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, 1, SL_SAMPLINGRATE_8, SL_PCMSAMPLEFORMAT_FIXED_16,
                                 SL_PCMSAMPLEFORMAT_FIXED_16, SL_SPEAKER_FRONT_CENTER, SL_BYTEORDER_LITTLEENDIAN};
  SLDataSink audioSnk = {&loc_bq, &format_pcm};

  const SLInterfaceID id[1] = {SL_IID_ANDROIDSIMPLEBUFFERQUEUE};
  const SLboolean req[1] = {SL_BOOLEAN_TRUE};
  result = (*engineEngine)->CreateAudioRecorder(engineEngine, &Info->SLObjectTalk, &audioSrc, &audioSnk, 1, id, req);
  if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);
  PRINTF("%s(%d) ret:%d\n", __FUNCTION__, __LINE__, 0);
  result = (*Info->SLObjectTalk)->Realize(Info->SLObjectTalk, SL_BOOLEAN_FALSE);
  if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);
  PRINTF("%s(%d) ret:%d\n", __FUNCTION__, __LINE__, 0);
  result = (*Info->SLObjectTalk)->GetInterface(Info->SLObjectTalk, SL_IID_RECORD, &Info->SLTalk);
  if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);
  PRINTF("%s(%d) ret:%d\n", __FUNCTION__, __LINE__, 0);
  result = (*Info->SLObjectTalk)->GetInterface(Info->SLObjectTalk, SL_IID_ANDROIDSIMPLEBUFFERQUEUE,
                                               &Info->SLBufferQueueTalk);
  if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);
  PRINTF("%s(%d) ret:%d\n", __FUNCTION__, __LINE__, 0);
  result = (*Info->SLBufferQueueTalk)->RegisterCallback(Info->SLBufferQueueTalk, callback_AudioQueueTalk,
                                                        (void *) Info);
  if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);
  PRINTF("%s(%d) ret:%d\n", __FUNCTION__, __LINE__, 0);


  result = (*Info->SLTalk)->SetRecordState(Info->SLTalk, SL_RECORDSTATE_STOPPED);
  if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);
  result = (*Info->SLBufferQueueTalk)->Clear(Info->SLBufferQueueTalk);
  if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);

  result = (*Info->SLBufferQueueTalk)->Enqueue(Info->SLBufferQueueTalk, Info->BufferTalk, AUDIO_COLLECT_SIZE);
  if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);
  // start recording
  result = (*Info->SLTalk)->SetRecordState(Info->SLTalk, SL_RECORDSTATE_RECORDING);
  if (SL_RESULT_SUCCESS != result) goto errors;//assert(SL_RESULT_SUCCESS == result);
  return (HANDLE) Info;

  errors:
  free(Info);
  return NULL;
}
//-----------------------------------------------------------------------------
bool
thSLESAudioTalk_SetFormat(HANDLE talkHandle, i32 nChannels, i32 wBitsPerSample, i32 nSamplesPerSec, i32 AudioPacketSize,
                          TAudioTalkCallBack callback, void *UserCustom)
{
  TSLESAudioTalkInfo *Info = (TSLESAudioTalkInfo *) talkHandle;
  if (!Info) return false;
  PRINTF("%s(%d) ret:%d\n", __FUNCTION__, __LINE__, 0);
  Info->callback = callback;
  Info->UserCustom = UserCustom;
  return true;
}
//-----------------------------------------------------------------------------
bool thSLESAudioTalk_Free(HANDLE talkHandle)
{
  PRINTF("========================== thSLESAudioTalk_Free");
  SLresult result;
  TSLESAudioTalkInfo *Info = (TSLESAudioTalkInfo *) talkHandle;
  if (!Info) return false;
  PRINTF("%s(%d) ret:%d\n", __FUNCTION__, __LINE__, 0);
  result = (*Info->SLTalk)->SetRecordState(Info->SLTalk, SL_RECORDSTATE_STOPPED);
  if (SL_RESULT_SUCCESS != result) return false;//assert(SL_RESULT_SUCCESS == result);
  result = (*Info->SLBufferQueueTalk)->Clear(Info->SLBufferQueueTalk);
  if (SL_RESULT_SUCCESS != result) return false;//assert(SL_RESULT_SUCCESS == result);

  if (Info->SLObjectTalk != NULL)
  {
    (*Info->SLObjectTalk)->Destroy(Info->SLObjectTalk);
    Info->SLObjectTalk = NULL;
    Info->SLTalk = NULL;
    Info->SLBufferQueueTalk = NULL;
  }

  return true;
}
//-----------------------------------------------------------------------------
#endif
