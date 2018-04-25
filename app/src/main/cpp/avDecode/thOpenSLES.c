//-----------------------------------------------------------------------------
// Author      : Öìºì²¨
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#include "../include/libthSDK.h"
#include "thOpenSLES.h"

//-----------------------------------------------------------------------------
#if 1
typedef struct TAudioQueuePkt{
  char* buffer;
  int wp;
  int rp;
  int size;
}TAudioQueuePkt;

void callback_AudioQueuePlay(SLAndroidSimpleBufferQueueItf bq, void* context)
{
  int len = AudioQueue_Read(App.Audio.outrb, (char*)App.Audio.nextBuffer,App.Audio.nextSize);
  if (len > 0)
  {
    (*App.Audio.bqPlayerBufferQueue)->Enqueue(App.Audio.bqPlayerBufferQueue, App.Audio.nextBuffer, App.Audio.nextSize);
    App.Audio._isplay = 1;
  }
  else
  {
    App.Audio._isplay = 0;
    usleep(500000);
  }
}
//-----------------------------------------------------------------------------
void callback_AudioQueueTalk(SLAndroidSimpleBufferQueueItf bq, void* context)
{
  int Chl = (int)context;
  LOGE("%s(%d)\n", __FUNCTION__, __LINE__);
  static int flag = 1;
  (*App.Audio.recorderBufferQueue)->Enqueue(App.Audio.recorderBufferQueue, App.Audio.recorderBuffer[flag], RECORDER_FRAMES * sizeof(short));
  flag = !flag;
  if (thNet_IsConnect(App.Video[Chl].NetHandle))
  {
    thNet_SetTalk(App.Video[Chl].NetHandle, (char*)App.Audio.recorderBuffer[flag], RECORDER_FRAMES * sizeof(short));
  }
}
//-----------------------------------------------------------------------------
void PlayAudioBuf(char* Buf,int Len)
{
  if (App.Audio._isplay == 0)
  {
    AudioQueue_Write(App.Audio.outrb, Buf,Len);
    int space = AudioQueue_Check(App.Audio.outrb, 0);
    if (space < 4096) return;
    int len = AudioQueue_Read(App.Audio.outrb, (char*)App.Audio.nextBuffer,App.Audio.nextSize);
    App.Audio._isplay = 1;
    (*App.Audio.bqPlayerBufferQueue)->Enqueue(App.Audio.bqPlayerBufferQueue, App.Audio.nextBuffer, App.Audio.nextSize);
    return;
  }
  else
  {
    int space = AudioQueue_Check(App.Audio.outrb, 0);
    int size = AudioQueue_Write(App.Audio.outrb, Buf,Len);
  }
}
//-----------------------------------------------------------------------------
TAudioQueuePkt* AudioQueue_Create(int bytes)
{
  TAudioQueuePkt* p;
  if ((p = calloc(1, sizeof(TAudioQueuePkt))) == NULL) return NULL;
  p->size = bytes;
  p->wp = p->rp = 0;

  if ((p->buffer = calloc(bytes, sizeof(char))) == NULL)
  {
    free (p);
    return NULL;
  }
  return p;
}
//-----------------------------------------------------------------------------
int AudioQueue_Check(TAudioQueuePkt *p, int writeCheck)
{
  int wp = p->wp, rp = p->rp, size = p->size;
  if(writeCheck)
  {
    if (wp > rp) return rp - wp + size - 1;
    else if (wp < rp) return rp - wp - 1;
    else return size - 1;
  }
  else
  {
    if (wp > rp) return wp - rp;
    else if (wp < rp) return wp - rp + size;
    else return 0;
  } 
}
//-----------------------------------------------------------------------------
int AudioQueue_Read(TAudioQueuePkt *p, char *out, int bytes)
{
  int remaining;
  int bytesread, size = p->size;
  int i=0, rp = p->rp;
  char *buffer = p->buffer;
  if ((remaining = AudioQueue_Check(p, 0)) == 0) {
    return 0;
  }
  bytesread = bytes > remaining ? remaining : bytes;
  for(i=0; i < bytesread; i++){
    out[i] = buffer[rp++];
    if(rp == size) rp = 0;
  }
  p->rp = rp;
  return bytesread;
}
//-----------------------------------------------------------------------------
int AudioQueue_Write(TAudioQueuePkt *p, const char *in, int bytes){
  int remaining;
  int byteswrite, size = p->size;
  int i=0, wp = p->wp;
  char *buffer = p->buffer;
  if ((remaining = AudioQueue_Check(p, 1)) == 0) {
    return 0;
  }
  byteswrite = bytes > remaining ? remaining : bytes;
  for(i=0; i < byteswrite; i++)
  {
    buffer[wp++] = in[i];
    if(wp == size) wp = 0;
  }
  p->wp = wp;
  return byteswrite;
}
//-----------------------------------------------------------------------------
int AudioQueue_Free(TAudioQueuePkt *p)
{
  if(p == NULL) return;
  free(p->buffer);
  free(p);
  return 1;
}
#endif
//-----------------------------------------------------------------------------
typedef struct TOpenSLESInfo {
  SLObjectItf engineObject;
  SLEngineItf engineEngine;
  SLObjectItf outputMixObject;
  SLObjectItf bqPlayerObject;
  SLPlayItf bqPlayerPlay;
  SLAndroidSimpleBufferQueueItf bqPlayerBufferQueue;
  SLEffectSendItf bqPlayerEffectSend;
  SLObjectItf recorderObject;
  SLRecordItf recorderRecord;
  SLAndroidSimpleBufferQueueItf recorderBufferQueue;
  TAudioQueuePkt* outrb;
  short* nextBuffer;
  unsigned nextSize;
  int _isplay;
#define RECORDER_FRAMES 1024 * 4
  short recorderBuffer[2][RECORDER_FRAMES];
}TOpenSLESInfo;

HANDLE thOpenSLESPlay_Init()
{
  TOpenSLESInfo* Info = (TOpenSLESInfo*)malloc(sizeof(TOpenSLESInfo));
  if (!Info) return NULL;
  memset(Info, 0, sizeof(TOpenSLESInfo));


  SLresult result;
  // create engine
  result = slCreateEngine(&App.Audio.engineObject, 0, NULL, 0, NULL, NULL);
  assert(SL_RESULT_SUCCESS == result);
  // realize the engine
  result = (*App.Audio.engineObject)->Realize(App.Audio.engineObject, SL_BOOLEAN_FALSE);
  assert(SL_RESULT_SUCCESS == result);
  // get the engine interface, which is needed in order to create other objects
  result = (*App.Audio.engineObject)->GetInterface(App.Audio.engineObject, SL_IID_ENGINE, &App.Audio.engineEngine);
  assert(SL_RESULT_SUCCESS == result);
  // create output mix, with environmental reverb specified as a non-required interface
  const SLInterfaceID ids[1] = {SL_IID_ENVIRONMENTALREVERB};
  const SLboolean req[1] = {SL_BOOLEAN_FALSE};
  result = (*App.Audio.engineEngine)->CreateOutputMix(App.Audio.engineEngine, &App.Audio.outputMixObject, 1, ids, req);
  assert(SL_RESULT_SUCCESS == result);
  // realize the output mix
  result = (*App.Audio.outputMixObject)->Realize(App.Audio.outputMixObject, SL_BOOLEAN_FALSE);
  assert(SL_RESULT_SUCCESS == result);

  //int jaudio_CreatePlayQueue(JNIEnv* env, jclass clazz)
  // configure audio source
  SLDataLocator_AndroidSimpleBufferQueue loc_bufq = {SL_DATALOCATOR_ANDROIDSIMPLEBUFFERQUEUE, 2};
  SLDataFormat_PCM format_pcm = {SL_DATAFORMAT_PCM, 1, SL_SAMPLINGRATE_8, SL_PCMSAMPLEFORMAT_FIXED_16, SL_PCMSAMPLEFORMAT_FIXED_16, SL_SPEAKER_FRONT_CENTER, SL_BYTEORDER_LITTLEENDIAN};
  SLDataSource audioSrc = {&loc_bufq, &format_pcm};

  // configure audio sink
  SLDataLocator_OutputMix loc_outmix = {SL_DATALOCATOR_OUTPUTMIX, App.Audio.outputMixObject};
  SLDataSink audioSnk = {&loc_outmix, NULL};

  // create audio player
  const SLInterfaceID ids1[2] = {SL_IID_BUFFERQUEUE, SL_IID_EFFECTSEND};
  const SLboolean req1[2] = {SL_BOOLEAN_TRUE, SL_BOOLEAN_TRUE};
  result = (*App.Audio.engineEngine)->CreateAudioPlayer(App.Audio.engineEngine, &App.Audio.bqPlayerObject, &audioSrc, &audioSnk, 2, ids1, req1);
  assert(SL_RESULT_SUCCESS == result);

  // realize the player
  result = (*App.Audio.bqPlayerObject)->Realize(App.Audio.bqPlayerObject, SL_BOOLEAN_FALSE);
  assert(SL_RESULT_SUCCESS == result);

  // get the play interface
  result = (*App.Audio.bqPlayerObject)->GetInterface(App.Audio.bqPlayerObject, SL_IID_PLAY, &App.Audio.bqPlayerPlay);
  assert(SL_RESULT_SUCCESS == result);

  // get the buffer queue interface
  result = (*App.Audio.bqPlayerObject)->GetInterface(App.Audio.bqPlayerObject, SL_IID_BUFFERQUEUE,
    &App.Audio.bqPlayerBufferQueue);
  assert(SL_RESULT_SUCCESS == result);

  // register callback on the buffer queue
  result = (*App.Audio.bqPlayerBufferQueue)->RegisterCallback(App.Audio.bqPlayerBufferQueue, callback_AudioQueuePlay, NULL);
  assert(SL_RESULT_SUCCESS == result);

  // get the effect send interface
  result = (*App.Audio.bqPlayerObject)->GetInterface(App.Audio.bqPlayerObject, SL_IID_EFFECTSEND,
    &App.Audio.bqPlayerEffectSend);
  assert(SL_RESULT_SUCCESS == result);

  // set the player's state to playing
  result = (*App.Audio.bqPlayerPlay)->SetPlayState(App.Audio.bqPlayerPlay, SL_PLAYSTATE_PLAYING);
  assert(SL_RESULT_SUCCESS == result);



  Info->outrb = AudioQueue_Create(2048*sizeof(short)*1000);

  Info->nextBuffer = (short *) malloc ( sizeof(short) * 2048 );
  Info->nextSize = 2048;
  Info->_isplay = 0;
  return NULL;
}
//-----------------------------------------------------------------------------
bool thOpenSLESPlay_SetFormat(HANDLE audioHandle, i32 nChannels, i32 wBitsPerSample, i32 nSamplesPerSec, i32 AudioPacketSize)
{
  return false;
}
//-----------------------------------------------------------------------------
bool thOpenSLESPlay_FillMem(HANDLE audioHandle, char* Buf, i32 BufLen)
{
  return false;
}
//-----------------------------------------------------------------------------
bool thOpenSLESPlay_Free(HANDLE audioHandle)
{
  return false;
}
//-----------------------------------------------------------------------------
