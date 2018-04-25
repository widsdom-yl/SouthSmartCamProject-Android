//-----------------------------------------------------------------------------
// Author      : 朱红波
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#ifndef List_h
#define List_h

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

#define MAX_LST_SIZE 4096
typedef struct TList {
  void* FList[MAX_LST_SIZE];
  int FCount;                //统计数量，不能修改
}TList;

EXPORT TList* LstInit();
EXPORT void LstFree(TList* Lst);
EXPORT int LstAdd(TList* Lst, void* Item);
EXPORT void LstClear(TList* Lst);
EXPORT void LstDelete(TList* Lst, int Index);
EXPORT int LstIndexOf(TList* Lst, void* Item);
EXPORT void LstInsert(TList* Lst, int Index, void* Item);    
EXPORT void LstMove(TList* Lst, int CurIndex, int NewIndex);
EXPORT void LstExchange(TList* Lst, int CurIndex, int NewIndex);
EXPORT int LstRemove(TList* Lst, void* Item);
EXPORT void* LstItems(TList* Lst, int Index);
EXPORT int LstCount(TList* Lst);

#ifdef __cplusplus
}
#endif 

#endif
