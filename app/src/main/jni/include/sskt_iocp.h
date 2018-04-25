//-----------------------------------------------------------------------------
// Author      : 朱红波
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#ifndef sskt_iocp_h
#define sskt_iocp_h

#ifdef WIN32
#include <winsock2.h>
#include <windows.h>
#include "TFun.h"
#include "List.h"

#ifndef EXPORT
#ifdef WIN32
#define EXPORT __declspec(dllexport)
#else
#define EXPORT
#endif
#endif

#pragma comment(lib,"ws2_32.lib")
#ifdef __cplusplus
extern "C"
{
#endif

  //-----------------------------------------------------------------------------服务器
#define MAX_BUFSIZE 1024*64        //数据缓冲区大小  
  //-------------------------------------
  typedef enum TSocketEvent{
    seInitIOPort, 
    seInitSocket,
    seConnect,
    seDisconnect,
    seListen,
    seAccept,
    seWrite,
    seRead,
  }TSocketEvent;
  //-------------------------------------
  typedef struct TPerHandleData{
    OVERLAPPED Overlapped; //重叠结构  
    char buf[MAX_BUFSIZE]; //数据缓冲区  
    TSocketEvent Event;    //操作类型  
  }TPerHandleData;
  //-------------------------------------
  typedef struct TcltNode{
    SOCKET hSocket; //套接字句柄  
    struct sockaddr_in cAddr; //对方的地址  
    char FromIP[16];
    int FromPort;
    char UserID[100];
    TPerHandleData Block;
    WSABUF wsaBuffer;
  } TcltNode;
  //-----------------------------------------------------------------------------服务器
  typedef void(__cdecl* TOnConnNotify)(const HANDLE Handle, void* Sender, TcltNode* cltNode, char* FromIP, int FromPort); //连接事件
  typedef void(__cdecl* TOnDisConnNotify)(const HANDLE Handle, void* Sender, TcltNode* cltNode, char* FromIP, int FromPort); //断开事件
  typedef void(__cdecl* TOnRecvNotify)(const HANDLE Handle, void* Sender, TcltNode* cltNode, char* FromIP, int FromPort, char* Buf, int BufLen); //收取事件

  EXPORT HANDLE __cdecl iocp_sskt_Init(int ServerPort, TOnConnNotify OnConnNotify, TOnDisConnNotify OnDisConnNotify, TOnRecvNotify OnRecvNotify, void* Sender);
  EXPORT bool __cdecl iocp_sskt_Free(HANDLE Handle);
  EXPORT bool __cdecl iocp_sskt_SendBuf(HANDLE FromHandle, char* Buf, int BufLen);
  //-----------------------------------------------------------------------------服务器

#ifdef __cplusplus
}
#endif 
#endif
#endif
