//-----------------------------------------------------------------------------
// Author      : ÷Ï∫Ï≤®
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#ifndef HttpsPost_h
#define HttpsPost_h

#include "cm_types.h"

//-----------------------------------------------------------------------------
#ifdef __cplusplus
extern "C"
{
#endif 

  EXPORT int HttpsPost(char* url/*'https://xxx.xx.xx'*/, char* UserName, char* Password, char* SendData, int SendDataLen, char* RecvData, int RecvDataSize);

#ifdef __cplusplus
}
#endif 

#endif
