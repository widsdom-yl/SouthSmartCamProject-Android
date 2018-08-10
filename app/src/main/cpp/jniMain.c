#include <mtk_SmartConfig.h>
#include "../include/libthSDK.h"
#include "../include/TFun.h"
#include "jni.h"
#include "avDecode/thffmpeg.h"
#include "avDecode/thOpenGL_SLES.h"


//-----------------------------------------------------------------------------
typedef void (*pvUcnvFunc)(const char *lpcstrDstEcd, const char *lpcstrSrcEcd, char *dst, unsigned long dstLen,
                           const char *src, unsigned long nInLen, unsigned long *pnErrCode);

void UcnvConvert_GB2312toUTF8(char *dst, unsigned long dstLen, const char *src, unsigned long *pnErrC)
{
  pvUcnvFunc g_pvUcnvConvert = NULL;
  void *g_pvUcnvDll = NULL;
  char sFuncName[256];
  int i;

  *pnErrC = 0;
  if (NULL == g_pvUcnvDll) g_pvUcnvDll = dlopen("/system/lib/libicuuc.so", RTLD_LAZY);
  if (NULL == g_pvUcnvDll) return;

  if (NULL == g_pvUcnvConvert) g_pvUcnvConvert = (pvUcnvFunc) dlsym(g_pvUcnvDll, "ucnv_convert_4_2");
  if (NULL == g_pvUcnvConvert) g_pvUcnvConvert = (pvUcnvFunc) dlsym(g_pvUcnvDll, "ucnv_convert_3_8");
  for (i = 44; i <= 70; i++)
  {
    if (g_pvUcnvConvert) break;
    sprintf(sFuncName, "ucnv_convert_%d", i);
    g_pvUcnvConvert = (pvUcnvFunc) dlsym(g_pvUcnvDll, sFuncName);
  }
  if (NULL == g_pvUcnvConvert) return;
  g_pvUcnvConvert("utf8", "gb2312", dst, dstLen, src, strlen(src), pnErrC);
  dlclose(g_pvUcnvDll);
}

//-----------------------------------------------------------------------------
void UcnvConvert_UTF8toGB2312(char *dst, unsigned long dstLen, const char *src, unsigned long *pnErrC)
{
  pvUcnvFunc g_pvUcnvConvert = NULL;
  void *g_pvUcnvDll = NULL;
  char sFuncName[256];
  int i;

  *pnErrC = 0;
  if (NULL == g_pvUcnvDll) g_pvUcnvDll = dlopen("/system/lib/libicuuc.so", RTLD_LAZY);
  if (NULL == g_pvUcnvDll) return;

  if (NULL == g_pvUcnvConvert) g_pvUcnvConvert = (pvUcnvFunc) dlsym(g_pvUcnvDll, "ucnv_convert_4_2");
  if (NULL == g_pvUcnvConvert) g_pvUcnvConvert = (pvUcnvFunc) dlsym(g_pvUcnvDll, "ucnv_convert_3_8");
  for (i = 44; i <= 70; i++)
  {
    if (g_pvUcnvConvert) break;
    sprintf(sFuncName, "ucnv_convert_%d", i);
    g_pvUcnvConvert = (pvUcnvFunc) dlsym(g_pvUcnvDll, sFuncName);
  }
  if (NULL == g_pvUcnvConvert) return;
  g_pvUcnvConvert("gb2312", "utf8", dst, dstLen, src, strlen(src), pnErrC);
//  PRINTF("********SRC:%s\n", src);
//  PRINTF("********dst:%s\n", dst);
  dlclose(g_pvUcnvDll);
}


//-----------------------------------------------------------------------------
//*****************************************************************************
//-----------------------------------------------------------------------------
int JNICALL JNI_OnLoad(JavaVM *vm, void *Reserved)
{
  JNIEnv *env = NULL;
  if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_4) != JNI_OK) return -1;
  assert(env != NULL);
  return JNI_VERSION_1_4;
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_P2PInit(JNIEnv *env, jclass obj)
{
  return P2P_Init();
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_P2PFree(JNIEnv *env, jclass obj)
{
  return P2P_Free();
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_IsConnectWLAN(JNIEnv *env, jclass obj)
{
  return IsConnectWLAN();
}
//-----------------------------------------------------------------------------
JNIEXPORT jstring JNICALL Java_com_thSDK_lib_GetLocalIP(JNIEnv *env, jclass obj)
{
  jstring jtmpBuf = NULL;
  jtmpBuf = (*env)->NewStringUTF(env, GetLocalIP());
  return jtmpBuf;
}
//-----------------------------------------------------------------------------
JNIEXPORT u64 JNICALL
Java_com_thSDK_lib_thNetInit(JNIEnv *env, jclass obj, bool IsInsideDecode, bool IsQueue, bool IsAdjustTime,
                             bool IsAutoReConn, jstring jSN)
{
  u64 NetHandle = 0;
  u32 iSN = 0;
  int ret;
  char *sSN = (char *) (*env)->GetStringUTFChars(env, jSN, NULL);
  if (strlen(sSN) == 0) sSN = NULL;
  ret = sscanf(sSN, "%x", &iSN);
  if (ret != 1) goto exits;

  NetHandle = thNet_Init(IsInsideDecode, IsQueue, IsAdjustTime, IsAutoReConn, iSN);

  exits:
  (*env)->ReleaseStringUTFChars(env, jSN, sSN);
  return NetHandle;
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetSetDecodeStyle(JNIEnv *env, jclass obj, u64 NetHandle, int DecodeStyle)
{
  return thNet_SetDecodeStyle((HANDLE) NetHandle, DecodeStyle);
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetFree(JNIEnv *env, jclass obj, u64 NetHandle)
{
  return thNet_Free((HANDLE) NetHandle);
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL
Java_com_thSDK_lib_thNetConnect(JNIEnv *env, jclass obj, u64 NetHandle, jstring jUserName, jstring jPassword,
                                jstring jIPUID, int DataPort, int TimeOut)
{
  int ret;
  char *UserName = (char *) (*env)->GetStringUTFChars(env, jUserName, NULL);
  char *Password = (char *) (*env)->GetStringUTFChars(env, jPassword, NULL);
  char *IPUID = (char *) (*env)->GetStringUTFChars(env, jIPUID, NULL);
  ret = thNet_Connect((HANDLE) NetHandle, UserName, Password, IPUID, DataPort, TimeOut);
  (*env)->ReleaseStringUTFChars(env, jUserName, UserName);
  (*env)->ReleaseStringUTFChars(env, jPassword, Password);
  (*env)->ReleaseStringUTFChars(env, jIPUID, IPUID);
  return ret;
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetDisConn(JNIEnv *env, jclass obj, u64 NetHandle)
{
  return thNet_DisConn((HANDLE) NetHandle);
}

//-----------------------------------------------------------------------------
void thread_thNetDisConnFree(HANDLE NetHandle)
{
  int ret;
  ret = thNet_DisConn((HANDLE) NetHandle);
  if (ret)
  {
    thNet_Free(NetHandle);
  }
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetThreadDisConnFree(JNIEnv *env, jclass obj, u64 NetHandle)
{
  ThreadCreate((void *) thread_thNetDisConnFree, (void *) NetHandle, true);
  return true;
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetIsConnect(JNIEnv *env, jclass obj, u64 NetHandle)
{
  return thNet_IsConnect((HANDLE) NetHandle);
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetSendSensePkt(JNIEnv *env, jclass obj, u64 NetHandle)
{
  return thNet_SendSensePkt((HANDLE) NetHandle);
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetGetConnectStatus(JNIEnv *env, jclass obj, u64 NetHandle)
{
  return thNet_GetConnectStatus((HANDLE) NetHandle);
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL
Java_com_thSDK_lib_thNetPlay(JNIEnv *env, jclass obj, u64 NetHandle, int VideoChlMask, int AudioChlMask,
                             int SubVideoChlMask)
{
  return thNet_Play((HANDLE) NetHandle, VideoChlMask, AudioChlMask, SubVideoChlMask);
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetStop(JNIEnv *env, jclass obj, u64 NetHandle)
{
  return thNet_Stop((HANDLE) NetHandle);
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetIsPlay(JNIEnv *env, jclass obj, u64 NetHandle)
{
  return thNet_IsPlay((HANDLE) NetHandle);
}
//-----------------------------------------------------------------------------
JNIEXPORT jstring JNICALL Java_com_thSDK_lib_thNetGetAllCfg(JNIEnv *env, jclass obj, u64 NetHandle)
{
  unsigned long pnErrC;
  char conv[1024 * 64];
  char *tmpBuf = NULL;
  jstring jtmpBuf = NULL;
  conv[0] = 0x00;
  tmpBuf = thNet_GetAllCfg((HANDLE) NetHandle);

  UcnvConvert_GB2312toUTF8(conv, sizeof(conv), tmpBuf, &pnErrC);
  if (tmpBuf) jtmpBuf = (*env)->NewStringUTF(env, conv);
  return jtmpBuf;
}

JNIEXPORT jstring JNICALL Java_com_thSDK_lib_testGetFfmpeg(JNIEnv *env, jclass obj)
{
  char info[10000] = {0};
  LOGE("=======Java_com_thSDK_lib_testGetFfmpeg");
  sprintf(info, "%s\n", avcodec_configuration());
  return (*env)->NewStringUTF(env, info);

}

JNIEXPORT jint Java_com_thSDK_lib_jsmtInit(JNIEnv *env, jclass obj)
{
  return InitSmartConnection();
}
//-----------------------------------------------------------------------------
JNIEXPORT jint Java_com_thSDK_lib_jsmtStop(JNIEnv *env, jclass obj)
{
  return StopSmartConnection();
}
//-----------------------------------------------------------------------------
JNIEXPORT jint
Java_com_thSDK_lib_jsmtStart(JNIEnv *env, jclass obj, jstring nSSID, jstring nPassword, jstring nTlv, jstring nTarget,
                             int nAuthMode)
{
  const char *SSID = (*env)->GetStringUTFChars(env, nSSID, NULL);
  const char *Password = (*env)->GetStringUTFChars(env, nPassword, NULL);
  const char *Tlv = (*env)->GetStringUTFChars(env, nTlv, NULL);
  const char *Target = (*env)->GetStringUTFChars(env, nTarget, NULL);
  char AuthMode = nAuthMode;
  int ret = StartSmartConnection(SSID, Password, (unsigned char *) Tlv, strlen(Tlv), Target, AuthMode);
  (*env)->ReleaseStringUTFChars(env, nSSID, SSID);
  (*env)->ReleaseStringUTFChars(env, nPassword, Password);
  (*env)->ReleaseStringUTFChars(env, nTlv, Tlv);
  (*env)->ReleaseStringUTFChars(env, nTarget, Target);
  return ret;
}

//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetTalkOpen(JNIEnv *env, jclass obj, u64 NetHandle)
{
  return thNet_TalkOpen((HANDLE) NetHandle);
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetTalkClose(JNIEnv *env, jclass obj, u64 NetHandle)
{
  return thNet_TalkClose((HANDLE) NetHandle);
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetRemoteFilePlay(JNIEnv *env, jclass obj, u64 NetHandle, jstring jFileName)
{
  int ret;
  char *FileName = (char *) (*env)->GetStringUTFChars(env, jFileName, NULL);
  ret = thNet_RemoteFilePlay((HANDLE) NetHandle, FileName);
  (*env)->ReleaseStringUTFChars(env, jFileName, FileName);
  return ret;
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetRemoteFileStop(JNIEnv *env, jclass obj, u64 NetHandle)
{
  return thNet_RemoteFileStop((HANDLE) NetHandle);
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL
Java_com_thSDK_lib_thNetRemoteFilePlayControl(JNIEnv *env, jclass obj, u64 NetHandle, int PlayCtrl, int Speed, int Pos)
{
  return thNet_RemoteFilePlayControl((HANDLE) NetHandle, PlayCtrl, Speed, Pos);
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetAudioPlayOpen(JNIEnv *env, jclass obj, u64 NetHandle)
{
  return thNet_AudioPlayOpen((HANDLE) NetHandle);
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetAudioPlayClose(JNIEnv *env, jclass obj, u64 NetHandle)
{
  return thNet_AudioPlayClose((HANDLE) NetHandle);
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetSetAudioIsMute(JNIEnv *env, jclass obj, u64 NetHandle, int IsAudioMute)
{
  return thNet_SetAudioIsMute((HANDLE) NetHandle, IsAudioMute);
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL
Java_com_thSDK_lib_thNetOpenGLUpdateArea(JNIEnv *env, jclass obj, u64 NetHandle, int Left, int Top, int Right,
                                         int Bottom)
{
  TDspInfo DspInfo;
  DspInfo.DspHandle = 12345;
  DspInfo.Channel = 0;
  DspInfo.DspRect.left = Left;
  DspInfo.DspRect.top = Top;
  DspInfo.DspRect.right = Right;
  DspInfo.DspRect.bottom = Bottom;
  return thNet_AddDspInfo((HANDLE) NetHandle, &DspInfo);
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetOpenGLRender(JNIEnv *env, jclass obj, u64 NetHandle)
{
  return thNet_ExtendDraw((HANDLE) NetHandle);
}
//-----------------------------------------------------------------------------
JNIEXPORT jstring JNICALL Java_com_thSDK_lib_thNetHttpGet(JNIEnv *env, jclass obj, u64 NetHandle, jstring jurl)
{
  unsigned long pnErrC;
  int ret;
  int BufLen = 0;
  static char Buf[1024 * 64];
  static char conv[1024 * 64];
  jstring Result = NULL;
  char *url = (char *) (*env)->GetStringUTFChars(env, jurl, NULL);
  Buf[0] = 0x00;
  conv[0] = 0x00;

  UcnvConvert_UTF8toGB2312(conv, sizeof(conv), url, &pnErrC);

  PRINTF("%s(%d) url:%s,\n convreverse:%s\n", __FUNCTION__, __LINE__, url, conv);
  ret = thNet_HttpGet((HANDLE) NetHandle, conv, Buf, &BufLen);
  if (ret)
  {
    conv[0] = 0x00;
    PRINTF("%s(%d) Buf:%s\n", __FUNCTION__, __LINE__, Buf);
    UcnvConvert_GB2312toUTF8(conv, sizeof(conv), Buf, &pnErrC);
    Result = (*env)->NewStringUTF(env, conv);
  }
  (*env)->ReleaseStringUTFChars(env, jurl, url);
  return Result;
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetHttpGetStop(JNIEnv *env, jclass obj, u64 NetHandle)
{

  return thNet_HttpGetStop((HANDLE) NetHandle);
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetSetRecPath(JNIEnv *env, jclass obj, u64 NetHandle, jstring jRecPath)
{
  int ret;
  char *RecPath = (char *) (*env)->GetStringUTFChars(env, jRecPath, NULL);
  ret = thNet_SetRecPath((HANDLE) NetHandle, RecPath);
  (*env)->ReleaseStringUTFChars(env, jRecPath, RecPath);
  return ret;
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetStartRec(JNIEnv *env, jclass obj, u64 NetHandle, jstring jRecFileName)
{
  int ret;
  char *RecFileName = (char *) (*env)->GetStringUTFChars(env, jRecFileName, NULL);
  if (strlen(RecFileName) == 0) RecFileName = NULL;
  ret = thNet_StartRec((HANDLE) NetHandle, RecFileName);
  (*env)->ReleaseStringUTFChars(env, jRecFileName, RecFileName);
  return ret;
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetIsRec(JNIEnv *env, jclass obj, u64 NetHandle)
{
  return thNet_IsRec((HANDLE) NetHandle);
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetStopRec(JNIEnv *env, jclass obj, u64 NetHandle)
{
  return thNet_StopRec((HANDLE) NetHandle);
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetSetJpgPath(JNIEnv *env, jclass obj, u64 NetHandle, jstring jJpgPath)
{
  int ret;
  char *JpgPath = (char *) (*env)->GetStringUTFChars(env, jJpgPath, NULL);
  ret = thNet_SetJpgPath((HANDLE) NetHandle, JpgPath);
  (*env)->ReleaseStringUTFChars(env, jJpgPath, JpgPath);
  return ret;
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thNetSaveToJpg(JNIEnv *env, jclass obj, u64 NetHandle, jstring jJpgFileName)
{
  int ret;
  char *JpgFileName = (char *) (*env)->GetStringUTFChars(env, jJpgFileName, NULL);
  if (strlen(JpgFileName) == 0) JpgFileName = NULL;
  ret = thNet_SaveToJpg((HANDLE) NetHandle, JpgFileName);
  (*env)->ReleaseStringUTFChars(env, jJpgFileName, JpgFileName);
  return ret;
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_OpenGLRender(JNIEnv *env, jclass obj, u64 NetHandle)
{
}

//-----------------------------------------------------------------------------
typedef struct TSearchInfo
{
#define Search_SNLst_COUNT   256

  u32 Search_SNLst[Search_SNLst_COUNT];
  char tmpBuf[1000 * 10];
  int SearchCount;
  char20 LocalIP;
  int IsJson;
} TSearchInfo;

static TSearchInfo Search;

//-------------------------------------
void
callback_SearchDev(void *UserCustom, u32 SN, int DevType, char *DevModal, char *SoftVersion, int DataPort, int HttpPort,
                   int rtspPort, char *DevName, char *DevIP, char *DevMAC, char *SubMask, char *Gateway, char *DNS1,
                   char *DDNSServer, char *DDNSHost, char *UID)
{
  unsigned long pnErrC;
  char Str[1000];
  char uDevName[1024];
  int i;
  bool IsFind = false;
  for (i = 0; i < Search_SNLst_COUNT; i++)
  {
    if (Search.Search_SNLst[i] == 0x00) break;
    if (Search.Search_SNLst[i] == SN)
    {
      IsFind = true;
      break;
    }
  }
  if (IsFind) return;
  Search.Search_SNLst[i] = SN;

  if (!IsSameSegmentIP(Search.LocalIP, DevIP)) return;

  memset(uDevName, 0, sizeof(uDevName));
  UcnvConvert_GB2312toUTF8(uDevName, sizeof(uDevName), DevName, &pnErrC);
  PRINTF("SearchDevCallBack DevIP:%s(%d) %s|%s\n", DevIP, DataPort, DevName, uDevName);

  if (DDNSServer[0] == 0x00) DDNSServer = "NULL";
  if (DDNSHost[0] == 0x00) DDNSHost = "NULL";
  if (UID[0] == 0x00) UID = "NULL";

  if (strlen(Search.tmpBuf) >= 10000 - 100) return;

  if (Search.IsJson)
  {
    sprintf(Str, "{");
    strcat(Search.tmpBuf, Str);
    sprintf(Str, "\"SN\":\"%.8x\",", SN);
    strcat(Search.tmpBuf, Str);
    sprintf(Str, "\"DevModal\":\"%s\",", DevModal);
    strcat(Search.tmpBuf, Str);
    sprintf(Str, "\"DevName\":\"%s\",", uDevName);
    strcat(Search.tmpBuf, Str);
    sprintf(Str, "\"DevMAC\":\"%s\",", DevMAC);
    strcat(Search.tmpBuf, Str);
    sprintf(Str, "\"DevIP\":\"%s\",", DevIP);
    strcat(Search.tmpBuf, Str);
    sprintf(Str, "\"SubMask\":\"%s\",", SubMask);
    strcat(Search.tmpBuf, Str);
    sprintf(Str, "\"Gateway\":\"%s\",", Gateway);
    strcat(Search.tmpBuf, Str);
    sprintf(Str, "\"DNS1\":\"%s\",", DNS1);
    strcat(Search.tmpBuf, Str);
    sprintf(Str, "\"SoftVersion\":\"%s\",", SoftVersion);
    strcat(Search.tmpBuf, Str);
    sprintf(Str, "\"DataPort\":%d,", DataPort);
    strcat(Search.tmpBuf, Str);
    sprintf(Str, "\"HttpPort\":%d,", HttpPort);
    strcat(Search.tmpBuf, Str);
    sprintf(Str, "\"rtspPort\":%d,", rtspPort);
    strcat(Search.tmpBuf, Str);
    sprintf(Str, "\"DDNSServer\":\"%s\",", DDNSServer);
    strcat(Search.tmpBuf, Str);
    sprintf(Str, "\"DDNSHost\":\"%s\",", DDNSHost);
    strcat(Search.tmpBuf, Str);
    sprintf(Str, "\"UID\":\"%s\"", UID);
    strcat(Search.tmpBuf, Str);
    sprintf(Str, "},");
    strcat(Search.tmpBuf, Str);
  } else
  {
    if (Search.SearchCount != 0) strcat(Search.tmpBuf, "@");
    sprintf(Str, "%s,%.8x,%d,%d,%s,%s,%s,%s,%s", DevIP, SN, DataPort, HttpPort, DevMAC, uDevName, DDNSServer, DDNSHost,
            UID);
    strcat(Search.tmpBuf, Str);
  }
  Search.SearchCount++;
}
//-----------------------------------------------------------------------------
JNIEXPORT jstring JNICALL Java_com_thSDK_lib_thNetSearchDevice(JNIEnv *env, jclass obj, int TimeOut, int IsJson)
{
  int len;
  HANDLE SearchHandle;
  struct timeval tv1, tv2;
  jstring jtmpBuf = NULL;
  Search.SearchCount = 0;
  memset(Search.tmpBuf, 0, sizeof(Search.tmpBuf));
  memset(Search.Search_SNLst, 0, sizeof(u32) * Search_SNLst_COUNT);
  Search.IsJson = IsJson;

  sprintf(Search.LocalIP, "%s", GetLocalIP());

  SearchHandle = thSearch_Init(callback_SearchDev, NULL);
  if (!SearchHandle) return jtmpBuf;
  if (Search.IsJson) strcat(Search.tmpBuf, "[");
  thSearch_SearchDevice(SearchHandle);
  gettimeofday(&tv1, NULL);
  while (1)
  {
    usleep(1000 * 100);
    thSearch_SearchDevice(SearchHandle);
    gettimeofday(&tv2, NULL);
    if (timeval_dec(&tv2, &tv1) >= TimeOut) break;
  }
  thSearch_Free(SearchHandle);
  SearchHandle = NULL;
  if (Search.IsJson)
  {
    len = strlen(Search.tmpBuf);
    if (Search.tmpBuf[len - 1] == ',') Search.tmpBuf[len - 1] = 0x00;
    strcat(Search.tmpBuf, "]");
  }
  PRINTF("%s(%d) Count:%d\n", __FUNCTION__, __LINE__, Search.SearchCount);

  jtmpBuf = (*env)->NewStringUTF(env, Search.tmpBuf);
  return jtmpBuf;
}

//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thManageAddDevice(JNIEnv *env, jclass obj, jstring jSN, u64 NetHandle)
{
  int i, ret;
  u32 iSN = 0;

  char *sSN = (char *) (*env)->GetStringUTFChars(env, jSN, NULL);
  if (strlen(sSN) == 0) sSN = NULL;
  ret = sscanf(sSN, "%x", &iSN);
  if (ret != 1)
  {
    ret = false;
    goto exits;
  }

  ret = thManage_AddDevice(iSN, NetHandle);

  exits:
  (*env)->ReleaseStringUTFChars(env, jSN, sSN);
  return ret;
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thManageDelDevice(JNIEnv *env, jclass obj, jstring jSN)
{
  int i, ret;
  u32 iSN = 0;
  char *sSN = (char *) (*env)->GetStringUTFChars(env, jSN, NULL);
  if (strlen(sSN) == 0) sSN = NULL;
  ret = sscanf(sSN, "%x", &iSN);
  if (ret != 1)
  {
    ret = false;
    goto exits;
  }

  ret = thManage_DelDevice(iSN);

  exits:
  (*env)->ReleaseStringUTFChars(env, jSN, sSN);
  return ret;
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thManageDisconnFreeAll(JNIEnv *env, jclass obj)
{
  return thManage_DisconnFreeAll();
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL Java_com_thSDK_lib_thManageNetworkSwitch(JNIEnv *env, jclass obj,
                                                                int NetWorkType)//TYPE_NONE=-1 TYPE_MOBILE=0 TYPE_WIFI=1
{
  return thManage_NetworkSwitch(NetWorkType);
}
//-----------------------------------------------------------------------------
JNIEXPORT bool JNICALL
Java_com_thSDK_lib_thManageForeBackgroundSwitch(JNIEnv *env, jclass obj, int IsForeground)//Background=0 Foreground=1
{
  return thManage_ForeBackgroundSwitch(IsForeground);
}
//-----------------------------------------------------------------------------
