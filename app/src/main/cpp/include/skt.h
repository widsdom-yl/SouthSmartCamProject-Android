//-----------------------------------------------------------------------------
// Author      : ��첨
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#ifndef TSSkt_H
#define TSSkt_H

#include "../include/TFun.h"

#ifdef __cplusplus
extern "C"
{
#endif 
//-----------------------------------------------------------------------------
typedef void(*TOnUDPRecvEvent)(void* Sender, char* Buf, i32 BufLen);//must
typedef struct TudpParam
{
  H_THREADLOCK Lock;
  u32 SearchIPLst[512];
  void* UserCustom;
  TOnUDPRecvEvent OnRecvEvent;
  i32 Port;
  i32 IsExitUDP;
  char* LocalIP;
  char* MultiIP;
  int IsMulticast;
  int TTL;
  struct sockaddr_in FromAddr;
  H_THREAD tHandle; //SSkt�߳̾��
  int SocketHandle;
  THandle Flag;

  void* Data;

}TudpParam;

EXPORT bool udp_Init(TudpParam* Param);
EXPORT bool udp_Free(TudpParam* Param);

#ifdef __cplusplus
}
#endif 

#endif
