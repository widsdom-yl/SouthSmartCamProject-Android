//-----------------------------------------------------------------------------
// Author      : 朱红波
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#include "thffmpeg.h"
#include "../include/avDecode.h"
#include "../include/TFun.h"

#define IS_AAC

#ifdef IS_AAC
#include		"../include/vo-aacenc/voAAC.h"
#include		"../include/vo-aacenc/cmnMemory.h"

#ifdef WIN32
#pragma comment (lib, "lib.win32/vo-aacenc/libvo-aacenc.a")
#endif
#endif
/*
#include <GL/GL.h>
#include <GL/GLAux.h>
#include <GL/GLU.h>
#pragma comment (lib, "OpenGL32.Lib") ?1.0
*/
/*
FrameInfo.Frame.IsIFrame = (*(DWORD*)(iAddr+0) == 0x01000000) && (
(*(DWORD*)(iAddr+4) << 16 == 0x42670000) ||//baseline
(*(DWORD*)(iAddr+4) << 16 == 0x4d670000) ||//mp
(*(DWORD*)(iAddr+4) << 16 == 0x64670000)   //hp
*///67 42 baseline
//-----------------------------------------------------------------------------
typedef struct TRecInfoPkt {
    int IsRec;
    H_THREADLOCK Lock;
    int IsWriteHead;
    unsigned char BufSPS[256];
    int LenSPS;
    unsigned char BufPPS[256];
    int LenPPS;
    unsigned char BufSEI[256];
    int LenSEI;
    unsigned char Buf_extradataV[256];
    int Len_extradataV;
    unsigned char Buf_extradataA[4];
    int Len_extradataA;

    AVFormatContext* avFormat;

    AVStream* StreamV;
    int64_t VideoFrameCount;
    TMediaType VideoType;
    int Width;
    int Height;
    int FrameRate;
    int FrameRate90000;

    AVStream* StreamA;
    int64_t AudioFrameCount;
    TMediaType AudioType;
    int nChannels;
    int nSamplesPerSec;
    int wBitsPerSample;

#define BUFSIZE_AACWRITE 2048//Len只能为2048，不然会丢包
    char BufAudioWrite[1024*100];
    int PosAudioWrite;

#define READ_SIZE	1024*64
#ifdef IS_AAC
    unsigned char aacOutBuf[READ_SIZE];
    VO_AUDIO_CODECAPI      aacAPI;
    VO_HANDLE              haacCodec;
    AACENC_PARAM				   aacParam;
    VO_MEM_OPERATOR        moper;
    VO_CODEC_INIT_USERDATA UserData;
#endif
}TRecInfoPkt;

int VideoGet_SPS_PPS_IFrame(TRecInfoPkt* Info, unsigned char* Buf, int Len);
void make_ExtraDataAudio(int nSamplesPerSec, int nChannels, unsigned char* ExtraDataAudio);
//-----------------------------------------------------------------------------
HANDLE thRecordStart(char* FileName, TMediaType VideoType, int Width, int Height, int FrameRate, TMediaType AudioType, int nChannels, int nSamplesPerSec, int wBitsPerSample)
{
  int ret;
  TRecInfoPkt* Info = NULL;
  AVCodecContext* CodecContextV = NULL;
  AVCodecContext* CodecContextA = NULL;
#ifdef WIN32
  __try
  {
    avcodec_register_all();
  }
  __except(1)
  {
  }
  __try
  {
    av_register_all();
  }
  __except(1)
  {
  }
#else
  avcodec_register_all();
  av_register_all();
#endif

  Info = (TRecInfoPkt*)malloc(sizeof(TRecInfoPkt));
  memset(Info, 0, sizeof(TRecInfoPkt));
  ThreadLockInit(&Info->Lock);
  Info->IsRec = false;

  avformat_alloc_output_context2(&Info->avFormat, NULL, NULL, FileName);
  if (Info->avFormat == NULL) return NULL;

  if (avio_open(&Info->avFormat->pb, FileName, AVIO_FLAG_WRITE) < 0)
  {
    avformat_free_context(Info->avFormat);
    free(Info);
    return NULL;
  }

  Info->VideoType = VideoType;
  Info->Width = Width;
  Info->Height = Height;
  Info->FrameRate = FrameRate;

  if (Info->VideoType != CODEC_NONE)
  {
    Info->FrameRate90000 = 90000;
    Info->avFormat->video_codec_id = Info->VideoType;

    Info->StreamV = avformat_new_stream(Info->avFormat, NULL);
    if (!Info->StreamV) goto exits;
    Info->StreamV->index = Info->StreamV->index;
    avcodec_get_context_defaults3(Info->StreamV->codec, NULL);
    CodecContextV = Info->StreamV->codec;
    CodecContextV->codec_id = Info->VideoType;
    CodecContextV->width = Info->Width;
    CodecContextV->height = Info->Height;
    CodecContextV->time_base.den = Info->FrameRate90000;
    CodecContextV->time_base.num = 1;
    CodecContextV->bit_rate = 400000;
    CodecContextV->codec_type = AVMEDIA_TYPE_VIDEO;
    CodecContextV->pix_fmt = AV_PIX_FMT_YUV420P;
    CodecContextV->qmin = 2;
    CodecContextV->qmax = 31;
    CodecContextV->flags = 0;
    CodecContextV->flags |= CODEC_FLAG_GLOBAL_HEADER;
    CodecContextV->me_range = 0;
    CodecContextV->max_qdiff = 3;
    CodecContextV->qcompress = 0.50;
    CodecContextV->frame_number = 1;
    CodecContextV->gop_size= Info->FrameRate;
    CodecContextV->extradata = NULL;//Buf_extradata;
    CodecContextV->extradata_size = 0;//sizeof(Buf_extradata);
  }

  Info->AudioType =   AudioType;
  Info->nChannels = nChannels;
  Info->nSamplesPerSec = nSamplesPerSec;
  Info->wBitsPerSample = wBitsPerSample;
  if (Info->AudioType != CODEC_NONE)
  {
    Info->avFormat->audio_codec_id = CODEC_AAC;//Info->AudioType;

    Info->StreamA = avformat_new_stream(Info->avFormat, NULL);
    if (!Info->StreamA) goto exits;
    Info->StreamA->index = Info->StreamA->index;
    avcodec_get_context_defaults3(Info->StreamA->codec, NULL);
    CodecContextA = Info->StreamA->codec;
    CodecContextA->codec_id = CODEC_AAC;//Info->AudioType;

    CodecContextA->codec_type = AVMEDIA_TYPE_AUDIO;
    CodecContextA->sample_fmt = AV_SAMPLE_FMT_FLT;
    if (Info->wBitsPerSample ==  8) CodecContextA->sample_fmt = AV_SAMPLE_FMT_U8;
    if (Info->wBitsPerSample == 16) CodecContextA->sample_fmt = AV_SAMPLE_FMT_S16;
    if (Info->wBitsPerSample == 32) CodecContextA->sample_fmt = AV_SAMPLE_FMT_S32;
    CodecContextA->sample_rate = Info->nSamplesPerSec;//8000;
    CodecContextA->channels = Info->nChannels;//1;
    CodecContextA->bit_rate = Info->nSamplesPerSec * 2 * 8 * Info->nChannels;//128000;
    CodecContextA->flags |= CODEC_FLAG_GLOBAL_HEADER;
    CodecContextA->extradata = NULL;//Buf_extradata;
    CodecContextA->extradata_size = 0;//sizeof(Buf_extradata);
#ifdef IS_AAC
    voGetAACEncAPI(&Info->aacAPI);
    Info->moper.Alloc = cmnMemAlloc;
    Info->moper.Copy  = cmnMemCopy;
    Info->moper.Free  = cmnMemFree;
    Info->moper.Set   = cmnMemSet;
    Info->moper.Check = cmnMemCheck;
    Info->UserData.memflag = VO_IMF_USERMEMOPERATOR;
    Info->UserData.memData = (VO_PTR)(&Info->moper);
    ret = Info->aacAPI.Init(&Info->haacCodec, VO_AUDIO_CodingAAC, &Info->UserData);
    if(ret < 0) goto exits;
    Info->aacParam.nChannels = Info->nChannels;//1;
    Info->aacParam.sampleRate = Info->nSamplesPerSec;//8000;
    Info->aacParam.bitRate = Info->nSamplesPerSec * 2 * 8 * Info->nChannels;//128000
    ret = Info->aacAPI.SetParam(Info->haacCodec, VO_PID_AAC_ENCPARAM, &Info->aacParam);
#endif
  }

  Info->IsWriteHead = false;

  if (Info->VideoType == CODEC_NONE && Info->AudioType != CODEC_NONE)//只有音频，没有视频的mp4
  {
    make_ExtraDataAudio(Info->nSamplesPerSec, Info->nChannels, Info->Buf_extradataA);
    Info->Len_extradataA = 2;
    Info->StreamA->codec->extradata = Info->Buf_extradataA;
    Info->StreamA->codec->extradata_size = Info->Len_extradataA;
    avformat_write_header(Info->avFormat, NULL);
    Info->IsWriteHead = true;
  }

  Info->IsRec = true;
  return (HANDLE)Info;

    exits:
  if (Info->avFormat)
  {
    if (Info->avFormat->pb) avio_close(Info->avFormat->pb);
    avformat_free_context(Info->avFormat);
  }
  free(Info);

  return NULL;
}
//-----------------------------------------------------------------------------
int VideoGet_SPS_PPS_IFrame(TRecInfoPkt* Info, unsigned char* Buf, int Len)
{
  /*
  //0x67=sps 0x68=pps 0x65=IFrame 0x61=PFrame
  00 00 00 01 67 42 00 2A 95 A8 1E 00 89 F9 50 00
  00 00 01 68 CE 3C 80 00 00 00 01 65 B8 00 00 0A
  A0 60 7F F5 DE 44 4E EE 5A 8A 00 02 06 F8 AC 0A
  98 CC 66 07 C6 A4 EA 82 9D E0 85 40 7A 40 F6 13
  */
#define MAX_NALU_LEN  256
  unsigned char NaluType = 0;
  unsigned char PrevNaluType = 0;
  int NaluTypeLen;
  int i, iLen;
  unsigned char* buf0 = NULL;
  unsigned char* buf1 = NULL;
  if (!(Buf[0] == 0x00 && Buf[1] == 0x00 && Buf[2] == 0x00 && Buf[3] == 0x01)) return 0;
  NaluType = Buf[4] & 0x1F;
  if (NaluType != 0x07 && NaluType != 0x08 && NaluType != 0x06 && NaluType != 0x05) return 0;

  if (Len > MAX_NALU_LEN) iLen = MAX_NALU_LEN; else iLen = Len;
  buf0 = &Buf[0];
  buf1 = &Buf[0];
  for (i=0; i<iLen-4; i++)//SPS PPS IFrame 合并的情况下
  {
    if (buf1[0] == 0x00 && buf1[1] == 0x00 && buf1[2] == 0x00 && buf1[3] == 0x01)
    {
      NaluType =Buf[i + 4] & 0x1F;
      if (NaluType != 0x07 && NaluType != 0x08 && NaluType != 0x06 && NaluType != 0x05) return 0;
      NaluTypeLen = buf1 - buf0;

      if (NaluTypeLen > 0)
      {
        if (PrevNaluType == 0x07 && Info->LenSPS == 0)
        {
          Info->LenSPS = NaluTypeLen;
          memcpy(Info->BufSPS, buf0, Info->LenSPS);//包含 00 00 00 01
        }
        if (PrevNaluType == 0x08 && Info->LenPPS == 0)
        {
          Info->LenPPS = NaluTypeLen;
          memcpy(Info->BufPPS, buf0, Info->LenPPS);//包含 00 00 00 01
        }
        if (PrevNaluType == 0x06 && Info->LenSEI == 0)
        {
          Info->LenSEI = NaluTypeLen;
          memcpy(Info->BufSEI, buf0, Info->LenSEI);//包含 00 00 00 01
        }
      }
      if (NaluType == 0x05)
      {
        if (Info->Len_extradataV == 0 && Info->LenSPS > 0 && Info->LenPPS > 0)
        {
          memcpy(&Info->Buf_extradataV[0], Info->BufSPS, Info->LenSPS);
          memcpy(&Info->Buf_extradataV[Info->LenSPS], Info->BufPPS, Info->LenPPS);
          Info->Len_extradataV = Info->LenSPS + Info->LenPPS;
          if (Info->LenSEI > 0)
          {
            memcpy(&Info->Buf_extradataV[Info->Len_extradataV], Info->BufSEI, Info->LenSEI);
            Info->Len_extradataV = Info->Len_extradataV + Info->LenSEI;
          }
        }
        return 1;
      }
      PrevNaluType = NaluType;
      buf0 = buf1;
    }
    buf1++;
  }

  if (NaluType == PrevNaluType)//单独 SPS PPS IFrame帧的情况下
  {
    if (PrevNaluType == 0x07 && Info->LenSPS == 0)
    {
      Info->LenSPS = Len;
      memcpy(Info->BufSPS, buf0, Info->LenSPS);//包含 00 00 00 01
    }
    if (PrevNaluType == 0x08 && Info->LenPPS == 0)
    {
      Info->LenPPS = Len;
      memcpy(Info->BufPPS, buf0, Info->LenPPS);//包含 00 00 00 01
    }
    if (PrevNaluType == 0x06 && Info->LenSEI == 0)
    {
      Info->LenSEI = Len;
      memcpy(Info->BufSEI, buf0, Info->LenSEI);//包含 00 00 00 01
    }
  }
  return 0;
}
//-----------------------------------------------------------------------------
void make_ExtraDataAudio(int nSamplesPerSec, int nChannels, unsigned char* ExtraDataAudio)
{
  int objtype = 2; // AAC LC by default
  int SampleIndex = 0;
  if (nSamplesPerSec      == 96000) SampleIndex = 0;
  else if (nSamplesPerSec == 88200) SampleIndex = 1;
  else if (nSamplesPerSec == 64000) SampleIndex = 2;
  else if (nSamplesPerSec == 48000) SampleIndex = 3;
  else if (nSamplesPerSec == 44100) SampleIndex = 4;
  else if (nSamplesPerSec == 32000) SampleIndex = 5;
  else if (nSamplesPerSec == 24000) SampleIndex = 6;
  else if (nSamplesPerSec == 22050) SampleIndex = 7;
  else if (nSamplesPerSec == 16000) SampleIndex = 8;
  else if (nSamplesPerSec == 12000) SampleIndex = 9;
  else if (nSamplesPerSec == 11025) SampleIndex = 10;
  else if (nSamplesPerSec ==  8000) SampleIndex = 11;
  else if (nSamplesPerSec ==  7350) SampleIndex = 12;
  ExtraDataAudio[0] = (objtype << 3) | (SampleIndex >> 1);
  ExtraDataAudio[1] = ((SampleIndex & 1) <<7) | (nChannels << 3);
}
//-----------------------------------------------------------------------------
bool thRecordWriteVideo(HANDLE RecHandle, char* Buf, int Len, i64 pts)//pts = -1 为自动计算
{
    pts = -1;
  int ret, IsIFrame;
  AVPacket packet;
  TRecInfoPkt* Info = (TRecInfoPkt*)RecHandle;
  if (!Info) return false;
  if (Len <= 0) return false;
  if (!Info->IsRec) return false;

  IsIFrame = VideoGet_SPS_PPS_IFrame(Info, (unsigned char*)Buf, Len);
  if (!Info->IsWriteHead)
  {
    PRINTF("%s(%d) IsIFrame:%d Info->Len_extradataV:%d\n", __FUNCTION__, __LINE__, IsIFrame, Info->Len_extradataV);
    if (IsIFrame && Info->Len_extradataV > 0)
    {
      Info->StreamV->codec->extradata = Info->Buf_extradataV;
      Info->StreamV->codec->extradata_size = Info->Len_extradataV;

      if (Info->AudioType != CODEC_NONE)
      {
        make_ExtraDataAudio(Info->nSamplesPerSec, Info->nChannels, Info->Buf_extradataA);
        Info->Len_extradataA = 2;
        Info->StreamA->codec->extradata = Info->Buf_extradataA;
        Info->StreamA->codec->extradata_size = Info->Len_extradataA;
      }
      avformat_write_header(Info->avFormat, NULL);
      Info->IsWriteHead = true;
    }
  }
  if (!Info->IsWriteHead) return false;


  ThreadLock(&Info->Lock);
  Info->VideoFrameCount = Info->VideoFrameCount+1;
    PRINTF("%s(%d) pts is %lld", __FUNCTION__, __LINE__,pts);
  PRINTF("%s(%d) record VideoFrameCount:%lld ", __FUNCTION__, __LINE__,Info->VideoFrameCount);

  av_init_packet(&packet);
  if (IsIFrame) packet.flags |= AV_PKT_FLAG_KEY; else packet.flags = 0;
  if (pts != -1) packet.pts = pts; else packet.pts = Info->VideoFrameCount * (Info->FrameRate90000 / Info->FrameRate);//只能设固定值？？？
  packet.dts = packet.pts;
  packet.stream_index = Info->StreamV->index;
  packet.data = (uint8_t*)Buf;
  packet.size = Len;
  ret = av_interleaved_write_frame(Info->avFormat, &packet);
    PRINTF("%s(%d) record VideoFrameCount:% ,end", __FUNCTION__, __LINE__,Info->VideoFrameCount);
  ThreadUnlock(&Info->Lock);
  return (ret == 0);
}
//-----------------------------------------------------------------------------
bool thRecordWriteAudio2048(HANDLE RecHandle, char* Buf, int Len, i64 pts)//pts = -1 为自动计算 Len只能为2048，不然会丢包
{

#ifdef IS_AAC
  VO_CODECBUFFER         inData;
  VO_CODECBUFFER         outData;
  VO_AUDIO_OUTPUTINFO    outInfo;
#endif
  int ret = 0;
  AVPacket packet;
  TRecInfoPkt* Info = (TRecInfoPkt*)RecHandle;
  if (!Info) return false;
  if (Len <= 0) return false;
  if (!Info->IsRec) return false;
  if (!Info->IsWriteHead) return false;
#ifdef IS_AAC
  inData.Buffer = (u8*)Buf;
  inData.Length = Len;

  ret = Info->aacAPI.SetInputData(Info->haacCodec, &inData);
  outData.Buffer   = Info->aacOutBuf;
  outData.Length = READ_SIZE;//必须指定大小
  outData.Time = 0;
  ret = Info->aacAPI.GetOutputData(Info->haacCodec,&outData, &outInfo);
  if (ret == VO_ERR_INPUT_BUFFER_SMALL)
  {
    printf("ret aacAPI.GetOutputData errrorrrrr\n");
  }
  if(ret != 0)
  {
    return false;
  }

  ThreadLock(&Info->Lock);
  Info->AudioFrameCount++;

  av_init_packet(&packet);
  if (pts != -1) packet.pts = pts; else packet.pts = Info->AudioFrameCount * (Len / 2);//ok
  packet.dts = packet.pts;
  packet.stream_index = Info->StreamA->index;
  packet.data = (uint8_t*)outData.Buffer;
  packet.size = outData.Length;
  ret = av_interleaved_write_frame(Info->avFormat, &packet);
  ThreadUnlock(&Info->Lock);
#endif
  return (ret == 0);
}
//-----------------------------------------------------------------------------
bool thRecordWriteAudio(HANDLE RecHandle, char* Buf, int Len, i64 pts)//pts = -1 为自动计算 Len只能为2048，不然会丢包
{
  int ret, i, idiv, imod;
  char* tmpBuf;
  TRecInfoPkt* Info = (TRecInfoPkt*)RecHandle;
  if (!Info) return false;
  if (Len <= 0) return false;
  if (!Info->IsRec) return false;
  if (!Info->IsWriteHead) return false;

  if (Len == BUFSIZE_AACWRITE)
  {
    return thRecordWriteAudio2048(RecHandle, Buf, Len, pts);
  }

  memcpy(&Info->BufAudioWrite[Info->PosAudioWrite], Buf, Len);
  Info->PosAudioWrite = Info->PosAudioWrite + Len;

  idiv = Info->PosAudioWrite / BUFSIZE_AACWRITE;
  imod = Info->PosAudioWrite % BUFSIZE_AACWRITE;

  tmpBuf = Info->BufAudioWrite;
  for (i=0; i<idiv; i++)
  {
    ret = thRecordWriteAudio2048(RecHandle, tmpBuf, BUFSIZE_AACWRITE, pts);
    tmpBuf = tmpBuf + BUFSIZE_AACWRITE;
  }
  if (idiv > 0)
  {
    memcpy(Info->BufAudioWrite, tmpBuf, imod);
    Info->PosAudioWrite = imod;
  }
  return true;
}
//-----------------------------------------------------------------------------
bool thRecordIsRec(HANDLE RecHandle)
{
  TRecInfoPkt* Info = (TRecInfoPkt*)RecHandle;
  if (!Info) return false;
  return (Info->IsRec);
}
//-----------------------------------------------------------------------------
bool thRecordStop(HANDLE RecHandle)
{
  int ret;
  TRecInfoPkt* Info = (TRecInfoPkt*)RecHandle;
  if (!Info) return false;

  ThreadLock(&Info->Lock);
  Info->IsRec = false;
  ThreadUnlock(&Info->Lock);
#ifdef IS_AAC
  if (Info->haacCodec) ret = Info->aacAPI.Uninit(Info->haacCodec);
#endif
  av_write_trailer(Info->avFormat);
  avio_close(Info->avFormat->pb);

  if (Info->VideoType != CODEC_NONE)
  {
    Info->StreamV->codec->extradata = NULL;
    Info->StreamV->codec->extradata_size = 0;
  }

  if (Info->AudioType != CODEC_NONE)
  {
    Info->StreamA->codec->extradata = NULL;
    Info->StreamA->codec->extradata_size = 0;
  }

  avformat_free_context(Info->avFormat);

  ThreadLockFree(&Info->Lock);
  free(Info);
    PRINTF("%s(%d) record stop", __FUNCTION__, __LINE__);
  return true;
}
//-----------------------------------------------------------------------------
HANDLE thRecordStart1(char* FileName, void* pCodecCtxV, void* pCodecCtxA)
{
  int ret;
  TRecInfoPkt* Info = NULL;
  AVCodecContext* CodecContextV = NULL;
  AVCodecContext* CodecContextA = NULL;

  AVCodecContext* pCodecV = (AVCodecContext*)pCodecCtxV;
  AVCodecContext* pCodecA = (AVCodecContext*)pCodecCtxA;
  if (!pCodecV && !pCodecA) return NULL;
#ifdef WIN32
  __try
  {
    avcodec_register_all();
  }
  __except(1)
  {
  }
  __try
  {
    av_register_all();
  }
  __except(1)
  {
  }
#else
  avcodec_register_all();
  av_register_all();
#endif

  Info = (TRecInfoPkt*)malloc(sizeof(TRecInfoPkt));
  memset(Info, 0, sizeof(TRecInfoPkt));
  ThreadLockInit(&Info->Lock);
  Info->IsRec = false;

  avformat_alloc_output_context2(&Info->avFormat, NULL, NULL, FileName);
  if (Info->avFormat == NULL) return NULL;

  if (avio_open(&Info->avFormat->pb, FileName, AVIO_FLAG_WRITE) < 0)
  {
    avformat_free_context(Info->avFormat);
    free(Info);
    return NULL;
  }

  if (pCodecV)
  {
    Info->VideoType = pCodecV->codec_id;
    Info->Width = pCodecV->width;
    Info->Height = pCodecV->height;
    Info->FrameRate = pCodecV->framerate.num / pCodecV->framerate.den;

    Info->FrameRate90000 = 90000;
    Info->avFormat->video_codec_id = Info->VideoType;

    Info->StreamV = avformat_new_stream(Info->avFormat, NULL);
    if (!Info->StreamV) goto exits;
    Info->StreamV->index = Info->StreamV->index;
    avcodec_get_context_defaults3(Info->StreamV->codec, NULL);
    CodecContextV = Info->StreamV->codec;
    CodecContextV->codec_id = Info->VideoType;
    CodecContextV->width = Info->Width;
    CodecContextV->height = Info->Height;
    CodecContextV->time_base.den = Info->FrameRate90000;
    CodecContextV->time_base.num = 1;
    CodecContextV->bit_rate = 400000;
    CodecContextV->codec_type = AVMEDIA_TYPE_VIDEO;
    CodecContextV->pix_fmt = AV_PIX_FMT_YUV420P;
    CodecContextV->qmin = 2;
    CodecContextV->qmax = 31;
    CodecContextV->flags = 0;
    CodecContextV->flags |= CODEC_FLAG_GLOBAL_HEADER;
    CodecContextV->me_range = 0;
    CodecContextV->max_qdiff = 3;
    CodecContextV->qcompress = 0.50;
    CodecContextV->frame_number = 1;
    CodecContextV->gop_size= Info->FrameRate;
    CodecContextV->extradata = pCodecV->extradata;//Buf_extradata;
    CodecContextV->extradata_size = pCodecV->extradata_size;//sizeof(Buf_extradata);
  }

  if (pCodecA)
  {
    Info->AudioType =   pCodecA->codec_id;
    Info->nChannels = pCodecA->channels;
    Info->nSamplesPerSec = pCodecA->sample_rate;
    Info->wBitsPerSample = pCodecA->bit_rate;

    Info->avFormat->audio_codec_id = CODEC_AAC;//Info->AudioType;

    Info->StreamA = avformat_new_stream(Info->avFormat, NULL);
    if (!Info->StreamA) goto exits;
    Info->StreamA->index = Info->StreamA->index;
    avcodec_get_context_defaults3(Info->StreamA->codec, NULL);
    CodecContextA = Info->StreamA->codec;
    CodecContextA->codec_id = CODEC_AAC;//Info->AudioType;

    CodecContextA->codec_type = AVMEDIA_TYPE_AUDIO;
    CodecContextA->sample_fmt = AV_SAMPLE_FMT_FLT;
    if (Info->wBitsPerSample ==  8) CodecContextA->sample_fmt = AV_SAMPLE_FMT_U8;
    if (Info->wBitsPerSample == 16) CodecContextA->sample_fmt = AV_SAMPLE_FMT_S16;
    if (Info->wBitsPerSample == 32) CodecContextA->sample_fmt = AV_SAMPLE_FMT_S32;
    CodecContextA->sample_rate = Info->nSamplesPerSec;//8000;
    CodecContextA->channels = Info->nChannels;//1;
    CodecContextA->bit_rate = Info->nSamplesPerSec * 2 * 8 * Info->nChannels;//128000;
    CodecContextA->flags |= CODEC_FLAG_GLOBAL_HEADER;
    CodecContextA->extradata = pCodecA->extradata;//Buf_extradata;
    CodecContextA->extradata_size = pCodecA->extradata_size;//sizeof(Buf_extradata);
#ifdef IS_AAC
    voGetAACEncAPI(&Info->aacAPI);
    Info->moper.Alloc = cmnMemAlloc;
    Info->moper.Copy  = cmnMemCopy;
    Info->moper.Free  = cmnMemFree;
    Info->moper.Set   = cmnMemSet;
    Info->moper.Check = cmnMemCheck;
    Info->UserData.memflag = VO_IMF_USERMEMOPERATOR;
    Info->UserData.memData = (VO_PTR)(&Info->moper);
    ret = Info->aacAPI.Init(&Info->haacCodec, VO_AUDIO_CodingAAC, &Info->UserData);
    if(ret < 0) goto exits;
    Info->aacParam.nChannels = Info->nChannels;//1;
    Info->aacParam.sampleRate = Info->nSamplesPerSec;//8000;
    Info->aacParam.bitRate = Info->nSamplesPerSec * 2 * 8 * Info->nChannels;//128000
    ret = Info->aacAPI.SetParam(Info->haacCodec, VO_PID_AAC_ENCPARAM, &Info->aacParam);
#endif
  }

  Info->IsWriteHead = false;

  if (pCodecV || pCodecA)
  {
    avformat_write_header(Info->avFormat, NULL);
    Info->IsWriteHead = true;
  }
  Info->IsRec = true;
  return (HANDLE)Info;

    exits:
  if (Info->avFormat)
  {
    if (Info->avFormat->pb) avio_close(Info->avFormat->pb);
    avformat_free_context(Info->avFormat);
  }
  free(Info);

  return NULL;
}
//-----------------------------------------------------------------------------
bool thRecordWriteVideo1(HANDLE RecHandle, char* Buf, int BufLen, void* Ppacket)
{
  int ret;
  AVPacket oPkt;
  TRecInfoPkt* Info = (TRecInfoPkt*)RecHandle;
  AVPacket* packet = (AVPacket*)Ppacket;

  if (!Info) return false;

  Info->VideoFrameCount++;
  av_init_packet(&oPkt);
  oPkt.flags = packet->flags;
  //if (IsIFrame) oPkt.flags |= AV_PKT_FLAG_KEY; else oPkt.flags = 0;
  //if (pts != -1) oPkt.pts = packet->pts; else oPkt.pts = Info->VideoFrameCount * (Info->FrameRate90000 / Info->FrameRate);//只能设固定值？？？
  oPkt.pts =  Info->VideoFrameCount * (Info->FrameRate90000 / Info->FrameRate);

  oPkt.dts = oPkt.pts;
  oPkt.stream_index = Info->StreamV->index;
  oPkt.data = Buf;
  oPkt.size = BufLen;
  ret = av_interleaved_write_frame(Info->avFormat, &oPkt);
}

bool thRecordWriteAudio1(HANDLE RecHandle, char* Buf, int BufLen, void* Ppacket)
{
  return false;
}
