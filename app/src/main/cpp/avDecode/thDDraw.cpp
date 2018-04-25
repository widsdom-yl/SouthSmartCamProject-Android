//-----------------------------------------------------------------------------
// Author      : 朱红波
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#include "thDDraw.h"
#include "thffmpeg.h"
#include "../include/avDecode.h"
#include <ddraw.h>

//#define INITGUID
#pragma comment(lib, "lib.win32/dx9sdk/ddraw.lib")
#pragma comment(lib, "lib.win32/dx9sdk/dxguid.lib") 

//-----------------------------------------------------------------------------
typedef struct TDDrawInfo {
  LPDIRECTDRAW7 DDraw;//对象指针
  LPDIRECTDRAWSURFACE7 DDPrimaryScreen;	// DirectDraw 主表面指针
  LPDIRECTDRAWSURFACE7 DDOffScreen;	// DirectDraw 离屏表面指针
  DDSURFACEDESC2       DDsd;// DirectDraw 表面描述
  LPDIRECTDRAWCLIPPER Clp;

  RECT SrcRect;
  HWND DspHandle;

  int DeviceBitsPixel;
  int iDDrawMode;

}TDDrawInfo;
//-----------------------------------------------------------------------------
int thDDrawGetDeviceBitsPixel()
{
  int Result = 0;
  HDC dc = GetDC(0);
  Result = GetDeviceCaps(dc, PLANES) * GetDeviceCaps(dc, BITSPIXEL);
  ReleaseDC(0, dc);
  return Result;
}
//-----------------------------------------------------------------------------
HANDLE thDDraw_Init(int iDDrawMode/*0=VIDEOMEMORY 1=SYSTEMMEMORY*/)
{
  int ret;
  TDDrawInfo* Info = (TDDrawInfo*)malloc(sizeof(TDDrawInfo));
  if (!Info) return NULL;

  memset(Info, 0, sizeof(TDDrawInfo));
  Info->DeviceBitsPixel = thDDrawGetDeviceBitsPixel();
  Info->iDDrawMode = iDDrawMode;
  ret = thDDraw_InitPrimaryScreen((HANDLE)Info);
  if (!ret) return NULL;
  ret = thDDraw_InitOffScreen((HANDLE)Info, 1920, 1080);
  if (!ret) return NULL;
  return (HANDLE)Info;
}
//-----------------------------------------------------------------------------
bool thDDraw_Free(HANDLE Handle)
{
  TDDrawInfo* Info = (TDDrawInfo*)Handle;
  if (!Info) return false;
  thDDraw_FreeOffScreen(Handle);
  thDDraw_FreePrimaryScreen(Handle);
  return false;
}
//-----------------------------------------------------------------------------
bool thDDraw_InitPrimaryScreen(HANDLE Handle)
{
  HRESULT hr;
  TDDrawInfo* Info = (TDDrawInfo*)Handle;
  if (!Info) return false;

  thDDraw_FreePrimaryScreen(Handle);
  //0.Create DDraw
  hr = DirectDrawCreateEx(NULL, (void**)&Info->DDraw, IID_IDirectDraw7, NULL);
  if (hr != DD_OK) return false;  
  hr = Info->DDraw->SetCooperativeLevel(0, DDSCL_NORMAL);
  if (hr != DD_OK) return false;
  //1.Create Primary
  memset(&Info->DDsd, 0, sizeof(Info->DDsd));
  Info->DDsd.dwSize = sizeof(Info->DDsd);
  Info->DDsd.dwFlags = DDSD_CAPS;
  Info->DDsd.ddsCaps.dwCaps = DDSCAPS_PRIMARYSURFACE;
  hr = Info->DDraw->CreateSurface(&Info->DDsd, &Info->DDPrimaryScreen, NULL);
  if (hr != DD_OK) return false;
  //2.Setup clipper
  if (Info->DDraw->CreateClipper(0, &Info->Clp, NULL) == DD_OK)
  {
    Info->Clp->SetHWnd(0, 0);
    Info->DDPrimaryScreen->SetClipper(Info->Clp);
    Info->Clp->Release();//ok
    Info->Clp = NULL;
  }
  return true;
}
//-----------------------------------------------------------------------------
bool thDDraw_FreePrimaryScreen(HANDLE Handle)
{
  TDDrawInfo* Info = (TDDrawInfo*)Handle;
  if (!Info) return false;

//  thDDraw_FreeOffScreen(Handle);
  if (Info->DDPrimaryScreen)
  {
    Info->DDPrimaryScreen->Release();//?
    Info->DDPrimaryScreen = NULL;
  }
  if (Info->DDraw)
  {
    Info->DDraw->Release();//?
    Info->DDraw = NULL;
  }
  return true;
}
//-----------------------------------------------------------------------------
bool thDDraw_InitOffScreen(HANDLE Handle, int w, int h)
{
  TDDrawInfo* Info = (TDDrawInfo*)Handle;
  if (!Info) return false;
  HRESULT hr;
  if (Info->DDraw == NULL) return false;
  if (Info->DDPrimaryScreen == NULL) return false;
  thDDraw_FreeOffScreen(Handle);
  //3.Create OffScreen
  Info->DDraw->GetDisplayMode(&Info->DDsd);
  if (Info->iDDrawMode == 0)
  {
    Info->DDsd.ddsCaps.dwCaps = DDSCAPS_OFFSCREENPLAIN | DDSCAPS_VIDEOMEMORY;
  }
  else
  {
    Info->DDsd.ddsCaps.dwCaps = DDSCAPS_OFFSCREENPLAIN | DDSCAPS_SYSTEMMEMORY;
  }

  Info->DDsd.dwFlags = DDSD_CAPS | DDSD_HEIGHT | DDSD_WIDTH | DDSD_PIXELFORMAT;
  Info->DDsd.ddpfPixelFormat.dwFlags = DDPF_RGB;//初始化rgb格式的页面
  Info->DDsd.dwWidth = w;
  Info->DDsd.dwHeight = h;

  hr = Info->DDraw->CreateSurface(&Info->DDsd, &Info->DDOffScreen, NULL);
  if (hr != DD_OK)
  {
    Info->DDsd.ddsCaps.dwCaps = DDSCAPS_OFFSCREENPLAIN | DDSCAPS_SYSTEMMEMORY;
    hr = Info->DDraw->CreateSurface(&Info->DDsd, &Info->DDOffScreen, NULL);
    if (hr != DD_OK) return false;
  }

  Info->SrcRect.left = 0;
  Info->SrcRect.top = 0;
  Info->SrcRect.right = w;
  Info->SrcRect.bottom = h;
  return true;
}
//-----------------------------------------------------------------------------
bool thDDraw_FreeOffScreen(HANDLE Handle)
{
  TDDrawInfo* Info = (TDDrawInfo*)Handle;
  if (!Info) return false;

  if (Info->DDOffScreen)
  {
    Info->DDOffScreen->Release();//?
    Info->DDOffScreen = NULL;
  }
  return true;
}
//-----------------------------------------------------------------------------
bool thDDraw_SetDspHandle(HANDLE Handle, HWND DspHandle)
{
  HRESULT hr;
  TDDrawInfo* Info = (TDDrawInfo*)Handle;
  if (!Info) return false;

  if (Info->DspHandle == DspHandle) return false;
  Info->DspHandle = DspHandle;
  if (Info->DDraw == NULL || Info->DDPrimaryScreen == NULL) return false;
  hr = Info->DDraw->SetCooperativeLevel(Info->DspHandle, DDSCL_NORMAL);
  if (hr != DD_OK) return false;
  if (Info->DDraw->CreateClipper(0, &Info->Clp, NULL) == DD_OK)
  {
    hr = Info->Clp->SetHWnd(0, Info->DspHandle);
    if (hr != DD_OK) return false;
    hr = Info->DDPrimaryScreen->SetClipper(Info->Clp);
    if (hr != DD_OK) return false;
    Info->Clp->Release();
    Info->Clp = NULL;
  }
  return true;
}
//-----------------------------------------------------------------------------
bool thDDraw_ShowText(HANDLE Handle, char* Text, int x, int y, int FontSize, int Color)
{
  int Len;
  HDC dc;
  tagLOGFONTA LogFont;
  HFONT h;

  TDDrawInfo* Info = (TDDrawInfo*)Handle;
  if (!Info) return false;

  dc = thDDraw_GetDC(Handle);

  x = x + 16;
  y = y + 16;
  Len = strlen(Text);
  memset(&LogFont, 0, sizeof(LogFont));
  LogFont.lfCharSet = DEFAULT_CHARSET;//1;
  LogFont.lfEscapement = 0;//角度 45度=450
  LogFont.lfOrientation = 0;
  sprintf(LogFont.lfFaceName, "Arial");
  LogFont.lfWeight = FW_BOLD;
  if (FontSize == 0) FontSize = 20;
  LogFont.lfHeight = FontSize;
  h = CreateFontIndirectA(&LogFont);
  __try
  {
    SetBkMode(dc, TRANSPARENT);
    SetTextAlign(dc, TA_LEFT);
    SelectObject(dc, h);
    SetTextColor(dc, 0xFFFFFFFF ^ Color);
    TextOutA(dc, x - 1, y - 1, Text, Len);
    TextOutA(dc, x - 1, y + 1, Text, Len);
    TextOutA(dc, x + 1, y + 1, Text, Len);
    TextOutA(dc, x + 1, y - 1, Text, Len);
    SetTextColor(dc, Color);
    TextOutA(dc, x, y, Text, Len);
  }
  __finally
  {
    DeleteObject(h);
  }
  return true;
}
//-----------------------------------------------------------------------------
bool thDDraw_ShowPicture(HANDLE Handle, int x, int y, char* Text, int Color)
{
  int Len;
  HDC dc;
  tagLOGFONTA LogFont;
  HFONT h;

  TDDrawInfo* Info = (TDDrawInfo*)Handle;
  if (!Info) return false;

  dc = thDDraw_GetDC(Handle);

  x = x + 20;
  y = y + 20;
  Len = strlen(Text);
  memset(&LogFont, 0, sizeof(LogFont));
  LogFont.lfCharSet = DEFAULT_CHARSET;//1;
  LogFont.lfEscapement = 0;//角度 45度=450
  LogFont.lfOrientation = 0;
  sprintf(LogFont.lfFaceName, "Webdings");
  LogFont.lfWeight = FW_NORMAL;
  LogFont.lfHeight = 40;
  h = CreateFontIndirectA(&LogFont);
  __try
  {
    SelectObject(dc, h);
    SetBkMode(dc, TRANSPARENT);
    if (Color == 0x008080 || Color == 0x00FFFF || Color == 0xC0C0C0 || Color == 0xFFFFFF)//土黄黄灰白
      SetTextColor(dc, RGB(0,0,0));
    else
      SetTextColor(dc, RGB(255,255,255));

    TextOutA(dc, x - 1, y - 1, Text, Len);
    TextOutA(dc, x - 1, y + 1, Text, Len);
    TextOutA(dc, x + 1, y + 1, Text, Len);
    TextOutA(dc, x + 1, y - 1, Text, Len);
    SetTextColor(dc, Color);
    TextOutA(dc, x, y, Text, Len);
  }
  __finally
  {
    DeleteObject(h);
  }
  return true;
}
//-----------------------------------------------------------------------------
bool thDDraw_SaveToBmp(HANDLE Handle, char* AFileName)
{
  TDDrawInfo* Info = (TDDrawInfo*)Handle;
  if (!Info) return false;
  return false;
}
//-----------------------------------------------------------------------------
bool thDDraw_SaveToJpg(HANDLE Handle, char* AFileName)
{
  TDDrawInfo* Info = (TDDrawInfo*)Handle;
  if (!Info) return false;
  return false;
}
//-----------------------------------------------------------------------------
HDC thDDraw_GetDC(HANDLE Handle)
{
  HRESULT hr;
  HDC dc = (HDC)0xffffffff;
  TDDrawInfo* Info = (TDDrawInfo*)Handle;
  if (!Info) return dc;

  hr = Info->DDOffScreen->Lock(NULL, &Info->DDsd, DDLOCK_WRITEONLY | DDLOCK_WAIT, NULL);
  if (DD_OK != hr) return dc;
  Info->DDOffScreen->GetDC(&dc);
  return dc;
}
//-----------------------------------------------------------------------------
bool thDDraw_ReleaseDC(HANDLE Handle, HDC dc)
{
  HRESULT hr;
  TDDrawInfo* Info = (TDDrawInfo*)Handle;
  if (!Info) return false;
  hr = Info->DDOffScreen->ReleaseDC(dc);
  Info->DDOffScreen->Unlock(NULL);
  return (hr == DD_OK);
}
//-----------------------------------------------------------------------------
bool thDDraw_SetOffScreenSize(HANDLE Handle, int w, int h)
{
  return thDDraw_InitOffScreen(Handle, w, h);
}
//-----------------------------------------------------------------------------
void* thDDraw_GetlpSurface(HANDLE Handle)
{
  TDDrawInfo* Info = (TDDrawInfo*)Handle;
  if (!Info) return NULL;
  return Info->DDsd.lpSurface;
}
//-----------------------------------------------------------------------------
bool thDDraw_FillMem(HANDLE Handle, TavPicture FrameV, int ImgWidth, int ImgHeight)
{
  int PixFmt;
  char* DstBuf;
  int idwRGBBitCount, m;
  HRESULT hr;
  TDDrawInfo* Info = (TDDrawInfo*)Handle;
  if (!Info) return false;
  if (FrameV.data[0] == NULL) return false;
  if (Info->DDraw == NULL || Info->DDPrimaryScreen == NULL) return false;
  if (ImgWidth == 0 || ImgHeight==0) return false;
  if (ImgWidth != Info->DDsd.dwWidth || ImgHeight != Info->DDsd.dwHeight)
  {
    thDDraw_FreeOffScreen(Handle);
    thDDraw_InitOffScreen(Handle, ImgWidth, ImgHeight);
  }
  hr = Info->DDOffScreen->Lock(NULL, &Info->DDsd, DDLOCK_WRITEONLY | DDLOCK_WAIT, NULL);
  if (Info->DDsd.lpSurface == NULL) return false;

  __try
  {
    if (DD_OK != hr) 
    {
      if (hr == DDERR_SURFACELOST) 
      {
        Info->DDOffScreen->Restore();
        Info->DDPrimaryScreen->Restore();
      }
      return false;
    }

    idwRGBBitCount = Info->DDsd.ddpfPixelFormat.dwRGBBitCount;
    if (!(idwRGBBitCount == 16 || idwRGBBitCount == 24 || idwRGBBitCount == 32 )) return false;
    Info->DeviceBitsPixel = thDDrawGetDeviceBitsPixel();
    PixFmt = AV_PIX_FMT_RGB32;
    if (Info->DeviceBitsPixel == 16) PixFmt = AV_PIX_FMT_RGB565;
    if (Info->DeviceBitsPixel == 24) PixFmt = AV_PIX_FMT_BGR24;
    if (Info->DeviceBitsPixel == 32) PixFmt = AV_PIX_FMT_RGB32;//AV_PIX_FMT_BGR32;

    m = Info->DeviceBitsPixel / 8;
    DstBuf = (char*)Info->DDsd.lpSurface;
    thImgConvertScale2(
      DstBuf,
      PixFmt,
      Info->DDsd.dwWidth,
      Info->DDsd.dwHeight,
      &FrameV,
      AV_PIX_FMT_YUV420P,
      ImgWidth,
      ImgHeight,
      0);
    Info->SrcRect.left = 0;
    Info->SrcRect.top = 0;
    Info->SrcRect.right = ImgWidth;
    Info->SrcRect.bottom = ImgHeight;
  }
  __finally
  {
    Info->DDOffScreen->Unlock(NULL);
  }
  return true;
}
//-----------------------------------------------------------------------------
bool thDDraw_Display(HANDLE Handle, HWND DspHandle, TRect DspRect)
{
  POINT TopLeft, BottomRight;
  HRESULT hr;
  TDDrawInfo* Info = (TDDrawInfo*)Handle;
  if (!Info) return false;

  if (Info->DspHandle != DspHandle)
  {
    thDDraw_SetDspHandle(Handle, DspHandle);
  }
  
  TopLeft.x = DspRect.left;
  TopLeft.y = DspRect.top;
  BottomRight.x = DspRect.right;
  BottomRight.y = DspRect.bottom;
  ClientToScreen(DspHandle, &TopLeft);//窗体区域坐标转为屏幕坐标
  ClientToScreen(DspHandle, &BottomRight);
  DspRect.left = TopLeft.x;
  DspRect.top = TopLeft.y;
  DspRect.right = BottomRight.x;
  DspRect.bottom = BottomRight.y;
  hr = Info->DDPrimaryScreen->Blt((RECT*)&DspRect, Info->DDOffScreen, (RECT*)&Info->SrcRect, 0, NULL);
  return (hr == DD_OK);
}
//-----------------------------------------------------------------------------

