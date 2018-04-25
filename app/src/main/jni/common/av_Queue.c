//-----------------------------------------------------------------------------
// Author      : Öìºì²¨
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#include "../include/av_Queue.h"
#include "../include/TFun.h"

typedef struct TavQueue {
  unsigned int iQueueNum;
  unsigned int IsMallocMemory;
  unsigned int IsMallocMemory1;
  H_THREADLOCK Lock;
  unsigned int iCount;
  unsigned int IdxRead;
  unsigned int IdxWrite;
  TavNode* Lst;
}TavQueue;
//-----------------------------------------------------------------------------
void* avQueue_Init(int iQueueNum, int IsMallocMemory, int IsMallocMemory1)
{
  TavQueue* avQueue = (TavQueue*)malloc(sizeof(TavQueue));
  memset(avQueue, 0, sizeof(TavQueue));
  avQueue->iQueueNum = iQueueNum;
  avQueue->Lst = (TavNode*)malloc(sizeof(TavNode) * avQueue->iQueueNum);
  memset(avQueue->Lst, 0, sizeof(TavNode) * avQueue->iQueueNum);

  avQueue->IsMallocMemory = IsMallocMemory;
  avQueue->IsMallocMemory1 = IsMallocMemory1;
  ThreadLockInit(&avQueue->Lock);
  return (void*)avQueue;
}
//-----------------------------------------------------------------------------
void avQueue_Free(void* h)
{
  int i;
  TavNode* tmpNode;
  TavQueue* avQueue = (TavQueue*)h;
  ThreadLock(&avQueue->Lock);
  for (i=0; i<avQueue->iQueueNum; i++)
  {
    tmpNode = &avQueue->Lst[i];
    if (avQueue->IsMallocMemory) if (tmpNode->Buf) free(tmpNode->Buf);
    tmpNode->Buf = NULL;
    tmpNode->BufLen = 0;

    if (avQueue->IsMallocMemory1) if (tmpNode->Buf1) free(tmpNode->Buf1);
    tmpNode->Buf1 = NULL;
    tmpNode->BufLen1 = 0;
  }
  free(avQueue->Lst);
  ThreadUnlock(&avQueue->Lock);
  ThreadLockFree(&avQueue->Lock);
  free(avQueue);
}
//-----------------------------------------------------------------------------
int avQueue_GetCount(void* h)
{
  int Result = 0;
  TavQueue* avQueue = (TavQueue*)h;
  if (!avQueue) return 0;
  ThreadLock(&avQueue->Lock);
  Result = avQueue->iCount;
  ThreadUnlock(&avQueue->Lock);
  return Result;
}
//-----------------------------------------------------------------------------
TavNode* avQueue_Write(void* h, char* Buf, int BufLen, char* Buf1, int BufLen1)
{
  TavNode* tmpNode;
  TavQueue* avQueue = (TavQueue*)h;
  if (!avQueue) return NULL;

  ThreadLock(&avQueue->Lock);
  if (avQueue->IdxWrite >= avQueue->iQueueNum) goto exits;
  tmpNode = &avQueue->Lst[avQueue->IdxWrite];
  if (tmpNode->Buf != NULL) goto exits;

  if (avQueue->IsMallocMemory && Buf)
  {
    tmpNode->Buf = malloc(BufLen + 1);
    tmpNode->Buf[BufLen] = 0x00;
    memcpy(tmpNode->Buf, Buf, BufLen);
    tmpNode->BufLen = BufLen;
  }
  else
  {
    tmpNode->Buf = Buf;
    tmpNode->BufLen = BufLen;
  }

  if (avQueue->IsMallocMemory1 && Buf1)
  {
    tmpNode->Buf1 = malloc(BufLen1 + 1);
    tmpNode->Buf1[BufLen1] = 0x00;
    memcpy(tmpNode->Buf1, Buf1, BufLen1);
    tmpNode->BufLen1 = BufLen1;
  }
  else
  {
    tmpNode->Buf1 = Buf1;
    tmpNode->BufLen1 = BufLen1;
  }

  avQueue->IdxWrite++;
  if (avQueue->IdxWrite >= avQueue->iQueueNum) avQueue->IdxWrite = 0;
  if (avQueue->iCount < avQueue->iQueueNum) avQueue->iCount++;
  ThreadUnlock(&avQueue->Lock);

  return tmpNode;

exits:
  ThreadUnlock(&avQueue->Lock);
  return NULL;
}
//-----------------------------------------------------------------------------
TavNode* avQueue_ReadBegin(void* h)
{
  TavNode* tmpNode;
  TavQueue* avQueue = (TavQueue*)h;
  if (!avQueue) return NULL;

  ThreadLock(&avQueue->Lock);
  if (avQueue->IdxRead >= avQueue->iQueueNum) goto exits;
  if (avQueue->iCount == 0) goto exits;
  tmpNode = &avQueue->Lst[avQueue->IdxRead];
  if (tmpNode->Buf == NULL) goto exits;
  avQueue->IdxRead++;
  if (avQueue->IdxRead >= avQueue->iQueueNum) avQueue->IdxRead = 0;
  avQueue->iCount--;
  if (avQueue->iCount < 0) avQueue->iCount = 0;
  ThreadUnlock(&avQueue->Lock);
  return tmpNode;

exits:
  ThreadUnlock(&avQueue->Lock);
  return NULL;
}
//-----------------------------------------------------------------------------
void avQueue_ReadEnd(void* h, TavNode* tmpNode)
{
  TavQueue* avQueue = (TavQueue*)h;
  if (!avQueue) return;
  if (!tmpNode) return;
  ThreadLock(&avQueue->Lock);
  if (avQueue->IsMallocMemory)  if (tmpNode->Buf)  free(tmpNode->Buf);
  if (avQueue->IsMallocMemory1) if (tmpNode->Buf1) free(tmpNode->Buf1);
  tmpNode->Buf = NULL;
  tmpNode->BufLen = 0;
  tmpNode->Buf1 = NULL;
  tmpNode->BufLen1 = 0;
  ThreadUnlock(&avQueue->Lock);
}
//-----------------------------------------------------------------------------
void avQueue_ClearAll(void* h)
{
  int i;
  TavNode* tmpNode;
  TavQueue* avQueue = (TavQueue*)h;
  if (!avQueue) return;
  ThreadLock(&avQueue->Lock);

  for (i=0; i<avQueue->iQueueNum; i++)
  {
    tmpNode = &avQueue->Lst[i];
    if (avQueue->IsMallocMemory) if (tmpNode->Buf) free(tmpNode->Buf);
    if (avQueue->IsMallocMemory1) if (tmpNode->Buf1) free(tmpNode->Buf1);
    tmpNode->Buf = NULL;
    tmpNode->BufLen = 0;
    tmpNode->Buf1 = NULL;
    tmpNode->BufLen1 = 0;
  }
  avQueue->iCount = 0;
  avQueue->IdxRead = 0;
  avQueue->IdxWrite = 0;

  ThreadUnlock(&avQueue->Lock);
}
