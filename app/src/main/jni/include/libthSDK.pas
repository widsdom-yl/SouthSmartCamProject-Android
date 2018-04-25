//-----------------------------------------------------------------------------
// Author      : 朱红波
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
unit libthSDK;

interface

uses
  Windows,Messages;


type
  Int=Integer;
{$DEFINE IS_MP_INIT}
//------------------------------------------------------------------------------
const
  CODEC_NONE=0;//AV_CODEC_ID_NONE
  CODEC_MJPEG=8;//AV_CODEC_ID_MJPEG
  CODEC_MPEG4=13;//AV_CODEC_ID_MPEG4
  CODEC_H264=28;//AV_CODEC_ID_H264
  CODEC_H265=174;//AV_CODEC_ID_H265 AV_CODEC_ID_HEVC
  CODEC_PCM=$10000;//AV_CODEC_ID_PCM_S16LE
  CODEC_G711=$10007;//AV_CODEC_ID_PCM_ALAW
  CODEC_AAC=$15002;//AV_CODEC_ID_AAC

const
  AV_PIX_FMT_YUV420P=0;
  AV_PIX_FMT_RGB565=43;
  AV_PIX_FMT_BGR24=3;
  AV_PIX_FMT_BGR32=30;


type
  PAVPicture=^TAVPicture;
  TAVPicture=packed record
    Data:array[0..7] of PChar;//PByte;
    linesize:array[0..7] of Int;// number of bytes per line
  end;

type
  PencParam=^TencParam;
  TencParam=record
    VideoType:Int;//MPEG4=0, MJPEG=1  H264=2
    FrameRate:Int;//帧率 1-30 MAX:PAL 25 NTSC 30
    IPIntegererval:Int;//IP帧间隔 1-120 default 30
    BitRate:Int;//码流 64K 128K 256K 512K 1024K 1536K 2048K 2560K 3072K

    yuvbuf:PChar;//必须先申请
    encWidth:Int;//宽 720 360 180 704 352 176 640 320 160
    encHeight:Int;//高 480 240 120 576 288 144
    encBuf:PChar;//必须先申请

    encBufSize:Int;//指定encBuf 大小
    encLen:Int;//解码后数据大小
    IsKeyFrame:Int;//是否I帧
    ForceKeyFrame:Int;//为1时，强制编码成I帧
  end;

//-----------------------------------------------------------------------------
const
  LibName='libthSDK.dll';
//-----------------------------------------------------------------------------
function g711_encode(DstBuf:PChar;const SrcBuf:PChar;srcBufLen:Int):Int;cdecl;external LibName;
function g711_decode(DstBuf:PChar;const SrcBuf:PChar;srcBufLen:Int):Int;cdecl;external LibName;
function pcm_DB(Buf:PChar;Len:Int):Int;cdecl;external LibName;
//-----------------------------------------------------------------------------
function thImgConvertFill(Picture:PAVPicture;Buf:PChar;fmt,Width,Height:Int):Int;cdecl;external LibName;
function thImgConvertScale(d:PChar;dfmt:Int;dw,dh:Int;s:PChar;sfmt:Int;sw,sh:Int;IsFlip:Int=0):Boolean;cdecl external LibName;
function thImgConvertScale1(Dst:PAVPicture;dfmt:Int;dw,dh:Int;Src:PAVPicture;sfmt:Int;sw,sh:Int;IsFlip:Int=0):Boolean;cdecl external LibName;
function thImgConvertScale2(d:PChar;dfmt:Int;dw,dh:Int;s:PAVPicture;sfmt:Int;sw,sh:Int;IsFlip:Int=0):Boolean;cdecl external LibName;
//-----------------------------------------------------------------------------
function thEncodeVideoInit(var Param:TencParam):THandle;cdecl external LibName;
function thEncodeVideoFrame(encHandle:THandle;Param:PencParam):Boolean;cdecl external LibName;
function thEncodeVideoFree(encHandle:THandle):Boolean;cdecl external LibName;
//-----------------------------------------------------------------------------
function thRecordStart(FileName:PChar;VideoType,Width,Height,FrameRate,AudioType,nChannels,nSamplesPerSec,wBitsPerSample:Int):THandle;cdecl external LibName;
function thRecordWriteVideo(RecHandle:THandle;Buf:PChar;Len:Int;pts:Int64):Boolean;cdecl external LibName;//pts = -1 为自动计算
function thRecordWriteAudio(RecHandle:THandle;Buf:PChar;Len:Int;pts:Int64):Boolean;cdecl external LibName;//pts = -1 为自动计算
function thRecordIsRec(RecHandle:THandle):Boolean;cdecl external LibName;
function thRecordStop(RecHandle:THandle):Boolean;cdecl external LibName;
//-----------------------------------------------------------------------------
function thDecodeVideoInit(VideoType:Int):THandle;cdecl external LibName;
function thDecodeVideoFrame(decHandle:THandle;encBuf:PChar;encLen:Int;var decWidth,decHeight:Int;avPicture:PAVPicture):Boolean;cdecl external LibName;
function thDecodeVideoFree(decHandle:THandle):Boolean;cdecl external LibName;
//-----------------------------------------------------------------------------
const
  AV_QUEUE_COUNT=60;

type
  PavNode=^TavNode;
  TavNode=record
    Buf:PChar;
    BufLen:Int;
    Buf1:PChar;
    BufLen1:Int;
  end;

function avQueue_Init(iQueueNum:Int;IsMallocMemory:Boolean=false;IsMallocMemory1:Boolean=false):THandle;cdecl external LibName;
function avQueue_GetCount(h:THandle):Int;cdecl external LibName;
function avQueue_Write(h:THandle;Buf:PChar;BufLen:Int;Buf1:PChar=nil;BufLen1:Int=0):PavNode;cdecl external LibName;
function avQueue_ReadBegin(h:THandle):PavNode;cdecl external LibName;
procedure avQueue_ReadEnd(h:THandle;tmpNode:PavNode);cdecl external LibName;
procedure avQueue_ClearAll(h:THandle);cdecl external LibName;
procedure avQueue_Free(h:THandle);cdecl external LibName;

//-----------------------------------------------------------------------------
function thDDraw_Init(iDDrawMode:Int):THandle;cdecl external LibName;
function thDDraw_Free(Handle:THandle):Boolean;cdecl external LibName;
function thDDraw_InitPrimaryScreen(Handle:THandle):Boolean;cdecl external LibName;
function thDDraw_FreePrimaryScreen(Handle:THandle):Boolean;cdecl external LibName;
function thDDraw_InitOffScreen(Handle:THandle):Boolean;cdecl external LibName;
function thDDraw_FreeOffScreen(Handle:THandle):Boolean;cdecl external LibName;
function thDDraw_SetDspHandle(Handle:THandle;DspHandle:hWnd):Boolean;cdecl external LibName;
function thDDraw_ShowText(Handle:THandle;Text:PChar;x,y,FontSize,Color:Int):Boolean;cdecl external LibName;
function thDDraw_ShowPicture(Handle:THandle;x,y:Int;Text:PChar;Color:Int):Boolean;cdecl external LibName;
function thDDraw_SaveToBmp(Handle:THandle;AFileName:PChar):Boolean;cdecl external LibName;
function thDDraw_SaveToJpg(Handle:THandle;AFileName:PChar):Boolean;cdecl external LibName;
function thDDraw_GetDC(Handle:THandle):hdc;cdecl external LibName;
function thDDraw_ReleaseDC(Handle:THandle;dc:hdc):Boolean;cdecl external LibName;
function thDDrawGetlpSurface(Handle:THandle):Pointer;cdecl external LibName;
function thDDraw_FillMem(Handle:THandle;FrameV:TAVPicture;w,h:Int):Boolean;cdecl external LibName;
function thDDraw_Display(Handle:THandle;DspHandle:hWnd;DspRect:TRect):Boolean;cdecl external LibName;
function thDDrawGetDeviceBitsPixel(Handle:THandle):Int;cdecl external LibName;
//-----------------------------------------------------------------------------

const
  Msg_ReadOver=WM_USER+$1000;
  Msg_PlayOver=WM_USER+$1001;
  Msg_Video=WM_USER+$1002;
  Msg_Audio=WM_USER+$1003;
  Msg_HasVideoData=WM_USER+$1004;
  Msg_Connect=WM_USER+$1005;
  Msg_DisConn=WM_USER+$1006;
  Msg_ReConn=WM_USER+$1007;
  Msg_Load=WM_USER+$1008;
  Msg_Unload=WM_USER+$1009;

type
  TPlayStatus=(
    PS_None=0,//空
    PS_Play=1,//播放
    PS_Pause=2,//暂停
    PS_Stop=3,//停止
    PS_Seek=4,//seek
    PS_Close=5//关闭
    );

type
  TStreamStatus=(
    st_ReadOver,
    st_PlayOver,
    st_Video,
    st_Audio
    );

  PavFormatInfo=^TavFormatInfo;
  TavFormatInfo=record ////sizeof 624 不能packet record
    VideoCodecID:Int;
    FrameRate:Int;
    BitRate:Int;
    Gop:Int;
    Width:Int;
    Height:Int;

    AudioCodecID:Int;
    nSamplesPerSec:Int;//采样率
    nChannels:Int;//单声道=0 立体声=1
    wBitsPerSample:Int;//8, 16

    VideoHandle:THandle;

    Duration:Int64;
    TimeStamp:Int64;

    videoBuf:PChar;
    videoBufLen:Int;
    videoPts:Int64;
    audioPts:Int64;
    audioBuf:PChar;
    audioBufLen:Int;

    NaluType:Int;//5 6 7 8 4
    Buf_extradataV:array[0..255] of Byte;
    Len_extradataV:Int;
    Buf_extradataA:array[0..255] of Byte;
    Len_extradataA:Int;

    Reserved:Int;//88
  end;

type
  TVideoShowModel=(
    vs_Stretched,//拉伸到显示区域
    vs_LetterBox//按比例缩放到显示区域
    );

type
  TavCallback=procedure(Handle:THandle;var avFormatInfo:TavFormatInfo;Status:TStreamStatus;UserData:Pointer);cdecl;
{$IFDEF IS_MP_INIT}
function MP_Init():THandle;cdecl external LibName;
function MP_Free(Handle:THandle):Int;cdecl external LibName;
function MP_LoadVideo(Handle:THandle;FileName:PChar;rtsp_any0_udp1_tcp2:Int;PlayHandle:THandle;ConnTimeOut:Int{ms};IsProcessVideo:Boolean;IsProcessAudio:Boolean):Boolean;cdecl external LibName;
{$ELSE}
function MP_LoadVideo(FileName:PChar;rtsp_any0_udp1_tcp2:Int;PlayHandle:THandle;ConnTimeOut:Int{ms};IsProcessVideo:Boolean;IsProcessAudio:Boolean):THandle;cdecl external LibName;
{$ENDIF}
function MP_SetCallback(Handle:THandle;avCallback:TavCallback;UserData:Pointer):Int;cdecl external LibName;
function MP_SetRect(Handle:THandle;PlayHandle:THandle;x,y,w,h:Int):Int;cdecl external LibName;
function MP_UnloadVideo(Handle:THandle;sleepmsClose:DWORD=300):Int;cdecl external LibName;
function MP_IsPauseDecode(Handle:THandle;IsPauseDecode:Boolean):Int;cdecl external LibName;
function MP_IsEof(Handle:THandle):Int;cdecl external LibName;

function MP_Play(Handle:THandle):Int;cdecl external LibName;
function MP_Pause(Handle:THandle):Int;cdecl external LibName;
function MP_Stop(Handle:THandle):Int;cdecl external LibName;

function MP_GetCurrentPosition(Handle:THandle;var Pos,total:Int64):Int;cdecl external LibName;
function MP_GetCurrentPlayStatus(Handle:THandle;var playStatus:TPlayStatus):Int;cdecl external LibName;
function MP_SeekToPosition(Handle:THandle;Pos:Int64):Int;cdecl external LibName;

function MP_SetPlaySpeed(Handle:THandle;speed:Int):Int;cdecl external LibName;

function MP_SetSyncReferenceTime(Handle:THandle;syncTime:Int64):Int;cdecl external LibName;

function MP_SetVolume(Handle:THandle;vol:Int):Int;cdecl external LibName;
function MP_GetVolume(Handle:THandle;var vol:Int):Int;cdecl external LibName;
function MP_SetMute(Handle:THandle;bMute:Boolean):Int;cdecl external LibName;

function MP_GetVideoShowModel(Handle:THandle;var VideoShowModel:Int):Int;cdecl external LibName;
function MP_SetVideoShowModel(Handle:THandle;VideoShowModel:Int):Int;cdecl external LibName;
function MP_Snapshot(Handle:THandle;FileName:PChar;TimeOut:Int):Int;cdecl external LibName;
function MP_BufLenTotal(Handle:THandle):DWORD;cdecl external LibName;

function MP_GetColor(Handle:THandle;var Brightness:Int;var Contrast:Int;var Saturation:Int):Int;cdecl external LibName;
function MP_SetColor(Handle:THandle;Brightness,Contrast,Saturation:Int):Int;cdecl external LibName;

const
  MEDIA_TYPE_NONE=0;//	未知类型
  MEDIA_TYPE_AUDIO=1;//	本地音频
  MEDIA_TYPE_VIDEO=2;//	本地视频
  MEDIA_TYPE_MP_LIVE=3;//	RTSP直播
  MEDIA_TYPE_MP_VOD=4;//	RTSP点播
  MEDIA_TYPE_RTMP_LIVE=5;//	RTMP直播
  MEDIA_TYPE_RTMP_VOD=6;//	RTMP点播
  MEDIA_TYPE_HTTP_LIVE=7;//	HTTP直播
  MEDIA_TYPE_HTTP_VOD=8;//	HTTP点播

function MP_GetMediaType(Handle:THandle):Int;cdecl external LibName;



function thrtsp_Init():THandle;cdecl external LibName;
function thrtsp_Free(Handle:THandle):Int;cdecl external LibName;
function thrtsp_Connect(Handle:THandle;FileName:PChar;ConnTimeOut:Int{ms}):Boolean;cdecl external LibName;
function thrtsp_DisConn(Handle:THandle;sleepmsClose:DWORD=300):Int;cdecl external LibName;

function thrtsp_SetCallback(Handle:THandle;avCallback:TavCallback;UserData:Pointer):Int;cdecl external LibName;
type
  PDspInfo1=^TDspInfo1;//Player显示信息
  TDspInfo1=packed record
    DspHandle:hWnd;//显示窗口句柄
    Channel:Int;//通道
    DspRect:TRect;//显示区域
  end;

function thrtsp_AddDspInfo(Handle:THandle;PInfo:PDspInfo1):Int;cdecl external LibName;
function thrtsp_DelDspInfo(Handle:THandle;PInfo:PDspInfo1):Int;cdecl external LibName;


function thrtsp_IsPauseDecode(Handle:THandle;IsPauseDecode:Boolean):Int;cdecl external LibName;
function thrtsp_IsEof(Handle:THandle):Int;cdecl external LibName;

function thrtsp_Play(Handle:THandle):Int;cdecl external LibName;
function thrtsp_BufLenTotal(Handle:THandle):DWORD;cdecl external LibName;

//-----------------------------------------------------------------------------
function ThreadCreate(funcAddr:Pointer;Param:Pointer;IsCloseHandle:Boolean=false):THandle;cdecl external LibName;
function ThreadExit(threadHandle:THandle;TimeOut:DWORD=500):Int;cdecl external LibName;
function Base64Encode(inbuf:PChar;insize:Int;outbuf:PChar;outsize:Int):Int;cdecl external LibName;
function Base64Decode(inbuf:PChar;insize:Int;outbuf:PChar;outsize:Int):Int;cdecl external LibName;
function SHA1Encode(inbuf:PChar;insize:Int;outbuf:PChar;outsize:Int):Int;cdecl external LibName;
function HttpGet(url:PChar;outbuf:PChar;var OutBufLen:Int;IsShowHead:Boolean;TimeOut:Int):Int;cdecl external LibName;
function HttpPost(url:PChar;inbuf:PChar;InBufLen:Int;outbuf:PChar;var OutBufLen:Int;IsShowHead:Boolean;TimeOut:Int):Int;cdecl external LibName;

type
  TTimerCallBack=procedure(mmHandle:THandle;uMsg:DWORD;dwUser:Pointer;dw1:DWORD;dw2:DWORD);stdcall;
function mmTimeSetEvent(uDelayms:DWORD;callback:TTimerCallBack;dwUser:Pointer):THandle;cdecl external LibName;
function mmTimeKillEvent(mmHandle:THandle):Int;cdecl external LibName;
function mmTimeGetTime():DWORD;cdecl external LibName;

//*****************************************************************************
//-----------------------------------------------------------------------------
{$Z4}//以下所有枚举全部为4个字节
function thNet_Init(IsInsideDecode:Boolean;IsQueue:Boolean;IsAdjustTime:Boolean;IsAutoReConn:Boolean):THandle;cdecl external LibName;
{-----------------------------------------------------------------------------
函数描述：初始化网络播放
参数说明：
IsInsideDecode:是否SDK内部解码显示
IsQueue:是否缓存队列
IsAdjustTime:是否校准设备时间为客户端时间
返 回 值：NetHandle:返回网络句柄
------------------------------------------------------------------------------}
type
  TAlmType=(
    Alm_None=0,//空
    Alm_MotionDetection=1,//位移报警Motion Detection
    Alm_DigitalInput=2,//DI报警
    Alm_SoundTrigger=3,////声音触发报警
    Net_Disconn=4,//网络断线
    Net_ReConn=5,//网络重连
    Alm_HddFill=6,//磁满
    Alm_VideoBlind=7,//视频遮挡
    Alm_VideoLost=8,//视频丢失
    Alm_Other3=9,//其它报警3
    Alm_Other4=10,//其它报警4
    Alm_RF=11,
    Alm_OtherMax=12
    );

type
  TvideoCallBack=procedure(
    UserCustom:Pointer;//用户自定义数据
    Chl:Int;
    Buf:PAnsiChar;//音视频解码前帧数据
    Len:Int;//数据长度
    IsIFrame:Int
    );cdecl;
  TaudioCallBack=procedure(
    UserCustom:Pointer;//用户自定义数据
    Chl:Int;
    Buf:PAnsiChar;//音视频解码前帧数据
    Len:Int//数据长度
    );cdecl;
  TalarmCallBack=procedure(
    AlmType:Int;//警报类型，参见TAlmType
    AlmTime:Int;//警报时间time_t
    AlmChl:Int;//警报通道
    UserCustom:Pointer//用户自定义数据
    );cdecl;

const
  Decode_None=0;
  Decode_IFrame=1;
  Decode_All=2;
function thNet_SetDecodeStyle(NetHandle:THandle;DecodeStyle:Int):Boolean;cdecl external LibName;//未完
{-----------------------------------------------------------------------------
函数描述：设置视频数据解码方式，以节省CPU占用率
参数说明：
NetHandle:网络句柄，由thNet_Init返回
DecodeStyle:解码方式,Decode_None=0 Decode_IFrame=1 Decode_All=2
Decode_None:不解码上屏
Decode_IFrame:只解码上屏I帧
Decode_All:解码上屏所有帧
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------}
function thNet_SetCallBack(NetHandle:THandle;videoEvent:TvideoCallBack;audioEvent:TaudioCallBack;AlmEvent:TalarmCallBack;UserCustom:Pointer):Boolean;cdecl external LibName;
{-----------------------------------------------------------------------------
函数描述：网络播放设置回调函数， 在调用thNet_Connect之前调用
参数说明：
NetHandle:网络句柄，由thNet_Init返回
avEvent:视频音频数据回调函数
AlmEvent:设备警报回调函数
UserCustom:用户自定义数据
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------}
function thNet_Free(NetHandle:THandle):Boolean;cdecl external LibName;
{-----------------------------------------------------------------------------
函数描述：释放网络播放
参数说明：
NetHandle:网络句柄，由thNet_Init返回
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------}
function thNet_Connect(NetHandle:THandle;UserName,Password,IPUID:PAnsiChar;DataPort:Int;TimeOut:DWORD):Boolean;cdecl external LibName;
{-----------------------------------------------------------------------------
函数描述：连接网络设备
参数说明：
NetHandle:网络句柄，由thNet_Init返回
UserName:连接帐号
Password:连接密码
IPUID:设备IP、域名 或者 P2P UID
DataPort:设备或转发服务器端口
TimeOut:连接超时，单位ms,缺省 3000 ms
IsQueue:是否队列解码上屏显示
返 回 值：成功返回true
失败返回false
------------------------------------------------------------------------------}
function thNet_DisConn(NetHandle:THandle):Boolean;cdecl external LibName;
{-----------------------------------------------------------------------------
函数描述：断开网络设备连接
参数说明：
NetHandle:网络句柄，由thNet_Init返回
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------}
function thNet_IsConnect(NetHandle:THandle):Boolean;cdecl external LibName;
function thNet_SendSensePkt(NetHandle:THandle):Boolean;cdecl external LibName;
const
  THNET_CONNSTATUS_NO=0;
  THNET_CONNSTATUS_CONNING=1;
  THNET_CONNSTATUS_SUCCESS=2;
  THNET_CONNSTATUS_FAIL=3;
function thNet_GetConnectStatus(NetHandle:THandle):Boolean;cdecl external LibName;
{-----------------------------------------------------------------------------
函数描述：设备是否连接
参数说明：
NetHandle:网络句柄，由thNet_Init返回
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------}
function thNet_Play(NetHandle:THandle;VideoChlMask,AudioChlMask,SubVideoChlMask:DWORD):Boolean;cdecl external LibName;
{-----------------------------------------------------------------------------
函数描述：开始播放
参数说明：
NetHandle:网络句柄，由thNet_Init返回
VideoChlMask:通道掩码，
bit:31..19 18 17 16 15..03 02 01 00
0 0 0 0 0 0 0 1
  AudioChlMask:通道掩码，
bit:31..19 18 17 16 15..03 02 01 00
0 0 0 0 0 0 0 1
  SubVideoChlMask:次码流通道掩码
bit:31..19 18 17 16 15..03 02 01 00
0 0 0 0 0 0 0 1
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------}
function thNet_Stop(NetHandle:THandle):Boolean;cdecl external LibName;
{-----------------------------------------------------------------------------
函数描述：停止播放
参数说明：
NetHandle:网络句柄，由thNet_Init返回
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------}
function thNet_IsPlay(NetHandle:THandle):Boolean;cdecl external LibName;
{-----------------------------------------------------------------------------
函数描述：设备是否播放
参数说明：
NetHandle:网络句柄，由thNet_Init返回
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------}
function thNet_GetAllCfg(NetHandle:THandle):PAnsiChar;cdecl external LibName;
{-----------------------------------------------------------------------------
函数描述：获取所有配置
参数说明：
NetHandle:网络句柄，由thNet_Init返回
返 回 值：
------------------------------------------------------------------------------}
function thNet_TalkOpen(NetHandle:THandle):Boolean;cdecl external LibName;
{-----------------------------------------------------------------------------
函数描述：开始对讲
参数说明：
NetHandle:网络句柄，由thNet_Init返回
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------}
function thNet_TalkClose(NetHandle:THandle):Boolean;cdecl external LibName;
{-----------------------------------------------------------------------------
函数描述：停止对讲
参数说明：
NetHandle:网络句柄，由thNet_Init返回
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------}
function net_SetTalk(NetHandle:THandle;Buf:PAnsiChar;Len:Int):Boolean;cdecl external LibName;//old
type
  TSearchDevCallBack=procedure(
    UserCustom:Pointer;//用户传入参数
    SN:DWORD;//序列号
    DevType:Int;//设备类型
    DevModal:PAnsiChar;//设备型号
    SoftVersion:PAnsiChar;//软件版本
    DataPort:Int;//数据端口
    HttpPort:Int;//http端口
    rtspPort:Int;//rtsp端口
    DevName:PAnsiChar;//设备名称
    DevIP:PAnsiChar;//设备IP
    DevMAC:PAnsiChar;//设备MAC地址
    SubMask:PAnsiChar;//设备子网掩码
    Gateway:PAnsiChar;//设备网关
    DNS1:PAnsiChar;//设备DNS
    DDNSServer:PAnsiChar;//DDNS服务器地址
    DDNSHost:PAnsiChar;//DDNS域名
    UID:PAnsiChar//P2P方式UID
    );cdecl;
function thSearch_Init(SearchEvent:TSearchDevCallBack;UserCustom:Pointer):THandle;cdecl external LibName;
{-----------------------------------------------------------------------------
函数描述：初始化查询设备
参数说明：
SearchEvent:查询设备回调函数
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------}
function thSearch_SetWiFiCfg(SearchHandle:THandle;SSID,Password:PAnsiChar):Boolean;cdecl external LibName;
function thSearch_SearchDevice(SearchHandle:THandle):Boolean;cdecl external LibName;
{-----------------------------------------------------------------------------
函数描述：开始查询设备
LocalIP:传入的本地IP，缺省为NULL
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------}
function thSearch_Free(SearchHandle:THandle):Boolean;cdecl external LibName;
{-----------------------------------------------------------------------------
函数描述：释放查询设备
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------}

function thNet_RemoteFilePlay(NetHandle:THandle;FileName:PAnsiChar):Boolean;cdecl external LibName;
{-----------------------------------------------------------------------------
函数描述：开始播放远程文件
参数说明：
NetHandle:网络句柄，由thNet_Init返回
FileName:传入的远程录像文件名
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------}
function thNet_RemoteFileStop(NetHandle:THandle):Boolean;cdecl external LibName;
{-----------------------------------------------------------------------------
函数描述：停止播放远程文件
参数说明：
NetHandle:网络句柄，由thNet_Init返回
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------}
function thNet_RemoteFilePlayControl(NetHandle:THandle;PlayCtrl,speed,Pos:Int):Boolean;cdecl external LibName;
{-----------------------------------------------------------------------------
函数描述：远程文件播放控制
参数说明：
NetHandle:网络句柄，由thNet_Init返回
PlayCtrl:PS_None=0,//空
PS_Play=1,//播放
PS_Pause=2,//暂停
PS_Stop=3,//停止
PS_FastBackward=4,//快退
PS_FastForward=5,//快进
PS_StepBackward=6,//步退
PS_StepForward=7,//步进
PS_DragPos=8,//拖动
speed:如果PlayCtrl=PS_StepBackward,PS_FastForward ，则保存快进快退倍率 1 2 4 8 16 32 倍率
Pos:如果PlayCtrl=PS_DragPos，则保存文件文件位置Pos
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------}
function thNet_AudioPlayOpen(NetHandle:THandle):Boolean;cdecl external LibName;
function thNet_AudioPlayClose(NetHandle:THandle):Boolean;cdecl external LibName;
function thNet_SetAudioIsMute(NetHandle:THandle;IsAudioMute:Integer):Boolean;cdecl external LibName;
{-----------------------------------------------------------------------------
函数描述：是否静音
参数说明：
NetHandle:网络句柄，由thNet_Init返回
IsAudioMute:是否静音
返 回 值：成功返回true，失败返回false
------------------------------------------------------------------------------}
type
  PDspInfo=^TDspInfo;//Player显示信息
  TDspInfo=packed record
    DspHandle:hWnd;//显示窗口句柄
    Channel:Int;//通道
    DspRect:TRect;//显示区域
  end;
function thNet_AddDspInfo(NetHandle:THandle;PDspInfo:PDspInfo):Boolean;cdecl external LibName;
{-------------------------------------------------------------------------------
函数描述：增加一个上屏区域，同一视频源可设多个上屏区域
参数说明：
NetHandle:网络句柄，由thNet_Init返回
PInfo:上屏显示信息指针
DspHandle:hWnd;//显示窗口句柄
Channel:Int;//通道，通道从0开始
DspRect:TRect;//显示区域
返 回 值：成功返回true，失败返回false
  -------------------------------------------------------------------------------}
function thNet_DelDspInfo(NetHandle:THandle;PDspInfo:PDspInfo):Boolean;cdecl external LibName;
{-------------------------------------------------------------------------------
函数描述：删除一个上屏区域
参数说明：
NetHandle:网络句柄，由thNet_Init返回
PInfo:上屏显示信息指针
DspHandle:hWnd;//显示窗口句柄
Channel:Int;//通道，通道从0开始
DspRect:TRect;//显示区域
返 回 值：成功返回true，失败返回false
  -------------------------------------------------------------------------------}

function thNet_HttpGet(NetHandle:THandle;url,Buf:PAnsiChar;var BufLen:Int):Boolean;cdecl external LibName;
function thNet_HttpGetStop(NetHandle:THandle):Boolean;cdecl external LibName;

function thNet_SetRecPath(NetHandle:THandle;RecPath:PAnsiChar):Boolean;cdecl external LibName;//linux 以"/"结束，windows以 "\"结束
function thNet_StartRec(NetHandle:THandle;RecFileName:PAnsiChar):Boolean;cdecl external LibName;//RecFileName=NULL,配合thNet_SetRecPath使用
function thNet_IsRec(NetHandle:THandle):Boolean;cdecl external LibName;
function thNet_StopRec(NetHandle:THandle):Boolean;cdecl external LibName;

function thNet_SetJpgPath(NetHandle:THandle;JpgPath:PAnsiChar):Boolean;cdecl external LibName;//linux 以"/"结束，windows以 "\"结束
function thNet_SaveToJpg(NetHandle:THandle;JpgFileName:PAnsiChar):Boolean;cdecl external LibName;//JpgFileName=NULL,配合thNet_SetJpgPath使用
{-------------------------------------------------------------------------------
函数描述：保存为JPG图片
参数说明：
NetHandle:网络句柄，由thNet_Init返回
FileName:文件名，必须带路径
返 回 值：成功返回true，失败返回false
  -------------------------------------------------------------------------------}
function P2P_Init():Boolean;cdecl external LibName;
function P2P_Free():Boolean;cdecl external LibName;
//-----------------------------------------------------------------------------
//nSample only=8000, 32000 nMode 0: Mild, 1: Medium , 2: Aggressive, IsX=1慢 IsX=0快
function LowNoiseInit(nSample,nMode,IsX:Int):THandle;cdecl external LibName;
function LowNoiseProcess(Handle:THandle;SrcBuf:PAnsiChar;srcBufLen:Int;DstBuf:PAnsiChar;var dstBufLen:Int):Boolean;cdecl external LibName;//每次10ms
function LowNoiseFree(Handle:THandle):Boolean;cdecl external LibName;


//FileSize = strlen(Str)*2 * (2000(125ms) + 1200(75ms))
function TextToDTMFWavFile(txt:PAnsiChar;Len:Int;WavFileName:PAnsiChar;iVolumn{0~100}:Int;iDelayms{=75ms}:Int):Boolean;cdecl external LibName;
function DTMFWavFileToText(FileName:PAnsiChar;txt:PAnsiChar):Boolean;cdecl external LibName;
function DTMFBufToText(Buf:PAnsiChar;BufLen:Int;iSample{=8000}:Int;iBits{=16}:Int;txt:PAnsiChar):Boolean;cdecl external LibName;


implementation

initialization


finalization

end.

