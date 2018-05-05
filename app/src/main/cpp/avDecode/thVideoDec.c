//-----------------------------------------------------------------------------
// Author      : Öìºì²¨
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#include "thffmpeg.h"
#include "../include/avDecode.h"
#include "libjpeg/myJpeg.h"

//-----------------------------------------------------------------------------
typedef struct TdecInfoPkt {
  //H_THREADLOCK Lock;
  //video
  TMediaType VideoType;
  AVCodecContext* pCodecCtxV;
  AVFrame* FrameV;
  //audio

}TdecInfoPkt;
//-----------------------------------------------------------------------------
HANDLE thDecodeVideoInit(int VideoType)
{
  TdecInfoPkt* Info = NULL;
  AVCodec* CodecV;

  Info = (TdecInfoPkt*)malloc(sizeof(TdecInfoPkt)); 
  memset(Info, 0, sizeof(TdecInfoPkt));
  //ThreadLockInit(&avQueue->Lock);
  Info->VideoType = (TMediaType)VideoType;

  avcodec_register_all();

  CodecV = avcodec_find_decoder(Info->VideoType);
  if (CodecV == NULL) goto exits;

  Info->pCodecCtxV = avcodec_alloc_context3(CodecV);
  if (Info->pCodecCtxV == NULL) goto exits;

  Info->pCodecCtxV->codec_type = AVMEDIA_TYPE_VIDEO;
  Info->pCodecCtxV->codec_id = Info->VideoType;

  if (avcodec_open2(Info->pCodecCtxV, CodecV, NULL) < 0) goto exits;

  Info->FrameV = av_frame_alloc();
  if (Info->FrameV == NULL) goto exits;

  return (HANDLE)Info;

exits:
  thDecodeVideoFree((HANDLE)Info);
  return 0;
}
//-----------------------------------------------------------------------------
bool thDecodeVideoFrame(HANDLE decHandle, char* encBuf, int encLen, int* decWidth, int* decHeight, TavPicture* avPicture)
{
  AVPacket packet;
  TdecInfoPkt* Info = (TdecInfoPkt*)decHandle;
  if (!Info) return false;
  if (!Info->pCodecCtxV) return false;
  if (!Info->FrameV) return false;
  if (encBuf == NULL && encLen > 0) return false;

  av_init_packet(&packet);
  packet.data = (uint8_t*)encBuf;
  packet.size = encLen;

  *decWidth = 0;
  *decHeight = 0;
  while (packet.size > 0)
  {
    int size = 0;
    int got_picture = 0;

    size = avcodec_decode_video2(Info->pCodecCtxV, Info->FrameV, &got_picture, &packet);

    if (size < 0) break;
    if (got_picture)
    {
      if ((Info->pCodecCtxV->width==0)||(Info->pCodecCtxV->height==0)) return false;
      *decWidth = Info->pCodecCtxV->width;
      *decHeight = Info->pCodecCtxV->height;
      memcpy(avPicture, Info->FrameV, sizeof(TavPicture));
      return true;
    }
    packet.data += size;
    packet.size -= size;
  }
  return false;
}
//-----------------------------------------------------------------------------
bool thDecodeVideoFree(HANDLE decHandle)
{
  TdecInfoPkt* Info = (TdecInfoPkt*)decHandle;
  if (!Info) return false;

  if (Info->pCodecCtxV) 
  {
    avcodec_close(Info->pCodecCtxV);
    Info->pCodecCtxV = NULL;
  }

  if (Info->FrameV)
  {
    av_free(Info->FrameV);
    Info->FrameV = NULL;
  }

  memset(Info, 0, sizeof(TdecInfoPkt));
  free(Info);
  return true;
}
//-----------------------------------------------------------------------------
bool thDecodeVideoSaveToJpg(HANDLE decHandle, char* FileName_Jpg)
{
  int w, h, ret;
  char* newBuf;
  TavPicture newFrameV;
  TdecInfoPkt* Info = (TdecInfoPkt*)decHandle;
  if (!Info) return false;
  if (!Info->FrameV) return false;
  w = Info->FrameV->width;
  h = Info->FrameV->height;

  newBuf = (char*)malloc(w * h * 3);
  thImgConvertFill(&newFrameV, newBuf, AV_PIX_FMT_RGB24, w, h);
  thImgConvertScale1(//only copy ?
    &newFrameV, 
    AV_PIX_FMT_RGB24,
    w,
    h,
    Info->FrameV,
    AV_PIX_FMT_YUV420P,
    w,
    h,
    0);
  ret = rgb24_jpg(FileName_Jpg, newBuf, w, h);
  free(newBuf);

  return ret;
}
