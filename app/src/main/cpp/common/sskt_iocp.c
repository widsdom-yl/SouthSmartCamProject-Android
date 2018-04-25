//-----------------------------------------------------------------------------
// Author      : 朱红波
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#include "../include/sskt_iocp.h"
#include "../include/TFun.h"
#include "List.h"

typedef struct TSSktInfo
{
  int IsExit;
  void* Sender;
  TOnConnNotify OnConnNotify;
  TOnDisConnNotify OnDisConnNotify;
  TOnRecvNotify OnRecvNotify;
  int ServerPort;
  TList* ConnLst;
  H_THREAD hthread_accept;
  int iCountIOCP;
  H_THREAD hthread_iocp[16];
  H_THREADLOCK Lock;
  HANDLE hIOCP;
  int hSocketSvr;
}TSSktInfo;

#ifdef WIN32
//-----------------------------------------------------------------------------
static void* thread_IOCP(void* Param)
{
  TSSktInfo* Info = (TSSktInfo*)Param;
  int ret;
  DWORD BufLen = 0;
  TPerHandleData* PBlock = NULL;
  TcltNode* cltNode = NULL;
  DWORD RBytes, Flags; 

  while (true)
  {
    if (Info->IsExit) break;

    ret = GetQueuedCompletionStatus(Info->hIOCP, &BufLen, (DWORD*)&cltNode, (LPOVERLAPPED*)&PBlock, INFINITE);
    if ((ret == 0 || BufLen == 0) && (cltNode->Block.Event == seRead || cltNode->Block.Event == seRead))
    {
      if (Info->OnDisConnNotify) Info->OnDisConnNotify(Info, Info->Sender, cltNode, cltNode->FromIP, cltNode->FromPort);
      closesocket(cltNode->hSocket);
      LstRemove(Info->ConnLst, cltNode);
      free(cltNode);
      continue;
    }
    if (ret == 1 && BufLen == 0xFFFFFFFF) break;

    switch (cltNode->Block.Event)
    {
    case seRead:
      cltNode->Block.buf[BufLen] = 0x00;
      cltNode->wsaBuffer.buf = cltNode->Block.buf;
      cltNode->wsaBuffer.len = MAX_BUFSIZE;
      cltNode->Block.Event = seRead;
      RBytes = 0;
      Flags = 0;
      WSARecv(cltNode->hSocket, &cltNode->wsaBuffer, 1, &RBytes, &Flags, &cltNode->Block.Overlapped, NULL);

      if (Info->OnRecvNotify)
      {
        Info->OnRecvNotify(Info, Info->Sender, cltNode, cltNode->FromIP, cltNode->FromPort, cltNode->Block.buf, BufLen);
      }
      break;

    case seWrite://内容同上
      cltNode->Block.buf[BufLen] = 0x00;
      cltNode->wsaBuffer.buf = cltNode->Block.buf;
      cltNode->wsaBuffer.len = MAX_BUFSIZE;
      cltNode->Block.Event = seRead;
      RBytes = 0;
      Flags = 0;
      WSARecv(cltNode->hSocket, &cltNode->wsaBuffer, 1, &RBytes, &Flags, &cltNode->Block.Overlapped, NULL);
      break;
    }
  }
  return NULL;
}
//-------------------------------------------------------------------------
static void* thread_accept(void* Param)
{
  TSSktInfo* Info = (TSSktInfo*)Param;
  SOCKADDR_IN cAddr;
  SOCKET hSocketClt;
  TcltNode* cltNode;
  WSABUF wsaBuffer;
  DWORD dRecv = 0;
  DWORD dFlag = 0;
  int iAddrLen = sizeof(SOCKADDR);

  while (1)
  {
    if (Info->IsExit) break;

    hSocketClt = accept(Info->hSocketSvr, (SOCKADDR*) &cAddr, &iAddrLen);
    if (hSocketClt == SOCKET_ERROR) break;
    //connect
    cltNode = (TcltNode*)malloc(sizeof(TcltNode));
    memset(cltNode, 0, sizeof(TcltNode));
    cltNode->hSocket = hSocketClt;
    memcpy(&cltNode->cAddr, &cAddr, iAddrLen);
    LstAdd(Info->ConnLst, cltNode);

    GetIPPortFromAddr(cltNode->cAddr, cltNode->FromIP, &cltNode->FromPort);
    if (Info->OnConnNotify) Info->OnConnNotify(Info, Info->Sender, cltNode, cltNode->FromIP, cltNode->FromPort);

    CreateIoCompletionPort((HANDLE)cltNode->hSocket, Info->hIOCP, (DWORD)cltNode, 0);

    memset(&cltNode->Block.Overlapped, 0, sizeof(OVERLAPPED));

#if 1//accept后 预收
    cltNode->Block.Event = seRead;
    wsaBuffer.buf = cltNode->Block.buf;
    wsaBuffer.len = MAX_BUFSIZE;
    WSARecv(cltNode->hSocket, &wsaBuffer, 1, &dRecv, &dFlag, &cltNode->Block.Overlapped, NULL);
#endif
  }
  return NULL;
}
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
TcltNode* svr_IndexOfhSocket(TList* lst, int hSocket)
{
  TcltNode* tmpNode;
  int i;
  for (i=0; i<LstCount(lst); i++)
  {
    tmpNode = (TcltNode*)LstItems(lst, i);
    if (tmpNode->hSocket == hSocket)
    {
      return tmpNode;
    }
  }
  return NULL;
}
//-----------------------------------------------------------------------------
HANDLE __cdecl iocp_sskt_Init(int ServerPort, TOnConnNotify OnConnNotify, TOnDisConnNotify OnDisConnNotify, TOnRecvNotify OnRecvNotify, void* Sender)
{
  int i, ret;
  SYSTEM_INFO SystemInfo;
  SOCKADDR_IN sAddr;
  SOCKET hSocketSvr;
  TSSktInfo* Info;
  WSADATA wsaData; 
  WSAStartup(MAKEWORD(2, 2), &wsaData);

  hSocketSvr = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
  sAddr.sin_addr.S_un.S_addr = htonl(INADDR_ANY);
  sAddr.sin_family = AF_INET;
  sAddr.sin_port = htons(ServerPort);
  ret = bind(hSocketSvr, (SOCKADDR*)&sAddr, sizeof(SOCKADDR));
  if (ret < 0) goto exits;
  ret = listen(hSocketSvr, 5);
  if (ret < 0) goto exits;

  Info = (TSSktInfo*)malloc(sizeof(TSSktInfo));
  memset(Info, 0, sizeof(TSSktInfo));
  ThreadLockInit(&Info->Lock);
  Info->ConnLst = LstInit();
  Info->ServerPort = ServerPort;
  Info->Sender = Sender;
  Info->OnConnNotify = OnConnNotify;
  Info->OnDisConnNotify = OnDisConnNotify;
  Info->OnRecvNotify = OnRecvNotify;
  Info->IsExit = false;

  Info->hSocketSvr = hSocketSvr;

  Info->hthread_accept = ThreadCreate(thread_accept, Info, false);

  Info->hIOCP = CreateIoCompletionPort(INVALID_HANDLE_VALUE, NULL, 0, 0);

  GetSystemInfo(&SystemInfo);  
  Info->iCountIOCP = SystemInfo.dwNumberOfProcessors * 2;
  Info->iCountIOCP = 1;
  for (i=0; i<Info->iCountIOCP; i++)//CPU数量
  {
    Info->hthread_iocp[i] = ThreadCreate(thread_IOCP, Info, false);
  }

  return (HANDLE)Info;

exits:
  closesocket(hSocketSvr);
  hSocketSvr = 0;
  return NULL;
}
//-----------------------------------------------------------------------------
bool __cdecl iocp_sskt_Free(HANDLE Handle)
{
  int i;
  TSSktInfo* Info = (TSSktInfo*)Handle;
  if (!Info) return false;

  ThreadLock(&Info->Lock);
  shutdown(Info->hSocketSvr, 2);
  closesocket(Info->hSocketSvr);
  Info->hSocketSvr = 0;
  ThreadUnlock(&Info->Lock);

  ThreadLock(&Info->Lock);
  Info->IsExit = true;
  ThreadUnlock(&Info->Lock);
  PostQueuedCompletionStatus(Info->hIOCP, 0xFFFFFFFF, 0, NULL);

  for (i=0; i<Info->iCountIOCP; i++)
  {
    ThreadExit(Info->hthread_iocp[i], 3000);//已关闭Handle
    Info->hthread_iocp[i] = 0;
  }

  ThreadExit(Info->hthread_accept, 3000);//已关闭Handle
  Info->hthread_accept = 0;

  CloseHandle(Info->hIOCP);
  Info->hIOCP = 0;

  ThreadLockFree(&Info->Lock);

  LstFree(Info->ConnLst);

  free(Info);

  WSACleanup();
  return true;
}
//-----------------------------------------------------------------------------
bool __cdecl iocp_sskt_SendBuf(HANDLE FromHandle, char* Buf, int BufLen)
{
  int ret;
  DWORD SBytes;
  TcltNode* cltNode = (TcltNode*)FromHandle;
  if (!cltNode) return false;
  cltNode->wsaBuffer.buf = Buf;  
  cltNode->wsaBuffer.len = BufLen;
  cltNode->Block.Event = seWrite;  
  SBytes = 0;
  ret = WSASend(cltNode->hSocket, &cltNode->wsaBuffer, 1, &SBytes, 0, &cltNode->Block.Overlapped, NULL); 
  return (ret == 0);
}
//-----------------------------------------------------------------------------
#endif
