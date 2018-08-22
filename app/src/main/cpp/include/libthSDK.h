//-----------------------------------------------------------------------------
// Author      : 朱红波
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#ifndef libthSDK_h
#define libthSDK_h

#include "cm_types.h"

#ifdef __cplusplus
extern "C"
{
#endif

#pragma pack(4)//n=1,2,4,8,16

//*****************************************************************************
//-----------------------------------------------------------------------------
EXPORT HANDLE thNet_Init(bool IsQueue, bool IsAdjustTime, bool IsAutoReConn);
/*-----------------------------------------------------------------------------
函数描述：初始化网络播放
参数说明：
IsInsideDecode:是否SDK内部解码显示
IsQueue:是否缓存队列
IsAdjustTime:是否校准设备时间为客户端时间
返 回 值：NetHandle:返回网络句柄
------------------------------------------------------------------------------*/

//-----------------------------------------------------------------------------
typedef void(TvideoCallBack)(void *UserCustom,         //用户自定义数据
                             i32 Chl, char *Buf,                //音视频解码前帧数据
                             i32 Len,                  //数据长度
                             int IsIFrame);

typedef void(TaudioCallBack)(void *UserCustom,         //用户自定义数据
                             i32 Chl, char *Buf,                //音视频解码前帧数据
                             i32 Len                   //数据长度
);

typedef void(TalarmCallBack)(i32 AlmType,             //警报类型，参见TAlmType
                             i32 AlmTime,             //警报时间time_t
                             i32 AlmChl,              //警报通道
                             void *UserCustom         //用户自定义数据
);


#define Decode_None   0
#define Decode_IFrame 1
#define Decode_All    2
EXPORT bool thNet_SetDecodeStyle(HANDLE NetHandle, int DecodeStyle);//未完
/*-----------------------------------------------------------------------------
函数描述：设置视频数据解码方式，以节省CPU占用率
参数说明：
NetHandle:网络句柄，由thNet_Init返回
DecodeStyle:解码方式,Decode_None=0 Decode_IFrame=1 Decode_All=2
Decode_None:  不解码上屏
Decode_IFrame:只解码上屏I帧
Decode_All:   解码上屏所有帧
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------*/
EXPORT bool
thNet_SetCallBack(HANDLE NetHandle, TvideoCallBack videoEvent, TaudioCallBack audioEvent, TalarmCallBack AlmEvent,
                  void *UserCustom);
/*-----------------------------------------------------------------------------
函数描述：网络播放设置回调函数， 在调用thNet_Connect之前调用
参数说明：
NetHandle:网络句柄，由thNet_Init返回
avEvent:视频音频数据回调函数
AlmEvent:设备警报回调函数
UserCustom:用户自定义数据
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------*/
EXPORT bool thNet_Free(HANDLE NetHandle);
/*-----------------------------------------------------------------------------
函数描述：释放网络播放
参数说明：
NetHandle:网络句柄，由thNet_Init返回
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------*/
EXPORT bool thNet_Connect(HANDLE NetHandle, char *UserName, char *Password, char *IPUID, i32 DataPort, u32 TimeOut);
/*-----------------------------------------------------------------------------
函数描述：连接网络设备
参数说明：
NetHandle:网络句柄，由thNet_Init返回
UserName:连接帐号
Password:连接密码
IPUID:设备IP、域名 或者 P2P UID
DataPort:设备或转发服务器端口
TimeOut:连接超时，单位ms,缺省 3000ms
返 回 值：成功返回true
失败返回false
------------------------------------------------------------------------------*/
EXPORT bool thNet_DisConn(HANDLE NetHandle);
/*-----------------------------------------------------------------------------
函数描述：断开网络设备连接
参数说明：
NetHandle:网络句柄，由thNet_Init返回
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------*/
EXPORT bool thNet_IsConnect(HANDLE NetHandle);

EXPORT bool thNet_SendSensePkt(HANDLE NetHandle);

#define THNET_CONNSTATUS_NO       0
#define THNET_CONNSTATUS_CONNING  1
#define THNET_CONNSTATUS_SUCCESS  2
#define THNET_CONNSTATUS_FAIL     3
EXPORT int thNet_GetConnectStatus(HANDLE NetHandle);
/*-----------------------------------------------------------------------------
函数描述：设备是否连接
参数说明：
NetHandle:网络句柄，由thNet_Init返回
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------*/
EXPORT bool thNet_Play(HANDLE NetHandle, u32 VideoChlMask, u32 AudioChlMask, u32 SubVideoChlMask);
/*-----------------------------------------------------------------------------
函数描述：开始播放
参数说明：
NetHandle:网络句柄，由thNet_Init返回
VideoChlMask:通道掩码，
bit: 31 .. 19 18 17 16   15 .. 03 02 01 00
0  0  0  0          0  0  0  1
AudioChlMask:通道掩码，
bit: 31 .. 19 18 17 16   15 .. 03 02 01 00
0  0  0  0          0  0  0  1
SubVideoChlMask:次码流通道掩码
bit: 31 .. 19 18 17 16   15 .. 03 02 01 00
0  0  0  0          0  0  0  1
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------*/
EXPORT bool thNet_Stop(HANDLE NetHandle);
/*-----------------------------------------------------------------------------
函数描述：停止播放
参数说明：
NetHandle:网络句柄，由thNet_Init返回
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------*/
EXPORT bool thNet_IsPlay(HANDLE NetHandle);
/*-----------------------------------------------------------------------------
函数描述：设备是否播放
参数说明：
NetHandle:网络句柄，由thNet_Init返回
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------*/
EXPORT char *thNet_GetAllCfg(HANDLE NetHandle);
/*-----------------------------------------------------------------------------
函数描述：获取所有配置
参数说明：
NetHandle:网络句柄，由thNet_Init返回
返 回 值：
------------------------------------------------------------------------------*/
EXPORT bool thNet_TalkOpen(HANDLE NetHandle);
/*-----------------------------------------------------------------------------
函数描述：开始对讲
参数说明：
NetHandle:网络句柄，由thNet_Init返回
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------*/
EXPORT bool thNet_TalkClose(HANDLE NetHandle);

EXPORT bool net_SetTalk(HANDLE NetHandle, char *Buf, int Len);//old
/*-----------------------------------------------------------------------------
函数描述：停止对讲
参数说明：
NetHandle:网络句柄，由thNet_Init返回
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------*/
typedef void(TSearchDevCallBack)(void *UserCustom,  //用户传入参数
                                 u32 SN,            //序列号
                                 i32 DevType,       //设备类型
                                 char *DevModal,    //设备型号
                                 char *SoftVersion, //软件版本
                                 i32 DataPort,      //数据端口
                                 i32 HttpPort,      //http端口
                                 i32 rtspPort,      //rtsp端口
                                 char *DevName,     //设备名称
                                 char *DevIP,       //设备IP
                                 char *DevMAC,      //设备MAC地址
                                 char *SubMask,     //设备子网掩码
                                 char *Gateway,     //设备网关
                                 char *DNS1,        //设备DNS
                                 char *DDNSServer,  //DDNS服务器地址
                                 char *DDNSHost,    //DDNS域名
                                 char *UID          //P2P方式UID
);

EXPORT HANDLE thSearch_Init(TSearchDevCallBack SearchEvent, void *UserCustom);
/*-----------------------------------------------------------------------------
函数描述：初始化查询设备
参数说明：
SearchEvent:查询设备回调函数
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------*/
EXPORT bool thSearch_SetWiFiCfg(HANDLE SearchHandle, char *SSID, char *Password);

EXPORT bool thSearch_SearchDevice(HANDLE SearchHandle);
/*-----------------------------------------------------------------------------
函数描述：开始查询设备
LocalIP:传入的本地IP，缺省为NULL
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------*/
EXPORT bool thSearch_Free(HANDLE SearchHandle);
/*-----------------------------------------------------------------------------
函数描述：释放查询设备
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------*/

//*****************************************************************************
EXPORT bool thNet_RemoteFilePlay(HANDLE NetHandle, char *FileName);
/*-----------------------------------------------------------------------------
函数描述：开始播放远程文件
参数说明：
NetHandle:网络句柄，由thNet_Init返回
FileName:传入的远程录像文件名
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------*/
EXPORT bool thNet_RemoteFileStop(HANDLE NetHandle);
/*-----------------------------------------------------------------------------
函数描述：停止播放远程文件
参数说明：
NetHandle:网络句柄，由thNet_Init返回
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------*/
EXPORT bool thNet_RemoteFilePlayControl(HANDLE NetHandle, i32 PlayCtrl, i32 Speed, i32 Pos);
/*-----------------------------------------------------------------------------
函数描述：远程文件播放控制
参数说明：
NetHandle:网络句柄，由thNet_Init返回
PlayCtrl:   PS_None               =0,                 //空
PS_Play               =1,                 //播放
PS_Pause              =2,                 //暂停
PS_Stop               =3,                 //停止
PS_FastBackward       =4,                 //快退
PS_FastForward        =5,                 //快进
PS_StepBackward       =6,                 //步退
PS_StepForward        =7,                 //步进
PS_DragPos            =8,                 //拖动
Speed:如果PlayCtrl=PS_StepBackward, PS_FastForward ，则保存快进快退倍率 1 2 4 8 16 32倍率
Pos:如果PlayCtrl=PS_DragPos，则保存文件文件位置Pos
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------*/
EXPORT int thNet_RemoteFileGetIndexType(HANDLE NetHandle);
EXPORT int thNet_RemoteFileIsClose(HANDLE NetHandle);
EXPORT int thNet_RemoteFileGetPosition(HANDLE NetHandle);//时间戳，单位ms

EXPORT bool thNet_RemoteFileSetPosition(HANDLE NetHandle, int Pos);//时间戳，单位ms

EXPORT int thNet_RemoteFileGetDuration(HANDLE NetHandle);//文件长度，单位ms

EXPORT bool thNet_AudioPlayOpen(HANDLE NetHandle);

EXPORT bool thNet_AudioPlayClose(HANDLE NetHandle);

EXPORT bool thNet_SetAudioIsMute(HANDLE NetHandle, int IsAudioMute);

/*-----------------------------------------------------------------------------
函数描述：是否静音
参数说明：
NetHandle:网络句柄，由thNet_Init返回
IsAudioMute:是否静音
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------*/
typedef struct TDspInfo
{//Player显示信息
  HWND DspHandle;//显示窗口句柄
  int Channel;//通道
  TRect DspRect;//显示区域
} TDspInfo;

EXPORT bool thNet_AddDspInfo(HANDLE NetHandle, TDspInfo *PDspInfo);
/*-------------------------------------------------------------------------------
函数描述：增加一个上屏区域，同一视频源可设多个上屏区域
参数说明：
NetHandle:网络句柄，由thNet_Init返回
PInfo:上屏显示信息指针
DspHandle:hWnd;//显示窗口句柄
Channel:Int;//通道，通道从0开始
DspRect:TRect;//显示区域
返 回 值：成功返回true，失败返回false
-------------------------------------------------------------------------------*/
EXPORT bool thNet_DelDspInfo(HANDLE NetHandle, TDspInfo *PDspInfo);
/*-------------------------------------------------------------------------------
函数描述：删除一个上屏区域
参数说明：
NetHandle:网络句柄，由thNet_Init返回
PInfo:上屏显示信息指针
DspHandle:hWnd;//显示窗口句柄
Channel:Int;//通道，通道从0开始
DspRect:TRect;//显示区域
返 回 值：成功返回true，失败返回false
-------------------------------------------------------------------------------*/
EXPORT bool thNet_HttpGet(HANDLE NetHandle, char *url, char *Buf, i32 *BufLen);

EXPORT bool thNet_HttpGetStop(HANDLE NetHandle);

EXPORT bool thNet_SetRecPath(HANDLE NetHandle, char *RecPath);//linux 以"/"结束，windows以 "\"结束
EXPORT bool thNet_StartRec(HANDLE NetHandle, char *RecFileName/*RecFileName=NULL,配合thNet_SetRecPath使用*/);

EXPORT bool thNet_IsRec(HANDLE NetHandle);

EXPORT bool thNet_StopRec(HANDLE NetHandle);

EXPORT bool thNet_SetJpgPath(HANDLE NetHandle, char *JpgPath);//linux 以"/"结束，windows以 "\"结束
EXPORT bool thNet_SaveToJpg(HANDLE NetHandle, char *JpgFileName/*JpgFileName=NULL,配合thNet_SetJpgPath使用*/);
/*-------------------------------------------------------------------------------
函数描述：保存为JPG图片
参数说明：
NetHandle:网络句柄，由thNet_Init返回
FileName:文件名，必须带路径
返 回 值：成功返回true，失败返回false
-------------------------------------------------------------------------------*/
EXPORT bool P2P_Init();

EXPORT bool P2P_Free();
//FileSize = strlen(Str)*2 * (2000(125ms) + 1200(75ms))
EXPORT bool TextToDTMFWavFile(char *txt, int len, char *WavFileName, int iVolumn/*0~100*/, int iDelayms/*=10ms*/);

EXPORT bool DTMFWavFileToText(char *FileName, char *txt);

EXPORT bool DTMFBufToText(char *Buf, int BufLen, int iSample/*=8000*/, int iBits/*=16*/, char *txt);

#if defined(ANDROID)
EXPORT bool thOpenGL_RenderRGB565(HANDLE NetHandle);

EXPORT bool thOpenGL_CreateEGL(HANDLE NetHandle, void *Window);

EXPORT bool thOpenGL_FreeEGL(HANDLE NetHandle);
EXPORT bool thOpenGL_IsRenderSuccess(HANDLE NetHandle);

#endif
/*
EXPORT bool thManage_AddDevice(u32 SN, HANDLE NetHandle);
EXPORT bool thManage_DelDevice(u32 SN);
EXPORT bool thManage_DisconnFreeAll();
EXPORT bool thManage_NetworkSwitch(int NetWorkType);//TYPE_NONE=-1 TYPE_MOBILE=0 TYPE_WIFI=1
EXPORT bool thManage_ForeBackgroundSwitch(int IsForeground);//Background=0 Foreground=1
*/
#ifdef __cplusplus
}
#endif

#endif 
