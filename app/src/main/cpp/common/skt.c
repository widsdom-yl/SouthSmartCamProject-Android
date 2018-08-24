//-----------------------------------------------------------------------------
// Author      : 朱红波
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#include "../include/skt.h"
#include "../include/TFun.h"

//***************************************************************************
//*************************************************************************** udp
//***************************************************************************
/* PS　状态
D 无法中断的休眠状态（通常 IO 的进程）；
R 正在运行可中在队列中可过行的；
S 处于休眠状态；
T 停止或被追踪；
W 进入内存交换（从内核2.6开始无效）；
X 死掉的进程（从来没见过）；
Z 僵尸进程；
< 优先级高的进程
N 优先级较低的进程
L 有些页被锁进内存；
s 进程的领导者（在它之下有子进程）；
l 多进程的（使用 CLONE_THREAD, 类似 NPTL pthreads）；
+ 位于后台的进程组；
*/
#ifndef linux
// Linux Signal
/*
1.SIGHUP本信号在用户终端连接(正常或非正常)结束时发出,通常是在终端的控制进程结束
时,通知同一session内的各个作业,这时它们与控制终端不再关联.
2.SIGINT程序终止(interrupt)信号,在用户键入INTR字符(通常是Ctrl-C)时发出
3.SIGQUIT和SIGINT类似,但由QUIT字符(通常是Ctrl-)来控制.进程在因收到SIGQUIT退出时
会产生core文件,在这个意义上类似于一个程序错误信号.
4.SIGILL执行了非法指令.通常是因为可执行文件本身出现错误,或者试图执行数据段.堆栈
溢出时也有可能产生这个信号.
5.SIGTRAP由断点指令或其它trap指令产生.由debugger使用.
6.SIGABRT程序自己发现错误并调用abort时产生.
6.SIGIOT在PDP-11上由iot指令产生,在其它机器上和SIGABRT一样.
7.SIGBUS非法地址,包括内存地址对齐(alignment)出错.eg:访问一个四个字长的整数,但其
地址不是4的倍数.
8.SIGFPE在发生致命的算术运算错误时发出.不仅包括浮点运算错误,还包括溢出及除数为0
等其它所有的算术的错误.
9.SIGKILL用来立即结束程序的运行.本信号不能被阻塞,处理和忽略.
10.SIGUSR1留给用户使用
11.SIGSEGV试图访问未分配给自己的内存,或试图往没有写权限的内存地址写数据.
12.SIGUSR2留给用户使用
13.SIGPIPE Broken pipe
14.SIGALRM时钟定时信号,计算的是实际的时间或时钟时间.alarm函数使用该信号.
15.SIGTERM程序结束(terminate)信号,与SIGKILL不同的是该信号可以被阻塞和
处理.通常用来要求程序自己正常退出.shell命令kill缺省产生这个信号.
17.SIGCHLD子进程结束时,父进程会收到这个信号.
18.SIGCONT让一个停止(stopped)的进程继续执行.本信号不能被阻塞.可以用一个 handler
来让程序在由stopped状态变为继续执行时完成特定的工作.例如,重新显示提示符
19.SIGSTOP停止(stopped)进程的执行.注意它和terminate以及interrupt的区别:该进程还
未结束,只是暂停执行.本信号不能被阻塞,处理或忽略.
20.SIGTSTP停止进程的运行,但该信号可以被处理和忽略.用户键入SUSP字符时(通常是Ctrl-Z)
发出这个信号
21.SIGTTIN当后台作业要从用户终端读数据时,该作业中的所有进程会收到SIGTTIN信号.缺
省时这些进程会停止执行.
22.SIGTTOU类似于SIGTTIN,但在写终端(或修改终端模式)时收到.
23.SIGURG有紧急数据或out-of-band数据到达socket时产生.
24.SIGXCPU超过CPU时间资源限制.这个限制可以由getrlimit/setrlimit来读取/改变
25.SIGXFSZ超过文件大小资源限制.
26.SIGVTALRM虚拟时钟信号.类似于SIGALRM,但是计算的是该进程占用的CPU时间.
27.SIGPROF类似于SIGALRM/SIGVTALRM,但包括该进程用的CPU时间以及系统调用的时间.
28.SIGWINCH窗口大小改变时发出.
29.SIGIO文件描述符准备就绪,可以开始进行输入/输出操作.
30.SIGPWR Power failure
*/
#define SIGHUP		 1
#define SIGINT		 2
#define SIGQUIT		 3
#define SIGILL		 4
#define SIGTRAP		 5
#define SIGABRT		 6
#define SIGIOT		 6
#define SIGBUS		 7
#define SIGFPE		 8
#define SIGKILL		 9
#define SIGUSR1		10
#define SIGSEGV		11
#define SIGUSR2		12
#define SIGPIPE		13
#define SIGALRM		14
#define SIGTERM		15
#define SIGSTKFLT	16
#define SIGCHLD		17
#define SIGCONT		18
#define SIGSTOP		19
#define SIGTSTP		20
#define SIGTTIN		21
#define SIGTTOU		22
#define SIGURG		23
#define SIGXCPU		24
#define SIGXFSZ		25
#define SIGVTALRM	26
#define SIGPROF		27
#define SIGWINCH	28
#define SIGIO		  29
#define SIGPOLL		SIGIO
/*
#define SIGLOST		29
*/
#define SIGPWR		30
#define SIGSYS		31
#define	SIGUNUSED	31

/* These should not be considered constants from userland.  */
#define SIGRTMIN	32
#define SIGRTMAX	_NSIG

#define SIGSWI		32

//Linux Error Code
#define  EPERM              1  //Operation not permitted
#define  ENOENT             2  //No such file or directory
#define  ESRCH              3  //No such process
#define  EINTR              4  //Interrupted system call
#define  EIO                5  //I/O error
#define  ENXIO              6  //No such device or address
#define  E2BIG              7  //Argument list too long
#define  ENOEXEC            8  //Exec format error
#define  EBADF              9  //Bad file number
#define  ECHILD            10  //No child processes
#define  EAGAIN            11  //Try again
#define  ENOMEM            12  //Out of memory
#define  EACCES            13  //Permission denied
#define  EFAULT            14  //Bad address
#define  ENOTBLK           15  //Block device required
#define  EBUSY             16  //Device or resource busy
#define  EEXIST            17  //File exists
#define  EXDEV             18  //Cross-device link
#define  ENODEV            19  //No such device
#define  ENOTDIR           20  //Not a directory
#define  EISDIR            21  //Is a directory
#define  EINVAL            22  //Invalid argument
#define  ENFILE            23  //File table overflow
#define  EMFILE            24  //Too many open files
#define  ENOTTY            25  //Not a typewriter
#define  ETXTBSY           26  //Text file busy
#define  EFBIG             27  //File too large
#define  ENOSPC            28  //No space left on device
#define  ESPIPE            29  //Illegal seek
#define  EROFS             30  //Read-only file system
#define  EMLINK            31  //Too many links
#define  EPIPE             32  //Broken pipe
#define  EDOM              33  //Math argument out of domain of func
#define  ERANGE            34  //Math result not representable
#define  EDEADLK           35  //Resource deadlock would occur
#define  ENAMETOOLONG      36  //File name too long
#define  ENOLCK            37  //No record locks available
#define  ENOSYS            38  //Function not implemented
#define  ENOTEMPTY         39  //Directory not empty
#define  ELOOP             40  //Too many symbolic links encountered
#define  EWOULDBLOCK   EAGAIN  //Operation would block
#define  ENOMSG            42  //No message of desired type
#define  EIDRM             43  //Identifier removed
#define  ECHRNG            44  //Channel number out of range
#define  EL2NSYNC          45  //Level 2 not synchronized
#define  EL3HLT            46  //Level 3 halted
#define  EL3RST            47  //Level 3 reset
#define  ELNRNG            48  //Link number out of range
#define  EUNATCH           49  //Protocol driver not attached
#define  ENOCSI            50  //No CSI structure available
#define  EL2HLT            51  //Level 2 halted
#define  EBADE             52  //Invalid exchange
#define  EBADR             53  //Invalid request descriptor
#define  EXFULL            54  //Exchange full
#define  ENOANO            55  //No anode
#define  EBADRQC           56  //Invalid request code
#define  EBADSLT           57  //Invalid slot
#define  EDEADLOCK    EDEADLK
#define  EBFONT            59  //Bad font file format
#define  ENOSTR            60  //Device not a stream
#define  ENODATA           61  //No data available
#define  ETIME             62  //Timer expired
#define  ENOSR             63  //Out of streams resources
#define  ENONET            64  //Machine is not on the network
#define  ENOPKG            65  //Package not installed
#define  EREMOTE           66  //Object is remote
#define  ENOLINK           67  //Link has been severed
#define  EADV              68  //Advertise error
#define  ESRMNT            69  //Srmount error
#define  ECOMM             70  //Communication error on send
#define  EPROTO            71  //Protocol error
#define  EMULTIHOP         72  //Multihop attempted
#define  EDOTDOT           73  //RFS specific error
#define  EBADMSG           74  //Not a data message
#define  EOVERFLOW         75  //Value too large for defined data type
#define  ENOTUNIQ          76  //Name not unique on network
#define  EBADFD            77  //File descriptor in bad state
#define  EREMCHG           78  //Remote address changed
#define  ELIBACC           79  //Can not access a needed shared library
#define  ELIBBAD           80  //Accessing a corrupted shared library
#define  ELIBSCN           81  //.lib section in a.out corrupted
#define  ELIBMAX           82  //Attempting to link in too many shared libraries
#define  ELIBEXEC          83  //Cannot exec a shared library directly
#define  EILSEQ            84  //Illegal u8 sequence
#define  ERESTART          85  //Interrupted system call should be restarted
#define  ESTRPIPE          86  //Streams pipe error
#define  EUSERS            87  //Too many users
#define  ENOTSOCK          88  //Socket operation on non-socket
#define  EDESTADDRREQ      89  //Destination address required
#define  EMSGSIZE          90  //Message too long
#define  EPROTOTYPE        91  //Protocol wrong type for socket
#define  ENOPROTOOPT       92  //Protocol not available
#define  EPROTONOSUPPORT   93  //Protocol not supported
#define  ESOCKTNOSUPPORT   94  //Socket type not supported
#define  EOPNOTSUPP        95  //Operation not supported on transport endpoint
#define  EPFNOSUPPORT      96  //Protocol family not supported
#define  EAFNOSUPPORT      97  //Address family not supported by protocol
#define  EADDRINUSE        98  //Address already in use
#define  EADDRNOTAVAIL     99  //Cannot assign requested address
#define  ENETDOWN         100  //Network is down
#define  ENETUNREACH      101  //Network is unreachable
#define  ENETRESET        102  //Network dropped connection because of reset
#define  ECONNABORTED     103  //Software caused connection abort
#define  ECONNRESET       104  //Connection reset by peer
#define  ENOBUFS          105  //No buffer space available
#define  EISCONN          106  //Transport endpoint is already connected
#define  ENOTCONN         107  //Transport endpoint is not connected
#define  ESHUTDOWN        108  //Cannot send after transport endpoint shutdown
#define  ETOOMANYREFS     109  //Too many references: cannot splice
#define  ETIMEDOUT        110  //Connection timed out
#define  ECONNREFUSED     111  //Connection refused
#define  EHOSTDOWN        112  //Host is down
#define  EHOSTUNREACH     113  //No route to host
#define  EALREADY         114  //Operation already in progress
#define  EINPROGRESS      115  //Operation now in progress
#define  ESTALE           116  //Stale NFS file handle
#define  EUCLEAN          117  //Structure needs cleaning
#define  ENOTNAM          118  //Not a XENIX named type file
#define  ENAVAIL          119  //No XENIX semaphores available
#define  EISNAM           120  //Is a named type file
#define  EREMOTEIO        121  //Remote I/O error
#define  EDQUOT           122  //Quota exceeded
#define  ENOMEDIUM        123  //No medium found
#define  EMEDIUMTYPE      124  //Wrong medium type

#endif

//-----------------------------------------------------------------------------
void th_udp(TudpParam* Info)
{
  char RecvBuf[1024*64];
  i32 RecvSize;
  fd_set fd_read;
  i32 ret;

  struct timeval tv;
  tv.tv_sec = 1;
  tv.tv_usec = 0;

  if (!Info) return;

  while (true)
  {
    if (Info->IsExitUDP) break;

    FD_ZERO(&fd_read);
    FD_SET(Info->SocketHandle, &fd_read);
    ret = select(Info->SocketHandle + 1, &fd_read, NULL, NULL, &tv);
    //1
    if (ret < 0)
    {
      if (errno == EINTR )
      {
        usleep(10*1000);
        continue;
      }
      else break;
    }
    //2
    if (ret == 0)
    {
      usleep(10*1000);
      continue;
    }
    //3
    if (ret > 0)
    {
      if (FD_ISSET(Info->SocketHandle, &fd_read))
      {
        i32 AddrLen = sizeof(Info->FromAddr);
        RecvSize = recvfrom(Info->SocketHandle, RecvBuf, sizeof(RecvBuf), 0,
          (struct sockaddr*)&Info->FromAddr,(i32*)&AddrLen);
        //3.1
        if (RecvSize < 0)
        {
          if ( errno == EINTR)
          {
            usleep(10*1000);
            continue;
          }
          else break;
        }
        //3.2
        if (RecvSize == 0)
        {
          usleep(10*1000);
          continue;
        }
        //3.3
        if (RecvSize > 0)
        {
          if (Info->OnRecvEvent)
          {
            Info->OnRecvEvent(Info, RecvBuf, RecvSize);
          }
        }
      }
    }
  }
}
//-----------------------------------------------------------------------------
bool udp_Init(TudpParam* Param)
{
  struct sockaddr_in SvrAddr;
  i32 flag, ret;
  struct timeval lvtimeout;
  struct ip_mreq mcast;
  int i;
  for (i=0; i<9; i++)
  {
    Param->SocketHandle = socket(AF_INET, SOCK_DGRAM, 0);
    if (Param->SocketHandle > 0) break;
    if (Param->SocketHandle <= 0)
    {
      usleep(1000*10);
      continue;
    }
  }
  if (Param->SocketHandle <= 0) return false;

  memset(&SvrAddr,0,sizeof(struct sockaddr_in));
  SvrAddr.sin_family = AF_INET;
  SvrAddr.sin_port = htons(Param->Port);
  SvrAddr.sin_addr.s_addr = htonl(INADDR_ANY);
  //ret = bind(Param->SocketHandle, (struct sockaddr*)&SvrAddr, sizeof(SvrAddr));
  for (i=0; i<9; i++)
  {
    ret = bind(Param->SocketHandle, (struct sockaddr*)&SvrAddr, sizeof(SvrAddr));
    if (ret == 0) break;
    usleep(1000*10);
    continue;
  }

  if (ret != 0)
  {
    udp_Free(Param);
    return false;
  }

  //设置多进程可以共享地址
  flag = 1;
  setsockopt(Param->SocketHandle, SOL_SOCKET, SO_REUSEADDR, &flag, sizeof(flag));
  if (ret != 0)
  {
    udp_Free(Param);
    return false;
  }

  lvtimeout.tv_usec = 0;
  lvtimeout.tv_sec = 1;
  setsockopt(Param->SocketHandle,SOL_SOCKET,SO_RCVTIMEO,&lvtimeout,sizeof(lvtimeout));
#if 0
  if (Param->IsMulticast)
  {
    //设置生存范围
    setsockopt(Param->SocketHandle,IPPROTO_IP,IP_MULTICAST_TTL,(char*)&Param->TTL,sizeof(int));
    //设置是否回显
    flag = 1;
    setsockopt(Param->SocketHandle, IPPROTO_IP, IP_MULTICAST_LOOP, &flag, sizeof(flag));
    //加入组
    memset(&mcast,0,sizeof(mcast));
    inet_aton(Param->LocalIP,&mcast.imr_interface);
    inet_aton(Param->MultiIP, &mcast.imr_multiaddr);
    ret = setsockopt(Param->SocketHandle, IPPROTO_IP,IP_ADD_MEMBERSHIP,&mcast,sizeof(mcast));
    PRINTF("********** IP_ADD_MEMBERSHIP ret:%d \n", ret);
    if (ret != 0)
    {
      udp_Free(Param);
      return false;
    }

    //退出组
    //setsockopt(SocketHandle,IPPROTO_IP,IP_DROP_MEMBERSHIP,&mcast,sizeof(mcast));//LeaveGroup
  }
#endif
  Param->IsExitUDP = false;
  ThreadLockInit(&Param->Lock);
  Param->tHandle = ThreadCreate(th_udp, Param, false);
  //pthread_create(&Param->tHandle, NULL, (void*(*)(void*))th_udp, Param);  
  //pthread_detach(Param->tHandle);
  return true;
}
//-----------------------------------------------------------------------------
bool udp_Free(TudpParam* Param)
{
  if (!Param) return false;
  ThreadLock(&Param->Lock);
  Param->IsExitUDP = true;
  ThreadUnlock(&Param->Lock);
  ThreadExit(Param->tHandle, 1000);
  Param->tHandle = 0;
  shutdown(Param->SocketHandle, 2);
  closesocket(Param->SocketHandle);
  Param->SocketHandle = 0;
  ThreadLockFree(&Param->Lock);
  memset(Param, 0, sizeof(TudpParam));
  return true;
}
//-----------------------------------------------------------------------------
