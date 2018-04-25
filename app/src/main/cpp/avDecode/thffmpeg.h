//-----------------------------------------------------------------------------
// Author      : ÷Ï∫Ï≤®
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#ifndef thffmpeg_h
#define thffmpeg_h

//#define LIB_DECODE// ø‚”–Œ Ã‚
#define LIB_PLAYER
//#define LIB_WRITEMP4
//#define LIB_ALL

#if defined(_MSC_VER)
#define WIN32_VC
#ifndef __cplusplus
#define inline
#endif
#endif

#ifndef WIN32
#ifndef HANDLE
#define HANDLE void*
#endif
#endif

#ifdef WIN32
#pragma warning(disable: 4013)
#pragma warning(disable: 4047)
#pragma warning(disable: 4024)
#pragma warning(disable: 4267)
#pragma warning(disable: 4800)
#pragma warning(disable: 4018)
#pragma warning(disable: 4996)
#pragma warning(disable: 4244)
#pragma warning(disable: 4018)
#pragma warning(disable: 4244)
#pragma warning(disable: 4311)
#pragma warning(disable: 4312)
#pragma warning(disable: 4533)
#pragma warning(disable: 4805)
#pragma warning(disable: 4996)
#pragma warning(disable: 4172)
#endif

#ifdef __cplusplus
extern "C"
{
#endif

#include "../include/ffmpeg/libavutil/frame.h"
#include "../include/ffmpeg/libavutil/time.h"
#include "../include/ffmpeg/libavformat/avformat.h"
#include "../include/ffmpeg/libavdevice/avdevice.h"
#include "../include/ffmpeg/libavcodec/avcodec.h"
#include "../include/ffmpeg/libswscale/swscale.h"
#include "../include/ffmpeg/libswresample/swresample.h"

#ifdef WIN32
#pragma comment (lib, "winmm.lib")
#pragma comment (lib, "vfw32.lib")
#pragma comment (lib, "Ws2_32.lib")
#pragma comment (lib, "atls.lib")

#pragma comment (lib, "lib.win32/dx9sdk/strmiids.lib")
#pragma comment (lib, "lib.win32/dx9sdk/d3d9.lib")
#pragma comment (lib, "lib.win32/dx9sdk/ddraw.lib")

#pragma comment (lib, "lib.win32/pthreads/pthread.lib")
#pragma comment (lib, "lib.win32/ffmpeg/libgcc.lib")
#pragma comment (lib, "lib.win32/ffmpeg/libmingwex.lib")

#ifdef LIB_DECODE
#pragma comment (lib, "lib.win32/ffmpeg/lib.decode/libavcodec.a")
#pragma comment (lib, "lib.win32/ffmpeg/lib.decode/libavformat.a")
#pragma comment (lib, "lib.win32/ffmpeg/lib.decode/libavutil.a")
#pragma comment (lib, "lib.win32/ffmpeg/lib.decode/libswscale.a")
#pragma comment (lib, "lib.win32/ffmpeg/lib.decode/libswresample.a")
#endif

#ifdef LIB_PLAYER
#pragma comment (lib, "lib.win32/ffmpeg/lib.player/libavcodec.a")
#pragma comment (lib, "lib.win32/ffmpeg/lib.player/libavformat.a")
#pragma comment (lib, "lib.win32/ffmpeg/lib.player/libavutil.a")
#pragma comment (lib, "lib.win32/ffmpeg/lib.player/libswscale.a")
#pragma comment (lib, "lib.win32/ffmpeg/lib.player/libswresample.a")
#endif

#ifdef LIB_WRITEMP4
#pragma comment (lib, "lib.win32/ffmpeg/lib.writemp4/libavcodec.a")
#pragma comment (lib, "lib.win32/ffmpeg/lib.writemp4/libavformat.a")
#pragma comment (lib, "lib.win32/ffmpeg/lib.writemp4/libavutil.a")
  //#pragma comment (lib, "lib.win32/ffmpeg/lib.writemp4/libswscale.a")
  //#pragma comment (lib, "lib.win32/ffmpeg/lib.writemp4/libswresample.a")
#endif

#ifdef LIB_ALL
#pragma comment (lib, "lib.win32/ffmpeg/lib.all/libavcodec.a")
#pragma comment (lib, "lib.win32/ffmpeg/lib.all/libavformat.a")
#pragma comment (lib, "lib.win32/ffmpeg/lib.all/libavutil.a")
#pragma comment (lib, "lib.win32/ffmpeg/lib.all/libswscale.a")
#pragma comment (lib, "lib.win32/ffmpeg/lib.all/libswresample.a")
#endif
#endif

#ifdef __cplusplus
}
#endif

#endif
