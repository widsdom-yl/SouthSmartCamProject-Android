//-----------------------------------------------------------------------------
// Author      : 朱红波
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#include "List.h"

//---------------------------------------------------------------------------创建
TList* LstInit()
{
  TList* Lst = (TList*)malloc(sizeof(TList));
  memset(Lst, 0, sizeof(TList));
  return Lst;
}
//---------------------------------------------------------------------------释放
void LstFree(TList* Lst)
{
  LstClear(Lst);
}
//---------------------------------------------------------------------------统计数量
int LstCount(TList* Lst)
{
  return Lst->FCount;
}
//---------------------------------------------------------------------------增加
int LstAdd(TList* Lst, void* Item)
{
  int Result;
  if (Lst->FCount >= MAX_LST_SIZE) return -1;

  Result = Lst->FCount;
  Lst->FList[Result] = Item;
  Lst->FCount++;
  return Result;
}
//---------------------------------------------------------------------------清除所有
void LstClear(TList* Lst)
{
  memset(Lst, 0, sizeof(TList));
}
//---------------------------------------------------------------------------删除
void LstDelete(TList* Lst, int Index)
{
  if ((Index < 0) || (Index >= Lst->FCount)) return;
  Lst->FCount--;
  if (Index < Lst->FCount)
  {
    memcpy(&Lst->FList[Index], &Lst->FList[Index + 1], (Lst->FCount - Index) * sizeof(void*));
  }
}
//---------------------------------------------------------------------------索引
int LstIndexOf(TList* Lst, void* Item)
{
  int Result = 0;

  while ((Result < Lst->FCount) && (Lst->FList[Result] != Item))
  {
    Result++;
  }
  if (Result == Lst->FCount)
    Result = -1;
  return Result;
}

//---------------------------------------------------------------------------插入
void LstInsert(TList* Lst, int Index, void* Item)
{
  if ((Index < 0) || (Index > Lst->FCount)) return;

  if (Index < Lst->FCount)
  {
    memcpy(&Lst->FList[Index + 1], &Lst->FList[Index], (Lst->FCount - Index) * sizeof(void*));
  }
  Lst->FList[Index] = Item;
  Lst->FCount++;
}
//---------------------------------------------------------------------------交换
void LstExchange(TList* Lst, int CurIndex, int NewIndex)
{
  void* tmp = Lst->FList[CurIndex];
  Lst->FList[CurIndex] = Lst->FList[NewIndex];
  Lst->FList[NewIndex] = tmp;
}
//---------------------------------------------------------------------------移动
void LstMove(TList* Lst, int CurIndex, int NewIndex)
{
  void* Item;
  if (CurIndex != NewIndex)
  {
    if ((NewIndex < 0) || (NewIndex >= Lst->FCount)) return;
    Item = Lst->FList[CurIndex];
    Lst->FList[CurIndex] = NULL;
    LstDelete(Lst, CurIndex);
    LstInsert(Lst, NewIndex, NULL);
    Lst->FList[NewIndex] = Item;
  }
}
//---------------------------------------------------------------------------删除
int LstRemove(TList* Lst, void* Item)
{
  int Result = LstIndexOf(Lst, Item);
  if (Result >= 0)
    LstDelete(Lst, Result);
  return Result;
}

//---------------------------------------------------------------------------序列节点
void* LstItems(TList* Lst, int Index)
{
  if(Index<0) return NULL;
  if(Index>=Lst->FCount) return NULL;
  return Lst->FList[Index];
}
//-------------------------------------------------------------------------
