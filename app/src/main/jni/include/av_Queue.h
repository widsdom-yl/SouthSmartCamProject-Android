//-----------------------------------------------------------------------------
// Author      : ÷Ï∫Ï≤®
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#ifndef av_Queue_h
#define av_Queue_h

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#ifndef EXPORT
#ifdef WIN32
#define EXPORT __declspec(dllexport)
#else
#define EXPORT
#endif
#endif

#ifdef __cplusplus
extern "C"
{
#endif 
//-----------------------------------------------------------------------------
typedef struct TavNode {
  char* Buf;
  unsigned int BufLen;
  char* Buf1;
  unsigned int BufLen1;
}TavNode;

//-----------------------------------------------------------------------------
EXPORT void* avQueue_Init(int iQueueNum, int IsMallocMemory, int IsMallocMemory1);
EXPORT int avQueue_GetCount(void* h);
EXPORT TavNode* avQueue_Write(void* h, char* Buf, int BufLen, char* Buf1, int BufLen1);
EXPORT TavNode* avQueue_ReadBegin(void* h);
EXPORT void avQueue_ReadEnd(void* h, TavNode* tmpNode);
EXPORT void avQueue_ClearAll(void* h);
EXPORT void avQueue_Free(void* h);

#ifdef __cplusplus
}
#endif 

#endif
