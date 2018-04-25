//-----------------------------------------------------------------------------
// Author      : 朱红波
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#ifndef cm_types_H
#define cm_types_H
/*
#ifdef _WIN32
   //define something for Windows (32-bit and 64-bit, this part is common)
   #ifdef _WIN64
      //define something for Windows (64-bit only)
   #else
      //define something for Windows (32-bit only)
   #endif
#elif __APPLE__
    #include "TargetConditionals.h"
    #if TARGET_IPHONE_SIMULATOR
         // iOS Simulator
    #elif TARGET_OS_IPHONE
        // iOS device
    #elif TARGET_OS_MAC
        // Other kinds of Mac OS
    #else
    #   error "Unknown Apple platform"
    #endif
#elif __ANDROID__
    // android
#elif __linux__
    // linux
#elif __unix__ // all unices not caught above
    // Unix
#elif defined(_POSIX_VERSION)
    // POSIX
#else
#   error "Unknown compiler"
#endif
*/
//#define WIN32
//#define linux
//#define ANDROID
//#define __APPLE__
//#define IPHONE   TARGET_OS_IPHONE

//#if !(defined(WIN32) || defined(WIN64))
#if !defined(WIN32) && defined(_WIN32)
#define WIN32 _WIN32
#endif

#if !defined(linux) && defined(__linux__)
#define linux __linux__
#endif

#if !defined(ANDROID) && defined(__ANDROID__)
#define ANDROID __ANDROID__
#endif

#ifdef WIN32

#include <windows.h>
#include <stdio.h>
#include <math.h>
#include <stdlib.h>
#include <winsock.h>
#ifndef __cplusplus
#define bool u8
#define true  1
#define false 0
#endif

#else
//#include <sys/shm.h> //android no
//#include <sys/msg.h> //android no
#include <dlfcn.h>
#include <assert.h>
#include <math.h>
#include <stdio.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/sysinfo.h>
#include <string.h>
#include <fcntl.h>
#include <errno.h>
#include <sys/timeb.h>
#include <dirent.h>
#include <sys/stat.h>
#include <stddef.h>
#include <ctype.h>
#include <time.h>
#include <getopt.h>
#include <linux/rtc.h>
#include <termios.h>
#include <signal.h>
#include <stdbool.h>
#include <pthread.h>
#include <semaphore.h>
#include <unistd.h>
#include <net/if.h>
#include <arpa/inet.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <netinet/tcp.h>
#include <netinet/ip_icmp.h>
#include <netdb.h>
#include <linux/soundcard.h>
#include <linux/fb.h>
#include <sys/wait.h>
#include <sys/ipc.h>
#include <sys/ioctl.h>
#include <linux/ioctl.h>
#include <sys/mman.h>
#include <sys/ioctl.h>
#include <sys/time.h>
#include <string.h>
#include <mntent.h>
#include <sys/statfs.h>
#ifndef __cplusplus
#include <stdbool.h>
#else
#define bool u8
#define true  1
#define false 0
#endif

#endif

#ifdef ANDROID
#include <android/log.h>
#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,"jni",__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,"jni",__VA_ARGS__)
#define PRINTF LOGE
#else
#define PRINTF printf
#endif

//-----------------------------------------------------------------------------
#ifndef EXPORT
#ifdef WIN32
#define EXPORT __declspec(dllexport)
#else
#define EXPORT
#endif
#endif

#ifndef STDCALL
#ifdef WIN32
#define STDCALL __stdcall
#else
#define STDCALL //__attribute__((__stdcall__))//android编不过
#endif
#endif

#ifdef WIN32
#pragma warning(disable: 4013)
#pragma warning(disable: 4047)
#pragma warning(disable: 4024)
#pragma warning(disable: 4133)
#pragma warning(disable: 4267)
#pragma warning(disable: 4293)
#pragma warning(disable: 4800)
#pragma warning(disable: 4018)
#pragma warning(disable: 4996)
#pragma warning(disable: 4244)
#pragma warning(disable: 4018)
#pragma warning(disable: 4244)
#pragma warning(disable: 4311)
#pragma warning(disable: 4312)
#pragma warning(disable: 4533)
#pragma warning(disable: 4800)
#pragma warning(disable: 4805)
#pragma warning(disable: 4996)
#pragma warning(disable: 4172)
#endif

#ifdef WIN32 //win32

#define  i8           __int8
#define i16          __int16
#define i32          __int32
#define i64          __int64
#define  u8  unsigned __int8
#define u16 unsigned __int16
#define u32 unsigned __int32
#define u64 unsigned __int64
#define ulong  unsigned long

#define H_THREAD      HANDLE
#define H_THREADLOCK  CRITICAL_SECTION
#define THandle       HANDLE

#define usleep(us)  Sleep((us)/1000)

#else //linux android ios mac

#define  i8   int8_t
#define i16  int16_t
#define i32  int32_t
#define i64  int64_t
#define  u8  uint8_t
#define u16 uint16_t
#define u32 uint32_t
#define u64 uint64_t
#define ulong unsigned long;

#define SOCKET        i32
#define H_THREAD      pthread_t
#define H_THREADLOCK  pthread_mutex_t
#ifndef HANDLE
#define HANDLE void*
#endif
#define THandle       HANDLE
#define HWND          HANDLE
#endif

typedef char char2[2];
typedef char char4[4];
typedef char char8[8];
typedef char char12[12];
typedef char char16[16];
typedef char char20[20];
typedef char char28[28];
typedef char char32[32];
typedef char char40[40];
typedef char char48[48];
typedef char char50[50];
typedef char char60[60];
typedef char char64[64];
typedef char char80[80];
typedef char char100[100];
typedef char char256[256];
typedef char char1024[1024];
typedef char char8192[1024*8];
typedef char8192 char8k;

#ifndef max
#define max(a,b) (((a) > (b)) ? (a) : (b))
#endif
#ifndef min
#define min(a,b) (((a) < (b)) ? (a) : (b))
#endif
#define Bit(S, Bits, Value) ( (Value==1) ? (S|(1<<Bits)) : (S&(~(1<<Bits))) )
#define BitXOR(S, Bits)     ( S ^ (1 << Bits) )
#define BitValue(S, Bits)   ( (S & (1 << Bits)) != 0 )

//-----------------------------------------------------------------------------
typedef struct TRect {                        //sizeof 16 u8
  i32 left;                                    //左
  i32 top;                                     //上
  i32 right;                                   //右
  i32 bottom;                                  //下
}TRect;

#endif

