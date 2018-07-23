//-----------------------------------------------------------------------------
// Author      : 朱红波
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#include "../include/TFun.h"
#include "../include/skt.h"
#include "axdll.h"
#include "../include/libthSDK.h"
#include "../include/avDecode.h"
#include "../avDecode/thffmpeg.h"
#include "../include/av_Queue.h"

#ifdef WIN32
#include "../include/pthreads/pthread.h"
#include "../include/pthreads/semaphore.h"
#endif
#define NET_TIMEOUT       10000  // ms

#define IsUsedP2P

#ifdef IsUsedP2P
#include "../include/tutk/IOTCAPIs.h"
#include "../include/tutk/AVAPIs.h"
#include "../include/tutk/AVFRAMEINFO.h"
#include "../include/tutk/AVIOCTRLDEFs.h"
#ifdef WIN32
#pragma comment (lib, "lib.win32/tutk/AVAPIs.lib")
#pragma comment (lib, "lib.win32/tutk/IOTCAPIs.lib")
#endif
#endif

typedef struct TPlayParam {
    //内部
    bool IsExit;
    bool IsAutoReConn;
    H_THREADLOCK Lock;
    pthread_cond_t SyncCond;

    char20 LocalIP;

    H_THREAD thRecv;//收取线程句柄

    HANDLE RecHandle;//录像句柄

    char1024 RecPath;//录像路径
    char1024 JpgPath;//拍照路径
    bool IsSnapShot;
    char1024 FileName_Jpg;

    bool IsTaskRec;//因还没有IFrame，没有没有写头，任务

    HANDLE decHandle;//解码句柄
    HANDLE RenderHandle;//显示句柄
    HANDLE audioHandle;//音频播放句柄

#define AUDIO_COLLECT_SIZE      2048
    HANDLE talkHandle;//音频对讲句柄

    TMediaType VideoMediaType;//视频类型

#define MAX_DSPINFO_COUNT  10
    TDspInfo DspInfoLst[MAX_DSPINFO_COUNT];//显示区域

    bool IsInsideDecode;//0=外部解码 1=内部解码
    bool IsQueue;//是否队列解码显示
    bool IsAdjustTime;//是否校准设备时间

#define MAX_QUEUE_COUNT    120
    HANDLE hQueueDraw;//上屏队列句柄
    HANDLE hTimerIDQueueDraw;//上屏事件句柄
    u32 iSleepTime;//
    int iFrameTime;//

    HANDLE hQueueVideo;//视频解码队列句柄
    H_THREAD thQueueVideo;//视频解码线程句柄
    HANDLE hQueueAudio;//音频解码队列句柄
    H_THREAD thQueueAudio;//音频解码线程句柄
    HANDLE hQueueRec;//录像队列句柄
    H_THREAD thQueueRec;//录像线程句柄

    //外部传入参数
    TDevCfg DevCfg;
#define MAX_BUF_SIZE 1024*1024
    char RecvBuffer[MAX_BUF_SIZE];//收取缓存
    i32 RecvLen;//收取缓存长度
    char RecvDownloadBuf[MAX_BUF_SIZE];//http p2pnew
    i32 RecvDownloadLen;//http p2pnew

    TavPicture FrameV420;

    TvideoCallBack* videoEvent;
    TaudioCallBack* audioEvent;
    TalarmCallBack* AlmEvent;
    void* UserCustom;

    int DecodeStyle;
    int DisplayStreamTypeOld, DisplayStreamType;

    u32 VideoChlMask;
    u32 AudioChlMask;
    u32 SubVideoChlMask;

    bool IsAudioMute;//是否静音
    //内部标志
    bool IsIFrameFlag;//视频流是否出现了I帧标志，初始=false, 遇到I帧 = true
    bool IsVideoDecodeSuccessFlag;//是否正确地解码了视频

    bool IsStopHttpGet;
    //sem_t semHttpDownload;

    i32 LastSenseTime;//断线重连用

    bool IsConnect;
    i32 iConnectStatus;
    i32 StreamType;//0主码流 1次码流
    i32 ImgWidth;
    i32 ImgHeight;
    i32 FrameRate;

    //IP
    char40 UserName;
    char40 Password;
    char256 IPUID;
    i32 DataPort;
    u32 TimeOut;

    u32 Session;
    SOCKET hSocket;

    //P2P
    i32 p2pSoftVersion;//0=old 1=qiu/new 2=now...
    i32 Isp2pConn;

    i32 p2p_SessionID;
    i32 p2p_avIndex;
    i32 p2p_talkIndex;

}TPlayParam;

static bool Isp2pInit = false;
//*****************************************************************************
void callback_AudioTalk(void* UserCustom, char* Buf, i32 Len);
void OnRecvDataNotify_Search(void* Sender, char* Buf, i32 BufLen);
void OnRecvDataNotify_av(HANDLE NetHandle, TDataFrameInfo* PInfo, char* Buf, int BufLen);
void thread_RecvData_TCP(HANDLE NetHandle);
void thread_RecvData_P2P(HANDLE NetHandle);
bool net_Login(HANDLE NetHandle);
bool net_GetAllCfg(HANDLE NetHandle);
bool net_SetTalk(HANDLE NetHandle, char* Buf, i32 BufLen);
bool net_Connect_IP(HANDLE NetHandle, bool IsCreateRecvThread);
bool net_Connect_P2P(HANDLE NetHandle, bool IsCreateRecvThread);

void STDCALL Time_QueueDraw(HANDLE mmHandle, u32 uMsg, void* dwUser, u32 dw1, u32 dw2);
void thread_QueueVideo(HANDLE NetHandle);
void thread_QueueAudio(HANDLE NetHandle);
void thread_QueueRec(HANDLE NetHandle);

//*****************************************************************************
//-----------------------------------------------------------------------------
void OnRecvDataNotify_Search(void* Sender, char* Buf, i32 BufLen)
{
    TNetCmdPkt* PPkt;
    TMulticastInfoPkt* PInfo;
    i32 i;
    u32 iFromAddr;
    TSearchDevCallBack* SearchEvent;
    TudpParam* Info = (TudpParam*)Sender;
    if (!Info) return;

    if (strstr(Buf, "M-SEARCH") != NULL) return;
    if (BufLen != sizeof(TNetCmdPkt)) return;
    PPkt = (TNetCmdPkt*)Buf;
    if (PPkt->HeadPkt.VerifyCode != Head_CmdPkt) return;
    if (PPkt->HeadPkt.PktSize != sizeof(TCmdPkt)) return;

    if (PPkt->CmdPkt.MsgID != Msg_GetMulticastInfo) return;

    memcpy(&iFromAddr, &Info->FromAddr.sin_addr, 4);
    for (i=0; i<512; i++)
    {
        if (Info->SearchIPLst[i] == iFromAddr) return;
        if (Info->SearchIPLst[i] == 0x00000000)
        {
            Info->SearchIPLst[i] = iFromAddr;
            break;
        }
    }

    SearchEvent = (TSearchDevCallBack*)Info->Flag;
    if (SearchEvent)
    {
        PInfo = &PPkt->CmdPkt.MulticastInfo;
        SearchEvent(
                Info->UserCustom,
                PInfo->DevInfo.SN,
                PInfo->DevInfo.DevType,
                PInfo->DevInfo.DevModal,
                PInfo->DevInfo.SoftVersion,
                PInfo->NetCfg.DataPort,
                PInfo->NetCfg.HttpPort,
                PInfo->NetCfg.rtspPort,
                PInfo->DevInfo.DevName,
                PInfo->NetCfg.Lan.DevIP,
                PInfo->NetCfg.Lan.DevMAC,
                PInfo->NetCfg.Lan.SubMask,
                PInfo->NetCfg.Lan.Gateway,
                PInfo->NetCfg.Lan.DNS1,
                PInfo->NetCfg.DDNS.DDNSServer,
                PInfo->NetCfg.DDNS.DDNSDomain,
                PInfo->p2pCfg.UID
        );
    }
}
//-----------------------------------------------------------------------------
HANDLE thSearch_Init(TSearchDevCallBack SearchEvent, void* UserCustom)
{
    int ret;
    TudpParam* Info;
#ifdef WIN32
    WSADATA wsaData;
  WSAStartup(MAKEWORD(2, 2), &wsaData);
#endif
    PRINTF("%s(%d)\n", __FUNCTION__, __LINE__);
    Info = (TudpParam*)malloc(sizeof(TudpParam));
    memset(Info, 0, sizeof(TudpParam));
    Info->UserCustom = UserCustom;
    Info->Port = Port_Ax_Search_Local;
    Info->OnRecvEvent = OnRecvDataNotify_Search;

    //Info->LocalIP = GetLocalIP();
    Info->Flag = (HANDLE)SearchEvent;
    ret = udp_Init(Info);
    if (!ret)
    {
        free(Info);
        return NULL;
    }
    return (HANDLE)Info;
}
//-----------------------------------------------------------------------------
bool thSearch_SearchDevice(HANDLE SearchHandle)
{
    i32 flag = 1;
    struct sockaddr_in Addr;
    TNetCmdPkt Pkt;
    TudpParam* Info = (TudpParam*)SearchHandle;
    if (!Info) return false;
    PRINTF("%s(%d)\n", __FUNCTION__, __LINE__);
    //if (!LocalIP) Info->LocalIP = GetLocalIP();
    memset(Info->SearchIPLst, 0, sizeof(Info->SearchIPLst));
    memset(&Pkt, 0, sizeof(Pkt));

    Pkt.HeadPkt.VerifyCode = Head_CmdPkt;
    Pkt.HeadPkt.PktSize = sizeof(TCmdPkt);
    Pkt.CmdPkt.MsgID = Msg_GetMulticastInfo;
    memset(&Addr,0,sizeof(struct sockaddr_in));
    Addr.sin_family = AF_INET;
    //inet_aton(IP_Ax_Multicast, &Addr.sin_addr);
    //Addr.sin_port = htons(Port_Ax_Multicast);
    //sendto(Info->SockeHANDLE, &Pkt, sizeof(Pkt), 0, (struct sockaddr*)&Addr, sizeof(Addr));

    setsockopt(Info->SocketHandle, SOL_SOCKET, SO_BROADCAST, (char*)&flag, sizeof(flag));
#ifdef WIN32
    Addr.sin_addr.S_un.S_addr = inet_addr("255.255.255.255");
#else
    inet_aton("255.255.255.255", &Addr.sin_addr);
#endif
    Addr.sin_port = htons(Port_Ax_Multicast);
    sendto(Info->SocketHandle, (char*)&Pkt, sizeof(Pkt), 0, (struct sockaddr*)&Addr, sizeof(Addr));
    return true;
}
//-----------------------------------------------------------------------------
bool thSearch_SetWiFiCfg(HANDLE SearchHandle, char* SSID, char* Password)
{
    i32 flag = 1;
    struct sockaddr_in Addr;
    TNetCmdPkt Pkt;
    TudpParam* Info = (TudpParam*)SearchHandle;
    if (!Info) return false;
    PRINTF("%s(%d)\n", __FUNCTION__, __LINE__);
    //if (LocalIP) Info->LocalIP = GetLocalIP();
    memset(&Pkt, 0, sizeof(Pkt));
    Pkt.HeadPkt.VerifyCode = Head_CmdPkt;
    Pkt.HeadPkt.PktSize = sizeof(TCmdPkt);
    Pkt.CmdPkt.MsgID = Msg_SetWiFiCfg;
    Pkt.CmdPkt.WiFiCfgPkt.Active = true;
    Pkt.CmdPkt.WiFiCfgPkt.IsAPMode = false;
    strcpy(Pkt.CmdPkt.WiFiCfgPkt.SSID_STA, SSID);
    strcpy(Pkt.CmdPkt.WiFiCfgPkt.Password_STA, Password);

    memset(&Addr,0,sizeof(struct sockaddr_in));
    Addr.sin_family = AF_INET;

    setsockopt(Info->SocketHandle, SOL_SOCKET, SO_BROADCAST, (char*)&flag, sizeof(flag));
#ifdef WIN32
    Addr.sin_addr.S_un.S_addr = inet_addr("255.255.255.255");
#else
    inet_aton("255.255.255.255", &Addr.sin_addr);
#endif
    Addr.sin_port = htons(Port_Ax_Multicast);
    sendto(Info->SocketHandle, (char*)&Pkt, sizeof(Pkt), 0, (struct sockaddr*)&Addr, sizeof(Addr));
    return true;
}
//-----------------------------------------------------------------------------
bool thSearch_Free(HANDLE SearchHandle)
{
    int ret;
    TudpParam* Info = (TudpParam*)SearchHandle;
    if (!Info) return false;
    PRINTF("%s(%d)\n", __FUNCTION__, __LINE__);
    ret = udp_Free(Info);
    if (ret) free(Info);
    return ret;
}
//-----------------------------------------------------------------------------
HANDLE thNet_Init(bool IsInsideDecode, bool IsQueue, bool IsAdjustTime, bool IsAutoReConn)
{
#ifdef WIN32
    WSADATA wsaData;
#endif
    TPlayParam* Play = (TPlayParam*)malloc(sizeof(TPlayParam));
    if (!Play) return NULL;
    PRINTF("%s(%d) IsInsideDecode:%d IsQueue:%d IsAdjustTime:%d IsAutoReConn:%d\n", __FUNCTION__, __LINE__, IsInsideDecode, IsQueue, IsAdjustTime, IsAutoReConn);
#ifdef WIN32
    WSAStartup(MAKEWORD(2, 2), &wsaData);
#endif
    memset(Play, 0, sizeof(TPlayParam));
    Play->IsInsideDecode = IsInsideDecode;
    Play->IsQueue = IsQueue;
    Play->IsAdjustTime = IsAdjustTime;
    Play->IsAutoReConn = IsAutoReConn;

    Play->IsAudioMute = true;
    Play->p2p_avIndex = -1;
    Play->p2p_SessionID = -1;
    Play->p2p_talkIndex = -1;
    ThreadLockInit(&Play->Lock);
    pthread_cond_init(&Play->SyncCond, NULL);

    Play->DisplayStreamType = 1;
    Play->DisplayStreamTypeOld = Play->DisplayStreamType;

    Play->DecodeStyle = Decode_All;

    Play->iConnectStatus = THNET_CONNSTATUS_NO;
    return (HANDLE)Play;
}
//-----------------------------------------------------------------------------
bool thNet_SetCallBack(HANDLE NetHandle, TvideoCallBack videoEvent, TaudioCallBack audioEvent, TalarmCallBack AlmEvent, void* UserCustom)
{
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    PRINTF("%s(%s)(%s)\n", __FUNCTION__, Play->IPUID, Play->LocalIP);
    Play->videoEvent = videoEvent;
    Play->audioEvent = audioEvent;
    Play->AlmEvent = AlmEvent;
    Play->UserCustom = UserCustom;
    return true;
}
//-----------------------------------------------------------------------------
bool thNet_Free(HANDLE NetHandle)
{
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    PRINTF("%s(%s)(%s)\n", __FUNCTION__, Play->IPUID, Play->LocalIP);
    thNet_DisConn(NetHandle);
    ThreadLockFree(&Play->Lock);
    pthread_cond_destroy(&Play->SyncCond);
    memset(Play, 0, sizeof(TPlayParam));
    free(Play);
#ifdef WIN32
    //  WSACleanup();
#endif
    return true;
}
//-----------------------------------------------------------------------------
bool net_Login(HANDLE NetHandle)//not export
{
    TVideoFormat* fmtv;
    i32 ret;
    TNetCmdPkt Pkt;
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;

    if (!Play->Isp2pConn)
    {
        memset(&Pkt, 0, sizeof(Pkt));
        Pkt.HeadPkt.VerifyCode = Head_CmdPkt;
        Pkt.HeadPkt.PktSize = sizeof(Pkt.CmdPkt);
        Pkt.CmdPkt.PktHead = Pkt.HeadPkt.VerifyCode;
        Pkt.CmdPkt.MsgID = Msg_Login;
        strcpy(Pkt.CmdPkt.LoginPkt.UserName, Play->UserName);
        strcpy(Pkt.CmdPkt.LoginPkt.Password, Play->Password);

        //strcpy(Pkt.CmdPkt.LoginPkt.DevIP, Play->DevIP);

        //Pkt.CmdPkt.LoginPkt.SendSensePkt = true;
        ret = SendBuf(Play->hSocket, (char*)&Pkt, sizeof(Pkt), NET_TIMEOUT);
        memset(&Pkt, 0, sizeof(Pkt));
        ret = RecvBuf(Play->hSocket, (char*)&Pkt, sizeof(Pkt), NET_TIMEOUT);
        if (Pkt.CmdPkt.Value == 0) return false;
        Play->Session = Pkt.CmdPkt.Session;
        Play->DevCfg.DevInfoPkt = Pkt.CmdPkt.LoginPkt.DevInfoPkt;
        Play->DevCfg.VideoCfgPkt.VideoFormat = Pkt.CmdPkt.LoginPkt.v[0];
        Play->DevCfg.AudioCfgPkt.AudioFormat = Pkt.CmdPkt.LoginPkt.a[0];
        fmtv = &Play->DevCfg.VideoCfgPkt.VideoFormat;

        return true;
    }

    return false;
}
//-----------------------------------------------------------------------------
bool net_GetAllCfg(HANDLE NetHandle)//not export
{
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;

    if (!Play->Isp2pConn)
    {
        THeadPkt Head;
        i32 ret, i;
        TNetCmdPkt Pkt;
        memset(&Pkt, 0, sizeof(Pkt));
        Pkt.HeadPkt.VerifyCode = Head_CmdPkt;
        Pkt.HeadPkt.PktSize = sizeof(Pkt.CmdPkt);
        Pkt.CmdPkt.PktHead = Head_CmdPkt;
        Pkt.CmdPkt.MsgID = Msg_GetAllCfg;
        Pkt.CmdPkt.Session = Play->Session;
        if (Play->IsAdjustTime) Pkt.CmdPkt.Value = GetTime();
        ret = SendBuf(Play->hSocket, (char*)&Pkt, sizeof(TNetCmdPkt), NET_TIMEOUT);

        for (i=0; i<5; i++)
        {
            ret = RecvBuf(Play->hSocket, (char*)&Head, sizeof(THeadPkt), NET_TIMEOUT);
            if (Head.VerifyCode == Head_CfgPkt) break;
        }

        if (Head.VerifyCode != Head_CfgPkt) return false;
        if (Head.PktSize != sizeof(TDevCfg)) return false;
        ret = RecvBuf(Play->hSocket, (char*)&Play->DevCfg, sizeof(TDevCfg), NET_TIMEOUT);

        return ret;
    }
    else
    {
#ifdef IsUsedP2P
        i32 ioType, ret;
        TNewCmdPkt Pkt;
        memset(&Pkt, 0, sizeof(TNewCmdPkt));
        Pkt.VerifyCode = Head_CmdPkt;
        Pkt.MsgID = Msg_GetAllCfg;
        Pkt.Result = 0;
        Pkt.PktSize = 4;
        if (Play->IsAdjustTime) Pkt.Value = GetTime();
        ioType = Head_CmdPkt;
        ret = avSendIOCtrl(Play->p2p_avIndex, ioType, (char*)&Pkt, 8 + Pkt.PktSize);
        if (ret < 0) return false;
        ret = avRecvIOCtrl(Play->p2p_avIndex, (u32*)&ioType, (char*)&Pkt, sizeof(Pkt), 3000);
        if (ret < 0) return false;
        if (Pkt.VerifyCode == Head_CmdPkt && Pkt.MsgID == Msg_GetAllCfg)
        {
            NewDevCfg_to_DevCfg(&Pkt.NewDevCfg, &Play->DevCfg);
            return true;
        }
#endif
        return false;
    }
}
//-----------------------------------------------------------------------------
void STDCALL Time_QueueDraw(HANDLE mmHandle, u32 uMsg, void* dwUser, u32 dw1, u32 dw2)
{
    TavNode* tmpNode;
    int i, ret, iPktCount;
    TavPicture* pFrameV;
    TPlayParam* Play = (TPlayParam*)dwUser;
    if (!Play) return;

#if !(defined(ANDROID) && defined(IS_VIDEOPLAY_OPENGL))
    if (mmHandle != Play->hTimerIDQueueDraw) return;//WIN32 ios
#endif
    if (Play->IsExit) return;

    //0
    iPktCount = avQueue_GetCount(Play->hQueueDraw);

#ifdef WIN32
    //  PostMessage((HWND)2033304, 0x00008888, 0, iPktCount);
#endif
    if (iPktCount == 0) return;
    //PRINTF("iPktCount:%d  ", iPktCount);
    if (iPktCount > 5 && iPktCount <= 10)
    {
        tmpNode = avQueue_ReadBegin(Play->hQueueDraw);
        if (tmpNode != NULL)
        {
            if (tmpNode->Buf1 != NULL) free(tmpNode->Buf1);
        }
        avQueue_ReadEnd(Play->hQueueDraw, tmpNode);
    }

    //1
    iPktCount = avQueue_GetCount(Play->hQueueDraw);
    if (iPktCount == 0) return;
    if (iPktCount > 10)
    {
        for (i=0; i<1; i++)
        {
            tmpNode = avQueue_ReadBegin(Play->hQueueDraw);
            if (tmpNode != NULL)
            {
                if (tmpNode->Buf1 != NULL) free(tmpNode->Buf1);
            }
            avQueue_ReadEnd(Play->hQueueDraw,tmpNode);
        }
    }
    //2
    tmpNode = avQueue_ReadBegin(Play->hQueueDraw);
    if (tmpNode == NULL) return;
    if (tmpNode->Buf == NULL)
    {
        if (tmpNode->Buf1 != NULL) free(tmpNode->Buf1);
        avQueue_ReadEnd(Play->hQueueDraw, tmpNode);
        return;
    }
    //3
    pFrameV = (TavPicture*)tmpNode->Buf;
    if (Play->ImgWidth > 0 && Play->ImgHeight > 0)
    {
        //zhb ThreadLock(&Play->Lock);
        ret = thRender_FillMem(Play->RenderHandle, *pFrameV, Play->ImgWidth, Play->ImgHeight);
        for (i=0; i<MAX_DSPINFO_COUNT; i++)
        {
            TDspInfo* PDspInfo = &Play->DspInfoLst[i];
            if (PDspInfo->DspHandle == NULL) continue;
            ret = thRender_Display(Play->RenderHandle, PDspInfo->DspHandle, PDspInfo->DspRect);
        }
        //zhb ThreadUnlock(&Play->Lock);
    }

    if (tmpNode->Buf1 != NULL) free(tmpNode->Buf1);
    avQueue_ReadEnd(Play->hQueueDraw, tmpNode);
}
//-----------------------------------------------------------------------------
void thread_QueueVideo(HANDLE NetHandle)
{
    int iYUVSize;
    char* newBuf;
    TavPicture newFrameV;
    int i, iCount, ret;
    TavNode* tmpNode;
    TDataFrameInfo* PInfo;
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return;
    while (1)
    {
        if (Play->IsExit) break;
        //if (!Play->IsConnect) break;//不能要，不然重连不了

        if (Play->DisplayStreamTypeOld != Play->DisplayStreamType)
        {
            Play->DisplayStreamTypeOld = Play->DisplayStreamType;

            if (thNet_IsRec(NetHandle))
            {
                thNet_StopRec(NetHandle);
                thNet_StartRec(NetHandle, NULL);
            }

            //zhb ThreadLock(&Play->Lock);

            iCount = avQueue_GetCount(Play->hQueueVideo);
            for (i=iCount-1; i>=0; i--)
            {
                tmpNode = avQueue_ReadBegin(Play->hQueueVideo);
                if (tmpNode == NULL) continue;
                avQueue_ReadEnd(Play->hQueueVideo, tmpNode);
            }

            iCount = avQueue_GetCount(Play->hQueueDraw);
            for (i=iCount-1; i>=0; i--)
            {
                if (Play->IsExit) break;
                tmpNode = avQueue_ReadBegin(Play->hQueueDraw);
                if (tmpNode == NULL) continue;
                if (tmpNode->Buf1) free(tmpNode->Buf1);
                avQueue_ReadEnd(Play->hQueueDraw, tmpNode);
            }

            if (Play->decHandle)
            {
                thDecodeVideoFree(Play->decHandle);
                Play->decHandle = NULL;
            }
            Play->decHandle = thDecodeVideoInit(Play->VideoMediaType);

            //zhb ThreadUnlock(&Play->Lock);
        }

        iCount = avQueue_GetCount(Play->hQueueVideo);
        if (iCount >= MAX_QUEUE_COUNT-1)
        {
            for (i=0; i<iCount; i++)
            {
                if (Play->IsExit) break;
                tmpNode = avQueue_ReadBegin(Play->hQueueVideo);
                if (tmpNode == NULL)
                {
                    Play->IsIFrameFlag = false;
                    break;
                }
                if (tmpNode->Buf == NULL || tmpNode->BufLen <= 0)
                {
                    Play->IsIFrameFlag = false;
                    break;
                }
                PInfo = (TDataFrameInfo*)tmpNode->Buf1;
                if (PInfo)
                {
                    if (PInfo->Frame.IsIFrame) goto labDecode;
                }
                avQueue_ReadEnd(Play->hQueueVideo, tmpNode);
            }
            continue;
        }
        //todo
        tmpNode = avQueue_ReadBegin(Play->hQueueVideo);
        if (tmpNode == NULL)
        {
            usleep(1000);
            continue;
        }

        if (tmpNode->Buf == NULL || tmpNode->BufLen <= 0) goto labReadEnd;

        labDecode:
        PInfo = (TDataFrameInfo*)tmpNode->Buf1;
        if (!PInfo) goto labReadEnd;
        if (Play->DecodeStyle == Decode_None) goto labReadEnd;
        if (Play->DecodeStyle == Decode_IFrame && !PInfo->Frame.IsIFrame) goto labReadEnd;
        ret = thDecodeVideoFrame(Play->decHandle, tmpNode->Buf, tmpNode->BufLen, &Play->ImgWidth, &Play->ImgHeight, &Play->FrameV420);
        if (!ret) goto labReadEnd;

        Play->IsVideoDecodeSuccessFlag = true;
        if (Play->IsSnapShot)
        {
            thDecodeVideoSaveToJpg(Play->decHandle, Play->FileName_Jpg);
            Play->IsSnapShot = false;
        }

#if (defined(ANDROID) && defined(IS_VIDEOPLAY_OPENGL))
#define TEXTURE_WIDTH 1024//android
#define TEXTURE_HEIGHT 512
        iYUVSize = Play->ImgWidth * Play->ImgHeight * 2;//AV_PIX_FMT_RGB565
        newBuf = (char*)malloc(iYUVSize);
        if (!newBuf) goto labReadEnd;
        thImgConvertFill(&newFrameV, newBuf, AV_PIX_FMT_RGB565, Play->ImgWidth, Play->ImgHeight);
        thImgConvertScale1(//only copy ?
                &newFrameV,
                AV_PIX_FMT_RGB565,
                TEXTURE_WIDTH,//Play->ImgWidth,
                TEXTURE_HEIGHT,//Play->ImgHeight,
                &Play->FrameV420,
                AV_PIX_FMT_YUV420P,
                Play->ImgWidth,
                Play->ImgHeight,
                0);
#else
        iYUVSize = Play->ImgWidth * Play->ImgHeight * 2;//AV_PIX_FMT_YUV420P //WIN32 ios
    newBuf = (char*)malloc(iYUVSize);
    if (!newBuf) goto labReadEnd;
    thImgConvertFill(&newFrameV, newBuf, AV_PIX_FMT_YUV420P, Play->ImgWidth, Play->ImgHeight);
    thImgConvertScale1(//only copy ?
      &newFrameV,
      AV_PIX_FMT_YUV420P,
      Play->ImgWidth,
      Play->ImgHeight,
      &Play->FrameV420,
      AV_PIX_FMT_YUV420P,
      Play->ImgWidth,
      Play->ImgHeight,
      0);
#endif

        if (Play->FrameRate == 0) Play->FrameRate = 30;
        Play->iFrameTime = 1000 / Play->FrameRate;

#if !(defined(ANDROID) && defined(IS_VIDEOPLAY_OPENGL))//WIN32 ios
        if (Play->iSleepTime != Play->iFrameTime)
    {
      Play->iSleepTime = Play->iFrameTime;
      //zhb ThreadLock(&Play->Lock);
      if (Play->hTimerIDQueueDraw > 0)
      {
        mmTimeKillEvent(Play->hTimerIDQueueDraw);
        Play->hTimerIDQueueDraw = NULL;
      }
      Play->hTimerIDQueueDraw = mmTimeSetEvent(Play->iSleepTime, Time_QueueDraw, (void*)Play);
      //zhb ThreadUnlock(&Play->Lock);
    }
#endif

        if (avQueue_Write(Play->hQueueDraw, (char*)&newFrameV, sizeof(TavPicture), newBuf, iYUVSize) == NULL)
        {
            free(newBuf);
        }

        labReadEnd:
        avQueue_ReadEnd(Play->hQueueVideo, tmpNode);
    }
}
//-----------------------------------------------------------------------------
void thread_QueueAudio(HANDLE NetHandle)
{
    TavNode* tmpNode;
    TDataFrameInfo* PInfo;
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return;
    while (1)
    {
        if (Play->IsExit) break;
        //if (!Play->IsConnect) break;//不能要，不然重连不了
        tmpNode = avQueue_ReadBegin(Play->hQueueAudio);
        if (tmpNode == NULL)
        {
            usleep(1000);
            continue;
        }

        if (tmpNode->Buf == NULL || tmpNode->BufLen <= 0)
        {
            avQueue_ReadEnd(Play->hQueueAudio, tmpNode);
            usleep(1000);
            continue;
        }

        PInfo = (TDataFrameInfo*)tmpNode->Buf1;
        if (PInfo)
        {
            if (!Play->IsAudioMute && Play->audioHandle) thAudioPlay_PlayFrame(Play->audioHandle, tmpNode->Buf, tmpNode->BufLen);
        }

        avQueue_ReadEnd(Play->hQueueAudio, tmpNode);
    }
}
//-----------------------------------------------------------------------------
void thread_QueueRec(HANDLE NetHandle)
{
    TavNode* tmpNode;
    TDataFrameInfo* PInfo;
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return;
    while (1)
    {
        if (Play->IsExit) break;
        //if (!Play->IsConnect) break;//不能要，不然重连不了
        tmpNode = avQueue_ReadBegin(Play->hQueueRec);
        if (tmpNode == NULL)
        {
            usleep(1000);
            continue;
        }

        if (tmpNode->Buf == NULL || tmpNode->BufLen <= 0)
        {
            avQueue_ReadEnd(Play->hQueueRec, tmpNode);
            usleep(1000);
            continue;
        }

        PInfo = (TDataFrameInfo*)tmpNode->Buf1;
        if (PInfo)
        {
            if (PInfo->Head.VerifyCode == Head_VideoPkt)
            {
                u64 pts = PInfo->Frame.FrameTime / 1000 * 90;
                if (Play->RecHandle) thRecordWriteVideo(Play->RecHandle, tmpNode->Buf, tmpNode->BufLen, pts);
            }
            else if (PInfo->Head.VerifyCode == Head_AudioPkt)
            {
                if (Play->RecHandle)
                {
                    thRecordWriteAudio(Play->RecHandle, tmpNode->Buf, tmpNode->BufLen, -1);
                }
            }
        }

        avQueue_ReadEnd(Play->hQueueRec, tmpNode);
    }
}
//-----------------------------------------------------------------------------
void OnRecvDataNotify_av(HANDLE NetHandle, TDataFrameInfo* PInfo, char* Buf, int BufLen)
{
    int i, ret;
    u64 pts;
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return;

    if (PInfo->Head.VerifyCode == Head_VideoPkt)
    {
        if (Play->videoEvent) Play->videoEvent(Play->UserCustom, PInfo->Frame.StreamType, Buf, BufLen, PInfo->Frame.IsIFrame);

        if (Play->IsQueue)
        {
            avQueue_Write(Play->hQueueRec, Buf, BufLen, (char*)PInfo, sizeof(TDataFrameInfo));
            avQueue_Write(Play->hQueueVideo, Buf, BufLen, (char*)PInfo, sizeof(TDataFrameInfo));
        }
        else
        {
            if (PInfo->Frame.StreamType != Play->DisplayStreamType) return;
            //1
            if (Play->DisplayStreamTypeOld != Play->DisplayStreamType)
            {
                ThreadLock(&Play->Lock);
                Play->DisplayStreamTypeOld = Play->DisplayStreamType;
                if (thNet_IsRec(NetHandle))//1.1
                {
                    thNet_StopRec(NetHandle);
                    thNet_StartRec(NetHandle, NULL);
                }
                if (Play->IsInsideDecode)
                {
                    if (Play->decHandle)//1.2
                    {
                        thDecodeVideoFree(Play->decHandle);
                        Play->decHandle = NULL;
                    }
                    Play->decHandle = thDecodeVideoInit(Play->VideoMediaType);//1.3
                }
                ThreadUnlock(&Play->Lock);
            }
            //2
            pts = PInfo->Frame.FrameTime / 1000 * 90;
            if (Play->RecHandle)
            {
                thRecordWriteVideo(Play->RecHandle, Buf, BufLen, pts);
            }

            //3
            if (Play->IsInsideDecode)
            {
                ret = thDecodeVideoFrame(Play->decHandle, Buf, BufLen, &Play->ImgWidth, &Play->ImgHeight, &Play->FrameV420);//yuv420
                if (!ret) return;
                Play->IsVideoDecodeSuccessFlag = true;
                if (Play->IsSnapShot)
                {
                    thDecodeVideoSaveToJpg(Play->decHandle, Play->FileName_Jpg);
                    Play->IsSnapShot = false;
                }

                if (Play->ImgWidth > 0 && Play->ImgHeight > 0)
                {
#if (defined(ANDROID) && defined(IS_VIDEOPLAY_OPENGL))//android
                    ret = thRender_FillMem(Play->RenderHandle, Play->FrameV420, Play->ImgWidth, Play->ImgHeight);//ddraw yuv420->rgb32
                    pthread_cond_signal(&Play->SyncCond);
#else//WIN32 ios
                    ret = thRender_FillMem(Play->RenderHandle, Play->FrameV420, Play->ImgWidth, Play->ImgHeight);//ddraw yuv420->rgb32
          for (i=0; i<MAX_DSPINFO_COUNT; i++)
          {
            TDspInfo* PDspInfo = &Play->DspInfoLst[i];
            if (PDspInfo->DspHandle == NULL) continue;
            ret = thRender_Display(Play->RenderHandle, PDspInfo->DspHandle, PDspInfo->DspRect);
          }
#endif
                }
            }
        }
    }
    else if (PInfo->Head.VerifyCode == Head_AudioPkt)
    {
        if (Play->audioEvent) Play->audioEvent(Play->UserCustom, PInfo->Frame.StreamType, Buf, BufLen);
        if (Play->IsQueue)
        {
            avQueue_Write(Play->hQueueRec, Buf, BufLen, (char*)PInfo, sizeof(TDataFrameInfo));
            avQueue_Write(Play->hQueueAudio, Buf, BufLen, (char*)PInfo, sizeof(TDataFrameInfo));
        }
        else
        {
            if (Play->RecHandle)
            {
                thRecordWriteAudio(Play->RecHandle, Buf, BufLen, -1);
            }
            if (!Play->IsAudioMute && Play->audioHandle) thAudioPlay_PlayFrame(Play->audioHandle, Buf, BufLen);
        }
    }
}
//-----------------------------------------------------------------------------
bool thNet_SendSensePkt(HANDLE NetHandle)
{
    int ret;
    THeadPkt Head;
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    if (!Play->IsConnect) return false;
    Head.VerifyCode = Head_SensePkt;
    Head.PktSize = 0;

    if (!Play->Isp2pConn)
    {
        ret = SendBuf(Play->hSocket, (char*)&Head, sizeof(THeadPkt), NET_TIMEOUT);
    }
    else
    {
        ret = (avSendIOCtrl(Play->p2p_avIndex, Head_CmdPkt, (char*)&Head, sizeof(THeadPkt)) >= 0);
    }
    PRINTF("%s(%s)(%s) ret:%d\n", __FUNCTION__, Play->IPUID, Play->LocalIP, ret);
    ThreadLock(&Play->Lock);
    Play->IsConnect = ret;
    ThreadUnlock(&Play->Lock);
    return ret;
}
//-----------------------------------------------------------------------------
void thread_RecvData_TCP(HANDLE NetHandle)
{
    i32 optsize;
    time_t t, t1;
    bool ret;
    i32 HEADPKTSIZE;
    THeadPkt* PHead;
    char* RecvBuffer;
    TPlayParam* Play;
    if (NetHandle == 0) return;
    Play = (TPlayParam*)NetHandle;
    if (!Play) return;

    RecvBuffer = Play->RecvBuffer;
    PHead = (THeadPkt*)(Play->RecvBuffer);
    HEADPKTSIZE = sizeof(THeadPkt);
    ret = false;

    starts:
#ifndef WIN32
    fcntl(Play->hSocket, F_SETFL, O_NONBLOCK);//非阻塞方式
#else
    optsize = 1;
  ioctlsocket(Play->hSocket, FIONBIO, (u_long*)&optsize);//非阻塞方式
#endif

    t = GetTime();
    t1 = t;
    Play->LastSenseTime = 0;

    while(1)
    {
        if (Play->IsExit) return;

        t1 = GetTime();
        Play->LastSenseTime = t1 - t;

        if (Play->VideoChlMask == 0 && Play->SubVideoChlMask == 0 && Play->AudioChlMask == 0)
        {
            if (Play->LastSenseTime >= 10)
            {
                ret = thNet_SendSensePkt(NetHandle);
                if (!ret)
                {
                    if (!Play->IsAutoReConn)
                    {
                        thNet_DisConn(NetHandle);
                        return;
                    }
                    goto reconnects;
                }

                Play->LastSenseTime = 0;//最后侦测时间
                t = t1;
            }
            usleep(1000*100);
            continue;
        }

        if (Play->LastSenseTime >= 10)
        {
            reconnects:
            closesocket(Play->hSocket);
            Play->hSocket = 0;
            Play->IsConnect = false;
            if (Play->AlmEvent) Play->AlmEvent(Net_Disconn, t1, 0, Play->UserCustom);

            //connect
            ret = net_Connect_IP(NetHandle, false);
            PRINTF("reconnects(%s) IsConnect:%d\n", Play->IPUID, ret);
            if (ret)
            {
                if (Play->AlmEvent) Play->AlmEvent(Net_ReConn, t1, 0, Play->UserCustom);
                thNet_Play(NetHandle, Play->VideoChlMask, Play->AudioChlMask, Play->SubVideoChlMask);
            }
            Play->LastSenseTime = 0;
            t = t1;
            goto starts;
        }
        //2
        if (Play->hSocket <= 0)
        {
            usleep(100*1000);
            continue;
        }
        //3
        ret = RecvBuf(Play->hSocket, (char*)PHead, HEADPKTSIZE, NET_TIMEOUT / 2);
        if (!ret) continue;

        if (
                PHead->VerifyCode!=Head_VideoPkt      &&
                PHead->VerifyCode!=Head_AudioPkt      &&
                PHead->VerifyCode!=Head_SensePkt      &&
                PHead->VerifyCode!=Head_TalkPkt       &&
                PHead->VerifyCode!=Head_UploadPkt     &&
                PHead->VerifyCode!=Head_DownloadPkt   &&
                PHead->VerifyCode!=Head_CfgPkt        &&
                PHead->VerifyCode!=Head_MotionInfoPkt &&  //add at 20130506
                PHead->VerifyCode!=Head_CmdPkt)
        {
            continue;
        }

        if (PHead->PktSize + HEADPKTSIZE > sizeof(Play->RecvBuffer)) continue;

        ret = RecvBuf(Play->hSocket, RecvBuffer + HEADPKTSIZE, PHead->PktSize, NET_TIMEOUT / 2);//PktSize==0 return true
        if (!ret) continue;

        Play->LastSenseTime = 0;//最后侦测时间
        t = t1;

        switch (PHead->VerifyCode)
        {
            case Head_VideoPkt:
            case Head_AudioPkt:
            {
                TDataFrameInfo* PInfo = (TDataFrameInfo*)RecvBuffer;
                char* Buf = &RecvBuffer[sizeof(TDataFrameInfo)];
                i32 BufLen = PHead->PktSize - 16;

                if (PHead->VerifyCode == Head_VideoPkt)
                {
                    //20180702 add
                    if (Play->VideoChlMask == 1 && Play->SubVideoChlMask == 0) {if (PInfo->Frame.StreamType != 0) continue;}
                    if (Play->VideoChlMask == 0 && Play->SubVideoChlMask == 1) {if (PInfo->Frame.StreamType != 1) continue;}
                    //20180702 add

                    if (!Play->IsIFrameFlag)
                    {
                        if (PInfo->Frame.IsIFrame) Play->IsIFrameFlag = true;
                    }
                    if (!Play->IsIFrameFlag) continue;

                    OnRecvDataNotify_av(NetHandle, PInfo, Buf, BufLen);
                }
                else if (PHead->VerifyCode == Head_AudioPkt)
                {
                    if (Play->DevCfg.AudioCfgPkt.AudioFormat.wFormatTag == G711)
                    {
                        char dstBuf[4096];
                        i32 dstBufLen;
                        dstBufLen = g711_decode(dstBuf, (unsigned char*)Buf, BufLen);
                        OnRecvDataNotify_av(NetHandle, PInfo, dstBuf, dstBufLen);
                    }
                    else//PCM
                    {
                        OnRecvDataNotify_av(NetHandle, PInfo, Buf, BufLen);
                    }
                }
            }
                break;

            case Head_CmdPkt://网络命令包
            {
                TNetCmdPkt* PPkt = (TNetCmdPkt*)RecvBuffer;
                if (PPkt->CmdPkt.MsgID == Msg_Alarm)
                {
                    if (Play->AlmEvent)
                    {
                        Play->AlmEvent(
                                PPkt->CmdPkt.AlmSendPkt.AlmType,
                                PPkt->CmdPkt.AlmSendPkt.AlmTime,
                                PPkt->CmdPkt.AlmSendPkt.AlmPort,
                                Play->UserCustom);
                    }
                }
            }
                break;
        }//end switch
    }//end for (;;)
}
//-----------------------------------------------------------------------------
bool net_Connect_IP(HANDLE NetHandle, bool IsCreateRecvThread)
{
    bool ret = false;
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    if (Play->IsConnect) return true;
    Play->IsConnect = false;
    Play->Isp2pConn = false;

    Play->IsTaskRec = false;

    strcpy(Play->LocalIP, GetLocalIP());

    Play->hSocket = SktConnect(Play->IPUID, Play->DataPort, Play->TimeOut);//ok
    if (Play->hSocket <= 0) return false;

    ret = net_Login(NetHandle);
    if (ret == false)
    {
        thNet_DisConn(NetHandle);
        return false;
    }
    ret = net_GetAllCfg(NetHandle);
    if (ret == false)
    {
        thNet_DisConn(NetHandle);
        return false;
    }

    Play->IsExit = false;
    if (IsCreateRecvThread) Play->thRecv = ThreadCreate(thread_RecvData_TCP, (void*)NetHandle, false);

    Play->IsConnect = ret;

    return Play->IsConnect;
}
//-----------------------------------------------------------------------------
#ifdef IsUsedP2P
void thread_RecvData_P2P(HANDLE NetHandle)
{
    char dstBuf[8192];
    i32 dstBufLen;
    i32 ret;
    i32 outBufSize = 0;
    i32 outFrmSize = 0;
    i32 outFrmInfoSize = 0;
    time_t t, t1;
    FRAMEINFO_t frameInfo;
    u32 frmNo;
    i32 flag = 0, cnt = 0;

    TPlayParam* Play = (TPlayParam*)NetHandle;
    TNewCmdPkt* PPkt;
    TDataFrameInfo* PInfo;
    i32 BufLen;
    BufLen = 0;
    Play->RecvLen = 0;

    starts:
    t = GetTime();
    t1 = t;

    Play->LastSenseTime = 0;

    while(1)
    {
        if (Play->IsExit) break;
        //1
        t1 = GetTime();
        Play->LastSenseTime = t1 - t;

        if (Play->VideoChlMask == 0 && Play->SubVideoChlMask == 0 && Play->AudioChlMask == 0 && Play->IsStopHttpGet)
        {
            if (Play->LastSenseTime >= 10)
            {
                ret = thNet_SendSensePkt(NetHandle);
                if (!ret)
                {
                    if (!Play->IsAutoReConn)
                    {
                        thNet_DisConn(NetHandle);
                        return;
                    }
                    goto reconnects;
                }

                Play->LastSenseTime = 0;//最后侦测时间
                t = t1;
            }
            usleep(1000*100);
            continue;
        }

        if (Play->LastSenseTime >= 10)
        {
            reconnects:
            if (Play->p2p_avIndex >= 0)   {avClientStop(Play->p2p_avIndex); Play->p2p_avIndex = -1;}
            if (Play->p2p_talkIndex >=0)  {avServStop(Play->p2p_talkIndex); Play->p2p_talkIndex = -1;}
            if (Play->p2p_SessionID >= 0) {IOTC_Session_Close(Play->p2p_SessionID); Play->p2p_SessionID = -1;}
#if 0
            avDeInitialize();
      IOTC_DeInitialize();
#endif
            Play->IsConnect = false;
            if (Play->AlmEvent) Play->AlmEvent(Net_Disconn, t1, 0, Play->UserCustom);

            ret = net_Connect_P2P(NetHandle, false);
            PRINTF("reconnects(%s) IsConnect:%d\n", Play->IPUID, ret);
            if (ret)
            {
                if (Play->AlmEvent) Play->AlmEvent(Net_ReConn, t1, 0, Play->UserCustom);
                thNet_Play(NetHandle, Play->VideoChlMask, Play->AudioChlMask, Play->SubVideoChlMask);
            }
            Play->LastSenseTime = 0;
            t = t1;
            goto starts;
        }
        //2

        if (Play->p2pSoftVersion < 1)
        {
            if (Play->VideoChlMask==0 && Play->AudioChlMask==0 && Play->SubVideoChlMask==0)
            {
                usleep(1000*10);
                continue;
            }
        }

        BufLen = avRecvFrameData2(Play->p2p_avIndex, Play->RecvBuffer, MAX_BUF_SIZE,
                                  &outBufSize, &outFrmSize, (char*)&frameInfo, sizeof(FRAMEINFO_t), &outFrmInfoSize, &frmNo);

        if(BufLen == AV_ER_DATA_NOREADY) {usleep(1000*1); continue;}
        else if(BufLen == AV_ER_LOSED_THIS_FRAME) continue;
        else if(BufLen == AV_ER_INCOMPLETE_FRAME) continue;
        else if(BufLen == AV_ER_SESSION_CLOSE_BY_REMOTE) break;
        else if(BufLen == AV_ER_REMOTE_TIMEOUT_DISCONNECT)break;
        else if(BufLen == IOTC_ER_INVALID_SID) break;
        if (BufLen <= 0) continue;

        Play->LastSenseTime = 0;//最后侦测时间
        t = t1;

        Play->RecvLen = BufLen;
        PPkt = (TNewCmdPkt*)(Play->RecvBuffer);
        if (PPkt->VerifyCode == Head_CmdPkt && PPkt->MsgID == Msg_Alarm)
        {
            if (Play->AlmEvent) Play->AlmEvent(PPkt->AlmSendPkt.AlmType, PPkt->AlmSendPkt.AlmTime, PPkt->AlmSendPkt.AlmPort, Play->UserCustom);
            continue;
        }

        PInfo = (TDataFrameInfo*)(Play->RecvBuffer);

        if (PInfo->Head.VerifyCode == Head_VideoPkt)
        {
            //20180702 add
            if (Play->VideoChlMask == 1 && Play->SubVideoChlMask == 0) {if (PInfo->Frame.StreamType != 0) continue;}
            if (Play->VideoChlMask == 0 && Play->SubVideoChlMask == 1) {if (PInfo->Frame.StreamType != 1) continue;}
            //20180702 add

            if (!Play->IsIFrameFlag)
            {
                if (PInfo->Frame.IsIFrame) Play->IsIFrameFlag = true;
            }
            if (!Play->IsIFrameFlag) continue;

            OnRecvDataNotify_av(NetHandle, PInfo, Play->RecvBuffer+24, Play->RecvLen-24);
        }
        else if (PInfo->Head.VerifyCode == Head_AudioPkt)
        {
            if (Play->DevCfg.AudioCfgPkt.AudioFormat.wFormatTag == G711)
            {
                dstBufLen = g711_decode(dstBuf, (unsigned char*)Play->RecvBuffer+24, Play->RecvLen-24);
                OnRecvDataNotify_av(NetHandle, PInfo, dstBuf, dstBufLen);
            }
            else//PCM
            {
                OnRecvDataNotify_av(NetHandle, PInfo, Play->RecvBuffer+24, Play->RecvLen-24);
            }
        }
        else if (PInfo->Head.VerifyCode == Head_DownloadPkt)
        {
            memcpy(Play->RecvDownloadBuf, Play->RecvBuffer + sizeof(PInfo->Head), PInfo->Head.PktSize);
            Play->RecvDownloadLen = PInfo->Head.PktSize;
            //if (Play->semHttpDownload) sem_post(&Play->semHttpDownload);
        }
    }
}
#endif
//-----------------------------------------------------------------------------
bool net_Connect_P2P(HANDLE NetHandle, bool IsCreateRecvThread)
{
#ifdef IsUsedP2P
    char20 sLocalIP;
    i32 v1, v2, v3, v4;
    unsigned long nServType;
    i32 nResend = -1;
    struct st_SInfo Sinfo;
    char *mode[] = {"P2P", "RLY", "LAN"};
    u16 val = 0;
    i32 tmpSID;
    i32 ret = false;
    TPlayParam* Play = (TPlayParam*)NetHandle;
    PRINTF("================ before connect p2p,Play->IPUID:%s\n",Play->IPUID);
    if (NetHandle == 0) return false;
    if (Play->IsConnect) return true;
    Play->IsConnect = false;
    Play->IsExit = false;
    Play->IsTaskRec = false;
    Play->Isp2pConn = true;
#ifdef WIN32
    if (!Isp2pInit)
  {
    P2P_Init();
    Isp2pInit = true;
  }
  sprintf(sLocalIP, GetLocalIP());
  strcpy(Play->LocalIP, sLocalIP);
#endif
    //#warning "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"
#if 1
    tmpSID = IOTC_Get_SessionID();
    PRINTF("================ -1  connect p2p,tmpSID :%d,Play->IPUID:%s\n",tmpSID,Play->IPUID);
    Play->p2p_SessionID = IOTC_Connect_ByUID_Parallel(Play->IPUID, tmpSID);
    PRINTF("================ -2  connect p2p,Play->p2p_SessionID :%d,Play->IPUID:%s\n",Play->p2p_SessionID,Play->IPUID);
    if (Play->p2p_SessionID < 0) goto exits;
#else
    Play->p2p_SessionID = IOTC_Connect_ByUID(Play->IPUID);
  PRINTF("%s(%d) IOTC_Connect_ByUID:%d\n", __FUNCTION__, __LINE__, Play->p2p_SessionID);
  if (Play->p2p_SessionID < 0) goto exits;
#endif

    memset(&Sinfo, 0, sizeof(struct st_SInfo));
    IOTC_Session_Check(Play->p2p_SessionID, &Sinfo);
    Play->p2p_avIndex = avClientStart2(Play->p2p_SessionID, Play->UserName, Play->Password, Play->TimeOut, &nServType, 0, &nResend);
    PRINTF("================ 0  connect p2p,nethandel is %lld\n", NetHandle);
    if(Play->p2p_avIndex < 0) goto exits;
    PRINTF("================ 1 connect p2p,nethandel is %lld\n", NetHandle);
    avSendIOCtrl(Play->p2p_avIndex, IOTYPE_INNER_SND_DATA_DELAY, (char*)&val, sizeof(u16));
#define AUDIO_SPEAKER_CHANNEL 5
    //Play->p2p_talkIndex = avServStart(Play->p2p_SessionID, NULL, NULL, 60, 0, AUDIO_SPEAKER_CHANNEL);
    Play->p2p_talkIndex = avServStart(Play->p2p_SessionID, NULL, NULL, Play->TimeOut, 0, AUDIO_SPEAKER_CHANNEL);

    if(Play->p2p_talkIndex < 0) goto exits;

    ret = net_GetAllCfg(NetHandle);
    PRINTF("================ 2 connect p2p,nethandel is %lld\n", NetHandle);
    if (!ret) goto exits;
    ret = sscanf(Play->DevCfg.DevInfoPkt.SoftVersion, "V%d.%d.%d.%d", &v1, &v2, &v3, &v4);
    Play->p2pSoftVersion = 0;//old
    if (ret == 4 && v1 > 5 && v2 > 200) Play->p2pSoftVersion = 1;
    if (strstr(Play->DevCfg.DevInfoPkt.DevModal, "CCY") > 0) Play->p2pSoftVersion = 1;//qiu
    if (Play->DevCfg.p2pCfgPkt.Version > 1) Play->p2pSoftVersion = Play->DevCfg.p2pCfgPkt.Version;//由StreamType改过来的
    PRINTF("p2pSoftVersion:%d\n", Play->p2pSoftVersion);

    if (IsCreateRecvThread) Play->thRecv = ThreadCreate(thread_RecvData_P2P, (void*)NetHandle, false);

    Play->IsConnect = true;
    return Play->IsConnect;

    exits:
    thNet_DisConn(NetHandle);
    return false;
#else
    return false;
#endif
}
//-----------------------------------------------------------------------------
bool thNet_Connect(HANDLE NetHandle, char* UserName, char* Password, char* IPUID, i32 DataPort, u32 TimeOut)
{
    bool ret;
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    PRINTF("connect ,IPUID is %s\n",IPUID);

    if (Play->IsConnect) return true;
    /*
    if (Play->iConnectStatus == THNET_CONNSTATUS_CONNING)
    {
    PRINTF("%s(%s)(%s) UserName:%s Password:%s THNET_CONNSTATUS_CONNING\n", __FUNCTION__, Play->IPUID, Play->LocalIP,
    Play->UserName, Play->Password, Play->TimeOut);
    return false;
    }
    */
    Play->iConnectStatus = THNET_CONNSTATUS_CONNING;

    strcpy(Play->UserName, UserName);
    strcpy(Play->Password, Password);
    strcpy(Play->IPUID, IPUID);
    PRINTF("connect ,strcpy Play->IPUID is %s\n",Play->IPUID);
    Play->DataPort = DataPort;
    Play->TimeOut = TimeOut;
    if (Play->TimeOut == 0) Play->TimeOut = NET_TIMEOUT;

    Play->Isp2pConn = (!(IsValidIP(IPUID) || IsValidHost(IPUID))) && (strlen(IPUID)==20);

    Play->IsStopHttpGet = true;//必须


    if (Play->Isp2pConn == false)
    {
        ret = net_Connect_IP(NetHandle, true);
    }
    else
    {
        ret = net_Connect_P2P(NetHandle, true);
    }

    if (Play->IsConnect)
    {
        //sem_init(&Play->semHttpDownload, 0, 0);
        if (Play->IsQueue)
        {
            Play->hQueueDraw = avQueue_Init(MAX_QUEUE_COUNT, true, false);//newBuf在Time_QueueDraw中释放

            Play->hQueueVideo = avQueue_Init(MAX_QUEUE_COUNT, true, true);
            Play->hQueueAudio = avQueue_Init(MAX_QUEUE_COUNT, true, true);
            Play->hQueueRec = avQueue_Init(MAX_QUEUE_COUNT, true, true);

            Play->thQueueVideo = ThreadCreate(thread_QueueVideo, Play, false);
            Play->thQueueAudio = ThreadCreate(thread_QueueAudio, Play, false);
            Play->thQueueRec = ThreadCreate(thread_QueueRec, Play, false);
        }

        if (Play->IsInsideDecode)
        {
            TMediaType MediaType = CODEC_NONE;
            TVideoFormat* vfmt = &Play->DevCfg.VideoCfgPkt.VideoFormat;
            if (vfmt->VideoType == MPEG4) vfmt->VideoType = H264;

            if (vfmt->VideoType == MJPEG) MediaType = CODEC_MJPEG;
            else if (vfmt->VideoType == MPEG4) MediaType = CODEC_MPEG4;
            else if (vfmt->VideoType == H264)  MediaType = CODEC_H264;
            else if (vfmt->VideoType == H265)  MediaType = CODEC_H265;
            Play->VideoMediaType = MediaType;
            Play->decHandle = thDecodeVideoInit(MediaType);
            Play->RenderHandle = thRender_Init(0);
        }
    }

    if (Play->IsConnect) Play->iConnectStatus = THNET_CONNSTATUS_SUCCESS; else  Play->iConnectStatus = THNET_CONNSTATUS_FAIL;

    PRINTF("%s(%s)(%s) UserName:%s Password:%s TimeOut:%d IsConnect:%d\n", __FUNCTION__, Play->IPUID, Play->LocalIP,
           Play->UserName, Play->Password, Play->TimeOut, Play->IsConnect);
    return Play->IsConnect;
}
//-----------------------------------------------------------------------------
bool thNet_DisConn(HANDLE NetHandle)
{
    int i, iCount;
    TavNode* tmpNode;
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    if (!Play->IsConnect) return true;
    PRINTF("%s(%s)(%s)\n", __FUNCTION__, Play->IPUID, Play->LocalIP);

    memset(Play->LocalIP, 0, sizeof(Play->LocalIP));

    thNet_AudioPlayClose(NetHandle);//free audio player
    thNet_TalkClose(NetHandle);

    thNet_StopRec(NetHandle);

    //if (Play->semHttpDownload) sem_destroy(&Play->semHttpDownload);
    ThreadLock(&Play->Lock);
    Play->IsExit = true;
    ThreadUnlock(&Play->Lock);
    if (!Play->Isp2pConn)
    {
        if (Play->thRecv > 0)
        {
            ThreadExit(Play->thRecv, 0);//1000;
            Play->thRecv = 0;
        }

        Play->Session = 0;
        if (Play->hSocket > 0)
        {
            closesocket(Play->hSocket);
            Play->hSocket = 0;
        }
    }
    else
    {
#ifdef IsUsedP2P
        if (Play->thRecv > 0)
        {
            ThreadExit(Play->thRecv, 0);//1000;
            Play->thRecv = 0;
        }
        if (Play->p2p_avIndex >= 0)
        {
            avClientStop(Play->p2p_avIndex);
            Play->p2p_avIndex = -1;
        }
        if (Play->p2p_talkIndex >=0)
        {
            avServStop(Play->p2p_talkIndex);
            Play->p2p_talkIndex = -1;
        }
        if (Play->p2p_SessionID >= 0)
        {
            IOTC_Session_Close(Play->p2p_SessionID);
            Play->p2p_SessionID = -1;
        }
#ifdef WIN32
        P2P_Free();
#endif

#endif
    }
    Play->IsConnect = false;
    Play->iConnectStatus = THNET_CONNSTATUS_NO;
    if (Play->IsQueue)
    {
        if (Play->hTimerIDQueueDraw)
        {
            mmTimeKillEvent(Play->hTimerIDQueueDraw);
            Play->hTimerIDQueueDraw = NULL;
            Play->iSleepTime = 0;
        }

        if (Play->hQueueDraw)
        {
            iCount = avQueue_GetCount(Play->hQueueDraw);
            for (i=0; i<iCount; i++)
            {
                tmpNode = avQueue_ReadBegin(Play->hQueueDraw);
                if (!tmpNode) continue;
                if (tmpNode->Buf1) free(tmpNode->Buf1);//render malloc buf
                avQueue_ReadEnd(Play->hQueueDraw, tmpNode);
            }
            avQueue_Free(Play->hQueueDraw);
            Play->hQueueDraw = NULL;
        }

        if (Play->thQueueVideo)
        {
            ThreadExit(Play->thQueueVideo, 0);//1000;
            Play->thQueueVideo = NULL;
        }

        if (Play->hQueueVideo)
        {
            avQueue_Free(Play->hQueueVideo);
            Play->hQueueVideo = NULL;
        }

        if (Play->thQueueAudio)
        {
            ThreadExit(Play->thQueueAudio, 0);//1000;
            Play->thQueueAudio = NULL;
        }

        if (Play->hQueueAudio)
        {
            avQueue_Free(Play->hQueueAudio);
            Play->hQueueAudio = 0;
        }

        if (Play->thQueueRec)
        {
            ThreadExit(Play->thQueueRec, 0);//1000;
            Play->thQueueRec = NULL;
        }

        if (Play->hQueueRec)
        {
            avQueue_Free(Play->hQueueRec);
            Play->hQueueRec = NULL;
        }
    }

    if (Play->IsInsideDecode)
    {
        if (Play->decHandle)
        {
            if (thDecodeVideoFree(Play->decHandle)) Play->decHandle = NULL;
        }
        if (Play->RenderHandle)
        {
            if (thRender_Free(Play->RenderHandle)) Play->RenderHandle = NULL;
        }
        if (Play->audioHandle)
        {
            thAudioPlay_Free(Play->audioHandle);
            Play->audioHandle = NULL;
        }
        if (Play->talkHandle)
        {
            thAudioTalk_Free(Play->talkHandle);
            Play->talkHandle = NULL;
        }
    }

    return true;
}
//-----------------------------------------------------------------------------
bool thNet_IsConnect(HANDLE NetHandle)
{
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;

    return Play->IsConnect;
}
//-----------------------------------------------------------------------------
int thNet_GetConnectStatus(HANDLE NetHandle)
{
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    return Play->iConnectStatus;
}
//-----------------------------------------------------------------------------
bool thNet_Play(HANDLE NetHandle, u32 VideoChlMask, u32 AudioChlMask, u32 SubVideoChlMask)
{

    bool Result = false;
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    if (!Play->IsConnect) return false;

    if (!(VideoChlMask == 0 && AudioChlMask == 0 && SubVideoChlMask == 0))
        PRINTF("%s(%s)(%s) VideoChlMask:%d AudioChlMask:%d SubVideoChlMask:%d\n", __FUNCTION__, Play->IPUID, Play->LocalIP, VideoChlMask, AudioChlMask, SubVideoChlMask);

    ThreadLock(&Play->Lock);
    Play->VideoChlMask = VideoChlMask;
    Play->AudioChlMask = AudioChlMask;
    Play->SubVideoChlMask = SubVideoChlMask;
    Play->IsIFrameFlag = false;
    Play->IsVideoDecodeSuccessFlag = false;

    if (Play->VideoChlMask)
    {
        //Play->ImgWidth = Play->DevCfg.VideoCfgPkt.VideoFormat.Width;
        //Play->ImgHeight = Play->DevCfg.VideoCfgPkt.VideoFormat.Height;
        Play->FrameRate = Play->DevCfg.VideoCfgPkt.VideoFormat.FrameRate;
        Play->DisplayStreamType = 0;

    }
    else if (Play->SubVideoChlMask)
    {
        //GetWidthHeightFromStandard(Play->DevCfg.VideoCfgPkt.VideoFormat.Sub.StandardEx, &Play->ImgWidth, &Play->ImgHeight);
        Play->FrameRate = Play->DevCfg.VideoCfgPkt.VideoFormat.Sub.FrameRate;
        Play->DisplayStreamType = 1;
    }
    ThreadUnlock(&Play->Lock);

    if (!Play->Isp2pConn)
    {
        TNetCmdPkt Pkt;
        if (Play->Session == 0) return false;
        memset(&Pkt, 0, sizeof(Pkt));
        Pkt.HeadPkt.VerifyCode = Head_CmdPkt;
        Pkt.HeadPkt.PktSize = sizeof(Pkt.CmdPkt);
        Pkt.CmdPkt.PktHead = Pkt.HeadPkt.VerifyCode;
        Pkt.CmdPkt.MsgID = Msg_PlayLive;
        Pkt.CmdPkt.Session = Play->Session;

        Pkt.CmdPkt.LivePkt.VideoChlMask = VideoChlMask;//主码流
        Pkt.CmdPkt.LivePkt.AudioChlMask = AudioChlMask;
        Pkt.CmdPkt.LivePkt.SubVideoChlMask = SubVideoChlMask;
        Result = (SendBuf(Play->hSocket, (char*)&Pkt, sizeof(Pkt), NET_TIMEOUT) == true);
    }
    else
    {
#ifdef IsUsedP2P
        i32 ret;
        TNewCmdPkt Pkt;
        memset(&Pkt, 0, sizeof(Pkt));
        Pkt.VerifyCode = Head_CmdPkt;
        Pkt.PktSize = sizeof(Pkt.LivePkt);
        Pkt.MsgID = Msg_PlayLive;
        Pkt.Result = 0;
        Pkt.LivePkt.VideoChlMask = VideoChlMask;//主码流
        Pkt.LivePkt.AudioChlMask = AudioChlMask;
        Pkt.LivePkt.SubVideoChlMask = SubVideoChlMask;

        ret = avSendIOCtrl(Play->p2p_avIndex, Head_CmdPkt, (char*)&Pkt, 8 + Pkt.PktSize);
        if(ret < 0) return false;
        Result = true;
#endif
    }

    if (Result)
    {
        if (Play->IsTaskRec && Play->RecHandle == NULL)
        {
            Play->IsTaskRec = false;
            thNet_StartRec(NetHandle, NULL);
        }
    }
    return Result;
}
//-----------------------------------------------------------------------------
bool thNet_Stop(HANDLE NetHandle)
{
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    if (!Play->IsConnect) return false;
    PRINTF("%s(%s)(%s)\n", __FUNCTION__, Play->IPUID, Play->LocalIP);
    thNet_Play(NetHandle, 0, 0, 0);
    return true;
}
//-----------------------------------------------------------------------------
bool thNet_IsPlay(HANDLE NetHandle)
{
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    if (!Play->IsConnect) return false;
    return (Play->VideoChlMask == 1 || Play->SubVideoChlMask == 1 || Play->AudioChlMask == 1);
}
//-----------------------------------------------------------------------------
bool net_SetTalk(HANDLE NetHandle, char* Buf, i32 BufLen)
{
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    if (!Play->IsConnect) return false;
    if (!Play->Isp2pConn)
    {
        bool ret;
        TTalkHeadPkt Head;
        if (Play->Session == 0) return false;
        Head.VerifyCode = Head_TalkPkt;
        Head.PktSize = BufLen + 24;
        ret = SendBuf(Play->hSocket, (char*)&Head, sizeof(TTalkHeadPkt), NET_TIMEOUT);
        if (ret) return (SendBuf(Play->hSocket, Buf, BufLen, NET_TIMEOUT) == true);
    }
    else
    {
#ifdef IsUsedP2P
        i32 i, idiv, imod, iMaxSize, ret;
        FRAMEINFO_t framInfo;

        if (Play->p2p_SessionID < 0) return false;
        if (Play->p2p_talkIndex < 0) return false;

        memset(&framInfo, 0, sizeof(FRAMEINFO_t));
        framInfo.timestamp = 0;
        framInfo.codec_id = MEDIA_CODEC_AUDIO_PCM;
        framInfo.cam_index = 0;
        framInfo.flags = (AUDIO_SAMPLE_8K << 2) | (AUDIO_DATABITS_16 << 1) | AUDIO_CHANNEL_MONO;
        framInfo.onlineNum = 1;//online;

        iMaxSize = 1024;
        idiv = BufLen / iMaxSize;
        imod = (BufLen % iMaxSize);
        for (i=0; i<idiv; i++)
        {
            ret = avSendAudioData(Play->p2p_talkIndex, &Buf[i*iMaxSize], iMaxSize, &framInfo, sizeof(FRAMEINFO_t));
        }
        if (imod > 0) ret = avSendAudioData(Play->p2p_talkIndex, &Buf[i*iMaxSize], imod, &framInfo, sizeof(FRAMEINFO_t));
#endif
        return true;
    }

    return false;
}
//-----------------------------------------------------------------------------
void callback_AudioTalk(void* UserCustom, char* Buf, i32 Len)
{
    HANDLE NetHandle = (HANDLE)UserCustom;
    if (NetHandle == 0) return;
    net_SetTalk(NetHandle, Buf, Len);
}
//-----------------------------------------------------------------------------
bool thNet_TalkOpen(HANDLE NetHandle)
{
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    if (!Play->IsConnect) return false;
    if (!Play->IsInsideDecode) return false;
    PRINTF("%s(%s)(%s)\n", __FUNCTION__, Play->IPUID, Play->LocalIP);
    ThreadLock(&Play->Lock);
    Play->talkHandle = thAudioTalk_Init();
    if (Play->talkHandle)
    {
        thAudioTalk_SetFormat(
                Play->talkHandle,
                Play->DevCfg.AudioCfgPkt.AudioFormat.nChannels,
                Play->DevCfg.AudioCfgPkt.AudioFormat.wBitsPerSample,
                Play->DevCfg.AudioCfgPkt.AudioFormat.nSamplesPerSec,
                AUDIO_COLLECT_SIZE,
                callback_AudioTalk,
                Play);
    }
    ThreadUnlock(&Play->Lock);
    return (Play->talkHandle != NULL);
}
//-----------------------------------------------------------------------------
bool thNet_TalkClose(HANDLE NetHandle)
{
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    if (!Play->IsConnect) return false;
    if (!Play->IsInsideDecode) return false;
    PRINTF("%s(%s)(%s)\n", __FUNCTION__, Play->IPUID, Play->LocalIP);
    if (Play->talkHandle)
    {
        ThreadLock(&Play->Lock);
        thAudioTalk_Free(Play->talkHandle);
        Play->talkHandle = NULL;
        ThreadUnlock(&Play->Lock);
    }
    return true;
}
//-----------------------------------------------------------------------------
bool thNet_RemoteFilePlay(HANDLE NetHandle, char* FileName)
{
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    if (!Play->IsConnect) return false;
    PRINTF("%s(%s)(%s) FileName:%s\n", Play->IPUID, Play->LocalIP, FileName);
    if (!Play->Isp2pConn)
    {
        TNetCmdPkt Pkt;
        bool ret = false;
        if (Play->Session == 0) return false;
        Play->VideoChlMask = 0;
        Play->AudioChlMask = 1;
        Play->SubVideoChlMask = 1;

        memset(&Pkt, 0, sizeof(Pkt));
        Pkt.HeadPkt.VerifyCode = Head_CmdPkt;
        Pkt.HeadPkt.PktSize = sizeof(Pkt.CmdPkt);
        Pkt.CmdPkt.PktHead = Pkt.HeadPkt.VerifyCode;
        Pkt.CmdPkt.MsgID = Msg_StartPlayRecFile;
        Pkt.CmdPkt.Session = Play->Session;
        strcpy(Pkt.CmdPkt.RecFilePkt.FileName, FileName);
        ret = SendBuf(Play->hSocket, (char*)&Pkt, sizeof(Pkt), NET_TIMEOUT);
        return ret;
    }
    else
    {
#ifdef IsUsedP2P
        i32 ret = false;
        TNewCmdPkt Pkt;
        Play->VideoChlMask = 0;
        Play->AudioChlMask = 1;
        Play->SubVideoChlMask = 1;

        memset(&Pkt, 0, sizeof(Pkt));
        Pkt.VerifyCode = Head_CmdPkt;
        Pkt.MsgID = Msg_StartPlayRecFile;
        Pkt.Result = false;
        Pkt.PktSize = sizeof(Pkt.RecFilePkt);
        strcpy(Pkt.RecFilePkt.FileName, FileName);
        ret = avSendIOCtrl(Play->p2p_avIndex, Head_CmdPkt, (char*)&Pkt, 8 + Pkt.PktSize);
        if (ret < 0) return false;
#endif
        return true;
    }
}
//-----------------------------------------------------------------------------
bool thNet_RemoteFileStop(HANDLE NetHandle)
{
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    if (!Play->IsConnect) return false;
    PRINTF("%s(%s)(%s)\n", __FUNCTION__, Play->IPUID, Play->LocalIP);
    if (!Play->Isp2pConn)
    {
        i32 ret;
        TNetCmdPkt Pkt;
        if (Play->Session == 0) return false;
        Play->VideoChlMask = 0;
        Play->AudioChlMask = 0;
        Play->SubVideoChlMask = 0;
        memset(&Pkt, 0, sizeof(Pkt));
        Pkt.HeadPkt.VerifyCode = Head_CmdPkt;
        Pkt.HeadPkt.PktSize = sizeof(Pkt.CmdPkt);
        Pkt.CmdPkt.PktHead = Pkt.HeadPkt.VerifyCode;
        Pkt.CmdPkt.Session = Play->Session;
        Pkt.CmdPkt.MsgID = Msg_StopPlayRecFile;
        ret = SendBuf(Play->hSocket, (char*)&Pkt, sizeof(Pkt), NET_TIMEOUT);
        return ret;
    }
    else
    {
#ifdef IsUsedP2P
        TNewCmdPkt Pkt;
        i32 ret = false;
        Play->VideoChlMask = 0;
        Play->AudioChlMask = 0;
        Play->SubVideoChlMask = 0;
        memset(&Pkt, 0, sizeof(Pkt));
        Pkt.VerifyCode = Head_CmdPkt;
        Pkt.MsgID = Msg_StopPlayRecFile;
        Pkt.Result = false;
        Pkt.PktSize = 0;
        ret = avSendIOCtrl(Play->p2p_avIndex, Head_CmdPkt, (char*)&Pkt, 8 + Pkt.PktSize);
        if (ret < 0) return false;
#endif
        return true;
    }
}
//-----------------------------------------------------------------------------
bool thNet_RemoteFilePlayControl(HANDLE NetHandle, i32 PlayCtrl, i32 Speed, i32 Pos)
{
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    if (!Play->IsConnect) return false;
    PRINTF("%s(%s)(%s) PlayCtrl:%d Speed:%d Pos:%d\n", __FUNCTION__, Play->IPUID, Play->LocalIP, PlayCtrl, Speed, Pos);

    if (!Play->Isp2pConn)
    {
        i32 ret;
        TNetCmdPkt Pkt;
        if (Play->Session == 0) return false;
        memset(&Pkt, 0, sizeof(Pkt));
        Pkt.HeadPkt.VerifyCode = Head_CmdPkt;
        Pkt.HeadPkt.PktSize = sizeof(Pkt.CmdPkt);
        Pkt.CmdPkt.PktHead = Pkt.HeadPkt.VerifyCode;
        Pkt.CmdPkt.Session = Play->Session;
        Pkt.CmdPkt.MsgID = Msg_PlayControl;
        Pkt.CmdPkt.PlayCtrlPkt.PlayCtrl = (TPlayCtrl)PlayCtrl;
        Pkt.CmdPkt.PlayCtrlPkt.Speed = Speed;
        Pkt.CmdPkt.PlayCtrlPkt.Pos = Pos;
        ret = SendBuf(Play->hSocket, (char*)&Pkt, sizeof(Pkt), NET_TIMEOUT);
        return ret;
    }
    else
    {
#ifdef IsUsedP2P
        i32 ret = false;
        TNewCmdPkt Pkt;
        memset(&Pkt, 0, sizeof(Pkt));
        Pkt.VerifyCode = Head_CmdPkt;
        Pkt.MsgID = Msg_PlayControl;
        Pkt.Result = false;
        Pkt.PktSize = sizeof(Pkt.PlayCtrlPkt);
        Pkt.PlayCtrlPkt.PlayCtrl = (TPlayCtrl)PlayCtrl;
        Pkt.PlayCtrlPkt.Speed = Speed;
        Pkt.PlayCtrlPkt.Pos = Pos;
        ret = avSendIOCtrl(Play->p2p_avIndex, Head_CmdPkt, (char*)&Pkt, 8 + Pkt.PktSize);
        if (ret < 0) return false;
        return true;
#endif
    }

    return false;
}
//-----------------------------------------------------------------------------
bool thNet_HttpGetStop(HANDLE NetHandle)
{
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    PRINTF("%s(%s)(%s)\n", __FUNCTION__, Play->IPUID, Play->LocalIP);
    ThreadLock(&Play->Lock);
    Play->IsStopHttpGet = true;
    ThreadUnlock(&Play->Lock);
    //if (Play->semHttpDownload) sem_post(&Play->semHttpDownload);
    return true;
}
//-----------------------------------------------------------------------------
bool thNet_HttpGet(HANDLE NetHandle, char* url, char* Buf, i32* BufLen)
{
    bool Result = false;
    TPlayParam* Play = (TPlayParam*)NetHandle;
    *BufLen = 0;
    if (NetHandle == 0) return Result;
    if (!Play->IsConnect) return Result;

    if (!Play->Isp2pConn)
    {
        if (Play->DevCfg.DevInfoPkt.Info.SDKVersion >= 1)
        {
            THeadPkt Head;
            SOCKET hSocket;
            i32 ret, i;
            TNetCmdPkt Pkt;
            for (i=0; i<5; i++)
            {
                hSocket = SktConnect(Play->IPUID, Play->DataPort, Play->TimeOut);
                if (hSocket > 0) break;
            }
            if (hSocket == 0 || hSocket == -1) goto exits;
            memset(&Pkt, 0, sizeof(Pkt));
            Pkt.HeadPkt.VerifyCode = Head_CmdPkt;
            Pkt.HeadPkt.PktSize = sizeof(Pkt.CmdPkt);
            Pkt.CmdPkt.PktHead = Head_CmdPkt;
            Pkt.CmdPkt.MsgID = Msg_HttpGet;
            strcpy(Pkt.CmdPkt.ValueStr, url);
            ret = SendBuf(hSocket, (char*)&Pkt, sizeof(TNetCmdPkt), Play->TimeOut);
            for (i=0; i<5; i++)
            {
                ret = RecvBuf(hSocket, (char*)&Head, sizeof(THeadPkt), Play->TimeOut);
                if (Head.VerifyCode == Head_DownloadPkt) break;
            }
            if (Head.VerifyCode != Head_DownloadPkt)
            {
                if (hSocket) closesocket(hSocket);
                goto exits;
            }
            Result = RecvBuf(hSocket, (char*)Buf, Head.PktSize, NET_TIMEOUT);
            if (Result)
            {
                *BufLen = Head.PktSize;
                Buf[Head.PktSize] = 0x00;
            }

            if (hSocket) closesocket(hSocket);
        }
        else
        {
            i32 iErrorCode;
            iErrorCode = HttpGet(url, Buf, BufLen, false, Play->TimeOut);
            Result =  (iErrorCode == 200);
            if (Result) Buf[*BufLen] = 0x00;
        }
    }
    else
    {
#ifdef IsUsedP2P
        char1024 SvrName, HostName, UserNamePassword;
        char PageName[1024];
        i32 port, ret, dt;
        i32 ioType;
        TNewCmdPkt SendPkt;
        TNewCmdPkt recvPkt;
        struct timespec abstime;
        ret = sscanf(url, "http://%[^@]@%[^/]%s", UserNamePassword, SvrName, PageName);
        if (ret != 3)
        {
            ret = sscanf(url, "http://%[^/]%s", SvrName, PageName);
            if (ret != 2) strcpy(PageName, "/");
        }

        if (sscanf(SvrName, "%[^:]:%d", HostName, &port) == 2)
        {

            if ((strlen(HostName) == 0) || (port < 0) || (port >0xffff))
            {
                PRINTF("------------------------- httpget p2p aaaa,HostName is %s,port is %d\n",HostName,port);
                goto exits;
            }
        }
        else
        {
            strcpy(HostName, SvrName);
            port = 80;
        }
        PRINTF("------------------------- httpget p2p b\n");
        *BufLen = 0;
        memset(&SendPkt, 0, sizeof(TNewCmdPkt));
        SendPkt.VerifyCode = Head_CmdPkt;
        SendPkt.MsgID = Msg_HttpGet;
        SendPkt.Result = 0;
        sprintf(SendPkt.Buf, "http://localhost:%d%s", port, PageName);
        SendPkt.PktSize = strlen(SendPkt.Buf);

        Play->RecvDownloadLen = 0;
        ioType = Head_CmdPkt;
        ret = avSendIOCtrl(Play->p2p_avIndex, ioType, (char*)&SendPkt, 8 + SendPkt.PktSize);
        PRINTF("------------------------- httpget p2p 1,ret is %d\n",ret);
        if (ret < 0) goto exits;

        if (Play->p2pSoftVersion >= 1)
        {

            ThreadLock(&Play->Lock);
            Play->IsStopHttpGet = false;
            ThreadUnlock(&Play->Lock);
            PRINTF("------------------------- httpget p2p 2\n");
            dt = GetTime();
            abstime.tv_sec =  dt + 3;
            abstime.tv_nsec = 0;
            while(1)
            {
                if (Play->IsExit) goto exits;
                if (GetTime() - dt >= 10) goto exits;//10s
                if (Play->IsStopHttpGet) goto exits;
                //ret = sem_wait(&Play->semHttpDownload);
                //ret = sem_trywait(&Play->semHttpDownload);
                //ret = sem_timedwait(&Play->semHttpDownload, &abstime);
                //if (ret != 0) continue;
                if (Play->RecvDownloadLen > 0)
                {
                    memcpy(Buf, Play->RecvDownloadBuf, Play->RecvDownloadLen);
                    Buf[Play->RecvDownloadLen] = 0x00;
                    *BufLen = Play->RecvDownloadLen;
                    Play->RecvDownloadLen = 0;
                    Result = true;
                    break;
                }
            }
        }
        else
        {
            PRINTF("------------------------- httpget p2p 3\n");
            while(1)
            {
                if (Play->IsExit) goto exits;
                if (Play->IsStopHttpGet) goto exits;
                ret = avRecvIOCtrl(Play->p2p_avIndex, (u32*)&ioType, (char*)&recvPkt, sizeof(recvPkt), Play->TimeOut);
                if (ret < 0) goto exits;
                if (!(recvPkt.VerifyCode == Head_CmdPkt && recvPkt.MsgID == Msg_HttpGet)) goto exits;

                memcpy(&Buf[*BufLen], recvPkt.Buf, recvPkt.PktSize);
                *BufLen = *BufLen + recvPkt.PktSize;
                if (recvPkt.Result == 1)
                {
                    Result = true;
                    break;
                }
            }
            PRINTF("------------------------- httpget p2p 4\n");
        }
#endif
        PRINTF("------------------------- httpget p2p 5\n");
    }

    exits:
    ThreadLock(&Play->Lock);
    Play->IsStopHttpGet = true;
    ThreadUnlock(&Play->Lock);
    return Result;
}
//-----------------------------------------------------------------------------
bool thNet_SetRecPath(HANDLE NetHandle, char* RecPath)
{
    int len;
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    if (!Play->IsConnect) return false;
    PRINTF("%s(%s) RecPath:%s\n", __FUNCTION__, Play->IPUID, RecPath);
    strcpy(Play->RecPath, RecPath);
    len = strlen(Play->RecPath);
#ifdef WIN32
    if (Play->RecPath[len-1] != '\\') strcat(Play->RecPath, "\\");
#else
    if (Play->RecPath[len-1] != '/') strcat(Play->RecPath, "/");
#endif
    return true;
}
//-----------------------------------------------------------------------------
bool thNet_StartRec(HANDLE NetHandle, char* RecFileName/*FileName=NULL,配合thNet_SetRecPath使用*/)
{
    TPlayParam* Play = (TPlayParam*)NetHandle;
    TVideoFormat* vfmt;
    TAudioFormat* afmt;
    TMediaType MediaType = CODEC_NONE;
    SYSTEMTIME st;
    char1024 PathFileName;//录像

    if (NetHandle == 0) return false;
    if (!Play->IsConnect) return false;
    PRINTF("%s(%s) RecFileName:%s\n", __FUNCTION__, Play->IPUID, RecFileName);
    vfmt = &Play->DevCfg.VideoCfgPkt.VideoFormat;
    afmt = &Play->DevCfg.AudioCfgPkt.AudioFormat;

    if (vfmt->VideoType == MJPEG) MediaType = CODEC_MJPEG;
    else if (vfmt->VideoType == MPEG4) MediaType = CODEC_MPEG4;
    else if (vfmt->VideoType == H264)  MediaType = CODEC_H264;
    else if (vfmt->VideoType == H265)  MediaType = CODEC_H265;

    if (RecFileName == NULL)
    {
        if (Play->RecPath[0] == 0x00) return false;
        Time_tToSystemTime(GetTime(), &st);
#ifdef WIN32
        sprintf(PathFileName, "%s%0.4d%0.2d%0.2d_%0.2d%0.2d%0.2d.mp4", Play->RecPath, st.wYear, st.wMonth, st.wDay, st.wHour, st.wMinute, st.wSecond);
#else
        sprintf(PathFileName, "%s%.4d%.2d%.2d_%.2d%.2d%.2d.mp4", Play->RecPath, st.wYear, st.wMonth, st.wDay, st.wHour, st.wMinute, st.wSecond);
#endif
    }
    else
    {
        strcpy(PathFileName, RecFileName);
    }
    PRINTF("%s(%s) PathFileName:%s\n", __FUNCTION__, Play->IPUID, PathFileName);
    if (Play->ImgWidth > 0 && Play->ImgHeight > 0)//一开始没有thNet_Play之前，ImgWidth=0 ImgHeight=0
    {
        Play->RecHandle = thRecordStart(
                PathFileName,
                MediaType, Play->ImgWidth, Play->ImgHeight, Play->FrameRate,//主 次码流 是否要分开？
                CODEC_PCM, afmt->nChannels, afmt->nSamplesPerSec, afmt->wBitsPerSample);
        PRINTF("%s(%d) Play->RecHandle:%p\n", __FUNCTION__, __LINE__, Play->RecHandle);
        return (Play->RecHandle != NULL);
    }
    else
    {
        Play->IsTaskRec = true;
        return false;
    }
}
//-----------------------------------------------------------------------------
bool thNet_IsRec(HANDLE NetHandle)
{
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    if (!Play->IsConnect) return false;
    PRINTF("%s(%s)\n", __FUNCTION__, Play->IPUID);
    return thRecordIsRec(Play->RecHandle);
}
//-----------------------------------------------------------------------------
bool thNet_StopRec(HANDLE NetHandle)
{
    int ret;
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    if (!Play->IsConnect) return false;
    PRINTF("%s(%s)\n", __FUNCTION__, Play->IPUID);
    if (Play->RecHandle)
    {
        ret = thRecordStop(Play->RecHandle);
        if (ret) Play->RecHandle = NULL;
        return ret;
    }
    return true;
}
//-----------------------------------------------------------------------------
bool thNet_SetDecodeStyle(HANDLE NetHandle, int DecodeStyle)
{
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    if (!Play->IsConnect) return false;
    if ((u32)DecodeStyle > Decode_All) return false;
    PRINTF("%s(%s) DecodeStyle:%d\n", __FUNCTION__, Play->IPUID, DecodeStyle);
    ThreadLock(&Play->Lock);
    Play->DecodeStyle = DecodeStyle;
    ThreadUnlock(&Play->Lock);
    return true;
}
//-----------------------------------------------------------------------------
bool thNet_AudioPlayOpen(HANDLE NetHandle)
{
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    if (!Play->IsInsideDecode) return false;
    PRINTF("%s(%s)\n", __FUNCTION__, Play->IPUID);

    ThreadLock(&Play->Lock);
    Play->IsAudioMute = 0;
    Play->audioHandle = thAudioPlay_Init();
    if (Play->audioHandle)
    {
        thAudioPlay_SetFormat(
                Play->audioHandle,
                Play->DevCfg.AudioCfgPkt.AudioFormat.nChannels,
                Play->DevCfg.AudioCfgPkt.AudioFormat.wBitsPerSample,
                Play->DevCfg.AudioCfgPkt.AudioFormat.nSamplesPerSec,
                AUDIO_COLLECT_SIZE);
    }
    ThreadUnlock(&Play->Lock);
    return true;
}
//-----------------------------------------------------------------------------
bool thNet_AudioPlayClose(HANDLE NetHandle)
{
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    if (!Play->IsInsideDecode) return false;
    PRINTF("%s(%s)\n", __FUNCTION__, Play->IPUID);

    ThreadLock(&Play->Lock);
    Play->IsAudioMute = 1;
    if (Play->audioHandle)
    {
        thAudioPlay_Free(Play->audioHandle);
        Play->audioHandle = NULL;
    }
    ThreadUnlock(&Play->Lock);
    return true;
}
//-----------------------------------------------------------------------------
bool thNet_SetAudioIsMute(HANDLE NetHandle, int IsAudioMute)
{
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    if (!Play->IsInsideDecode) return false;
    PRINTF("%s(%s) IsAudioMute:%d\n", __FUNCTION__, Play->IPUID, IsAudioMute);

    ThreadLock(&Play->Lock);
    Play->IsAudioMute = IsAudioMute;
    ThreadUnlock(&Play->Lock);
    return true;
}
//-----------------------------------------------------------------------------
bool thNet_AddDspInfo(HANDLE NetHandle, TDspInfo* PDspInfo)
{
    int i;
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;

    for (i=0; i<MAX_DSPINFO_COUNT; i++)
    {
        if (Play->DspInfoLst[i].DspHandle == PDspInfo->DspHandle)
        {
            Play->DspInfoLst[i].Channel = PDspInfo->Channel;
            Play->DspInfoLst[i].DspRect = PDspInfo->DspRect;
            return true;
        }
    }

    for (i=0; i<MAX_DSPINFO_COUNT; i++)
    {
        if (Play->DspInfoLst[i].DspHandle == NULL)
        {
            Play->DspInfoLst[i] = *PDspInfo;
            return true;
        }
    }
    return false;
}
//-----------------------------------------------------------------------------
bool thNet_DelDspInfo(HANDLE NetHandle, TDspInfo* PDspInfo)
{
    int i;
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    for (i=0; i<MAX_DSPINFO_COUNT; i++)
    {
        if (Play->DspInfoLst[i].DspHandle == PDspInfo->DspHandle)
        {
            memset(&Play->DspInfoLst[i], 0, sizeof(TDspInfo));
            return true;
        }
    }
    return false;
}
//-----------------------------------------------------------------------------
bool thNet_SetJpgPath(HANDLE NetHandle, char* JpgPath)
{
    int len;
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    if (!Play->IsConnect) return false;
    PRINTF("%s(%s) JpgPath:%s\n", __FUNCTION__, Play->IPUID, JpgPath);
    strcpy(Play->JpgPath, JpgPath);
    len = strlen(Play->JpgPath);
#ifdef WIN32
    if (Play->JpgPath[len-1] != '\\') strcat(Play->JpgPath, "\\");
#else
    if (Play->JpgPath[len-1] != '/') strcat(Play->JpgPath, "/");
#endif
    return true;
}
//-----------------------------------------------------------------------------
bool thNet_SaveToJpg(HANDLE NetHandle, char* JpgFileName)
{
    SYSTEMTIME st;
    char1024 PathFileName;//录像
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    PRINTF("%s(%s) JpgFileName:%s\n", __FUNCTION__, Play->IPUID, JpgFileName);
    if (JpgFileName == NULL)
    {
        if (Play->JpgPath[0] == 0x00) return false;
        Time_tToSystemTime(GetTime(), &st);
#ifdef WIN32
        sprintf(PathFileName, "%s%0.4d%0.2d%0.2d_%0.2d%0.2d%0.2d.jpg", Play->JpgPath, st.wYear, st.wMonth, st.wDay, st.wHour, st.wMinute, st.wSecond);
#else
        sprintf(PathFileName, "%s%.4d%.2d%.2d_%.2d%.2d%.2d.jpg", Play->JpgPath, st.wYear, st.wMonth, st.wDay, st.wHour, st.wMinute, st.wSecond);
#endif
    }
    else
    {
        strcpy(PathFileName, JpgFileName);
    }

    ThreadLock(&Play->Lock);
    strcpy(Play->FileName_Jpg, PathFileName);
    Play->IsSnapShot = true;
    ThreadUnlock(&Play->Lock);
    return true;
}
//-----------------------------------------------------------------------------
char* thNet_GetAllCfg(HANDLE NetHandle)
{
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return NULL;
    if (!Play->IsConnect) return NULL;
    PRINTF("%s(%s)\n", __FUNCTION__, Play->IPUID);
    return DevCfg_to_Json(&Play->DevCfg);
}
//-----------------------------------------------------------------------------
bool thNet_ExtendDraw(HANDLE NetHandle)//android opengl render
{
    int i;
    int ret = false;
    TPlayParam* Play = (TPlayParam*)NetHandle;
    if (NetHandle == 0) return false;
    if (Play->RenderHandle == 0) return false;
    if (Play->IsQueue)
    {
        Time_QueueDraw(0, 0, (void*)NetHandle, 0, 0);
        return true;
    }
    else
    {
        struct timeval tm;
        struct timespec tnow;
        pthread_mutex_lock(&Play->Lock);
        //      pthread_cond_wait(&Play->SyncCond, &Play->Lock);
        gettimeofday(&tm, NULL);
        tnow.tv_sec = tm.tv_sec + 2;
        tnow.tv_nsec = tm.tv_usec * 1000;
        ret = pthread_cond_timedwait(&Play->SyncCond, &Play->Lock, &tnow);
        pthread_mutex_unlock(&Play->Lock);
        if (ret != 0) return false;

        for (i=0; i<MAX_DSPINFO_COUNT; i++)
        {
            TDspInfo* PDspInfo = &Play->DspInfoLst[i];
            if (PDspInfo->DspHandle == NULL) continue;
            if (PDspInfo->DspRect.right - PDspInfo->DspRect.left > 0 &&PDspInfo->DspRect.bottom - PDspInfo->DspRect.top > 0)
            {
                ret = thRender_Display(Play->RenderHandle, PDspInfo->DspHandle, PDspInfo->DspRect);
            }
        }
    }
    return ret;
}
//-----------------------------------------------------------------------------
bool P2P_Init()
{
    int ret;
    ret = IOTC_Initialize2(0);
    if (ret != IOTC_ER_NoERROR) return false;
    ret = avInitialize(32);
    Isp2pInit = true;
    PRINTF("%s(%d)\n", __FUNCTION__, __LINE__);
    return true;
}
//-----------------------------------------------------------------------------
bool P2P_Free()
{
    int ret;
    PRINTF("%s(%d)\n", __FUNCTION__, __LINE__);
    ret = avDeInitialize();
    ret = IOTC_DeInitialize();
    Isp2pInit = false;
    return true;
}
//-----------------------------------------------------------------------------
