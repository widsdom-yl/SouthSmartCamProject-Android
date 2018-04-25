//-----------------------------------------------------------------------------
// Author      : ÷Ï∫Ï≤®
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#ifndef thDDraw_h
#define thDDraw_h

#include "../include/avDecode.h"

#ifdef __cplusplus
extern "C"
{
#endif

  EXPORT HANDLE thDDraw_Init(int iDDrawMode/*0=VIDEOMEMORY 1=SYSTEMMEMORY*/);
  EXPORT bool thDDraw_FillMem(HANDLE Handle, TavPicture FrameV, int ImgWidth, int ImgHeight);
  EXPORT bool thDDraw_Display(HANDLE Handle, HWND DspHandle, TRect DspRect);
  EXPORT bool thDDraw_Free(HANDLE Handle);

  bool thDDraw_InitPrimaryScreen(HANDLE Handle);
  bool thDDraw_FreePrimaryScreen(HANDLE Handle);
  bool thDDraw_InitOffScreen(HANDLE Handle, int w, int h);
  bool thDDraw_FreeOffScreen(HANDLE Handle);
  bool thDDraw_SetDspHandle(HANDLE Handle, HWND DspHandle);
  bool thDDraw_ShowText(HANDLE Handle, char* Text, int x, int y, int FontSize, int Color);
  bool thDDraw_ShowPicture(HANDLE Handle, int x, int y, char* Text, int Color);
  bool thDDraw_SaveToBmp(HANDLE Handle, char* AFileName);
  bool thDDraw_SaveToJpg(HANDLE Handle, char* AFileName);
  HDC thDDraw_GetDC(HANDLE Handle);
  bool thDDraw_ReleaseDC(HANDLE Handle, HDC dc);
  bool thDDraw_SetOffScreenSize(HANDLE Handle, int w, int h);
  void* thDDraw_GetlpSurface(HANDLE Handle);
  int thDDrawGetDeviceBitsPixel();

#ifdef __cplusplus
}
#endif

#endif
