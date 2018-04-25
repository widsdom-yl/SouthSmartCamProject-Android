//-----------------------------------------------------------------------------
// Author      : 朱红波
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#ifndef rtspPlay_h
#define rtspPlay_h

#include "cm_types.h"


#ifdef __cplusplus
extern "C"
{
#endif 

  typedef void(* TavCallback1)(HANDLE Handle, void* avFormatInfo, int Status, const void* UserData);

  EXPORT HANDLE thrtsp_Init();
  EXPORT int thrtsp_Connect(HANDLE Handle, char* FileName, int iConnTimeOut/*ms*/);
  EXPORT int thrtsp_DisConn(HANDLE Handle, DWORD sleepmsClose/**/);//=300
  EXPORT int thrtsp_Free(HANDLE Handle);

  EXPORT int thrtsp_SetCallback(HANDLE Handle, TavCallback1 callback, const void* UserData);

  typedef struct TDspInfo {//Player显示信息
    HWND DspHandle;//显示窗口句柄
    int Channel;//通道
    TRect DspRect;//显示区域
  }TDspInfo;

  EXPORT bool thrtsp_AddDspInfo(HANDLE Handle, TDspInfo* PDspInfo);
  EXPORT bool thrtsp_DelDspInfo(HANDLE Handle, TDspInfo* PDspInfo);
  EXPORT int thrtsp_IsPauseDecode(HANDLE Handle, int IsPauseDecode);

  EXPORT int thrtsp_IsEof(HANDLE Handle);
  EXPORT int thrtsp_Play(HANDLE Handle);


#ifdef __cplusplus
}
#endif 

#endif
