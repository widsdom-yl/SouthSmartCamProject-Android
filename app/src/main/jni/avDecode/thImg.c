//-----------------------------------------------------------------------------
// Author      : Öìºì²¨
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#include "thffmpeg.h"
#include "../include/avDecode.h"
//-----------------------------------------------------------------------------
bool thImgConvertScale(char* d, int dfmt, int dw, int dh, char* s, int sfmt, int sw, int sh, int IsFlip)
{
  AVPicture dst = {0};
  AVPicture src = {0};

#ifdef __cplusplus
  avpicture_fill(&dst, (uint8_t*)d, (AVPixelFormat)dfmt, dw, dh);
  avpicture_fill(&src, (uint8_t*)s, (AVPixelFormat)sfmt, sw, sh);
#else
  avpicture_fill(&dst, (uint8_t*)d, dfmt, dw, dh);
  avpicture_fill(&src, (uint8_t*)s, sfmt, sw, sh);
#endif
  return thImgConvertScale1(
    (TavPicture*)&dst, dfmt, dw, dh, 
    (TavPicture*)&src, sfmt, sw, sh,
    IsFlip);
}
//-----------------------------------------------------------------------------
bool thImgConvertScale1(TavPicture* dst, int dfmt, int dw, int dh, TavPicture* src, int sfmt, int sw, int sh, int IsFlip)
{
  struct SwsContext* pSwsCtx;
  pSwsCtx = sws_getContext(sw, sh, sfmt, dw, dh, dfmt, SWS_BICUBIC, NULL, NULL, NULL);

  if (IsFlip)
  {
    src->data[0] += src->linesize[0] * (sh - 1);
    src->linesize[0] *= -1;
    src->data[1] += src->linesize[1] * (sh / 2 - 1);
    src->linesize[1] *= -1;
    src->data[2] += src->linesize[2] * (sh / 2 - 1);
    src->linesize[2] *= -1;
  }

  sws_scale(pSwsCtx, src->data, src->linesize, 0, sh, dst->data, dst->linesize);
  sws_freeContext(pSwsCtx);
  return true;
}
//-----------------------------------------------------------------------------
bool thImgConvertScale2(char* d, int dfmt, int dw, int dh, TavPicture* src, int sfmt, int sw, int sh, int IsFlip)
{
  struct SwsContext* sws;
  AVPicture dst = {0};

#ifdef __cplusplus
  avpicture_fill(&dst, (uint8_t*)d, (AVPixelFormat)dfmt, dw, dh);
#else
  avpicture_fill(&dst, (uint8_t*)d, dfmt, dw, dh);
#endif

  if (IsFlip)
  {
    src->data[0] += src->linesize[0] * (sh - 1);
    src->linesize[0] *= -1;
    src->data[1] += src->linesize[1] * (sh / 2 - 1);
    src->linesize[1] *= -1;
    src->data[2] += src->linesize[2] * (sh / 2 - 1);
    src->linesize[2] *= -1;
  }

  sws = sws_getContext(sw, sh, sfmt, dw, dh, dfmt, SWS_BICUBIC, NULL, NULL, NULL);
  sws_scale(sws, &src->data, src->linesize, 0, sh, &dst.data, dst.linesize);
  sws_freeContext(sws);
  return true;
}
//-----------------------------------------------------------------------------
int thImgConvertFill(TavPicture* pFrame, char* Buf, int fmt, int Width, int Height)
{
  return avpicture_fill((AVPicture*)pFrame, (uint8_t*)Buf, fmt, Width, Height);
}

