#ifndef LowNoise_h
#define LowNoise_h

#include "cm_types.h"

#ifdef __cplusplus
extern "C"
{
#endif 

  EXPORT void* LowNoiseInit(int nSample, int nMode, int IsX);//nSample only=8000, 32000 nMode 0: Mild, 1: Medium , 2: Aggressive, IsX=1Âý IsX=0¿ì
  EXPORT bool LowNoiseProcess(void* Handle, char* srcBuf, int srcBufLen, char* dstBuf, int* pdstBufLen);//Ã¿´Î10ms
  EXPORT bool LowNoiseFree(void* Handle);


#ifdef __cplusplus
}
#endif 

#endif 
