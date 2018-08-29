//-----------------------------------------------------------------------------
// Author      : 朱红波
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#include "../include/TFun.h"

#ifdef WIN32
#include "../include/pthreads/semaphore.h"
#pragma comment (lib, "Ws2_32.lib")
#endif

//-----------------------------------------------------------------------------
void ThreadLockInit(H_THREADLOCK *lock)
{
#ifndef WIN32
  //*lock = PTHREAD_MUTEX_INITIALIZER;
  pthread_mutex_init(lock, NULL);
#else
  InitializeCriticalSection(lock);
#endif
}

//-----------------------------------------------------------------------------
void ThreadLockFree(H_THREADLOCK *lock)
{
#ifndef WIN32
  pthread_mutex_destroy(lock);
#else
  DeleteCriticalSection(lock);
#endif
}

//-----------------------------------------------------------------------------
void ThreadLock(H_THREADLOCK *lock)
{
#ifndef WIN32
  pthread_mutex_lock(lock);
#else
  EnterCriticalSection(lock);
#endif
}

//-----------------------------------------------------------------------------
void ThreadUnlock(H_THREADLOCK *lock)
{
#ifndef WIN32
  pthread_mutex_unlock(lock);
#else
  LeaveCriticalSection(lock);
#endif
}
//-----------------------------------------------------------------------------
H_THREAD ThreadCreate(void *funcAddr, void *Param, bool IsCloseHandle)
{
  H_THREAD tHandle;
#ifndef WIN32
  pthread_create(&tHandle, NULL, (void *(*)(void *)) funcAddr, Param);
  if (IsCloseHandle)
  {
    pthread_detach(tHandle);
    close(tHandle);
    tHandle = 0;
  }
  return tHandle;
#else
  u32 thID = 0;
  tHandle = CreateThread(NULL, 0, (LPTHREAD_START_ROUTINE)funcAddr, Param, 0, &thID);
  if (IsCloseHandle)
  {
    CloseHandle(tHandle);
    tHandle = NULL;
  }
  return tHandle;
#endif
}
//-----------------------------------------------------------------------------
i32 ThreadExit(H_THREAD tHandle, u32 TimeOut)
{
#ifndef WIN32
  pthread_join(tHandle, 0);
#ifndef ANDROID
  pthread_cancel(tHandle);
#endif
  close(tHandle);
  return 1;
#else
  u32 iExitCode, ret;
  //  __try
  {
    if (TimeOut == 0) TimeOut = INFINITE;
    //WaitForSingleObject(tHandle, INFINITE);
    WaitForSingleObject(tHandle, TimeOut);
    GetExitCodeThread(tHandle, &iExitCode);
    ret= CloseHandle(tHandle);
    return iExitCode;
  }
  //  __except( EXCEPTION_EXECUTE_HANDLER)
  //  {
  //    return -1;
  //  }
#endif
}
//-----------------------------------------------------------------------------
/*
bool PRINTF(char* format, ...)
{
char buffer[1000];
va_list argptr;
va_start(argptr, format);
sprintf(buffer, format, argptr);
va_end(argptr);
OutputDebugStringA(buffer);
return true;
}
*/
//-----------------------------------------------------------------------------
SOCKET FastConnect(char *aIP, i32 aPort, i32 TimeOut)//返回SocketHandle
{
#define NULLHANDLE  -1
  i32 hSocket = NULLHANDLE;
  i32 i, Ret, Flag;
  i32 Error = 0;
  fd_set rs, ws;
  struct timeval tval;
  struct sockaddr_in CSktAddr;
  u32 tmp = INADDR_NONE;
  struct hostent *h = NULL;

  if (!aIP) return NULLHANDLE;
  if (aPort == 0) return NULLHANDLE;

  memset(&CSktAddr, 0, sizeof(struct sockaddr_in));
  CSktAddr.sin_family = AF_INET;
  CSktAddr.sin_port = htons(aPort);
  //inet_aton(aIP, &CSktAddr.sin_addr);
  h = (struct hostent *) gethostbyname(aIP);
  if (h == NULL) return NULLHANDLE;
  memcpy(&CSktAddr.sin_addr, h->h_addr_list[0], h->h_length);

  //  hSocket = socket(AF_INET, SOCK_STREAM, IPPROTO_IP);
  //  if (hSocket <= 0) return NULLHANDLE;
  //hSocket = socket(AF_INET, SOCK_STREAM, IPPROTO_IP);
  //if (hSocket <= 0) hSocket = socket(AF_INET, SOCK_STREAM, IPPROTO_IP);
  for (i = 0; i < 10; i++)
  {
    hSocket = socket(AF_INET, SOCK_STREAM, IPPROTO_IP);
    if (hSocket > 0) break;
    usleep(1000 * 100);
  }
  if (hSocket <= 0) return NULLHANDLE;

#ifndef WIN32
  Flag = fcntl(hSocket, F_GETFL, 0);// ==2
  fcntl(hSocket, F_SETFL, Flag | O_NONBLOCK);//O_NONBLOCK==2048 |2 = 2050
#else
  Flag = 1;//0
  ioctlsocket(hSocket, FIONBIO, &Flag);//非阻塞方式
#endif

  Ret = connect(hSocket, (struct sockaddr *) &CSktAddr, sizeof(struct sockaddr_in));
  if (Ret == 0) goto Done;

  FD_ZERO(&rs);
  FD_SET(hSocket, &rs);
  ws = rs;//FD_SET(hSocket, &ws);  

  tval.tv_sec = TimeOut / 1000;//TimeOut;
  tval.tv_usec = (TimeOut % 1000) * 1000;
  if ((Ret = select(hSocket + 1, &rs, &ws, NULL, TimeOut ? &tval : NULL)) == 0)
  {
    closesocket(hSocket);
    //errno = ETIMEDOUT;
    return NULLHANDLE;
  }
  if (FD_ISSET(hSocket, &rs) || FD_ISSET(hSocket, &ws))
  {
    Error = 0;
    Ret = sizeof(i32);
    if (getsockopt(hSocket, SOL_SOCKET, SO_ERROR, &Error, &Ret) < 0)
    {
      closesocket(hSocket);
      //      errno = ETIMEDOUT;
      return NULLHANDLE;
    }
  }

  Done:
#ifndef WIN32
  fcntl(hSocket, F_SETFL, Flag);
#endif
  if (Error)
  {
    closesocket(hSocket);
    errno = Error;
    return NULLHANDLE;
  }
  return hSocket;
}
//-----------------------------------------------------------------------------
SOCKET SktConnect(char *IP, i32 Port, i32 TimeOut)
{
#ifndef WIN32
#define INVALID_SOCKET -1
#endif
  i32 IsConn;
  i32 ret, i;
  struct sockaddr_in CSktAddr;
  struct timeval tval;
  fd_set rs, ws;
  SOCKET hSocket;
  struct hostent *h = NULL;
  unsigned long flag = 1;

  for (i = 0; i < 100; i++)
  {
    hSocket = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);//IPPROTO_IP
    if (hSocket > 0) break;
    usleep(1000 * 100);
  }

  if (hSocket <= 0) return 0;
  if (hSocket == INVALID_SOCKET)
  {
    closesocket(hSocket);
    hSocket = 0;
    return 0;
  }
  CSktAddr.sin_family = AF_INET;
  CSktAddr.sin_port = htons(Port);

#if 0
  CSktAddr.sin_addr.s_addr = inet_addr(IP);
#else
  h = (struct hostent *) gethostbyname(IP);
  if (h == NULL) return NULLHANDLE;
  memcpy(&CSktAddr.sin_addr, h->h_addr_list[0], h->h_length);
#endif

#ifdef WIN32
  ioctlsocket(hSocket, FIONBIO, &flag);
#else
  fcntl(hSocket, F_SETFL, O_NONBLOCK);//非阻塞方式
#endif
  ret = connect(hSocket, (struct sockaddr *) &CSktAddr, sizeof(CSktAddr));
  FD_ZERO(&rs);
  FD_SET(hSocket, &rs);
  ws = rs;
  tval.tv_sec = TimeOut / 1000;
  tval.tv_usec = (TimeOut % 1000) * 1000;
  IsConn = select(hSocket + 1, &rs, &ws, NULL, &tval);
  if (IsConn != 1)
  {
    closesocket(hSocket);
    hSocket = 0;
  }
  return hSocket;
}
//-----------------------------------------------------------------------------
i32 SktDisConn(SOCKET hSocket)
{
  closesocket(hSocket);
  return 1;
}
//-----------------------------------------------------------------------------
i32 SktWaitForData(SOCKET hSocket, i32 TimeOut)
{
  struct timeval tval;
  fd_set rs;
  FD_ZERO(&rs);
  FD_SET(hSocket, &rs);
  tval.tv_sec = TimeOut / 1000;
  tval.tv_usec = (TimeOut % 1000) * 1000;
  return (select(0, &rs, NULL, NULL, &tval) > 0);
}
//-----------------------------------------------------------------------------
i32 SktSendBuf(SOCKET hSocket, char *Buf, i32 Len)
{
  return send(hSocket, Buf, Len, 0);
}
//-----------------------------------------------------------------------------
i32 SktRecvBuf(SOCKET hSocket, char *Buf, i32 Len)
{
  return recv(hSocket, Buf, Len, 0);
}
//-----------------------------------------------------------------------------
bool SendBuf(SOCKET hSocket, char *Buf, i32 BufLen, i32 TimeOut)
{
  //  i32 Ret = send(hSocket, Buf, BufLen, 0);
  //  return (Ret >= 0);
#define MTUSIZE 576 //mtu = 1500 Intranet,  576 Internet
#define SPLIT_PKT_SIZE_TCP  MTUSIZE-40

#ifdef WIN32
  i32 k,SendLen;
  u32 t;
  if (!Buf) return false;
  if (BufLen <= 0) return false;
  if (hSocket <= 0) return false;
  k = 0;
  t = GetTickCount();
  while (k < BufLen)
  {
    SendLen = min(SPLIT_PKT_SIZE_TCP, BufLen - k);
    SendLen = send(hSocket, Buf + k, SendLen, 0);
    if (SendLen != SOCKET_ERROR)
    {
      k = SendLen + k;
    }
    else
    {
      if (WSAGetLastError() == WSAEWOULDBLOCK)
      {
        if (GetTickCount() - t > TimeOut) return false;
        Sleep(1);
      }
      else
      {
        return false;
      }
    }
  }
  return true;

#else
  i32 k = 0;
  i32 SendLen = 0;
  u32 t, t1;

  if (!Buf) return false;
  if (BufLen <= 0) return false;
  if (hSocket <= 0) return false;

  t = GetTickCount();
  while (k < BufLen)
  {
    SendLen = min(SPLIT_PKT_SIZE_TCP, BufLen - k);
#ifdef __cplusplus
    if (hSocket <=0) return false;
#endif

    SendLen = send(hSocket, (char *) Buf + k, SendLen, 0);

    if (SendLen != -1)
    {
      k = SendLen + k;
    } else
    {
      if (errno == EINTR || errno == EAGAIN)//EWOULDBLOCK = EAGAIN
      {
        t1 = GetTickCount();
        if (t1 - t >= TimeOut)
        {
          return false;
        }
        errno = 0;
        usleep(1000 * 10);
        continue;
      } else
      {
        if (errno != 0)
        {
          errno = 0;
        }
        return false;
      }
    }
  }
  return true;
#endif
}
//-----------------------------------------------------------------------------
bool RecvBuf(SOCKET hSocket, char *RecvBuf, i32 BufLen, i32 TimeOut)
{
#ifdef WIN32
  i32 Len, RecvLen, LastError;
  char* Buf;
  u32 t;

  if (!RecvBuf) return false;
  Buf = RecvBuf;
  RecvLen = 0;

  t = GetTickCount();
  while (1)
  {
    Len = recv(hSocket, Buf + RecvLen, BufLen - RecvLen, 0);
    if (Len == SOCKET_ERROR)
    {
      LastError = WSAGetLastError();
      if (LastError == WSAEWOULDBLOCK)
      {
        if (GetTickCount() - t >= TimeOut) break;
        Sleep(1);
        continue;
      }
      else
      {
        closesocket(hSocket);
        //        hSocket:=INVALID_SOCKET;
        return false;
      }
    }

    RecvLen = RecvLen + Len;
    if (RecvLen == BufLen) return true;
  }
  return false;
#else
  char *Buf = RecvBuf;
  i32 Len, RecvLen;
  u32 t, t1;
  RecvLen = 0;

  if (!Buf) return false;
  if (BufLen == 0) return true;

  t = GetTickCount();
  while (true)
  {
#ifdef __cplusplus
    if (hSocket <=0) return false;
#endif
    Len = recv(hSocket, &Buf[RecvLen], BufLen - RecvLen, 0);
    if (Len != -1)
    {
      RecvLen = RecvLen + Len;
    } else
    {
      if (errno == EINTR || errno == EAGAIN)//EWOULDBLOCK = EAGAIN
      {
        t1 = GetTickCount();
        if (t1 - t >= TimeOut)
        {
          return false;
        }
        errno = 0;
        usleep(1000 * 10);
        continue;
      } else
      {
        if (errno != 0)
        {
          errno = 0;
        }
        return false;
      }
    }
    if (RecvLen == BufLen) return true;
  }
#endif
}
//-----------------------------------------------------------------------------
i32 SktRecvLength(SOCKET hSocket)
{
  u_long ret = 0;
#ifdef WIN32
  ioctlsocket(hSocket, FIONREAD, &ret);
#else
  ioctl(hSocket, FIONREAD, &ret);
#endif
  return ret;
}
//-----------------------------------------------------------------------------
i32 myusleep(i32 us)
{
#ifdef WIN32
  Sleep(us / 1000);
  return 1;
#else
  i32 ret;
  struct timeval tv;
  tv.tv_sec = us / 1000000;
  tv.tv_usec = us % 1000000;
  do
  {
    ret = select(0, NULL, NULL, NULL, &tv);
  } while (ret < 0 && errno == EINTR);
  return ret;
#endif
}

//-----------------------------------------------------------------------------
typedef struct TmmTimeInfo
{
  H_THREADLOCK Lock;
  sem_t sem;
  H_THREAD thTimer;
  H_THREAD thTimerEvent;
  bool IsExit;

  u32 uDelayms;
  TTimerCallBack *callback;
  void *dwUser;

} TmmTimeInfo;

//-----------------------------------------------------------------------------
void thread_mmTimerEvent(TmmTimeInfo *Info)
{
  if (!Info) return;
  while (1)
  {
    if (Info->IsExit) break;
#if 1
    if (sem_wait(&Info->sem) != 0) continue;
#else
    if (sem_trywait(&Info->sem) != 0) continue;//cpu high
#endif
    if (Info->callback) Info->callback((HANDLE) Info, 0, Info->dwUser, 0, 0);
  }
}

//-----------------------------------------------------------------------------
void thread_mmTimer(TmmTimeInfo *Info)
{
  if (!Info) return;
  while (1)
  {
    if (Info->IsExit) break;
    myusleep(Info->uDelayms * 1000);
    sem_post(&Info->sem);
  }
}
//-----------------------------------------------------------------------------
HANDLE mmTimeSetEvent(u32 uDelayms, TTimerCallBack callback, void *dwUser)
{
#ifdef WIN32
  return (HANDLE)timeSetEvent(uDelayms, 0, (LPTIMECALLBACK)callback, (u32)dwUser, 1);
#else
  TmmTimeInfo *Info = (TmmTimeInfo *) malloc(sizeof(TmmTimeInfo));
  memset(Info, 0, sizeof(TmmTimeInfo));
  Info->IsExit = false;
  Info->uDelayms = uDelayms;
  Info->callback = callback;
  Info->dwUser = dwUser;
  ThreadLockInit(&Info->Lock);
  sem_init(&Info->sem, 0, 0);
  Info->thTimer = ThreadCreate(thread_mmTimer, Info, false);
  Info->thTimerEvent = ThreadCreate(thread_mmTimerEvent, Info, false);
  return (HANDLE) Info;
#endif
}
//-----------------------------------------------------------------------------
i32 mmTimeKillEvent(HANDLE mmHandle)
{
#ifdef WIN32
  return timeKillEvent(mmHandle);
#else
  u32 ret;
  TmmTimeInfo *Info = (TmmTimeInfo *) mmHandle;
  if (!Info) return false;

  ThreadLock(&Info->Lock);
  Info->IsExit = true;
  ThreadUnlock(&Info->Lock);
  ret = ThreadExit(Info->thTimerEvent, 1000);
  if (ret) ret = ThreadExit(Info->thTimer, 1000);
  if (ret)
  {
    ThreadLockFree(&Info->Lock);
    sem_destroy(&Info->sem);
    free(Info);
    Info = NULL;
    mmHandle = NULL;
  }
  return ret;
#endif
}
//-----------------------------------------------------------------------------
u32 mmTimeGetTime()//add -lrt
{
#ifdef WIN32
  return timeGetTime();
#else
  u32 uptime = 0;
  struct timespec on;
  if (clock_gettime(CLOCK_MONOTONIC, &on) == 0)
  {
    uptime = on.tv_sec * 1000 + on.tv_nsec / 1000000;
  }
  return uptime;
#endif
}
//-----------------------------------------------------------------------------
#ifndef WIN32

u32 GetTickCount()
{
  static i32 t = 0;
  char buf[40];
  float a, b;
  struct timeval tv;
  gettimeofday(&tv, NULL);
  if (t == 0)
  {
#if 0
    FILE* f = fopen("/proc/uptime", "r+");
    if (f) 
    {
      fgets(buf, sizeof(buf), f);
      fclose(f);
      sscanf(buf, "%f %f", &a, &b);
      t = tv.tv_sec - (i32)a;
    } 
    else
    {
      t = tv.tv_sec;
    }
#else
    t = tv.tv_sec;
#endif
  }
  return (u32) (tv.tv_sec - t) * 1000 + tv.tv_usec / 1000;
}

#endif
//-----------------------------------------------------------------------------
#ifdef WIN32
i32 gettimeofday(struct timeval* tv, struct timezone *tz)
{
  SYSTEMTIME st;
  GetLocalTime(&st);
  tv->tv_sec = time(NULL);
  tv->tv_usec = st.wMilliseconds * 1000;
  return 0;
}
//-----------------------------------------------------------------------------
void ProcessMessages()
{
  MSG Msg;
  while (1)
  {
    if (!PeekMessage(&Msg, 0, 0, 0, PM_REMOVE)) break;
    if (Msg.message == WM_QUIT) return;
    TranslateMessage(&Msg);
    DispatchMessage(&Msg);
  }
}
//-----------------------------------------------------------------------------
void SleepEX(u32 ms)
{
  u32 m, n;
  n = GetTickCount();
  m = n;
  while (1)
  {
    m = GetTickCount();
    if (m - n >= ms) break;
    ProcessMessages();
  }
}
//-----------------------------------------------------------------------------
i32 ShowMsg(char* Text, i32 Warning1_Asterisk2_Question3_Error4, i32 DefBtn012, char* Title)//Result= 1 or 2
{
  u32 Flag;
  Flag = MB_OKCANCEL;
  if (Warning1_Asterisk2_Question3_Error4 == 1) Flag = Flag + MB_ICONWARNING;
  if (Warning1_Asterisk2_Question3_Error4 == 2) Flag = Flag + MB_ICONASTERISK;
  if (Warning1_Asterisk2_Question3_Error4 == 3) Flag = Flag + MB_ICONQUESTION;
  if (Warning1_Asterisk2_Question3_Error4 == 4) Flag = Flag + MB_ICONERROR;

  if (DefBtn012 == 0) Flag = Flag - MB_OKCANCEL + MB_OK;
  if (DefBtn012 == 1) Flag = Flag + MB_DEFBUTTON1;
  if (DefBtn012 == 2) Flag = Flag + MB_DEFBUTTON2;
  return MessageBoxA(NULL, Text, Title, MB_TOPMOST + Flag);
}
//-----------------------------------------------------------------------------
char* WorkPath()
{
  i32 i, m;
  char c;
  char Path[MAX_PATH + 1]; 
  GetModuleFileNameA(NULL, Path, MAX_PATH); 
  m = strlen(Path);
  for (i = m - 1; i>=0; i--)
  {
    c = Path[i];
    if (c == '\\')
    {
      Path[i+1] = 0x00;
      break;
    }
  }
  //(_tcschr(Path, '\\'))[1] = 0;//删除文件名，只获得路径_tcsrchr
  return Path;
}
//-----------------------------------------------------------------------------
char* GetFileVersion(char* FileName)
{  
#pragma comment(lib,"Version.lib")
  static char Str[100];
  u32 dwTemp, dwSize;
  u32 verMS, verLS, major, minor, build, revision;
  BYTE *pData = NULL;  
  UINT uLen;  
  VS_FIXEDFILEINFO *pVerInfo = NULL;  
  sprintf(Str, "0.0.0.0");

  dwSize = GetFileVersionInfoSize(FileName, &dwTemp);  
  if (dwSize == 0) return Str;
  pData = malloc(dwSize+1);  
  if (!GetFileVersionInfo(FileName, 0, dwSize, pData))  
  {  
    free(pData);
    return Str;  
  }  

  if (!VerQueryValue(pData, TEXT("\\"), (void **)&pVerInfo, &uLen))   
  {  
    free(pData);
    return Str;  
  }  

  verMS = pVerInfo->dwFileVersionMS;  
  verLS = pVerInfo->dwFileVersionLS;  
  major = HIWORD(verMS);  
  minor = LOWORD(verMS);  
  build = HIWORD(verLS);  
  revision = LOWORD(verLS);  
  free(pData);

  sprintf(Str, ("%d.%d.%d.%d"), major, minor, build, revision);  

  return Str;  
}
//-----------------------------------------------------------------------------
HANDLE comConnect(const char* comName, i32 BaudRate/*CBR_115200*/, char Parity/*'N'*/, i32 DataBits/*8*/, i32 StopBits/*1*/)
{
  i32 ret;
  HANDLE hComm;
  struct _COMMTIMEOUTS CommTimeouts;
  struct _DCB dcb;

  hComm = CreateFileA(comName, GENERIC_READ | GENERIC_WRITE, 0, NULL, OPEN_EXISTING, 0, 0);
  if (hComm == INVALID_HANDLE_VALUE) return NULL;
  ret = SetupComm(hComm, 4096, 4096);
  GetCommTimeouts(hComm, &CommTimeouts);
  CommTimeouts.ReadIntervalTimeout = 1000;//读间隔超时
  CommTimeouts.ReadTotalTimeoutConstant = 500;//读时间系数
  CommTimeouts.ReadTotalTimeoutMultiplier = 5000;//读时间常量
  CommTimeouts.WriteTotalTimeoutConstant = 500;//写时间系数
  CommTimeouts.WriteTotalTimeoutMultiplier = 2000;//写时间常量, 总超时的计算公式是：总超时＝时间系数×要求读/写的字符数＋时间常量
  ret = SetCommTimeouts(hComm, &CommTimeouts);

  GetCommState(hComm, &dcb);
  dcb.BaudRate = BaudRate;
  dcb.DCBlength       = sizeof(dcb);
  dcb.Parity      = NOPARITY;
  dcb.fParity     = 0;
  dcb.StopBits        = ONESTOPBIT;
  dcb.ByteSize        = 8;
  dcb.fOutxCtsFlow    = 0;
  dcb.fOutxDsrFlow    = 0;
  dcb.fDtrControl     = DTR_CONTROL_DISABLE;
  dcb.fDsrSensitivity = 0;
  dcb.fRtsControl     = RTS_CONTROL_DISABLE;
  dcb.fOutX           = 0;
  dcb.fInX            = 0;
  /* ----------------- misc parameters ----- */
  dcb.fErrorChar      = 0;
  dcb.fBinary         = 1;
  dcb.fNull           = 0;
  dcb.fAbortOnError   = 0;
  dcb.wReserved       = 0;
  dcb.XonLim          = 2;
  dcb.XoffLim         = 4;
  dcb.XonChar         = 0x13;
  dcb.XoffChar        = 0x19;
  dcb.EvtChar         = 0;

  ret = SetCommState(hComm, &dcb);
  PurgeComm(hComm, PURGE_RXCLEAR | PURGE_TXCLEAR | PURGE_RXABORT | PURGE_TXABORT);

  return hComm;
}
//-----------------------------------------------------------------------------
i32 comDisConn(HANDLE hComm)
{
  CloseHandle(hComm);
  return 1;
}
//-----------------------------------------------------------------------------
i32 comSendBuf(HANDLE hComm, char* Buf, i32 Len)
{
  i32 ret;
  u32  BytesToSend = 0;
  if (hComm == INVALID_HANDLE_VALUE) return 0;

  ret = WriteFile(hComm, Buf, Len, &BytesToSend, NULL);
  if (!ret)  
  {  
    u32 dwError = GetLastError();    
    PurgeComm(hComm, PURGE_RXCLEAR | PURGE_RXABORT);//清空串口缓冲区
    return 0;
  }  
  return BytesToSend;
}
//-----------------------------------------------------------------------------
i32 comRecvBuf(HANDLE hComm, char* Buf, i32 Len)
{  
  BOOL  ret = TRUE;
  u32 BytesRead = 0;
  if (hComm == INVALID_HANDLE_VALUE) return 0;

  ret = ReadFile(hComm, Buf, Len, &BytesRead, NULL);//从缓冲区读取一个字节的数据
  if ((!ret))  
  {  
    u32 dwError = GetLastError();
    PurgeComm(hComm, PURGE_RXCLEAR | PURGE_RXABORT);//清空串口缓冲区
    return 0;
  }
  return BytesRead;
}
//-----------------------------------------------------------------------------
i32 comRecvLength(HANDLE hComm)
{  
  u32 dwError = 0;
  COMSTAT comstat;
  i32 BytesInQue = 0;
  memset(&comstat, 0, sizeof(COMSTAT));
  if (ClearCommError(hComm, &dwError, &comstat))  
  {  
    BytesInQue = comstat.cbInQue;//获取在输入缓冲区中的字节数
  }  
  return BytesInQue;
}
//-----------------------------------------------------------------------------
i32 comWaitForData(HANDLE hComm, i32 TimeOut)
{
#if 1
  i32 ret;
  u32 dt = GetTickCount();
  while (1)
  {
    Sleep(10);
    if (GetTickCount() - dt >= TimeOut) break;
    ret = comRecvLength(hComm);
    if (ret > 0) break;
  }
  return ret;
#else
  u32 dwError = 0;
  COMSTAT comstat;
  i32 BytesInQue = 0;
  i32 ret;
  ret = WaitForSingleObject(hComm, 0);//INFINITE
  switch (ret)
  {
  case WAIT_FAILED:
    return 0;

  case WAIT_TIMEOUT:
    return 0;

  case WAIT_OBJECT_0:
    memset(&comstat, 0, sizeof(COMSTAT));
    if (ClearCommError(hComm, &dwError, &comstat))  
    {  
      BytesInQue = comstat.cbInQue;//获取在输入缓冲区中的字节数
    }  
    return BytesInQue;
  }
  return 0;
#endif
}
//-----------------------------------------------------------------------------
i32 UnicodeToAnsi(char* src, char* dst)
{
  i32 Len;
  WCHAR* wBuf = (WCHAR*)src;
  Len = WideCharToMultiByte(CP_ACP, 0, wBuf, -1, NULL, 0, NULL, NULL);
  Len = WideCharToMultiByte(CP_ACP, 0, wBuf, -1, dst, Len, NULL,NULL);  
  dst[Len] = 0x00;
  return Len;
}
//-----------------------------------------------------------------------------
i32 UnicodeTxtLen(char* src)
{
  i32 i = 0;
  i32 ret = 0;
  short* ssrc = (short*)src;
  while (1)
  {
    if (ssrc[i] == 0x0000) return (i + 1) * 2;
    i++;
  }
  return 0;
}
//-----------------------------------------------------------------------------
i32 AnsiToUnicode(char* src, char* dst)
{
  i32 Len;
  Len = MultiByteToWideChar(CP_ACP, 0, src, -1, NULL, 0);
  Len = MultiByteToWideChar(CP_ACP, 0, src, -1, dst, Len);
  Len = UnicodeTxtLen(dst);
  dst[Len] = 0x00;
  return Len;
}
//-----------------------------------------------------------------------------
char* ShareMemOpenFile(const char* FileName, HANDLE* FileHandle, HANDLE* MapHandle, u32 dwFileSize)
{
  HANDLE iFileHandle, iMapHandle;
  char* Buf;

  if (!FileExists((char*)FileName))
  {
    iFileHandle = FileCreate((char*)FileName);
    FileClose(iFileHandle);
    iFileHandle = NULL;
  }

  iFileHandle = CreateFileA(FileName, GENERIC_WRITE || GENERIC_READ, FILE_SHARE_READ || FILE_SHARE_WRITE, NULL, CREATE_ALWAYS || OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, NULL);
  if (iFileHandle == INVALID_HANDLE_VALUE) return NULL;

  iMapHandle = CreateFileMappingA(iFileHandle, NULL, PAGE_READWRITE, 0, dwFileSize, NULL);
  if (iMapHandle == 0)
  {
    FileClose(iFileHandle);
    return NULL;
  }

  Buf = MapViewOfFile(iMapHandle, FILE_MAP_READ || FILE_MAP_WRITE, 0, 0, 0);
  if (!Buf)
  {
    CloseHandle(iMapHandle);
    iMapHandle = NULL;
    CloseHandle(iFileHandle);
    iFileHandle = NULL;
    return NULL;
  }
  *FileHandle = iFileHandle;
  *MapHandle = iMapHandle;
  return Buf;
}
//-----------------------------------------------------------------------------
bool ShareMemCloseFile(HANDLE FileHandle, HANDLE MapHandle, void* FData)
{
  if (FData) UnmapViewOfFile(FData);
  if (MapHandle) CloseHandle(MapHandle);
  if (FileHandle != INVALID_HANDLE_VALUE) CloseHandle(FileHandle);
  return true;
}
#endif
//-----------------------------------------------------------------------------
HANDLE FileCreate(char *FileName)//创建一个新文件
{
#ifndef WIN32
  return fopen(FileName, "w+b");
#else
  return (HANDLE)CreateFileA(FileName, GENERIC_READ | GENERIC_WRITE, 0, NULL, CREATE_ALWAYS, FILE_ATTRIBUTE_NORMAL, 0);
#endif
}
//-----------------------------------------------------------------------------
HANDLE FileOpen(char *FileName)//打开一个文件，可读可写
{
  HANDLE ret;
#ifndef WIN32
  ret = fopen(FileName, "r+b");
#else
  ret = (HANDLE)CreateFileA(FileName, GENERIC_READ | GENERIC_WRITE, FILE_SHARE_READ, NULL, OPEN_EXISTING, FILE_ATTRIBUTE_NORMAL, 0);
#endif
  return ret;
}
//-----------------------------------------------------------------------------
bool FileClose(HANDLE f)//关闭文件
{
#ifndef WIN32
  return (fclose(f) == 0);
#else
  return CloseHandle(f);
#endif
}
//-----------------------------------------------------------------------------
bool FileWrite(HANDLE f, void *Buf, i32 Len)//写文件
{
#ifndef WIN32
  return (fwrite(Buf, Len, 1, f) != 0);
#else
  i32 Result;
  WriteFile(f, Buf, Len, &Result, NULL);
  return (Result == Len);
#endif
}
//-----------------------------------------------------------------------------
bool FileRead(HANDLE f, void *Buf, i32 Len)//读文件
{
#ifndef WIN32
  return (fread(Buf, Len, 1, f) != 0);
#else
  i32 Result;
  ReadFile(f, Buf, Len, &Result, NULL);
  return (Result == Len);
#endif
}
//-----------------------------------------------------------------------------
u32 FileSeek(HANDLE f, i32 Offset, i32 Origin)//文件定位 Origin=0从文件头  =1 当前位置  =2 从文件尾
{
#ifndef WIN32
  fpos_t pos;
  if (fseek(f, Offset, Origin) == 0)
  {
    fgetpos(f, &pos);
#ifdef ANDROID
    return pos;
#else
    return pos.__pos;
#endif
  } else
    return -1;
#else
  return SetFilePointer(f, Offset, NULL, Origin);
#endif
}
//-----------------------------------------------------------------------------
u32 FileGetPos(HANDLE f)//取得文件当前位置
{
#ifndef WIN32
  fpos_t pos;
  memset(&pos, 0, sizeof(pos));
  fgetpos(f, &pos);
#ifdef ANDROID
  return pos;
#else
  return pos.__pos;
#endif
#else
  return FileSeek(f, 0, 1);
#endif
}
//-----------------------------------------------------------------------------
bool FileDelete(char *FileName)//删除文件
{
#ifndef WIN32
  return (unlink(FileName) != -1);
#else
  return DeleteFile(FileName);
#endif
}

//-----------------------------------------------------------------------------
char *FileExtName(char *FileName)//'.txt'取得文件扩展名
{
  char *Ext = NULL;
  i32 i, m;
  m = strlen(FileName);
  for (i = m - 1; i >= 0; i--)
  {
    if (FileName[i] != '.') continue;
    if (i < m - 1) Ext = &FileName[i];
    break;
  }
  return Ext;
}

//-----------------------------------------------------------------------------
char *ExtractFileName(char *FileName)
{
  char *Ext = FileName;
  i32 i, m;
  m = strlen(FileName);
  for (i = m - 1; i >= 0; i--)
  {
    if ((FileName[i] != '/') && (FileName[i] != '\\')) continue;
    if (i < m - 1) Ext = &FileName[i + 1];
    break;
  }
  return Ext;
}
//-----------------------------------------------------------------------------
bool DirectoryExists(char *Directory)//目录是否存在
{
#ifndef WIN32
  struct stat st;
  if (stat(Directory, &st) == -1)
    return false;
  else
    return S_ISDIR(st.st_mode);
#else
  i32 Code;
  Code = GetFileAttributes(Directory);
  return ((Code != -1) && ((FILE_ATTRIBUTE_DIRECTORY & Code) != 0));
#endif
}
//-----------------------------------------------------------------------------
bool FileExists(char *FileName)//文件是否存在
{
#ifndef WIN32
  struct stat st;
  return (stat(FileName, &st) != -1);
#else
  WIN32_FIND_DATAA FindData;
  HANDLE Handle = FindFirstFile(FileName, &FindData);
  if (Handle == INVALID_HANDLE_VALUE) return false;
  FindClose(Handle);
  return true;
#endif
}
//-----------------------------------------------------------------------------
u32 FileGetSize(char *FileName)//取得文件大小
{
#ifndef WIN32
  struct stat statbuf;
  i32 i = stat(FileName, &statbuf);
  if (i < 0) return 0;
  S_ISDIR(statbuf.st_mode);
  S_ISREG(statbuf.st_mode);
  return statbuf.st_size;
#else
  WIN32_FIND_DATAA FindData;
  HANDLE Handle = FindFirstFile(FileName, &FindData);
  if (Handle == INVALID_HANDLE_VALUE) return -1;
  FindClose(Handle);
  return FindData.nFileSizeLow;
#endif
}
//-----------------------------------------------------------------------------
bool InArray(i32 Value, i32 Count, ...)//数值是否在...之中
{
  i32 i, m;
  char *args = (char *) &Count;//定位第一个参数
  for (i = 0; i < Count; i++)
  {
    args = args + 4;
    m = *(i32 *) args;
    if (Value == m) return true;
  }
  return false;
}
//-----------------------------------------------------------------------------
i32 RandomNum(i32 seed)//随机函数
{
  i32 RandSeed;
#ifndef WIN32
  struct timeval tv;
  gettimeofday(&tv, NULL);
  RandSeed = tv.tv_sec + tv.tv_usec;
  srandom(RandSeed);   //srandom(time(NULL));
  return random() % seed;
#else
  RandSeed = GetTickCount();
  srand(RandSeed); 
  return rand() % seed;
#endif
}
//-----------------------------------------------------------------------------
i32 timeval_dec(struct timeval *tv2, struct timeval *tv1)//时间相减，返回毫秒
{
  return (tv2->tv_sec - tv1->tv_sec) * 1000 + (tv2->tv_usec - tv1->tv_usec) / 1000;
}

//-----------------------------------------------------------------------------
void Time_tToSystemTime(i32 t, SYSTEMTIME *pst)//linux时间到windows时间
{
#ifndef WIN32
  struct timeval tv;
  struct tm *m;
  gettimeofday(&tv, NULL);
  m = localtime(&tv.tv_sec);
  pst->wYear = m->tm_year + 1900;
  pst->wMonth = m->tm_mon + 1;
  pst->wDay = m->tm_mday;
  pst->wHour = m->tm_hour;
  pst->wMinute = m->tm_min;
  pst->wSecond = m->tm_sec;
  pst->wMilliseconds = tv.tv_usec / 1000;
  pst->wDayOfWeek = m->tm_wday;
  printf("pst->wYear:%d,pst->wMonth:%d,pst->wMilliseconds:%d, pst->wDayOfWeek:%d pst->wHour:%d\n", pst->wYear, pst->wMonth,
         pst->wMilliseconds, pst->wDayOfWeek, pst->wHour);
#else
  FILETIME ft;
  LONGLONG ll = Int32x32To64(t, 10000000) + 116444736000000000;
  ft.dwLowDateTime = (u32) ll;
  ft.dwHighDateTime = (u32)(ll >> 32);
  FileTimeToSystemTime(&ft, pst);
#endif
}
//-----------------------------------------------------------------------------
i32 SystemTimeToTime_t(SYSTEMTIME *pst)//windows时间到linux时间
{
#ifndef WIN32
  struct tm m;
  m.tm_year = pst->wYear - 1900;
  m.tm_mon = pst->wMonth - 1;
  m.tm_mday = pst->wDay;
  m.tm_hour = pst->wHour;
  m.tm_min = pst->wMinute;
  m.tm_sec = pst->wSecond;
  m.tm_wday = pst->wDayOfWeek;
  return mktime(&m);
#else//#ifdef WIN32
  FILETIME ft;
  LONGLONG ll;
  ULARGE_INTEGER ui;
  SystemTimeToFileTime(pst, &ft );
  ui.LowPart = ft.dwLowDateTime;
  ui.HighPart = ft.dwHighDateTime;
  ll = (ft.dwHighDateTime << 32) + ft.dwLowDateTime;
  return (i32)((LONGLONG)(ui.QuadPart - 116444736000000000) / 10000000);
#endif
}
//-----------------------------------------------------------------------------
i32 GetTime() //取得系统时间
{
#ifndef WIN32
  return time(NULL);
#else
  SYSTEMTIME st;
  GetLocalTime(&st);
  return SystemTimeToTime_t(&st);
#endif
}
//-----------------------------------------------------------------------------
i32 GetTimezoneTime()
{
  time_t t, iaddsec;
  struct tm *tm_local;

  t = 0;
  tm_local = localtime(&t);
  iaddsec = tm_local->tm_hour * 3600 + tm_local->tm_min * 60 + tm_local->tm_sec;
  return time(NULL) + iaddsec;
}
//-----------------------------------------------------------------------------
i64 getutime() //取得微秒级时间
{
#ifndef WIN32
  struct timeval tv;
  gettimeofday(&tv, NULL);
  return (i64) (tv.tv_sec) * 1000000 + tv.tv_usec;
#else
  struct timeval tv;
  SYSTEMTIME st;
  GetLocalTime(&st);
  tv.tv_sec = SystemTimeToTime_t(&st);
  tv.tv_usec = st.wMilliseconds * 1000;
  return (i64)(tv.tv_sec)* 1000000+tv.tv_usec;
#endif
}

//-----------------------------------------------------------------------------
void QuickSort(i32 *Lst, i32 iLo, i32 iHi)//排序
{
  i32 Lo, Hi, Mid, T;
  Lo = iLo;
  Hi = iHi;
  Mid = Lst[(Lo + Hi) / 2];
  while (Lo <= Hi)
  {
    while (Lst[Lo] < Mid) Lo++;
    while (Lst[Hi] > Mid) Hi--;
    if (Lo <= Hi)
    {
      T = Lst[Lo];
      Lst[Lo] = Lst[Hi];
      Lst[Hi] = T;
      Lo++;
      Hi--;
    }
  }//until (Lo>Hi);
  if (Hi > iLo) QuickSort(Lst, iLo, Hi);
  if (Lo < iHi) QuickSort(Lst, Lo, iHi);
}
//------------------------------------------------------------------------------
i32 SearchByDichotomy(i32 *Lst, i32 iL, i32 iH, i32 Key)//两分法
{
  i32 iLow, iHigh, iMid, Result;
  iLow = iL;//Low(Lst);
  iHigh = iH;//High(Lst);
  Result = -1;
  while (iLow <= iHigh)
  {
    iMid = (iLow + iHigh) / 2;
    if (Key < Lst[iMid])
    {
      iHigh = iMid - 1;
    } else if (Key > Lst[iMid])
    {
      iLow = iMid + 1;
    } else if (Key = Lst[iMid])
    {
      Result = iMid;
      return Result;
    }
  }
  return Result;
}
//------------------------------------------------------------------------------
#define P_32        0xEDB88320L
i32 crc_tab32_init = false;
u32 crc_tab32[256];

void init_crc32_tab(void)
{
  i32 i, j;
  u32 crc;
  for (i = 0; i < 256; i++)
  {
    crc = (u32) i;
    for (j = 0; j < 8; j++)
    {
      if (crc & 0x00000001L)
        crc = (crc >> 1) ^ P_32;
      else
        crc = crc >> 1;
    }
    crc_tab32[i] = crc;
  }
  crc_tab32_init = true;
}
//-----------------------------------------------------------------------------
u32 crc32(char *buf, i32 buflen)
{
  u32 crc;
  u32 tmp, long_c;
  char c;
  i32 i;
  if (!crc_tab32_init) init_crc32_tab();
  crc = 0xffffffffL;
  for (i = 0; i < buflen; i++)
  {
    c = buf[i];
    long_c = 0x000000ffL & (u32) c;
    tmp = crc ^ long_c;
    crc = (crc >> 8) ^ crc_tab32[tmp & 0xff];
  }
  return crc ^ 0xffffffffL;
}
//-----------------------------------------------------------------------------
u32 crc32F(char *FileName)
{
  FILE *f;
  i32 buflen;
  char *buf;
  u32 crc;
  buflen = FileGetSize(FileName);
  if (buflen <= 0) return 0;
  f = fopen(FileName, "r+b");
  if (!f) return 0;
  buf = (char *) malloc(buflen);
  if (!buf) return 0;
  fread(buf, buflen, 1, f);
  fclose(f);
  crc = crc32(buf, buflen);
  free(buf);
  return crc;
}
//-----------------------------------------------------------------------------
u32 crc32F2(char *FileName, i32 FileSize)
{
  FILE *f;
  char *buf;
  u32 crc;
  if (FileSize <= 0) return 0;
  f = fopen(FileName, "r+b");
  if (!f) return 0;
  buf = (char *) malloc(FileSize);
  if (!buf) return 0;
  fread(buf, FileSize, 1, f);
  fclose(f);
  crc = crc32(buf, FileSize);
  free(buf);
  return crc;
}

//-----------------------------------------------------------------------------
char *LowerCase(char *s)//字符串，小写转大写
{
  i32 i;
  for (i = 0; i < strlen(s); i++)
  {
    if (s[i] >= 'A' && s[i] <= 'Z') s[i] = s[i] + 32;
  }
  return s;
}

//-----------------------------------------------------------------------------
char *UpperCase(char *s)//字符串，大写转小写
{
  i32 i;
  for (i = 0; i < strlen(s); i++)
  {
    if (s[i] >= 'a' && s[i] <= 'z') s[i] = s[i] - 32;
  }
  return s;
}
//-----------------------------------------------------------------------------
i32 CharCount(char c, const char *S)//字符数统计
{
  i32 Result = 0;
  i32 i;
  for (i = 0; i < strlen(S); i++) if (S[i] == c) Result++;
  return Result;
}
//-----------------------------------------------------------------------------
bool IsValidIP(const char *IP)//是否是有效的IP地址
{
  i32 i, m, len;
  i32 d[4];
  char ip[20];
  len = strlen(IP);
  if (len > 15) return false;
  strcpy(ip, IP);
  for (i = 0; i < len; i++)
  {
    if (ip[i] == '.')
      ip[i] = 0x20;//space
    else if ((ip[i] < '0') || (ip[i] > '9')) return false;
  }
  m = sscanf(ip, "%d %d %d %d", &d[3], &d[2], &d[1], &d[0]);
  if (m != 4) return false;
  for (i = 0; i < 4; i++)
  {
    if ((d[i] < 0) || (d[i] > 255)) return false;
  }
  return true;
}
//-----------------------------------------------------------------------------
bool IsValidHost(const char *Host)//是否是有效的域名
{
  i32 i;
  if (CharCount('.', Host) < 1) return false;
  if (CharCount('@', Host) > 0) return false;
  for (i = 0; i < strlen(Host); i++)
  {
    if (Host[i] < '.') return false;
    if (Host[i] > 'z') return false;
    if (Host[i] == '\\') return false;
    if (Host[i] == '[') return false;
    if (Host[i] == ']') return false;
    if (Host[i] == '(') return false;
    if (Host[i] == ')') return false;
    if (Host[i] == '^') return false;
    if (Host[i] == '`') return false;
  }
  return true;
}
//-----------------------------------------------------------------------------
bool IsValidMAC(const char *MAC, char SplitChar)//是否是有效的MAC地址
{
  i32 i, Ret;
  i32 d[6];
  char m[20];
  strcpy(m, MAC);
  for (i = 0; i < strlen(m); i++)
  {
    if (m[i] == SplitChar)
      m[i] = 0x20;//space
    else if (!(((m[i] >= '0') && (m[i] <= '9')) || ((m[i] >= 'a') && (m[i] <= 'f')) || ((m[i] >= 'A') && (m[i] <= 'F')))) return false;
  }
  Ret = sscanf(m, "%x %x %x %x %x %x", &d[5], &d[4], &d[3], &d[2], &d[1], &d[0]);
  if (Ret != 6) return false;
  for (i = 0; i < 6; i++)
  {
    if ((d[i] < 0) || (d[i] > 0xff)) return false;
  }
  return true;
}
//-----------------------------------------------------------------------------
bool IsLANIP(char *IP)//是否是内网IP地址
{
  u32 nIP;
  u8 a, b;

  nIP = IPToInt(IP);
  a = (u8) nIP;
  b = (u8) (nIP >> 8);
  return (((a == 192) && (b == 168)) || ((a == 172) && (b >= 16) && (b <= 31)) || (a == 0) || (b == 10));
}
//------------------------------------------------------------------------------
i32 IPToInt(char *IP)
{
  return inet_addr(IP);
}

//------------------------------------------------------------------------------
char *IntToIP(i32 IP)
{
  return inet_ntoa(*(struct in_addr *) &IP);
}
//-----------------------------------------------------------------------------
i32 Base64Encode1(char *src, char *dst)
{
  static const char *lst = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
  i32 len;
  char *strnew;
  char *strold;

  if (!src) return 0;
  if (!dst) return 0;

  len = strlen(src);
  strnew = dst;
  strold = src;

  while ((len - (strold - src)) >= 3)
  {
    strnew[0] = lst[(strold[0] & (u8) 0xFC) >> 2];
    strnew[1] = lst[((strold[0] & (u8) 0x03) << 4) | ((strold[1] & (u8) 0xF0) >> 4)];
    strnew[2] = lst[((strold[1] & (u8) 0x0F) << 2) | ((strold[2] & (u8) 0xC0) >> 6)];
    strnew[3] = lst[strold[2] & (u8) 0x3F];
    strnew += 4;
    strold += 3;
  }

  switch (len - (strold - src))
  {
    case 1:
      strnew[0] = lst[(strold[0] & (u8) 0xFC) >> 2];
      strnew[1] = lst[(strold[0] & (u8) 0x03) << 4];
      strnew[2] = '=';
      strnew[3] = '=';
      strnew += 4;
      break;

    case 2:
      strnew[0] = lst[(strold[0] & (u8) 0xFC) >> 2];
      strnew[1] = lst[((strold[0] & (u8) 0x03) << 4) | ((strold[1] & (u8) 0xF0) >> 4)];
      strnew[2] = lst[(strold[1] & (u8) 0x0F) << 2];
      strnew[3] = '=';
      strnew += 4;
  }
  return 1;
}
//-----------------------------------------------------------------------------
bool GetIPPortFromAddr(struct sockaddr_in Addr, char *IP, u16 *Port)
{
  //  CltAddr.sin_family = AF_INET;
  //  CltAddr.sin_addr.s_addr = inet_addr(DDNSSvrIP);
  //  CltAddr.sin_port = htons(Port);

  char *tmpIP;
  if (!IP) return false;
  tmpIP = inet_ntoa(Addr.sin_addr);
  strcpy(IP, tmpIP);
  *Port = ntohs(Addr.sin_port);
  return true;
}

//-----------------------------------------------------------------------------
void __encodeBase64(unsigned char *in, unsigned char *out)
{
  static const unsigned char encodeBase64Map[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
  out[0] = encodeBase64Map[(in[0] >> 2) & 0x3F];
  out[1] = encodeBase64Map[((in[0] << 4) & 0x30) | ((in[1] >> 4) & 0x0F)];
  out[2] = encodeBase64Map[((in[1] << 2) & 0x3C) | ((in[2] >> 6) & 0x03)];
  out[3] = encodeBase64Map[in[2] & 0x3F];
}
//-----------------------------------------------------------------------------
i32 Base64Encode(unsigned char *inbuf, i32 insize, unsigned char *outbuf, i32 outsize)
{
  i32 inpos = 0, outpos = 0;
  while (inpos != insize)
  {
    if (inpos + 3 <= insize)
    {
      if (outpos + 4 > outsize) return -1;
      __encodeBase64(inbuf + inpos, outbuf + outpos);
      inpos += 3;
      outpos += 4;
    }

    if (insize - inpos == 2)
    {
      unsigned char tail[3] = {0};
      tail[0] = *(inbuf + inpos);
      tail[1] = *(inbuf + inpos + 1);
      __encodeBase64(tail, outbuf + outpos);
      *(outbuf + outpos + 3) = '=';
      inpos += 2;
      outpos += 4;
    }
    if (insize - inpos == 1)
    {
      unsigned char tail[3] = {0};
      tail[0] = *(inbuf + inpos);
      __encodeBase64(tail, outbuf + outpos);
      *(outbuf + outpos + 3) = '=';
      *(outbuf + outpos + 2) = '=';
      inpos += 1;
      outpos += 4;
    }
  }
  return outpos;
}

//-----------------------------------------------------------------------------
unsigned char __decodeBase64Map(unsigned char a)
{
  if (a >= 'A' && a <= 'Z')
    return a - 'A';
  if (a >= 'a' && a <= 'z')
    return 26 + a - 'a';
  if (a >= '0' && a <= '9')
    return 52 + a - '0';
  if (a == '+')
    return 62;
  if (a == '/')
    return 63;
  if (a == '=')
    return 0;
  return -1;
}

//-----------------------------------------------------------------------------
void __decodeBase64(unsigned char *in, unsigned char *out)
{
  unsigned char map[4];
  map[0] = __decodeBase64Map(in[0]);
  map[1] = __decodeBase64Map(in[1]);
  map[2] = __decodeBase64Map(in[2]);
  map[3] = __decodeBase64Map(in[3]);
  out[0] = ((map[0] << 2) & 0xFC) | ((map[1] >> 4) & 0x03);
  out[1] = ((map[1] << 4) & 0xF0) | ((map[2] >> 2) & 0x0F);
  out[2] = ((map[2] << 6) & 0xC0) | ((map[3] >> 0) & 0x3F);
}
//-----------------------------------------------------------------------------
i32 Base64Decode(unsigned char *inbuf, i32 insize, unsigned char *outbuf, i32 outsize)
{
  i32 inpos = 0, outpos = 0;
  if (insize % 4) return -1;

  while (inpos != insize)
  {
    if (outpos + 3 > outsize) return -1;
    __decodeBase64(inbuf + inpos, outbuf + outpos);
    if (*(inbuf + inpos + 2) == '=')
    {
      outpos += 1;
      break;
    } else if (*(inbuf + inpos + 3) == '=')
    {
      outpos += 2;
      break;
    } else
    {
      outpos += 3;
    }
    inpos += 4;
  }
  return outpos;
}
//-----------------------------------------------------------------------------
bool IsSameSegmentIP(char *IP1, char *IP2)//两个IP是否在同一段
{
  struct sockaddr_in s1;
  struct sockaddr_in s2;
  inet_aton(IP1, &s1.sin_addr);
  inet_aton(IP2, &s2.sin_addr);
  int a, b;
  memcpy(&a, &s1.sin_addr, 4);
  memcpy(&b, &s2.sin_addr, 4);
  return ((int) (a << 8) == (int) (b << 8));
}

//-----------------------------------------------------------------------------
char *GetLocalIP()
{
#ifdef WIN32

  static char ip[100];
  HOSTENT* host;
  char hostname[256];  
  i32 ret;
  WSADATA wsaData;  
  WSAStartup(MAKEWORD(2, 2), &wsaData);  
  ret = gethostname(hostname, sizeof(hostname));  
  if (ret == SOCKET_ERROR) return NULL;
  host = gethostbyname(hostname);  
  if (host == NULL) return NULL;
  strcpy(ip, inet_ntoa(*(struct in_addr*)*host->h_addr_list));  
  return ip;

#else

  struct ifconf conf;
  struct ifreq *ifr;
  char buff[512];
  i32 num;
  i32 i, hSkt;
  for (i = 0; i < 9; i++)
  {
    hSkt = socket(PF_INET, SOCK_DGRAM, 0);
    if (hSkt > 0) break;
  }
  if (hSkt <= 0) return NULL;
  conf.ifc_len = 512;
  conf.ifc_buf = buff;
  ioctl(hSkt, SIOCGIFCONF, &conf);
  num = conf.ifc_len / sizeof(struct ifreq);
  ifr = conf.ifc_req;
  for (i = 0; i < num; i++)
  {
    struct sockaddr_in *sin = (struct sockaddr_in *) (&ifr->ifr_addr);
    ioctl(hSkt, SIOCGIFFLAGS, ifr);
    if (((ifr->ifr_flags & IFF_LOOPBACK) == 0) && (ifr->ifr_flags & IFF_UP))
    {
      closesocket(hSkt);
      return inet_ntoa(sin->sin_addr);
    }
    ifr++;
  }

  closesocket(hSkt);
  return NULL;
#endif
}

//-----------------------------------------------------------------------------
//SHA1状态数据结构类型
typedef struct TSHA_State
{
  u32 h[5]; // 5个初始链接变量;保存20字节摘要
  u8 block[64]; // 分组
  i32 blkused;
  u32 lenhi, lenlo; // 长度域
} TSHA_State;

//-----------------------------------------------
void SHA_Init(TSHA_State *s)
{
  s->h[0] = 0x67452301;
  s->h[1] = 0xefcdab89;
  s->h[2] = 0x98badcfe;
  s->h[3] = 0x10325476;
  s->h[4] = 0xc3d2e1f0;
  s->blkused = 0;
  s->lenhi = s->lenlo = 0;
}

//-----------------------------------------------
void SHATransform(u32 *digest, u32 *block)
{
#define rol(x, y) ( ((x) << (y)) | (((u32)x) >> (32-y)) )
  u32 w[80];
  u32 a, b, c, d, e;
  u32 tmp;
  i32 t;
  for (t = 0; t < 16; t++) w[t] = block[t];
  for (t = 16; t < 80; t++)
  {
    tmp = w[t - 3] ^ w[t - 8] ^ w[t - 14] ^ w[t - 16];
    w[t] = rol(tmp, 1);
  }

  a = digest[0];
  b = digest[1];
  c = digest[2];
  d = digest[3];
  e = digest[4];

  for (t = 0; t < 20; t++)
  {
    tmp = rol(a, 5) + ((b & c) | (d & ~b)) + e + w[t] + 0x5a827999;
    e = d;
    d = c;
    c = rol(b, 30);
    b = a;
    a = tmp;
  }

  for (t = 20; t < 40; t++)
  {
    tmp = rol(a, 5) + (b ^ c ^ d) + e + w[t] + 0x6ed9eba1;
    e = d;
    d = c;
    c = rol(b, 30);
    b = a;
    a = tmp;
  }

  for (t = 40; t < 60; t++)
  {
    tmp = rol(a, 5) + ((b & c) | (b & d) | (c & d)) + e + w[t] + 0x8f1bbcdc;
    e = d;
    d = c;
    c = rol(b, 30);
    b = a;
    a = tmp;
  }

  for (t = 60; t < 80; t++)
  {
    tmp = rol(a, 5) + (b ^ c ^ d) + e + w[t] + 0xca62c1d6;
    e = d;
    d = c;
    c = rol(b, 30);
    b = a;
    a = tmp;
  }

  digest[0] += a;
  digest[1] += b;
  digest[2] += c;
  digest[3] += d;
  digest[4] += e;
}

//-----------------------------------------------
void SHA_Bytes(TSHA_State *s, void *p, i32 len)
{
  i32 i;
  u32 wordblock[16];
  u32 lenw = len;
  unsigned char *q = (unsigned char *) p;

  s->lenlo += lenw;
  s->lenhi += (s->lenlo < lenw);

  if (s->blkused && s->blkused + len < 64)
  {
    memcpy(s->block + s->blkused, q, len);
    s->blkused += len;
  } else
  {
    while (s->blkused + len >= 64)
    {
      memcpy(s->block + s->blkused, q, 64 - s->blkused);
      q += 64 - s->blkused;
      len -= 64 - s->blkused;

      for (i = 0; i < 16; i++)
      {
        wordblock[i] = (((u32) s->block[i * 4 + 0]) << 24) | (((u32) s->block[i * 4 + 1]) << 16) | (((u32) s->block[i * 4 + 2]) << 8) |
                       (((u32) s->block[i * 4 + 3]) << 0);
      }
      SHATransform(s->h, wordblock);
      s->blkused = 0;
    }
    memcpy(s->block, q, len);
    s->blkused = len;
  }
}

//-----------------------------------------------
void SHA_Final(TSHA_State *s, unsigned char *output)
{
  i32 i, pad;
  unsigned char c[64];
  u32 lenhi, lenlo;

  if (s->blkused >= 56) pad = 56 + 64 - s->blkused; else pad = 56 - s->blkused;
  lenhi = (s->lenhi << 3) | (s->lenlo >> (32 - 3));
  lenlo = (s->lenlo << 3);
  memset(c, 0, pad);
  c[0] = 0x80;
  SHA_Bytes(s, &c, pad);

  c[0] = (lenhi >> 24) & 0xFF;
  c[1] = (lenhi >> 16) & 0xFF;
  c[2] = (lenhi >> 8) & 0xFF;
  c[3] = (lenhi >> 0) & 0xFF;
  c[4] = (lenlo >> 24) & 0xFF;
  c[5] = (lenlo >> 16) & 0xFF;
  c[6] = (lenlo >> 8) & 0xFF;
  c[7] = (lenlo >> 0) & 0xFF;

  SHA_Bytes(s, &c, 8);

  for (i = 0; i < 5; i++)
  {
    output[i * 4] = (s->h[i] >> 24) & 0xFF;
    output[i * 4 + 1] = (s->h[i] >> 16) & 0xFF;
    output[i * 4 + 2] = (s->h[i] >> 8) & 0xFF;
    output[i * 4 + 3] = (s->h[i]) & 0xFF;
  }
}
//-----------------------------------------------
i32 SHA1Encode(unsigned char *inbuf, i32 insize, unsigned char *outbuf, i32 outsize)
{
  TSHA_State s;
  SHA_Init(&s);
  SHA_Bytes(&s, inbuf, insize);
  SHA_Final(&s, outbuf);
  return strlen(outbuf);
}
//-----------------------------------------------------------------------------
HTTPERROR HttpGet(const char *url, char *OutBuf, i32 *OutBufLen, bool IsShowHead, i32 TimeOut)
{
  return HttpPost(url, NULL, 0, OutBuf, OutBufLen, IsShowHead, TimeOut);
}
//-----------------------------------------------------------------------------
HTTPERROR HttpPost(const char *url, char *InBuf, i32 InBufLen, char *OutBuf, i32 *OutBufLen, bool IsShowHead, i32 TimeOut)
{
#ifndef WIN32
#define SOCKET_ERROR -1
#define INVALID_SOCKET -1
#endif
#define SD_SEND 1
  char StrTmp[13];
  char StrHEAD[13];
  i32 iErrorCode;
  char256 SvrName, HostName, UserNamePassword, b64UserNamePassword, tmpBuf1;
  char SendStr[2048];
  char PageName[1024];
  struct sockaddr_in addr;
  struct hostent *h;
  unsigned long tmp_address = INADDR_NONE;
  i32 recvLen = 0;
  i32 port = 80;
  i32 m[4];
  SOCKET hSocket = INVALID_SOCKET;
  fd_set fs;
  struct timeval tv;
  time_t t;
  i32 ret, i;
  i32 iContentLength = 0;
  char *tmpBuf;
  i32 itmp;
  char *strGetPost = NULL;

  iErrorCode = 0;
  if (!OutBuf) return iErrorCode;

  OutBuf[0] = 0x00;
  *OutBufLen = 0;
  HostName[0] = 0x00;

  ret = sscanf(url, "http://%[^@]@%[^/]%s", UserNamePassword, SvrName, PageName);
  if (ret != 3)
  {
    ret = sscanf(url, "http://%[^/]%s", SvrName, PageName);
    UserNamePassword[0] = 0x00;
    if (ret != 2) sprintf(PageName, "/");
  }

  ret = sscanf(SvrName, "%[^:]:%d", HostName, &port);
  if (ret == 2)
  {
    if ((strlen(HostName) == 0) || (port <= 0) || (port > 0xffff)) return iErrorCode;
  } else
  {
    strcpy(HostName, SvrName);
    port = 80;
  }

  addr.sin_family = AF_INET;
  addr.sin_port = htons((u16) port);

  ret = sscanf(HostName, "%d.%d.%d.%d", &m[0], &m[1], &m[2], &m[3]);
  if ((ret == 4) && (m[0] >= 0) && (m[0] <= 255) && (m[1] >= 0) && (m[1] <= 255) && (m[2] >= 0) && (m[2] <= 255) && (m[3] >= 0) &&
      (m[3] <= 255))
  {
    if ((tmp_address = inet_addr(HostName)) != INADDR_NONE)
      memcpy(&addr.sin_addr, &tmp_address, sizeof(unsigned long));
    else return iErrorCode;
  } else
  {
    if ((h = gethostbyname(HostName)) != NULL)
      memcpy(&addr.sin_addr, h->h_addr_list[0], h->h_length);
    else return iErrorCode;
  }

  if (InBuf && InBufLen > 0)
  {//POST
    if (strlen(UserNamePassword) > 0)
    {
      memset(b64UserNamePassword, 0, sizeof(b64UserNamePassword));
      Base64Encode(UserNamePassword, strlen(UserNamePassword), b64UserNamePassword, sizeof(b64UserNamePassword));
      sprintf(SendStr, "POST %s HTTP/1.0\r\nHost: %s\r\nContent-Length: %d\r\nAuthorization: Basic %s\r\n\r\n", PageName, HostName,
              InBufLen, b64UserNamePassword);
    } else
    {
      sprintf(SendStr, "POST %s HTTP/1.0\r\nHost: %s\r\nContent-Length: %d\r\n\r\n", PageName, HostName, InBufLen);
    }
  } else
  {//GET
    if (strlen(UserNamePassword) > 0)
    {
      memset(b64UserNamePassword, 0, sizeof(b64UserNamePassword));
      Base64Encode(UserNamePassword, strlen(UserNamePassword), b64UserNamePassword, sizeof(b64UserNamePassword));
      sprintf(SendStr, "GET %s HTTP/1.0\r\nHost: %s\r\nAuthorization: Basic %s\r\n\r\n", PageName, HostName, b64UserNamePassword);
    } else
    {
      sprintf(SendStr, "GET %s HTTP/1.0\r\nHost: %s\r\n\r\n", PageName, HostName);
    }
  }

  //建立SOCKET
  hSocket = socket(AF_INET, SOCK_STREAM, 0);
  if (hSocket == INVALID_SOCKET) return iErrorCode;

  ret = connect(hSocket, (struct sockaddr *) &addr, sizeof(struct sockaddr_in));
  if (ret == SOCKET_ERROR) goto exits;

  TimeOut = TimeOut / 1000;
  tv.tv_sec = TimeOut;
  tv.tv_usec = 0;
  FD_ZERO(&fs);
  FD_SET(hSocket, &fs);

  ret = select(hSocket + 1, NULL, &fs, NULL, &tv);
  if (ret == SOCKET_ERROR) goto exits;
  if (ret == 0) goto exits;

  ret = send(hSocket, SendStr, strlen(SendStr), 0);
  if (ret == SOCKET_ERROR) goto exits;

  if (InBuf && InBufLen > 0)//POST
  {
    ret = send(hSocket, InBuf, InBufLen, 0);
    if (ret == SOCKET_ERROR) goto exits;
  }
  //收取头
  t = time(NULL);
  *OutBufLen = 0;
  do
  {
    tv.tv_sec = TimeOut - (time(NULL) - t);
    tv.tv_usec = 0;
    FD_ZERO(&fs);
    FD_SET(hSocket, &fs);

    ret = select(hSocket + 1, &fs, NULL, NULL, &tv);
    if (ret == SOCKET_ERROR) goto exits;
    if (ret == 0) goto exits;

    recvLen = recv(hSocket, &OutBuf[*OutBufLen], 1, 0);//一次收一个字节
    if (recvLen > 0) *OutBufLen = *OutBufLen + recvLen;
    if (*OutBufLen < 4) continue;

    if (*OutBufLen > 1024) goto exits;//头太大

    memcpy(&itmp, &OutBuf[*OutBufLen - 4], 4);
    if (itmp != 0x0A0D0A0D) continue;

    OutBuf[*OutBufLen] = 0x00;

    for (i = 0; i < *OutBufLen; i++)
    {
      if (OutBuf[i] >= 'A' && OutBuf[i] <= 'Z') OutBuf[i] = OutBuf[i] + 32;
    }
    //{$message 'HttpPost要优化'}
    tmpBuf = strstr(OutBuf, "content-length:");
    if (tmpBuf)
    {
      ret = sscanf(tmpBuf, "content-length:%d%s", &iContentLength, tmpBuf1);
      if (ret != 2)
      {
        ret = sscanf(tmpBuf, "content-length: %d%s", &iContentLength, tmpBuf1);
        if (ret != 2) goto exits;
      }
      break;
    }
  } while ((recvLen > 0) && ((time(NULL) - t) < TimeOut));

  if (iContentLength <= 0) goto exits;

  if (IsShowHead) iContentLength = iContentLength + *OutBufLen; else *OutBufLen = 0;

  memcpy(StrHEAD, OutBuf, 13);
  StrHEAD[12] = '\0';
  // HTTP/1.0 200 OK
  ret = sscanf(StrHEAD, "%s %d", StrTmp, &iErrorCode);
  if (ret != 2)
  {
    iErrorCode = 0;
    goto exits;
  }

  //收取数据
  t = time(NULL);
  do
  {
    tv.tv_sec = TimeOut - (time(NULL) - t);
    tv.tv_usec = 0;
    FD_ZERO(&fs);
    FD_SET(hSocket, &fs);

    ret = select(hSocket + 1, &fs, NULL, NULL, &tv);
    if (ret == SOCKET_ERROR)
      goto exits;
    if (ret == 0)
      goto exits;

    recvLen = recv(hSocket, &OutBuf[*OutBufLen], iContentLength - *OutBufLen, 0);
    if (recvLen > 0) *OutBufLen = *OutBufLen + recvLen;
  } while ((recvLen > 0) && ((time(NULL) - t) < TimeOut));
  if (*OutBufLen <= 0) goto exits;
  //
  exits:
  shutdown(hSocket, SD_SEND);
  closesocket(hSocket);

  return iErrorCode;
}
//-----------------------------------------------------------------------------
bool DiskExists(char *Path)//是否存在磁盘
{
  u32 TotalSpace = 0;
  u32 FreeSpace = 0;
  GetDiskSpace(Path, &TotalSpace, &FreeSpace);
  return (TotalSpace > 0);
}
//-----------------------------------------------------------------------------
bool GetDiskSpace(char *Path, u32 *TotalSpace, u32 *FreeSpace)//取得磁盘空间 M
{
#ifndef WIN32
  struct statfs stat;
  *FreeSpace = 0;
  *TotalSpace = 0;
  int Ret = statfs(Path, &stat);
  if (Ret == 0)
  {
    *FreeSpace = (u32) ((u32) (stat.f_bsize / 1024) * (u32) (stat.f_bfree / 1024));
    *TotalSpace = (u32) ((u32) (stat.f_bsize / 1024) * (u32) (stat.f_blocks / 1024));
  }
  //printf(" filetype 0x%x \n", stat.f_type);
  return (Ret == 0);
#else
#ifdef _MSC_VER
  unsigned __int64 iTotalFree, iTotalSpace;
  bool ret;
  *TotalSpace = 0;
  *FreeSpace  = 0;
  //ret = GetDiskSpace(Path, &iTotalFree, &iTotalSpace);
  ret = GetDiskFreeSpaceEx(Path, &iTotalFree, &iTotalSpace, NULL);
  if (ret)
  {
    *FreeSpace = iTotalFree    / 1024;
    *TotalSpace  = iTotalSpace / 1024;
  }
  return ret;
#endif
#endif
}

//------------------------------------------------------------------------------
void printPI()//打印圆周率
{
  static int a = 10000;
  static int b, d, e, g;
  static int c = 2800;
  static int f[2801];
  for (; b - c;)
  {
    f[b++] = a / 5;
  }

  for (; d = 0, g = c * 2;)
  {
    b = c;
    d = d + f[b] * a;
    --g;
    f[b] = d % g;
    d = d / g;
    g--;

    for (; --b;)
    {
      d = d * b;
      d = d + f[b] * a;
      --g;
      f[b] = d % g;
      d = d / g;
      g--;
    }
    c = c - 14;
    printf("%.4d", e + d / a);
    e = d % a;
  }
  printf("\n");
}
//-----------------------------------------------------------------------------
bool IsConnectWLAN()//外网是否已连接
{
  struct hostent *h;
  h = (struct hostent *) gethostbyname("www.google.com");
  return (h != NULL);
}
//-----------------------------------------------------------------------------
bool RectIsIntersect(TRect *r1, TRect *r2)//区域是否包含
{
  int iMaxLeft, iMaxTop, iMinRight, iMinBottom;
  iMaxLeft = max(r1->left, r2->left);
  iMaxTop = max(r1->top, r2->top);
  iMinRight = min(r1->right, r2->right);
  iMinBottom = min(r1->bottom, r2->bottom);

  return !(iMaxLeft > iMinRight || iMaxTop > iMinBottom);
}

//-----------------------------------------------------------------------------
int StrToHex(char *src, int srclen, char *dst)
{
  unsigned char h1, h2;
  unsigned char s1, s2;
  int i, dstlen;
  dstlen = srclen / 2;

  for (i = 0; i < dstlen; i++)
  {
    h1 = (unsigned char) src[2 * i];
    h2 = (unsigned char) src[2 * i + 1];
    s1 = toupper(h1) - 0x30;
    if (s1 > 9) s1 = s1 - 7;
    s2 = toupper(h2) - 0x30;
    if (s2 > 9) s2 = s2 - 7;
    dst[i] = s1 * 16 + s2;
  }
  dst[dstlen] = 0x00;
  return dstlen;
}

//-----------------------------------------------------------------------------
int HexToStr(char *src, int srclen, char *dst)
{
  unsigned char ddl, ddh;
  int i, dstlen;
  dstlen = srclen * 2;

  for (i = 0; i < srclen; i++)
  {
    ddh = (unsigned char) src[i] / 16;
    ddl = (unsigned char) src[i] % 16;
    if (ddh > 9) ddh = ddh + 7;
    if (ddl > 9) ddl = ddl + 7;

    dst[i * 2] = ddh + 0x30;
    dst[i * 2 + 1] = ddl + 0x30;
  }
  dst[srclen * 2] = '\0';
  return dstlen;
}
//-----------------------------------------------------------------------------

#ifdef linux
#warning "linux linuxlinux linux linux"
#endif

#ifdef ANDROID
#warning "ANDROID ANDROID ANDROID ANDROID ANDROID ANDROID ANDROID ANDROID"
#endif

#ifdef __APPLE__
#warning "__APPLE__ __APPLE__ __APPLE__ __APPLE__ __APPLE__"
#endif

#ifdef __MACOSX__
#warning "__MACOSX__ __MACOSX__ __MACOSX__ __MACOSX__ __MACOSX__"
#endif

#ifdef __IPHONEOS__
#warning "__IPHONEOS__ __IPHONEOS__ __IPHONEOS__ __IPHONEOS__ __IPHONEOS__"
#endif

#ifdef TARGET_OS_MAC
#warning "TARGET_OS_MAC TARGET_OS_MAC TARGET_OS_MAC TARGET_OS_MAC TARGET_OS_MAC"
#endif

#ifdef TARGET_OS_IPHONE
#warning "TARGET_OS_IPHONE TARGET_OS_IPHONE TARGET_OS_IPHONE TARGET_OS_IPHONE TARGET_OS_IPHONE"
#endif

#ifdef TARGET_IPHONE_SIMULATOR
#warning "TARGET_IPHONE_SIMULATOR TARGET_IPHONE_SIMULATOR TARGET_IPHONE_SIMULATOR"
#endif
