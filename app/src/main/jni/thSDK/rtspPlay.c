//-----------------------------------------------------------------------------
// Author      : 朱红波
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
//interrupt_cb
//avformat_open_input连接失败，需要重连
//av_read_frame 连接失败，需要重连

#include "../include/TFun.h"
#include "../include/rtspPlay.h"
#include "../avDecode/thffmpeg.h"
#include "../include/avDecode.h"
#include "../include/av_Queue.h"

typedef enum TStreamStatus
{
  st_ReadOver,
  st_PlayOver,
  st_Video,
  st_Audio, 
} TStreamStatus;

typedef struct TavFormatInfo//sizeof 624
  {
    //VIDEO
    int VideoCodecID;
    int FrameRate;
    int BitRate;
    int Gop;
    int Width;
    int Height;
    //AUDIO
    int AudioCodecID;
    int nSamplesPerSec; //采样率
    int nChannels; //单声道=0 立体声=1
    int wBitsPerSample; //8, 16

    HANDLE VideoHandle;

    LONGLONG Duration;
    LONGLONG TimeStamp;

    unsigned char* videoBuf;
    int videoBufLen;
    i64 videoPts;
    i64 audioPts;
    unsigned char* audioBuf;
    int audioBufLen;

    int NaluType;//5 6 7 8 4
    unsigned char Buf_extradataV[256];
    int Len_extradataV;
    unsigned char Buf_extradataA[256];
    int Len_extradataA;

    int Reserved;//88
  } TavFormatInfo;

typedef struct TrtspInfo
{
  bool IsQueue;
#define MAX_DSPINFO_COUNT  10
  TDspInfo* DspInfoLst[MAX_DSPINFO_COUNT];
  HANDLE decHandle;//解码句柄
  bool IsReDecodeFalg;
  HANDLE RenderHandle;//显示句柄
  TavFormatInfo avFormatInfo;
  i32 ImgWidth;
  i32 ImgHeight;
  i32 FrameRate;

  char FileName[1024];
  AVInputFormat* fmt;
  AVDictionary* options;

  bool IsExit;
  bool IsConnect;

  int ConnTimeOut;//ms

  TavCallback1 avCallback;
  const void* UserData;

  bool IsEof;
  unsigned int BufLenTotal;
  int IsPauseDecode;
  LONGLONG LastFrameRealtime;

  struct SwsContext* sws;
  AVFormatContext* pFormatCtx;
  AVCodecContext* pCodecCtxV;
  enum AVCodecID VideoMediaType;
  int audioIndex;
  int videoIndex;

  H_THREADLOCK Lock;// = PTHREAD_MUTEX_INITIALIZER;
  H_THREAD hthreadReadFile;

  int Reserved;
} TrtspInfo;

//-----------------------------------------------------------------------------
static int ffmpeg_error(int ret)
{
  if (ret == 0) return ret;
  switch (ret)
  {
  case AVERROR_BSF_NOT_FOUND:
    return ret;
    break;
  case AVERROR_BUG:
    return ret;
    break;
  case AVERROR_BUFFER_TOO_SMALL:
    return ret;
    break;
  case AVERROR_DECODER_NOT_FOUND:
    return ret;
    break;
  case AVERROR_DEMUXER_NOT_FOUND:
    return ret;
    break;
  case AVERROR_ENCODER_NOT_FOUND:
    return ret;
    break;
  case AVERROR_EOF:
    return ret;
    break;
  case AVERROR_EXIT:
    return ret;
    break;
  case AVERROR_EXTERNAL:
    return ret;
    break;
  case AVERROR_FILTER_NOT_FOUND:
    return ret;
    break;
  case AVERROR_INVALIDDATA:
    return ret;
    break;
  case AVERROR_MUXER_NOT_FOUND:
    return ret;
    break;
  case AVERROR_OPTION_NOT_FOUND:
    return ret;
    break;
  case AVERROR_PATCHWELCOME:
    return ret;
    break;
  case AVERROR_PROTOCOL_NOT_FOUND:
    return ret;
    break;
  case AVERROR_STREAM_NOT_FOUND:
    return ret;
    break;
  case AVERROR_BUG2:
    return ret;
    break;
  case AVERROR_UNKNOWN:
    return ret;
    break;
  case AVERROR_EXPERIMENTAL:
    return ret;
    break;
  case AVERROR_INPUT_CHANGED:
    return ret;
    break;
  case AVERROR_OUTPUT_CHANGED:
    return ret;
    break;
  case AVERROR_HTTP_BAD_REQUEST:
    return ret;
    break;
  case AVERROR_HTTP_UNAUTHORIZED:
    return ret;
    break;
  case AVERROR_HTTP_FORBIDDEN:
    return ret;
    break;
  case AVERROR_HTTP_NOT_FOUND:
    return ret;
    break;
  case AVERROR_HTTP_OTHER_4XX:
    return ret;
    break;
  case AVERROR_HTTP_SERVER_ERROR:
    return ret;
    break;
  default:
    return ret;
    break;
  }
}
//-----------------------------------------------------------------------------
static int VideoGetNaluType(unsigned char* Buf, int Len)
{
  //0x67=sps 0x68=pps 0x65=IFrame 0x61=PFrame
  if (*(int*)Buf == 0x01000000) return Buf[4] & 0x1F; else return 0;
}
//-----------------------------------------------------------------------------
static void rtsp_OnVideoEvent(TrtspInfo* Info, char* Buf, int BufLen, u64 pts)
{
  TavPicture FrameV;
  int i, ret;
  int ImgWidth = 0;
  int ImgHeight = 0;
  int NaluType = VideoGetNaluType((unsigned char*)Buf, BufLen);
  int IsIFrame = (NaluType == 7 || NaluType == 8 || NaluType == 5);

  if (Info->avCallback)
  {
    Info->avFormatInfo.VideoCodecID = Info->VideoMediaType;
    Info->avFormatInfo.Width = Info->ImgWidth;
    Info->avFormatInfo.Height = Info->ImgHeight;
    Info->avFormatInfo.FrameRate = Info->FrameRate;

    Info->avFormatInfo.videoBuf = (unsigned char*)Buf;
    Info->avFormatInfo.videoBufLen = BufLen;
    Info->avFormatInfo.NaluType = NaluType;
    Info->avFormatInfo.videoPts = pts;
    Info->avFormatInfo.Reserved = sizeof(TavFormatInfo);
    Info->avCallback((HANDLE)Info, &Info->avFormatInfo, st_Video, Info->UserData);
  }

  if (Info->IsPauseDecode)
  {
    Info->IsReDecodeFalg = true;
    return;
  }
  if (Info->IsReDecodeFalg)
  {
    if (!IsIFrame) return;
    Info->IsReDecodeFalg = false;
  }

  ret = thDecodeVideoFrame(Info->decHandle, Buf, BufLen, &ImgWidth, &ImgHeight, &FrameV);
  if (ret)
  {
    if (ImgWidth > 0 && ImgHeight > 0)
    {
      Info->ImgWidth = ImgWidth;
      Info->ImgHeight = ImgHeight;

      ThreadLock(&Info->Lock);
      ret = thRender_FillMem(Info->RenderHandle, FrameV, Info->ImgWidth, Info->ImgHeight);
      for (i=0; i<MAX_DSPINFO_COUNT; i++)
      {
        TDspInfo* PDspInfo = Info->DspInfoLst[i];
        if (PDspInfo == NULL) continue;
        ret = thRender_Display(Info->RenderHandle, PDspInfo->DspHandle, PDspInfo->DspRect);
      }
      ThreadUnlock(&Info->Lock);
    }

  }
}
//-----------------------------------------------------------------------------
static void th_ReadFile(void* Param)
{
  TrtspInfo* Info = (TrtspInfo*)Param;
  AVPacket packet;
  int ret = 0;

  while (1)
  {
    if (Info->IsExit) break;
    ret = av_read_frame(Info->pFormatCtx, &packet);
    ffmpeg_error(ret);
    if (ret == AVERROR_EOF)//文件读取完毕
    {
      ThreadLock(&Info->Lock);
      Info->IsEof = true;
      ThreadUnlock(&Info->Lock);
      av_packet_unref(&packet);
      break;
    }

    if (ret < 0)
    {
      usleep(1000 * 20);
      continue;
    }

    ThreadLock(&Info->Lock);
    Info->LastFrameRealtime = av_gettime();
    Info->BufLenTotal = Info->BufLenTotal + packet.size;
    ThreadUnlock(&Info->Lock);

    if (packet.stream_index == Info->videoIndex) rtsp_OnVideoEvent(Info, (char*)packet.data, packet.size, packet.pts);

    av_packet_unref(&packet);
  }
}
//-----------------------------------------------------------------------------
int thrtsp_Play(HANDLE Handle) 
{
  int ret = 0;
  TrtspInfo* Info = (TrtspInfo*)Handle;
  if (!Info) return false;
  if (!Info->IsConnect) return false;
  return true;
}
//-----------------------------------------------------------------------------
static int interrupt_cb(void* ctx)
{
  static int flag = 0;
  i64 iTime;
  TrtspInfo* Info = (TrtspInfo*)ctx;
  if (!Info) return 0;

  flag++; if (flag % 10 != 1) return 0;

  iTime = av_gettime();
  if (Info->LastFrameRealtime == 0) Info->LastFrameRealtime = iTime;

  if (iTime - Info->LastFrameRealtime > Info->ConnTimeOut * 1000)//5s超时退出
  {
    Info->LastFrameRealtime = iTime;
    //PostMessage((HWND)1181852, 0x00008888, 1, 1);
    return 1;
  }

  return 0;
}
//-----------------------------------------------------------------------------
HANDLE thrtsp_Init()
{
  TrtspInfo* Info = (TrtspInfo*)malloc(sizeof(TrtspInfo));
  memset(Info, 0, sizeof(TrtspInfo));
  ThreadLockInit(&Info->Lock);
  Info->IsConnect = false;
  Info->videoIndex = -1;
  Info->audioIndex = -1;
  Info->ImgWidth = 640;
  Info->ImgHeight = 480;
  Info->FrameRate = 25;
  return (HANDLE)Info;
}
//-----------------------------------------------------------------------------
int thrtsp_Free(HANDLE Handle)
{
  TrtspInfo* Info = (TrtspInfo*)Handle;
  if (!Info) return false;
  thrtsp_DisConn(Handle, 300);
  ThreadLockFree(&Info->Lock);
  free(Info);
  return true;
}
//-----------------------------------------------------------------------------
int thrtsp_Connect(HANDLE Handle, char* FileName, int iConnTimeOut/*ms*/)
{
  int i, ret;
  TrtspInfo* Info = NULL;

  static char strConnTimeOut[40];
  static char strBufferTime[40];

  __try
  {
    av_register_all();
    avformat_network_init();
    //avdevice_register_all();
    Info = (TrtspInfo*)Handle;
    if (!Info) return false;
    if (Info->IsConnect) return false;
    Info->IsExit = false;

    Info->IsPauseDecode = false;
    sprintf(Info->FileName, FileName);

    Info->IsEof = false;

    Info->ConnTimeOut = iConnTimeOut;

    //if (thrtsp_any0_udp1_tcp2 == 1) av_dict_set(&Info->options, "thrtsp_transport", "udp", 0);
    av_dict_set(&Info->options, "thrtsp_transport", "tcp", 0);
    //if (thrtsp_any0_udp1_tcp2_udpmulticast3_http4 == 3) av_dict_set(&Info->options, "thrtsp_transport", "udp_multicast", 0);
    //if (thrtsp_any0_udp1_tcp2_udpmulticast3_http4 == 4) av_dict_set(&Info->options, "thrtsp_transport", "http", 0);
    //sprintf(strConnTimeOut, "%d", Info->ConnTimeOut * 1000);
    //av_dict_set(&Info->options, "stimeout", strConnTimeOut, 0);//时间单位 us微妙 //有效，放到interrupt_cb中处理了

    Info->pFormatCtx = avformat_alloc_context();
    Info->pFormatCtx->interrupt_callback.callback = interrupt_cb;
    Info->pFormatCtx->interrupt_callback.opaque = Info;

    ret = avformat_open_input(&Info->pFormatCtx, Info->FileName, Info->fmt, &Info->options);//建立网络连接
    ffmpeg_error(ret);
    if (ret == AVERROR_EXIT) goto exits;
    if (ret != 0) goto exits;

    for (i =0; i<Info->pFormatCtx->nb_streams; i++)
    {
      if (Info->pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO) Info->videoIndex = i;
      if (Info->pFormatCtx->streams[i]->codec->codec_type == AVMEDIA_TYPE_AUDIO) Info->audioIndex = i;
    }
    if (Info->videoIndex == -1) goto exits;
    Info->pCodecCtxV = Info->pFormatCtx->streams[Info->videoIndex]->codec;
    if (!Info->pCodecCtxV) goto exits;
//    ret = avformat_find_stream_info(Info->pFormatCtx, NULL);
//    if (ret < 0) goto exits;
    Info->pCodecCtxV->width = Info->pCodecCtxV->width;//==0
    Info->pCodecCtxV->height = Info->pCodecCtxV->height;//==0

    Info->VideoMediaType = Info->pCodecCtxV->codec_id;
    Info->decHandle = thDecodeVideoInit(Info->VideoMediaType);
    Info->RenderHandle = thRender_Init(0);

    Info->hthreadReadFile = ThreadCreate(th_ReadFile, Info, false);

    Info->IsConnect = true;
    return true;

exits:
    thrtsp_DisConn((HANDLE)Info, 300);
    return false;
  }
  __except(1)
  {
    return false;
  }
}
//-----------------------------------------------------------------------------
int thrtsp_DisConn(HANDLE Handle, DWORD sleepmsClose/* = 300 */)
{
  TrtspInfo* Info = (TrtspInfo*)Handle;
  if (!Info) return false;

  __try
  {
    ThreadLock(&Info->Lock);
    Info->avCallback = NULL;
    Info->IsExit = true;
    ThreadUnlock(&Info->Lock);

//    if (sleepmsClose > 0) SleepEX(sleepmsClose);

    if (Info->hthreadReadFile)
    {
      ThreadExit(Info->hthreadReadFile, 300);
      Info->hthreadReadFile = NULL;
    }
    if (Info->pFormatCtx)
    {
      avformat_close_input(&Info->pFormatCtx);
      avformat_free_context(Info->pFormatCtx);
      Info->pFormatCtx = NULL;
    }

    if (Info->decHandle)
    {
      if (thDecodeVideoFree(Info->decHandle))
      {
        Info->decHandle = NULL;
      }    
    }
    if (Info->RenderHandle)
    {
      if (thRender_Free(Info->RenderHandle))
      {
        Info->RenderHandle = NULL;
      }
    }
  }
  __except(1)
  {
  }

  return true;
}
//-----------------------------------------------------------------------------
int thrtsp_SetCallback(HANDLE Handle, TavCallback1 avCallback, const void* UserData)
{
  TrtspInfo* Info = (TrtspInfo*)Handle;
  if (!Info) return false;
  ThreadLock(&Info->Lock);
  Info->avCallback = avCallback;
  Info->UserData = UserData;
  ThreadUnlock(&Info->Lock);
  return true;
}
//-----------------------------------------------------------------------------
unsigned int thrtsp_BufLenTotal(HANDLE Handle)
{
  TrtspInfo* Info = (TrtspInfo*)Handle;
  if (!Info) return false;
  return Info->BufLenTotal;
}
//-----------------------------------------------------------------------------
int thrtsp_IsPauseDecode(HANDLE Handle, int IsPauseDecode)
{
  TrtspInfo* Info = (TrtspInfo*)Handle;
  if (!Info) return false;

  ThreadLock(&Info->Lock);
  Info->IsPauseDecode = IsPauseDecode;
  ThreadUnlock(&Info->Lock);

  return true;
}
//-----------------------------------------------------------------------------
int thrtsp_IsEof(HANDLE Handle)
{
  TrtspInfo* Info = (TrtspInfo*)Handle;
  if (!Info) return false;
  return Info->IsEof;
}
//-----------------------------------------------------------------------------
bool thrtsp_AddDspInfo(HANDLE Handle, TDspInfo* PDspInfo)
{
  int i;
  TrtspInfo* Info = (TrtspInfo*)Handle;
  if (Handle == 0) return false;
  for (i=0; i<MAX_DSPINFO_COUNT; i++)
  {
    if (Info->DspInfoLst[i] == PDspInfo) return true;
    if (Info->DspInfoLst[i] == NULL)
    {
      Info->DspInfoLst[i] = PDspInfo;
      return true;
    }
  }
  return false;
}
//-----------------------------------------------------------------------------
bool thrtsp_DelDspInfo(HANDLE Handle, TDspInfo* PDspInfo)
{
  int i;
  TrtspInfo* Info = (TrtspInfo*)Handle;
  if (Handle == 0) return false;
  for (i=0; i<MAX_DSPINFO_COUNT; i++)
  {
    if (Info->DspInfoLst[i] == PDspInfo)
    {
      Info->DspInfoLst[i] = NULL;
      return true;
    }
  }
  return false;
}

