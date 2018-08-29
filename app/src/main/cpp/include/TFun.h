//-----------------------------------------------------------------------------
// Author      : ��첨
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#ifndef TFun_h
#define TFun_h

#include "cm_types.h"

//-----------------------------------------------------------------------------
#ifdef __cplusplus
extern "C"
{
#endif 

  //-----------------------------------------------------------------------------
  EXPORT void ThreadLockInit(H_THREADLOCK* lock);
  EXPORT void ThreadLockFree(H_THREADLOCK* lock);
  EXPORT void ThreadLock(H_THREADLOCK* lock);
  EXPORT void ThreadUnlock(H_THREADLOCK* lock);
  EXPORT H_THREAD ThreadCreate(void* funcAddr, void* Param, bool IsCloseHandle);
  EXPORT i32 ThreadExit(H_THREAD tHandle, u32 TimeOut);//500

  EXPORT bool GetIPPortFromAddr(struct sockaddr_in Addr, char* IP, u16* Port);
  EXPORT SOCKET FastConnect(char* aIP, i32 aPort, i32 TimeOut);
  EXPORT SOCKET SktConnect(char* IP, i32 Port, i32 TimeOut);
  EXPORT i32 SktDisConn(SOCKET hSocket);
  EXPORT i32 SktWaitForData(SOCKET hSocket, i32 TimeOut);//3000
  EXPORT i32 SktSendBuf(SOCKET hSocket, char* Buf, i32 Len);
  EXPORT i32 SktRecvBuf(SOCKET hSocket, char* Buf, i32 Len);
  EXPORT i32 SktRecvLength(SOCKET hSocket);
  EXPORT bool SendBuf(SOCKET hSocket, char* Buf, i32 BufLen, i32 TimeOut);
  EXPORT bool RecvBuf(SOCKET hSocket, char* RecvBuf, i32 BufLen, i32 TimeOut);

  EXPORT i32 myusleep(i32 us);
  typedef void (STDCALL TTimerCallBack)(HANDLE mmHandle, u32 uMsg, void* dwUser, u32 dw1, u32 dw2);
  EXPORT HANDLE mmTimeSetEvent(u32 uDelayms, TTimerCallBack callback, void* dwUser);
  EXPORT i32 mmTimeKillEvent(HANDLE mmHandle);
  EXPORT u32 mmTimeGetTime();

#ifdef WIN32
  EXPORT i32 gettimeofday(struct timeval*tv, struct timezone *tz);
  EXPORT void ProcessMessages();
  EXPORT void SleepEX(u32 ms);
  EXPORT i32 ShowMsg(char* Text, i32 Warning1_Asterisk2_Question3_Error4, i32 DefBtn012, char* Title);//Result= 1 or 2
  EXPORT char* WorkPath();
  EXPORT char* GetFileVersion(char* FileName);

  EXPORT HANDLE comConnect(const char* comName, i32 BaudRate/*CBR_115200*/, char Parity/*'N'*/, i32 DataBits/*8*/, i32 StopBits/*1*/);
  EXPORT i32 comDisConn(HANDLE hComm);
  EXPORT i32 comWaitForData(HANDLE hComm, i32 TimeOut);
  EXPORT i32 comSendBuf(HANDLE hComm, char* Buf, i32 Len);
  EXPORT i32 comRecvBuf(HANDLE hComm, char* Buf, i32 Len);
  EXPORT i32 comRecvLength(HANDLE hComm);

  EXPORT i32 UnicodeToAnsi(char* src, char* dst);
  EXPORT i32 UnicodeTxtLen(char* src);
  EXPORT i32 AnsiToUnicode(char* src, char* dst);

  EXPORT char* ShareMemOpenFile(const char* FileName, HANDLE* FileHandle, HANDLE* MapHandle, u32 dwFileSize);//����MAP Buf��ַ
  EXPORT bool ShareMemCloseFile(HANDLE FileHandle, HANDLE MapHandle, void* FData);


#else
  typedef struct SYSTEMTIME {
    u16 wYear;
    u16 wMonth;
    u16 wDayOfWeek;
    u16 wDay;
    u16 wHour;
    u16 wMinute;
    u16 wSecond;
    u16 wMilliseconds;
  }SYSTEMTIME;

#define closesocket close
  EXPORT u32 GetTickCount();
#endif

  EXPORT bool DiskExists(char* Path);
  EXPORT bool GetDiskSpace(char* Path, u32* TotalSpace, u32* FreeSpace);// M
  EXPORT char* FileExtName(char* FileName); //ȡ���ļ���չ��//'.txt'
  EXPORT char* ExtractFileName(char* FileName);
  EXPORT bool DirectoryExists(char* Directory);
  EXPORT bool FileExists(char* FileName);
  EXPORT u32 FileGetSize(char* FileName);
  EXPORT bool FileDelete(char* FileName);
  EXPORT HANDLE FileCreate(char* FileName);
  EXPORT HANDLE FileOpen(char* FileName);
  EXPORT bool FileClose(HANDLE f);
  EXPORT u32 FileGetPos(HANDLE f);
  EXPORT u32 FileSeek(HANDLE f, i32 Offset, i32 Origin); //Origin=0 1 2
  EXPORT bool FileRead(HANDLE f, void* Buf, i32 Len);
  EXPORT bool FileWrite(HANDLE f, void* Buf, i32 Len);

  EXPORT bool InArray(i32 Value, i32 Count, ...);//��ֵ�Ƿ���...֮��
  EXPORT i32 RandomNum(i32 seed);

  //ʱ�亯��
  EXPORT i32 timeval_dec(struct timeval* tv2, struct timeval* tv1);//ms
  EXPORT i32 GetTime();
  EXPORT i32 GetTimezoneTime();
  EXPORT i64 getutime();//ȡ��΢�뼶ʱ�� tv.tv_sec*1000000 + tv.tv_usec

  EXPORT void Time_tToSystemTime(i32 t, SYSTEMTIME* pst);
  EXPORT i32 SystemTimeToTime_t(SYSTEMTIME* pst);

  EXPORT void QuickSort(i32* Lst, i32 iLo, i32 iHi);
  EXPORT i32 SearchByDichotomy(i32* Lst, i32 iL, i32 iH, i32 Key);//���ַ�

  EXPORT u32 crc32(char* buf, i32 buflen);
  EXPORT u32 crc32F(char* FileName);
  EXPORT u32 crc32F2(char* FileName, i32 FileSize);

  EXPORT i32 CharCount(char c, const char* S);//�ַ���ͳ��
  EXPORT char* LowerCase(char* s);
  EXPORT char* UpperCase(char* s);

  EXPORT bool IsValidIP(const char* IP);
  EXPORT bool IsValidHost(const char* Host);
  EXPORT bool IsValidMAC(const char* MAC, char SplitChar);
  EXPORT bool IsLANIP(char* IP);
  EXPORT i32 IPToInt(char* IP);
  EXPORT char* IntToIP(i32 IP);

#define HTTPERROR i32
  EXPORT HTTPERROR HttpPost(const char* url, char* InBuf, i32 InBufLen, char* OutBuf, i32* OutBufLen, bool IsShowHead, i32 TimeOut);
  //200=OK
  EXPORT HTTPERROR HttpGet(const char* url, char* OutBuf, i32* OutBufLen, bool IsShowHead, i32 TimeOut);

  EXPORT i32 Base64Encode(unsigned char* inbuf, i32 insize, unsigned char* outbuf, i32 outsize);
  EXPORT i32 Base64Decode(unsigned char* inbuf, i32 insize, unsigned char* outbuf, i32 outsize);
  EXPORT i32 SHA1Encode(unsigned char* inbuf, i32 insize, unsigned char* outbuf, i32 outsize);
//-----------------------------------------------------------------------------
  EXPORT char* GetLocalIP();
  EXPORT bool IsSameSegmentIP(char* IP1, char* IP2);//两个IP是否在同一段
  EXPORT bool IsConnectWLAN();//�����Ƿ�������
  EXPORT void printPI();//��ӡԲ����

  EXPORT bool RectIsIntersect(TRect* r1, TRect* r2);//�����Ƿ����

  EXPORT int StrToHex(char* src, int srclen, char* dst);//'0' -> "30"
  EXPORT int HexToStr(char* src, int srclen, char* dst);//"30" -> '0'

#ifdef __cplusplus
}
#endif 

#endif
