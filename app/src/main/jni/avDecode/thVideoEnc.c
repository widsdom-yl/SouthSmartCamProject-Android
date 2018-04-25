//-----------------------------------------------------------------------------
// Author      : Öìºì²¨
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#include "thffmpeg.h"
#include "../include/avDecode.h"
//http://blog.csdn.net/leixiaohua1020/article/category/1360795
//-----------------------------------------------------------------------------
typedef struct TencInfoPkt {
  AVCodecContext* pCodecCtxV;
  AVPacket packet;
  AVFrame* FrameV;
}TencInfoPkt;

//-----------------------------------------------------------------------------
HANDLE thEncodeVideoInit(TencParam* Param)
{
  AVCodec* CodecV;
  TencInfoPkt* Info = NULL;
  Info = (TencInfoPkt*)malloc(sizeof(TencInfoPkt)); 
  memset(Info, 0, sizeof(TencInfoPkt));

  av_register_all();
  avcodec_register_all();

  CodecV = avcodec_find_encoder(Param->VideoType);
  if (CodecV == NULL) goto exits;

  Info->pCodecCtxV = avcodec_alloc_context3(CodecV);
  if (Info->pCodecCtxV == NULL) goto exits;

  Info->pCodecCtxV->codec_type = AVMEDIA_TYPE_VIDEO;
  Info->pCodecCtxV->codec_id = Param->VideoType;

  Info->pCodecCtxV->max_b_frames = 0;
  Info->pCodecCtxV->pre_me = 2;
  Info->pCodecCtxV->lmin = 10;
  Info->pCodecCtxV->lmax = 50;
  Info->pCodecCtxV->qmin = 10;
  Info->pCodecCtxV->qmax = 51;
  Info->pCodecCtxV->qblur = 0.0;
  Info->pCodecCtxV->spatial_cplx_masking = (float)0.3;
  Info->pCodecCtxV->me_pre_cmp = 2;
  Info->pCodecCtxV->rc_qsquish = 1;
  Info->pCodecCtxV->b_quant_factor = (float)4.9;
  Info->pCodecCtxV->b_quant_offset = 2;
  Info->pCodecCtxV->i_quant_factor = (float)0.1;
  Info->pCodecCtxV->i_quant_offset = (float)0.0;
  Info->pCodecCtxV->rc_strategy = 2;
  Info->pCodecCtxV->b_frame_strategy = 0;
  Info->pCodecCtxV->dct_algo = 0;
  Info->pCodecCtxV->lumi_masking = (float)0.0;
  Info->pCodecCtxV->dark_masking = (float)0.0;

  Info->pCodecCtxV->width = Param->encWidth;
  Info->pCodecCtxV->height = Param->encHeight;
  Info->pCodecCtxV->time_base.den = Param->FrameRate;//Ö¡ÂÊ
  Info->pCodecCtxV->time_base.num = 1;
  Info->pCodecCtxV->pix_fmt = AV_PIX_FMT_YUV420P;
  Info->pCodecCtxV->gop_size = Param->IPInterval;
  Info->pCodecCtxV->bit_rate = Param->BitRate;
  Info->pCodecCtxV->bit_rate_tolerance = Param->BitRate;

//CBR
#if 1
  Info->pCodecCtxV->bit_rate = Param->BitRate;
  Info->pCodecCtxV->rc_min_rate =Param->BitRate;
  Info->pCodecCtxV->rc_max_rate = Param->BitRate; 
  Info->pCodecCtxV->bit_rate_tolerance = Param->BitRate;
  Info->pCodecCtxV->rc_buffer_size=Param->BitRate;
  Info->pCodecCtxV->rc_initial_buffer_occupancy = Info->pCodecCtxV->rc_buffer_size*3/4;
  Info->pCodecCtxV->rc_buffer_aggressivity= (float)1.0;
  Info->pCodecCtxV->rc_initial_cplx= (float)0.5; 
  Info->pCodecCtxV->qcompress = (float)0.0;
  Info->pCodecCtxV->qmin = 10;
  Info->pCodecCtxV->qmax = 51;
#else
//VBR
  Info->pCodecCtxV->flags |= CODEC_FLAG_QSCALE;
  Info->pCodecCtxV->qmin =10;
  Info->pCodecCtxV->qmax = 51;
  Info->pCodecCtxV->bit_rate = Param->BitRate;
  Info->pCodecCtxV->rc_min_rate =Param->BitRate*1/4;
  Info->pCodecCtxV->rc_max_rate = Param->BitRate*4/3;  
#endif

//·ÀÖ¹±àÂëÑÓ³ÙÐÞ¸Ä²ÎÊý
  av_opt_set(Info->pCodecCtxV->priv_data, "preset", "superfast", 0);  
  av_opt_set(Info->pCodecCtxV->priv_data, "tune", "zerolatency", 0);

  if (avcodec_open2(Info->pCodecCtxV, CodecV, NULL) < 0) goto exits;
  Info->FrameV = av_frame_alloc();
  Info->FrameV->pts = 0;

  return (HANDLE)Info;

exits:
  thEncodeVideoFree((HANDLE)Info);
  return NULL;
}
//-----------------------------------------------------------------------------
bool thEncodeVideoFrame(HANDLE encHandle, TencParam* Param)
{
  char tmpBuf[1000];
  int ret = 0;
  int got_picture = 0;
  TencInfoPkt* Info = (TencInfoPkt*)encHandle;
  Param->encLen = 0;
  if (!Info) return false;

  Info->FrameV->data[0] = (uint8_t*)Param->yuvbuf;
  Info->FrameV->data[1] = (uint8_t*)Param->yuvbuf + Param->encWidth * Param->encHeight;
  Info->FrameV->data[2] = (uint8_t*)Param->yuvbuf + Param->encWidth * Param->encHeight * 5 / 4;
  Info->FrameV->linesize[0] =  Param->encWidth;
  Info->FrameV->linesize[1] =  Param->encWidth / 2;
  Info->FrameV->linesize[2] =  Param->encWidth / 2;

  Info->FrameV->pict_type = AV_PICTURE_TYPE_NONE;//pict_type = 1 mpeg4Ç¿ÖÆIÖ¡
  if (Param->ForceKeyFrame)
  {
    Info->FrameV->pict_type = AV_PICTURE_TYPE_I;//pict_type = 1 mpeg4Ç¿ÖÆIÖ¡
    Param->ForceKeyFrame = 0;
  }

  //FrameV.pts = av_gettime();
  Info->FrameV->pts = Info->FrameV->pts + 1;// GetTickCount();

  av_init_packet(&Info->packet);

  ret = avcodec_encode_video2(Info->pCodecCtxV, &Info->packet, Info->FrameV, &got_picture);  
  if (ret >= 0 && got_picture)
  { 
    Param->encLen = Info->packet.size;
    memcpy(Param->encBuf, (char*)Info->packet.data, Param->encLen);

    Param->IsKeyFrame = Info->pCodecCtxV->coded_frame->key_frame;
    sprintf(tmpBuf, "Key:%d encLen:%d\n", Param->IsKeyFrame, Param->encLen);
    av_packet_unref(&Info->packet);
    ////av_free_packet(&Info->packet);
    return true;
  }
  return false;
}
//-----------------------------------------------------------------------------
bool thEncodeVideoFree(HANDLE encHandle)
{
  TencInfoPkt* Info = (TencInfoPkt*)encHandle;
  if (!Info) return false;
//  av_packet_unref(&Info->packet);
  if (Info->pCodecCtxV) 
  {
    avcodec_close(Info->pCodecCtxV);
    Info->pCodecCtxV = NULL;
  }
  if (Info->FrameV)
  {
    av_free(Info->FrameV);
  }

  memset(Info, 0, sizeof(TencInfoPkt));
  free(Info);
  return true;
}
//-----------------------------------------------------------------------------
