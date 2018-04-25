//-----------------------------------------------------------------------------
// Author      : 朱红波
// Date        : 2012.01.18
// Version     : V 1.00
// Description : 
//-----------------------------------------------------------------------------
#include "axdll.h"

bool DevCfg_to_NewDevCfg(TDevCfg* DevCfg, TNewDevCfg* NewDevCfg)
{
  i32 i;
  //memset(NewDevCfg, 0, sizeof(TNewDevCfg));
  //DevInfo
  strcpy(NewDevCfg->DevInfo.DevModal, DevCfg->DevInfoPkt.DevModal);
  NewDevCfg->DevInfo.DevModal[7] = 0x00;
  NewDevCfg->DevInfo.SN = DevCfg->DevInfoPkt.SN;
  strcpy(NewDevCfg->DevInfo.SoftVersion, DevCfg->DevInfoPkt.SoftVersion);
  NewDevCfg->DevInfo.SoftVersion[15] = 0x00;
  memcpy(NewDevCfg->DevInfo.DevName, DevCfg->DevInfoPkt.DevName, 19);
  NewDevCfg->DevInfo.DevName[19] = 0x00;
  NewDevCfg->DevInfo.StandardMask = DevCfg->DevInfoPkt.Info.StandardMask;
  NewDevCfg->DevInfo.SubStandardMask = DevCfg->DevInfoPkt.Info.SubStandardMask;
  NewDevCfg->DevInfo.DevType = DevCfg->DevInfoPkt.DevType;
  NewDevCfg->DevInfo.ExistWiFi = DevCfg->DevInfoPkt.Info.ExistWiFi;
  NewDevCfg->DevInfo.ExistSD = DevCfg->DevInfoPkt.Info.ExistSD;
  NewDevCfg->DevInfo.ethLinkStatus = DevCfg->DevInfoPkt.Info.ethLinkStatus;
  NewDevCfg->DevInfo.wifiStatus = DevCfg->DevInfoPkt.Info.wifiStatus;
  NewDevCfg->DevInfo.upnpStatus = DevCfg->DevInfoPkt.Info.upnpStatus;
  NewDevCfg->DevInfo.WlanStatus = DevCfg->DevInfoPkt.Info.WlanStatus;
  NewDevCfg->DevInfo.p2pStatus = DevCfg->DevInfoPkt.Info.p2pStatus;
  NewDevCfg->DevInfo.HardType = DevCfg->DevInfoPkt.Info.HardType;
  NewDevCfg->DevInfo.TimeZone = DevCfg->DevInfoPkt.TimeZone;
  NewDevCfg->DevInfo.DoubleStream = DevCfg->DevInfoPkt.DoubleStream;
  NewDevCfg->DevInfo.ExistFlash = DevCfg->DevInfoPkt.Info.ExistFlash;
  NewDevCfg->DevInfo.SDKVersion = DevCfg->DevInfoPkt.Info.SDKVersion;//20171108
  //NetCfg
  NewDevCfg->NetCfg.DataPort = DevCfg->NetCfgPkt.DataPort;
  NewDevCfg->NetCfg.rtspPort = DevCfg->NetCfgPkt.rtspPort;
  NewDevCfg->NetCfg.HttpPort = DevCfg->NetCfgPkt.HttpPort;
  NewDevCfg->NetCfg.IPType = DevCfg->NetCfgPkt.Lan.IPType;
  NewDevCfg->NetCfg.DevIP = IPToInt(DevCfg->NetCfgPkt.Lan.DevIP);
  NewDevCfg->NetCfg.SubMask = IPToInt(DevCfg->NetCfgPkt.Lan.SubMask);
  NewDevCfg->NetCfg.Gateway = IPToInt(DevCfg->NetCfgPkt.Lan.Gateway);
  NewDevCfg->NetCfg.DNS1 = IPToInt(DevCfg->NetCfgPkt.Lan.DNS1);
  strcpy(NewDevCfg->NetCfg.DevMAC, DevCfg->NetCfgPkt.Lan.DevMAC);
  NewDevCfg->NetCfg.ActiveuPnP = DevCfg->NetCfgPkt.uPnP.Active;
  NewDevCfg->NetCfg.ActiveDDNS = DevCfg->NetCfgPkt.DDNS.Active;
  NewDevCfg->NetCfg.DDNSType = DevCfg->NetCfgPkt.DDNS.DDNSType;
  strcpy(NewDevCfg->NetCfg.DDNSDomain, DevCfg->NetCfgPkt.DDNS.DDNSDomain);
  strcpy(NewDevCfg->NetCfg.DDNSServer, DevCfg->NetCfgPkt.DDNS.DDNSServer);
  //wifiCfg
  NewDevCfg->wifiCfg.ActiveWIFI = DevCfg->WiFiCfgPkt.Active;
  NewDevCfg->wifiCfg.IsAPMode = DevCfg->WiFiCfgPkt.IsAPMode;
  strcpy(NewDevCfg->wifiCfg.SSID_AP, DevCfg->WiFiCfgPkt.SSID_AP);
  strcpy(NewDevCfg->wifiCfg.Password_AP, DevCfg->WiFiCfgPkt.Password_AP);
  strcpy(NewDevCfg->wifiCfg.SSID_STA, DevCfg->WiFiCfgPkt.SSID_STA);
  strcpy(NewDevCfg->wifiCfg.Password_STA, DevCfg->WiFiCfgPkt.Password_STA);
  NewDevCfg->wifiCfg.Channel = DevCfg->WiFiCfgPkt.Channel;
  NewDevCfg->wifiCfg.EncryptType = DevCfg->WiFiCfgPkt.EncryptType;
  NewDevCfg->wifiCfg.Auth = DevCfg->WiFiCfgPkt.WPA.Auth;
  NewDevCfg->wifiCfg.Enc = DevCfg->WiFiCfgPkt.WPA.Enc;
  //p2pCfg
  NewDevCfg->p2pCfg.ActiveP2P = DevCfg->p2pCfgPkt.Active;
  NewDevCfg->p2pCfg.Version = DevCfg->p2pCfgPkt.Version;
  NewDevCfg->p2pCfg.p2pType = DevCfg->p2pCfgPkt.p2pType;
  strcpy(NewDevCfg->p2pCfg.UID, DevCfg->p2pCfgPkt.UID);
  NewDevCfg->p2pCfg.UID[20] = 0x00;
  //VideoCfg
  NewDevCfg->VideoCfg.StandardEx0 = GetStandardFromWidthHeight(DevCfg->VideoCfgPkt.VideoFormat.Width, DevCfg->VideoCfgPkt.VideoFormat.Height);
  NewDevCfg->VideoCfg.FrameRate0 = DevCfg->VideoCfgPkt.VideoFormat.FrameRate;
  NewDevCfg->VideoCfg.BitRate0 = DevCfg->VideoCfgPkt.VideoFormat.BitRate / 1024;
  NewDevCfg->VideoCfg.StandardEx1 = DevCfg->VideoCfgPkt.VideoFormat.Sub.StandardEx;
  NewDevCfg->VideoCfg.FrameRate1 = DevCfg->VideoCfgPkt.VideoFormat.Sub.FrameRate;
  NewDevCfg->VideoCfg.BitRate1 = DevCfg->VideoCfgPkt.VideoFormat.Sub.BitRate / 1024;
  NewDevCfg->VideoCfg.IsMirror = DevCfg->VideoCfgPkt.VideoFormat.IsMirror;
  NewDevCfg->VideoCfg.IsFlip = DevCfg->VideoCfgPkt.VideoFormat.IsFlip;
  NewDevCfg->VideoCfg.IsShowFrameRate = DevCfg->VideoCfgPkt.VideoFormat.IsShowFrameRate;
  NewDevCfg->VideoCfg.VideoType = DevCfg->VideoCfgPkt.VideoFormat.VideoType;
  NewDevCfg->VideoCfg.BitRateType0 = DevCfg->VideoCfgPkt.VideoFormat.BitRateType;
  NewDevCfg->VideoCfg.BitRateQuant0 = DevCfg->VideoCfgPkt.VideoFormat.BitRateQuant;
  NewDevCfg->VideoCfg.BitRateType1 = DevCfg->VideoCfgPkt.VideoFormat.Sub.BitRateType;
  NewDevCfg->VideoCfg.BitRateQuant1 = DevCfg->VideoCfgPkt.VideoFormat.Sub.BitRateQuant;
  //AudioCfg
  NewDevCfg->AudioCfg.ActiveAUDIO = DevCfg->AudioCfgPkt.Active;
  NewDevCfg->AudioCfg.InputTypeAUDIO = DevCfg->AudioCfgPkt.InputType;
  NewDevCfg->AudioCfg.VolumeLineIn = DevCfg->AudioCfgPkt.VolumeLineIn;
  NewDevCfg->AudioCfg.VolumeLineOut = DevCfg->AudioCfgPkt.VolumeLineOut;
  NewDevCfg->AudioCfg.nChannels = DevCfg->AudioCfgPkt.AudioFormat.nChannels;
  NewDevCfg->AudioCfg.wBitsPerSample = DevCfg->AudioCfgPkt.AudioFormat.wBitsPerSample;
  NewDevCfg->AudioCfg.nSamplesPerSec = DevCfg->AudioCfgPkt.AudioFormat.nSamplesPerSec;
  NewDevCfg->AudioCfg.wFormatTag = DevCfg->AudioCfgPkt.AudioFormat.wFormatTag;

  for (i=0; i<3; i++)//DevCfg->UserCfgPkt.Count
  {
    strcpy(NewDevCfg->UserCfg.UserName[i], DevCfg->UserCfgPkt.Lst[i].UserName);
    strcpy(NewDevCfg->UserCfg.Password[i], DevCfg->UserCfgPkt.Lst[i].Password);
    NewDevCfg->UserCfg.Authority[i] = DevCfg->UserCfgPkt.Lst[i].Authority;
  }
  //TDIAlm
  NewDevCfg->DIAlm.ActiveDI = DevCfg->AlmCfgPkt.DIAlm.Active;
  NewDevCfg->DIAlm.IsAlmRec = DevCfg->AlmCfgPkt.DIAlm.IsAlmRec;
  NewDevCfg->DIAlm.IsFTPUpload = DevCfg->AlmCfgPkt.DIAlm.IsFTPUpload;
  NewDevCfg->DIAlm.ActiveDO = DevCfg->AlmCfgPkt.DIAlm.ActiveDO;
  NewDevCfg->DIAlm.IsSendEmail = DevCfg->AlmCfgPkt.DIAlm.IsSendEmail;
  NewDevCfg->DIAlm.AlmOutTimeLen = DevCfg->AlmCfgPkt.AlmOutTimeLen;
  //NewDevCfg->DIAlm.hm = DevCfg->;
  //TMDAlm
  NewDevCfg->MDAlm.ActiveMD = DevCfg->MDCfgPkt.Active;
  NewDevCfg->MDAlm.Sensitive = DevCfg->MDCfgPkt.Sensitive;
  NewDevCfg->MDAlm.IsAlmRec = DevCfg->AlmCfgPkt.MDAlm.IsAlmRec;
  NewDevCfg->MDAlm.IsFTPUpload = DevCfg->AlmCfgPkt.MDAlm.IsFTPUpload;
  NewDevCfg->MDAlm.ActiveDO = DevCfg->AlmCfgPkt.MDAlm.ActiveDO;
  NewDevCfg->MDAlm.IsSendEmail = DevCfg->AlmCfgPkt.MDAlm.IsSendEmail;
  NewDevCfg->MDAlm.AlmOutTimeLen = DevCfg->AlmCfgPkt.AlmOutTimeLen;
  //NewDevCfg->MDAlm.hm = DevCfg->MDCfgPkt.hm;
  memcpy(&NewDevCfg->MDAlm.Rect, &DevCfg->MDCfgPkt.NewRect, sizeof(NewDevCfg->MDAlm.Rect));
  //SoundAlm
  NewDevCfg->SoundAlm.ActiveSoundTrigger = DevCfg->AudioCfgPkt.SoundTriggerActive;
  NewDevCfg->SoundAlm.Sensitive = DevCfg->AudioCfgPkt.SoundTriggerSensitive;
  NewDevCfg->SoundAlm.IsAlmRec = DevCfg->AlmCfgPkt.SoundAlm.IsAlmRec;
  NewDevCfg->SoundAlm.IsFTPUpload = DevCfg->AlmCfgPkt.SoundAlm.IsFTPUpload;
  NewDevCfg->SoundAlm.ActiveDO = DevCfg->AlmCfgPkt.SoundAlm.ActiveDO;
  NewDevCfg->SoundAlm.IsSendEmail = DevCfg->AlmCfgPkt.SoundAlm.IsSendEmail;
  NewDevCfg->SoundAlm.AlmOutTimeLen = DevCfg->AlmCfgPkt.AlmOutTimeLen;
  //RecCfg
  NewDevCfg->RecCfg.RecStreamType = DevCfg->RecCfgPkt.RecStreamType;
  NewDevCfg->RecCfg.IsRecAudio = DevCfg->RecCfgPkt.IsRecAudio;
  NewDevCfg->RecCfg.RecStyle = DevCfg->RecCfgPkt.RecStyle;
  NewDevCfg->RecCfg.Plan = DevCfg->RecCfgPkt.Plan;
  NewDevCfg->RecCfg.Rec_AlmTimeLen = DevCfg->RecCfgPkt.Rec_AlmTimeLen;
  NewDevCfg->RecCfg.Rec_NmlTimeLen = DevCfg->RecCfgPkt.Rec_NmlTimeLen;
//ExtractCfg
  NewDevCfg->ExtractCfg.VerifyCode = Head_CmdPkt;//0xAAAAAAAA
  strcpy(NewDevCfg->ExtractCfg.DevName, DevCfg->DevInfoPkt.DevName);
  NewDevCfg->ExtractCfg.DiskSize = DevCfg->DiskCfgPkt.DiskSize;
  NewDevCfg->ExtractCfg.FreeSize = DevCfg->DiskCfgPkt.FreeSize;
  NewDevCfg->ExtractCfg.PlatformType = DevCfg->DevInfoPkt.Info.PlatformType;
  NewDevCfg->ExtractCfg.IsAudioGain = DevCfg->AudioCfgPkt.IsAudioGain;
  NewDevCfg->ExtractCfg.AudioGainLevel = DevCfg->AudioCfgPkt.AudioGainLevel;//未用到
  NewDevCfg->ExtractCfg.IRCutSensitive = DevCfg->VideoCfgPkt.VideoFormat.IRCutSensitive;
  NewDevCfg->ExtractCfg.HideAreaActive = DevCfg->HideAreaCfgPkt.Active;
  NewDevCfg->ExtractCfg.HideAreaLeft = (unsigned char)(DevCfg->HideAreaCfgPkt.NewRect.left * 100 + 0.5);
  NewDevCfg->ExtractCfg.HideAreaTop = (unsigned char)(DevCfg->HideAreaCfgPkt.NewRect.top * 100 + 0.5);
  NewDevCfg->ExtractCfg.HideAreaRight = (unsigned char)(DevCfg->HideAreaCfgPkt.NewRect.right * 100 + 0.5);
  NewDevCfg->ExtractCfg.HideAreaBottom = (unsigned char)(DevCfg->HideAreaCfgPkt.NewRect.bottom * 100 + 0.5);
  NewDevCfg->ExtractCfg.VideoTypeMask = DevCfg->DevInfoPkt.Info.VideoTypeMask;
  NewDevCfg->ExtractCfg.Standard = DevCfg->VideoCfgPkt.VideoFormat.Standard;
  NewDevCfg->ExtractCfg.IPInterval = DevCfg->VideoCfgPkt.VideoFormat.IPInterval;
  NewDevCfg->ExtractCfg.Brightness = DevCfg->VideoCfgPkt.VideoFormat.Brightness;
  NewDevCfg->ExtractCfg.Contrast = DevCfg->VideoCfgPkt.VideoFormat.Contrast;
  NewDevCfg->ExtractCfg.Hue = DevCfg->VideoCfgPkt.VideoFormat.Hue;
  NewDevCfg->ExtractCfg.Saturation = DevCfg->VideoCfgPkt.VideoFormat.Saturation;
  NewDevCfg->ExtractCfg.Sharpness = DevCfg->VideoCfgPkt.VideoFormat.Sharpness;
  NewDevCfg->ExtractCfg.BrightnessNight = DevCfg->VideoCfgPkt.VideoFormat.BrightnessNight;
  NewDevCfg->ExtractCfg.ContrastNight = DevCfg->VideoCfgPkt.VideoFormat.ContrastNight;
  NewDevCfg->ExtractCfg.HueNight = DevCfg->VideoCfgPkt.VideoFormat.HueNight;
  NewDevCfg->ExtractCfg.SaturationNight = DevCfg->VideoCfgPkt.VideoFormat.SaturationNight;
  NewDevCfg->ExtractCfg.SharpnessNight = DevCfg->VideoCfgPkt.VideoFormat.SharpnessNight;
  NewDevCfg->ExtractCfg.IsWDR = DevCfg->VideoCfgPkt.VideoFormat.IsWDR;
  NewDevCfg->ExtractCfg.WDRLevel = DevCfg->VideoCfgPkt.VideoFormat.WDRLevel;
  NewDevCfg->ExtractCfg.IsShowTime = DevCfg->VideoCfgPkt.VideoFormat.IsShowTime;
  NewDevCfg->ExtractCfg.IsShowTitle = DevCfg->VideoCfgPkt.VideoFormat.IsShowTitle;
  NewDevCfg->ExtractCfg.IsShowFrameRate = DevCfg->VideoCfgPkt.VideoFormat.IsShowFrameRate;
  NewDevCfg->ExtractCfg.IsShowLogo = DevCfg->VideoCfgPkt.VideoFormat.IsShowLogo;

  NewDevCfg->ExtractCfg.TimeX      = (u8)(DevCfg->VideoCfgPkt.VideoFormat.TimeX * 100 + 0.5);
  NewDevCfg->ExtractCfg.TimeY      = (u8)(DevCfg->VideoCfgPkt.VideoFormat.TimeY * 100 + 0.5);
  NewDevCfg->ExtractCfg.TitleX     = (u8)(DevCfg->VideoCfgPkt.VideoFormat.TitleX * 100 + 0.5);
  NewDevCfg->ExtractCfg.TitleY     = (u8)(DevCfg->VideoCfgPkt.VideoFormat.TitleY * 100 + 0.5);
  NewDevCfg->ExtractCfg.FrameRateX = (u8)(DevCfg->VideoCfgPkt.VideoFormat.FrameRateX * 100 + 0.5);
  NewDevCfg->ExtractCfg.FrameRateY = (u8)(DevCfg->VideoCfgPkt.VideoFormat.FrameRateY * 100 + 0.5);
  NewDevCfg->ExtractCfg.LogoX      = (u8)(DevCfg->VideoCfgPkt.VideoFormat.LogoX * 100 + 0.5);
  NewDevCfg->ExtractCfg.LogoY      = (u8)(DevCfg->VideoCfgPkt.VideoFormat.LogoY * 100 + 0.5);
 
  NewDevCfg->ExtractCfg.IsOsdTransparent = DevCfg->VideoCfgPkt.VideoFormat.IsOsdTransparent;
  NewDevCfg->ExtractCfg.IsOSDBigFont = DevCfg->VideoCfgPkt.VideoFormat.IsOSDBigFont;

  NewDevCfg->ExtractCfg.IsNewSendStyle = DevCfg->VideoCfgPkt.VideoFormat.IsNewSendStyle;
  NewDevCfg->ExtractCfg.MaxUserConn = DevCfg->DevInfoPkt.MaxUserConn;

  NewDevCfg->ExtractCfg.RebootHM.w = DevCfg->DevInfoPkt.RebootHM.w;
  NewDevCfg->ExtractCfg.RebootHM.start_h = DevCfg->DevInfoPkt.RebootHM.start_h;
  NewDevCfg->ExtractCfg.RebootHM.start_m = DevCfg->DevInfoPkt.RebootHM.start_m;
  NewDevCfg->ExtractCfg.RebootHM.Days = DevCfg->DevInfoPkt.RebootHM.Days;
  NewDevCfg->ExtractCfg.ProcRunningTime = DevCfg->DevInfoPkt.ProcRunningTime;
  NewDevCfg->ExtractCfg.OEMType = DevCfg->DevInfoPkt.OEMType;

  return true;
}

bool NewDevCfg_to_DevCfg(TNewDevCfg* NewDevCfg, TDevCfg* DevCfg)
{
  i32 i, m;
  //不能要 memset(DevCfg, 0, sizeof(DevCfg));
  //DevInfo
  strcpy(DevCfg->DevInfoPkt.DevModal, NewDevCfg->DevInfo.DevModal);
  DevCfg->DevInfoPkt.SN = NewDevCfg->DevInfo.SN;
  strcpy(DevCfg->DevInfoPkt.SoftVersion, NewDevCfg->DevInfo.SoftVersion);
  if (NewDevCfg->ExtractCfg.VerifyCode == Head_CmdPkt)
  {
    strcpy(DevCfg->DevInfoPkt.DevName, NewDevCfg->ExtractCfg.DevName);
  }
  else
  {
    strcpy(DevCfg->DevInfoPkt.DevName, NewDevCfg->DevInfo.DevName);
  }
  DevCfg->DevInfoPkt.Info.StandardMask = NewDevCfg->DevInfo.StandardMask;
  DevCfg->DevInfoPkt.Info.SubStandardMask = NewDevCfg->DevInfo.SubStandardMask;
  DevCfg->DevInfoPkt.DevType = NewDevCfg->DevInfo.DevType;
  DevCfg->DevInfoPkt.Info.ExistWiFi = NewDevCfg->DevInfo.ExistWiFi;
  DevCfg->DevInfoPkt.Info.ExistSD = NewDevCfg->DevInfo.ExistSD;
  DevCfg->DevInfoPkt.Info.ethLinkStatus = NewDevCfg->DevInfo.ethLinkStatus;
  DevCfg->DevInfoPkt.Info.wifiStatus = NewDevCfg->DevInfo.wifiStatus;
  DevCfg->DevInfoPkt.Info.upnpStatus = NewDevCfg->DevInfo.upnpStatus;
  DevCfg->DevInfoPkt.Info.WlanStatus = NewDevCfg->DevInfo.WlanStatus;
  DevCfg->DevInfoPkt.Info.p2pStatus = NewDevCfg->DevInfo.p2pStatus;
  DevCfg->DevInfoPkt.Info.HardType = NewDevCfg->DevInfo.HardType;
  DevCfg->DevInfoPkt.TimeZone = NewDevCfg->DevInfo.TimeZone;
  DevCfg->DevInfoPkt.DoubleStream = NewDevCfg->DevInfo.DoubleStream;
  DevCfg->DevInfoPkt.Info.ExistFlash = NewDevCfg->DevInfo.ExistFlash;
  DevCfg->DevInfoPkt.Info.SDKVersion = NewDevCfg->DevInfo.SDKVersion;//20171108
  //NetCfg
  DevCfg->NetCfgPkt.DataPort = NewDevCfg->NetCfg.DataPort;
  DevCfg->NetCfgPkt.rtspPort = NewDevCfg->NetCfg.rtspPort;
  DevCfg->NetCfgPkt.HttpPort = NewDevCfg->NetCfg.HttpPort;
  DevCfg->NetCfgPkt.Lan.IPType = NewDevCfg->NetCfg.IPType;
  strcpy(DevCfg->NetCfgPkt.Lan.DevIP, (const char*)IntToIP(NewDevCfg->NetCfg.DevIP));
  strcpy(DevCfg->NetCfgPkt.Lan.SubMask, (const char*)IntToIP(NewDevCfg->NetCfg.SubMask));
  strcpy(DevCfg->NetCfgPkt.Lan.Gateway, (const char*)IntToIP(NewDevCfg->NetCfg.Gateway));
  strcpy(DevCfg->NetCfgPkt.Lan.DNS1, (const char*)IntToIP(NewDevCfg->NetCfg.DNS1));
  strcpy(DevCfg->NetCfgPkt.Lan.DevMAC, NewDevCfg->NetCfg.DevMAC);
  DevCfg->NetCfgPkt.uPnP.Active = NewDevCfg->NetCfg.ActiveuPnP;
  DevCfg->NetCfgPkt.DDNS.Active = NewDevCfg->NetCfg.ActiveDDNS;
  DevCfg->NetCfgPkt.DDNS.DDNSType = NewDevCfg->NetCfg.DDNSType;
  strcpy(DevCfg->NetCfgPkt.DDNS.DDNSDomain, NewDevCfg->NetCfg.DDNSDomain);
  strcpy(DevCfg->NetCfgPkt.DDNS.DDNSServer, NewDevCfg->NetCfg.DDNSServer);
  //wifiCfg
  DevCfg->WiFiCfgPkt.Active = NewDevCfg->wifiCfg.ActiveWIFI;
  DevCfg->WiFiCfgPkt.IsAPMode = NewDevCfg->wifiCfg.IsAPMode;
  strcpy(DevCfg->WiFiCfgPkt.SSID_AP, NewDevCfg->wifiCfg.SSID_AP);
  strcpy(DevCfg->WiFiCfgPkt.Password_AP, NewDevCfg->wifiCfg.Password_AP);
  strcpy(DevCfg->WiFiCfgPkt.SSID_STA, NewDevCfg->wifiCfg.SSID_STA);
  strcpy(DevCfg->WiFiCfgPkt.Password_STA, NewDevCfg->wifiCfg.Password_STA);
  DevCfg->WiFiCfgPkt.Channel = NewDevCfg->wifiCfg.Channel;
  DevCfg->WiFiCfgPkt.EncryptType = NewDevCfg->wifiCfg.EncryptType;
  DevCfg->WiFiCfgPkt.WPA.Auth = NewDevCfg->wifiCfg.Auth;
  DevCfg->WiFiCfgPkt.WPA.Enc = NewDevCfg->wifiCfg.Enc;
  //p2pCfg
  DevCfg->p2pCfgPkt.Active = NewDevCfg->p2pCfg.ActiveP2P;
  DevCfg->p2pCfgPkt.Version = NewDevCfg->p2pCfg.Version;
  DevCfg->p2pCfgPkt.p2pType = NewDevCfg->p2pCfg.p2pType;
  strcpy(DevCfg->p2pCfgPkt.UID, NewDevCfg->p2pCfg.UID);
  //VideoCfg
  GetWidthHeightFromStandard(NewDevCfg->VideoCfg.StandardEx0, &DevCfg->VideoCfgPkt.VideoFormat.Width, &DevCfg->VideoCfgPkt.VideoFormat.Height);
  DevCfg->VideoCfgPkt.VideoFormat.FrameRate = NewDevCfg->VideoCfg.FrameRate0;
  DevCfg->VideoCfgPkt.VideoFormat.BitRate = NewDevCfg->VideoCfg.BitRate0 * 1024;
  DevCfg->VideoCfgPkt.VideoFormat.Sub.StandardEx = NewDevCfg->VideoCfg.StandardEx1;
  DevCfg->VideoCfgPkt.VideoFormat.Sub.FrameRate = NewDevCfg->VideoCfg.FrameRate1;
  DevCfg->VideoCfgPkt.VideoFormat.Sub.BitRate = NewDevCfg->VideoCfg.BitRate1 * 1024;
  DevCfg->VideoCfgPkt.VideoFormat.IsMirror = NewDevCfg->VideoCfg.IsMirror;
  DevCfg->VideoCfgPkt.VideoFormat.IsFlip = NewDevCfg->VideoCfg.IsFlip;
  DevCfg->VideoCfgPkt.VideoFormat.IsShowFrameRate = NewDevCfg->VideoCfg.IsShowFrameRate;
  DevCfg->VideoCfgPkt.VideoFormat.VideoType = NewDevCfg->VideoCfg.VideoType;
  DevCfg->VideoCfgPkt.VideoFormat.BitRateType = NewDevCfg->VideoCfg.BitRateType0;
  DevCfg->VideoCfgPkt.VideoFormat.BitRateQuant = NewDevCfg->VideoCfg.BitRateQuant0;
  DevCfg->VideoCfgPkt.VideoFormat.Sub.BitRateType = NewDevCfg->VideoCfg.BitRateType1;
  DevCfg->VideoCfgPkt.VideoFormat.Sub.BitRateQuant = NewDevCfg->VideoCfg.BitRateQuant1;
  //AudioCfg
  DevCfg->AudioCfgPkt.Active = NewDevCfg->AudioCfg.ActiveAUDIO;
  DevCfg->AudioCfgPkt.InputType = NewDevCfg->AudioCfg.InputTypeAUDIO;
  DevCfg->AudioCfgPkt.VolumeLineIn = NewDevCfg->AudioCfg.VolumeLineIn;
  DevCfg->AudioCfgPkt.VolumeLineOut = NewDevCfg->AudioCfg.VolumeLineOut;
  DevCfg->AudioCfgPkt.AudioFormat.nChannels = NewDevCfg->AudioCfg.nChannels;
  DevCfg->AudioCfgPkt.AudioFormat.wBitsPerSample = NewDevCfg->AudioCfg.wBitsPerSample;
  DevCfg->AudioCfgPkt.AudioFormat.nSamplesPerSec = NewDevCfg->AudioCfg.nSamplesPerSec;
  DevCfg->AudioCfgPkt.AudioFormat.wFormatTag = NewDevCfg->AudioCfg.wFormatTag;

  m = 0;
  for (i=0; i<3; i++)//DevCfg->UserCfgPkt.Count
  {
    strcpy(DevCfg->UserCfgPkt.Lst[i].UserName, NewDevCfg->UserCfg.UserName[i]);
    strcpy(DevCfg->UserCfgPkt.Lst[i].Password, NewDevCfg->UserCfg.Password[i]);
    DevCfg->UserCfgPkt.Lst[i].Authority = NewDevCfg->UserCfg.Authority[i];
    DevCfg->UserCfgPkt.Lst[i].UserGroup = NewDevCfg->UserCfg.Authority[i];
    if (strlen(DevCfg->UserCfgPkt.Lst[i].UserName) > 0) m++;
  }
  DevCfg->UserCfgPkt.Count = m;
  //TDIAlm
  DevCfg->AlmCfgPkt.DIAlm.Active = NewDevCfg->DIAlm.ActiveDI;
  DevCfg->AlmCfgPkt.DIAlm.IsAlmRec = NewDevCfg->DIAlm.IsAlmRec;
  DevCfg->AlmCfgPkt.DIAlm.IsFTPUpload = NewDevCfg->DIAlm.IsFTPUpload;
  DevCfg->AlmCfgPkt.DIAlm.ActiveDO = NewDevCfg->DIAlm.ActiveDO;
  DevCfg->AlmCfgPkt.DIAlm.IsSendEmail = NewDevCfg->DIAlm.IsSendEmail;
  DevCfg->AlmCfgPkt.AlmOutTimeLen = NewDevCfg->DIAlm.AlmOutTimeLen;
  //NewDevCfg->DIAlm.hm = DevCfg->;
  //TMDAlm
  DevCfg->MDCfgPkt.Active = NewDevCfg->MDAlm.ActiveMD;
  DevCfg->MDCfgPkt.Sensitive = NewDevCfg->MDAlm.Sensitive;
  DevCfg->AlmCfgPkt.MDAlm.IsAlmRec = NewDevCfg->MDAlm.IsAlmRec;
  DevCfg->AlmCfgPkt.MDAlm.IsFTPUpload = NewDevCfg->MDAlm.IsFTPUpload;
  DevCfg->AlmCfgPkt.MDAlm.ActiveDO = NewDevCfg->MDAlm.ActiveDO;
  DevCfg->AlmCfgPkt.MDAlm.IsSendEmail = NewDevCfg->MDAlm.IsSendEmail;
  DevCfg->AlmCfgPkt.AlmOutTimeLen = NewDevCfg->MDAlm.AlmOutTimeLen;
  //NewDevCfg->MDAlm.hm = DevCfg->MDCfgPkt.hm;
  memcpy(&DevCfg->MDCfgPkt.NewRect, &NewDevCfg->MDAlm.Rect, sizeof(NewDevCfg->MDAlm.Rect));
  //SoundAlm
  DevCfg->AudioCfgPkt.SoundTriggerActive = NewDevCfg->SoundAlm.ActiveSoundTrigger;
  DevCfg->AudioCfgPkt.SoundTriggerSensitive = NewDevCfg->SoundAlm.Sensitive;
  DevCfg->AlmCfgPkt.SoundAlm.IsAlmRec = NewDevCfg->SoundAlm.IsAlmRec;
  DevCfg->AlmCfgPkt.SoundAlm.IsFTPUpload = NewDevCfg->SoundAlm.IsFTPUpload;
  DevCfg->AlmCfgPkt.SoundAlm.ActiveDO = NewDevCfg->SoundAlm.ActiveDO;
  DevCfg->AlmCfgPkt.SoundAlm.IsSendEmail = NewDevCfg->SoundAlm.IsSendEmail;
  DevCfg->AlmCfgPkt.AlmOutTimeLen = NewDevCfg->SoundAlm.AlmOutTimeLen;
  //RecCfg
  DevCfg->RecCfgPkt.RecStreamType = NewDevCfg->RecCfg.RecStreamType;
  DevCfg->RecCfgPkt.IsRecAudio = NewDevCfg->RecCfg.IsRecAudio;
  DevCfg->RecCfgPkt.RecStyle = (TRecStyle)NewDevCfg->RecCfg.RecStyle;
  DevCfg->RecCfgPkt.Plan = NewDevCfg->RecCfg.Plan;
  DevCfg->RecCfgPkt.Rec_AlmTimeLen = NewDevCfg->RecCfg.Rec_AlmTimeLen;
  DevCfg->RecCfgPkt.Rec_NmlTimeLen = NewDevCfg->RecCfg.Rec_NmlTimeLen;

//ExtractCfg
  if (NewDevCfg->ExtractCfg.VerifyCode == Head_CmdPkt)//0xAAAAAAAA
  {
    strcpy(DevCfg->DevInfoPkt.DevName, NewDevCfg->ExtractCfg.DevName);
    DevCfg->DiskCfgPkt.DiskSize = NewDevCfg->ExtractCfg.DiskSize;
    DevCfg->DiskCfgPkt.FreeSize = NewDevCfg->ExtractCfg.FreeSize;
    DevCfg->DevInfoPkt.Info.PlatformType = NewDevCfg->ExtractCfg.PlatformType;
    DevCfg->AudioCfgPkt.IsAudioGain = NewDevCfg->ExtractCfg.IsAudioGain;
    DevCfg->AudioCfgPkt.AudioGainLevel = NewDevCfg->ExtractCfg.AudioGainLevel;//未用到
    DevCfg->VideoCfgPkt.VideoFormat.IRCutSensitive = NewDevCfg->ExtractCfg.IRCutSensitive;
    DevCfg->HideAreaCfgPkt.Active = NewDevCfg->ExtractCfg.HideAreaActive;
    DevCfg->HideAreaCfgPkt.NewRect.left = NewDevCfg->ExtractCfg.HideAreaLeft / 100.0;
    DevCfg->HideAreaCfgPkt.NewRect.top = NewDevCfg->ExtractCfg.HideAreaTop / 100.0;
    DevCfg->HideAreaCfgPkt.NewRect.right = NewDevCfg->ExtractCfg.HideAreaRight / 100.0;
    DevCfg->HideAreaCfgPkt.NewRect.bottom = NewDevCfg->ExtractCfg.HideAreaBottom / 100.0;
    DevCfg->DevInfoPkt.Info.VideoTypeMask = NewDevCfg->ExtractCfg.VideoTypeMask;
    DevCfg->VideoCfgPkt.VideoFormat.Standard = NewDevCfg->ExtractCfg.Standard;
    DevCfg->VideoCfgPkt.VideoFormat.IPInterval = NewDevCfg->ExtractCfg.IPInterval;
    DevCfg->VideoCfgPkt.VideoFormat.Brightness = NewDevCfg->ExtractCfg.Brightness;
    DevCfg->VideoCfgPkt.VideoFormat.Contrast = NewDevCfg->ExtractCfg.Contrast;
    DevCfg->VideoCfgPkt.VideoFormat.Hue = NewDevCfg->ExtractCfg.Hue;
    DevCfg->VideoCfgPkt.VideoFormat.Saturation = NewDevCfg->ExtractCfg.Saturation;
    DevCfg->VideoCfgPkt.VideoFormat.Sharpness = NewDevCfg->ExtractCfg.Sharpness;
    DevCfg->VideoCfgPkt.VideoFormat.BrightnessNight = NewDevCfg->ExtractCfg.BrightnessNight;
    DevCfg->VideoCfgPkt.VideoFormat.ContrastNight = NewDevCfg->ExtractCfg.ContrastNight;
    DevCfg->VideoCfgPkt.VideoFormat.HueNight = NewDevCfg->ExtractCfg.HueNight;
    DevCfg->VideoCfgPkt.VideoFormat.SaturationNight = NewDevCfg->ExtractCfg.SaturationNight;
    DevCfg->VideoCfgPkt.VideoFormat.SharpnessNight = NewDevCfg->ExtractCfg.SharpnessNight;
    DevCfg->VideoCfgPkt.VideoFormat.IsWDR = NewDevCfg->ExtractCfg.IsWDR;
    DevCfg->VideoCfgPkt.VideoFormat.WDRLevel = NewDevCfg->ExtractCfg.WDRLevel;
    DevCfg->VideoCfgPkt.VideoFormat.IsShowTime = NewDevCfg->ExtractCfg.IsShowTime;
    DevCfg->VideoCfgPkt.VideoFormat.IsShowTitle = NewDevCfg->ExtractCfg.IsShowTitle;
    DevCfg->VideoCfgPkt.VideoFormat.IsShowFrameRate = NewDevCfg->ExtractCfg.IsShowFrameRate;
    DevCfg->VideoCfgPkt.VideoFormat.IsShowLogo = NewDevCfg->ExtractCfg.IsShowLogo;
    DevCfg->VideoCfgPkt.VideoFormat.TimeX = NewDevCfg->ExtractCfg.TimeX / 100.0;
    DevCfg->VideoCfgPkt.VideoFormat.TimeY = NewDevCfg->ExtractCfg.TimeY / 100.0;
    DevCfg->VideoCfgPkt.VideoFormat.TitleX = NewDevCfg->ExtractCfg.TitleX / 100.0;
    DevCfg->VideoCfgPkt.VideoFormat.TitleY = NewDevCfg->ExtractCfg.TitleY / 100.0;
    DevCfg->VideoCfgPkt.VideoFormat.FrameRateX = NewDevCfg->ExtractCfg.FrameRateX / 100.0;
    DevCfg->VideoCfgPkt.VideoFormat.FrameRateY = NewDevCfg->ExtractCfg.FrameRateY / 100.0;
    DevCfg->VideoCfgPkt.VideoFormat.LogoX = NewDevCfg->ExtractCfg.LogoX / 100.0;
    DevCfg->VideoCfgPkt.VideoFormat.LogoY = NewDevCfg->ExtractCfg.LogoY / 100.0;
    DevCfg->VideoCfgPkt.VideoFormat.IsOsdTransparent = NewDevCfg->ExtractCfg.IsOsdTransparent;
    DevCfg->VideoCfgPkt.VideoFormat.IsOSDBigFont = NewDevCfg->ExtractCfg.IsOSDBigFont;
    DevCfg->VideoCfgPkt.VideoFormat.IsNewSendStyle = NewDevCfg->ExtractCfg.IsNewSendStyle;

    DevCfg->DevInfoPkt.MaxUserConn = NewDevCfg->ExtractCfg.MaxUserConn;
    DevCfg->DevInfoPkt.RebootHM.w = NewDevCfg->ExtractCfg.RebootHM.w;
    DevCfg->DevInfoPkt.RebootHM.start_h = NewDevCfg->ExtractCfg.RebootHM.start_h;
    DevCfg->DevInfoPkt.RebootHM.start_m = NewDevCfg->ExtractCfg.RebootHM.start_m;
    DevCfg->DevInfoPkt.RebootHM.Days = NewDevCfg->ExtractCfg.RebootHM.Days;
    DevCfg->DevInfoPkt.ProcRunningTime = NewDevCfg->ExtractCfg.ProcRunningTime;
    DevCfg->DevInfoPkt.OEMType = NewDevCfg->ExtractCfg.OEMType;

  }
  return true;
}

//-----------------------------------------------------------------------------
char* cgi_Get_VideoType(int Value)
{
  char* Str = NULL;
  switch (Value)
  {
  case MPEG4: Str = "MPEG4"; break;
  case MJPEG: Str = "MJPEG"; break;
  case H264: Str = "H264"; break;
  case H265: Str = "H265"; break;
  default: break;
  }
  return Str;
}
//-----------------------------------------------------------------------------
char* cgi_Get_Standard(int Value)
{
  char* Str = NULL;
  switch (Value)
  {
  case V640x360: Str = "V640x360"; break;
  case V320x240: Str = "V320x240"; break;
  case V640x480: Str = "V640x480"; break;
  case V1280x720: Str = "V1280x720"; break;
  case V1280x960: Str = "V1280x960"; break;
  case V1920x1080: Str = "V1920x1080"; break;
  case V2560x1440: Str = "V2560x1440"; break;
  case V2592x1520: Str = "V2592x1520"; break;
  case V2592x1944: Str = "V2592x1944"; break;
  default: break;
  }
  return Str;
}
//-----------------------------------------------------------------------------
bool GetWidthHeightFromStandard(i32 Value, i32* w, i32* h)//由TStandard取得长、宽
{
  switch (Value)
  {
  case V640x360:   *w = 640; *h = 360; break;
  case V320x240:   *w = 320; *h = 240; break;
  case V640x480:   *w = 640; *h = 480; break;
  case V800x600:   *w = 800; *h = 600; break;
  case V1024x768:  *w = 1024; *h = 768; break;
  case V1280x720:  *w = 1280; *h = 720; break;
  case V1280x800:  *w = 1280; *h = 800; break;
  case V1280x960:  *w = 1280; *h = 960; break;
  case V1280x1024: *w = 1280; *h = 1024; break;
  case V1360x768:  *w = 1360; *h = 768; break;
  case V1400x1050: *w = 1400; *h = 1050; break;
  case V1600x1200: *w = 1600; *h = 1200; break;
  case V1920x1080: *w = 1920; *h = 1080; break;
  case V2560x1440: *w = 2560; *h = 1440; break;
  case V2592x1520: *w = 2592; *h = 1520; break;
  case V2592x1944: *w = 2592; *h = 1944; break;
/*    
  case V3840x2160: *w = 3840; *h = 2160; break;
  case V3072x1728: *w = 3072; *h = 1728; break;
*/
  default: return false;
  }
  return true;
}
//-----------------------------------------------------------------------------
i32 GetStandardFromWidthHeight(i32 w, i32 h)
{
  if ((w == 640)&&(h == 360)) return V640x360;
  else if ((w == 320)&&(h == 240)) return V320x240;
  else if ((w == 640)&&(h == 480)) return V640x480;
  else if ((w == 800)&&(h == 600)) return V800x600;
  else if ((w == 1024)&&(h == 768)) return V1024x768;
  else if ((w == 1280)&&(h == 720)) return V1280x720;
  else if ((w == 1280)&&(h == 800)) return V1280x800;
  else if ((w == 1280)&&(h == 960)) return V1280x960;
  else if ((w == 1280)&&(h == 1024)) return V1280x1024;
  else if ((w == 1360)&&(h == 768)) return V1360x768;
  else if ((w == 1400)&&(h == 1050)) return V1400x1050;
  else if ((w == 1600)&&(h == 1200)) return V1600x1200;
  else if ((w == 1920)&&(h == 1080)) return V1920x1080;
  else if ((w == 2560)&&(h == 1440)) return V2560x1440;
  else if ((w == 2592)&&(h == 1520)) return V2592x1520;
  else if ((w == 2592)&&(h == 1944)) return V2592x1944;
/*
  else if ((w == 3072)&&(h == 1728)) return V3072x1728;
  else if ((w == 3840)&&(h == 2160)) return V3840x2160;
*/
  else return StandardExMin;
}
//-----------------------------------------------------------------------------
void Json_cgi_GetDevInfo(char* cgiBuf, TDevCfg* DevCfg)//通过网页调用，取得设备信息
{
  //http://192.168.0.169/cfg1.cgi?User=admin&Psd=admin&MsgID=30
  int i, len;
  char Str[1024];
  sprintf(Str, "{"); strcat(cgiBuf, Str);
  sprintf(Str, "\"SDKVersion\":%d,", DevCfg->DevInfoPkt.Info.SDKVersion); strcat(cgiBuf, Str);//20171215 add

  sprintf(Str, "\"DevModal\":\"%s\",", DevCfg->DevInfoPkt.DevModal); strcat(cgiBuf, Str);
  sprintf(Str, "\"DevName\":\"%s\",", DevCfg->DevInfoPkt.DevName); strcat(cgiBuf, Str);
  sprintf(Str, "\"SN\":\"%0.8X\",", DevCfg->DevInfoPkt.SN); strcat(cgiBuf, Str);
  sprintf(Str, "\"SoftVersion\":\"%s\",", DevCfg->DevInfoPkt.SoftVersion); strcat(cgiBuf, Str);
  sprintf(Str, "\"FileVersion\":\"%s\",", DevCfg->DevInfoPkt.FileVersion); strcat(cgiBuf, Str);
  sprintf(Str, "\"ExistWiFi\":%d,", DevCfg->DevInfoPkt.Info.ExistWiFi); strcat(cgiBuf, Str);
  sprintf(Str, "\"ExistSD\":%d,", DevCfg->DevInfoPkt.Info.ExistSD); strcat(cgiBuf, Str);
  sprintf(Str, "\"ExistFlash\":%d,", DevCfg->DevInfoPkt.Info.ExistFlash); strcat(cgiBuf, Str);
  sprintf(Str, "\"HardType\":%d,", DevCfg->DevInfoPkt.Info.HardType); strcat(cgiBuf, Str);
  sprintf(Str, "\"TimeZone\":%d,", DevCfg->DevInfoPkt.TimeZone); strcat(cgiBuf, Str);

  sprintf(Str, "\"ethLinkStatus\":%d,", DevCfg->DevInfoPkt.Info.ethLinkStatus); strcat(cgiBuf, Str);
  sprintf(Str, "\"wifiStatus\":%d,", DevCfg->DevInfoPkt.Info.wifiStatus); strcat(cgiBuf, Str);//20171215 add
  sprintf(Str, "\"upnpStatus\":%d,", DevCfg->DevInfoPkt.Info.upnpStatus); strcat(cgiBuf, Str);
  sprintf(Str, "\"WlanStatus\":%d,", DevCfg->DevInfoPkt.Info.WlanStatus); strcat(cgiBuf, Str);
  sprintf(Str, "\"p2pStatus\":%d,", DevCfg->DevInfoPkt.Info.p2pStatus); strcat(cgiBuf, Str);
  sprintf(Str, "\"DoubleStream\":%d,", DevCfg->DevInfoPkt.DoubleStream); strcat(cgiBuf, Str);//20171215 add

  sprintf(Str, "\"MaxUserConn\":%d,", DevCfg->DevInfoPkt.MaxUserConn); strcat(cgiBuf, Str);//20171219 add
  sprintf(Str, "\"ProcRunningTime\":%d,", DevCfg->DevInfoPkt.ProcRunningTime); strcat(cgiBuf, Str);//20171219 add
  sprintf(Str, "\"OEMType\":%d,", DevCfg->DevInfoPkt.OEMType); strcat(cgiBuf, Str);//20171219 add
  
  sprintf(Str, "\"TimerRebootW\":%d,", DevCfg->DevInfoPkt.RebootHM.w); strcat(cgiBuf, Str);//20171219 add
  sprintf(Str, "\"TimerRebootStartH\":%d,", DevCfg->DevInfoPkt.RebootHM.start_h); strcat(cgiBuf, Str);//20171219 add
  sprintf(Str, "\"TimerRebootStartM\":%d,", DevCfg->DevInfoPkt.RebootHM.start_m); strcat(cgiBuf, Str);//20171219 add
  sprintf(Str, "\"TimerRebootStartDays\":%d,", DevCfg->DevInfoPkt.RebootHM.Days); strcat(cgiBuf, Str);//20171219 add

  sprintf(Str, "\"VideoType\":\""); strcat(cgiBuf, Str);//20171215 add
  for (i=MPEG4; i<=H265; i++)
  {
    if (((DevCfg->DevInfoPkt.Info.VideoTypeMask >> i) & 1)==1)
    {
      sprintf(Str, "%s,", cgi_Get_VideoType(i)); strcat(cgiBuf, Str);
    }
  }
  len = strlen(cgiBuf);
  if (cgiBuf[len - 1] == ',') cgiBuf[len - 1] = 0x00;
  sprintf(Str, "\","); strcat(cgiBuf, Str);
//
  sprintf(Str, "\"MainStream\":\""); strcat(cgiBuf, Str);
  for (i=StandardExMax; i>=StandardExMin; i--)
  {
    if (((DevCfg->DevInfoPkt.Info.StandardMask >> i) & 1)==1)
    {
      sprintf(Str, "%s,", cgi_Get_Standard(i)); strcat(cgiBuf, Str);
    }
  }
  len = strlen(cgiBuf);
  if (cgiBuf[len - 1] == ',') cgiBuf[len - 1] = 0x00;
  
  sprintf(Str, "\","); strcat(cgiBuf, Str);
//
  sprintf(Str, "\"SubStream\":\""); strcat(cgiBuf, Str);
  for (i=StandardExMax; i>=StandardExMin; i--)
  {
    if (((DevCfg->DevInfoPkt.Info.SubStandardMask >> i) & 1)==1)
    {
      sprintf(Str, "%s,", cgi_Get_Standard(i)); strcat(cgiBuf, Str);
    }
  }

  len = strlen(cgiBuf);
  if (cgiBuf[len - 1] == ',') cgiBuf[len - 1] = 0x00;

  sprintf(Str, "\""); strcat(cgiBuf, Str);
  sprintf(Str, "}"); strcat(cgiBuf, Str);
}
//-----------------------------------------------------------------------------
void Json_cgi_GetUserLst(char* cgiBuf, TDevCfg* DevCfg)//通过网页调用，取得用户列表
{
  //http://192.168.0.169/cfg1.cgi?User=admin&Psd=admin&MsgID=32
  int i, len;
  char Str[1024];
  sprintf(Str, "{"); strcat(cgiBuf, Str);
  for (i=0; i<3; i++)
  {    
    sprintf(Str, 
      "\"UserName%d\":\"%s\",\"Password%d\":\"%s\",\"Authority%d\":%d,",
      i, DevCfg->UserCfgPkt.Lst[i].UserName,
      i, DevCfg->UserCfgPkt.Lst[i].Password,
      i, DevCfg->UserCfgPkt.Lst[i].Authority);

    strcat(cgiBuf, Str);
  }
  len = strlen(cgiBuf);
  if (cgiBuf[len - 1] == ',') cgiBuf[len - 1] = 0x00;
  sprintf(Str, "}"); strcat(cgiBuf, Str);
}
//-----------------------------------------------------------------------------
void Json_cgi_GetNetCfg(char* cgiBuf, TDevCfg* DevCfg)//通过网页调用，取得网络配置
{
  int len;
  char Str[1024];
  //http://192.168.0.169/cfg1.cgi?User=admin&Psd=admin&MsgID=34
  sprintf(Str, "{"); strcat(cgiBuf, Str);
  sprintf(Str, "\"HttpPort\":%d,", DevCfg->NetCfgPkt.HttpPort); strcat(cgiBuf, Str);
  sprintf(Str, "\"DataPort\":%d,", DevCfg->NetCfgPkt.DataPort); strcat(cgiBuf, Str);
  sprintf(Str, "\"rtspPort\":%d,", DevCfg->NetCfgPkt.rtspPort); strcat(cgiBuf, Str);
  sprintf(Str, "\"uPnPActive\":%d,", DevCfg->NetCfgPkt.uPnP.Active); strcat(cgiBuf, Str);
  sprintf(Str, "\"IPType\":%d,", DevCfg->NetCfgPkt.Lan.IPType); strcat(cgiBuf, Str);
  sprintf(Str, "\"DevIP\":\"%s\",", DevCfg->NetCfgPkt.Lan.DevIP); strcat(cgiBuf, Str);
  sprintf(Str, "\"DevMAC\":\"%s\",", DevCfg->NetCfgPkt.Lan.DevMAC); strcat(cgiBuf, Str);
  sprintf(Str, "\"SubMask\":\"%s\",", DevCfg->NetCfgPkt.Lan.SubMask); strcat(cgiBuf, Str);
  sprintf(Str, "\"Gateway\":\"%s\",", DevCfg->NetCfgPkt.Lan.Gateway); strcat(cgiBuf, Str);
  sprintf(Str, "\"DNS1\":\"%s\",", DevCfg->NetCfgPkt.Lan.DNS1); strcat(cgiBuf, Str);
  sprintf(Str, "\"DDNSActive\":%d,", DevCfg->NetCfgPkt.DDNS.Active); strcat(cgiBuf, Str);
  sprintf(Str, "\"DDNSType\":%d,", DevCfg->NetCfgPkt.DDNS.DDNSType); strcat(cgiBuf, Str);
  sprintf(Str, "\"DDNSDomain\":\"%s\",", DevCfg->NetCfgPkt.DDNS.DDNSDomain); strcat(cgiBuf, Str);
  sprintf(Str, "\"DDNSDDNSServer\":\"%s\",", DevCfg->NetCfgPkt.DDNS.DDNSServer); strcat(cgiBuf, Str);
  sprintf(Str, "\"PPPOEActive\":%d,", DevCfg->NetCfgPkt.PPPOE.Active); strcat(cgiBuf, Str);
  sprintf(Str, "\"PPPOEAccount\":\"%s\",", DevCfg->NetCfgPkt.PPPOE.Account); strcat(cgiBuf, Str);
  sprintf(Str, "\"PPPOEPassword\":\"%s\",", DevCfg->NetCfgPkt.PPPOE.Password); strcat(cgiBuf, Str);
  len = strlen(cgiBuf);
  if (cgiBuf[len - 1] == ',') cgiBuf[len - 1] = 0x00;
  sprintf(Str, "}"); strcat(cgiBuf, Str);
}
//-----------------------------------------------------------------------------
void Json_cgi_GetWiFiCfg(char* cgiBuf, TDevCfg* DevCfg)//通过网页调用，取得WIFI配置
{
  //http://192.168.0.169/cfg1.cgi?User=admin&Psd=admin&MsgID=37
  char Str[1024];
  TWiFiCfgPkt* wPkt = &DevCfg->WiFiCfgPkt;
  sprintf(Str, "{"); strcat(cgiBuf, Str);
  sprintf(Str, "\"wifi_Active\":%d,", wPkt->Active); strcat(cgiBuf, Str);
  sprintf(Str, "\"wifi_IsAPMode\":%d,", wPkt->IsAPMode); strcat(cgiBuf, Str);
  sprintf(Str, "\"wifi_SSID_AP\":\"%s\",", wPkt->SSID_AP); strcat(cgiBuf, Str);
  sprintf(Str, "\"wifi_Password_AP\":\"%s\",", wPkt->Password_AP); strcat(cgiBuf, Str);

  sprintf(Str, "\"wifi_SSID_STA\":\"%s\",", wPkt->SSID_STA); strcat(cgiBuf, Str);
  sprintf(Str, "\"wifi_Password_STA\":\"%s\"", wPkt->Password_STA); strcat(cgiBuf, Str);

  sprintf(Str, "}"); strcat(cgiBuf, Str);
}
//-----------------------------------------------------------------------------
void Json_cgi_GetWiFiSTALst(char* cgiBuf, TDevCfg* DevCfg)//通过网页调用，取得保存在配置中WIFI路由器列表
{
  //http://192.168.0.169/cfg1.cgi?User=admin&Psd=admin&MsgID=70
  int i, len;
  char Str[1024];
  sprintf(Str, "["); strcat(cgiBuf, Str);
  for (i=0; i<DevCfg->wifiCfgLst.Count; i++)
  {    
    Twifi_sta_item* Item = &DevCfg->wifiCfgLst.Lst[i];
    if (Item->SSID[0] == 0) continue;
    sprintf(Str, "{\"SSID\":\"%s\",", Item->SSID); strcat(cgiBuf, Str);
    sprintf(Str, "\"Password\":\"%s\"},", Item->Password); strcat(cgiBuf, Str);
  }
  len = strlen(cgiBuf);
  if (cgiBuf[len - 1] == ',') cgiBuf[len - 1] = 0x00;
  sprintf(Str, "]"); strcat(cgiBuf, Str);
}
//-----------------------------------------------------------------------------
void Json_cgi_GetVideoCfg(char* cgiBuf, TDevCfg* DevCfg)//通过网页调用，取得视频配置
{
  int w, h;
  char Str[1024];
  //http://192.168.0.169/cfg1.cgi?User=admin&Psd=admin&MsgID=39
  TVideoFormat* vfmt = &DevCfg->VideoCfgPkt.VideoFormat;
  sprintf(Str, "{"); strcat(cgiBuf, Str);
  if (vfmt->Standard == NTSC) //NTSC
  {
    sprintf(Str, "\"Standard\":\"%s\",", "NTSC"); strcat(cgiBuf, Str);//20171215 add
  }
  else
  {
    sprintf(Str, "\"Standard\":\"%s\",", "PAL"); strcat(cgiBuf, Str);//20171215 add
  }

  sprintf(Str, "\"VideoType\":\"%s\",", cgi_Get_VideoType(vfmt->VideoType)); strcat(cgiBuf, Str);//20171215 add

  sprintf(Str, "\"Brightness\":%d,", vfmt->Brightness); strcat(cgiBuf, Str);
  sprintf(Str, "\"Contrast\":%d,", vfmt->Contrast); strcat(cgiBuf, Str);
  sprintf(Str, "\"Hue\":%d,", vfmt->Hue); strcat(cgiBuf, Str);
  sprintf(Str, "\"Saturation\":%d,", vfmt->Saturation); strcat(cgiBuf, Str);
  sprintf(Str, "\"Sharpness\":%d,", vfmt->Sharpness); strcat(cgiBuf, Str);

  sprintf(Str, "\"BrightnessNight\":%d,", vfmt->Brightness); strcat(cgiBuf, Str);
  sprintf(Str, "\"ContrastNight\":%d,", vfmt->Contrast); strcat(cgiBuf, Str);
  sprintf(Str, "\"HueNight\":%d,", vfmt->Hue); strcat(cgiBuf, Str);
  sprintf(Str, "\"SaturationNight\":%d,", vfmt->Saturation); strcat(cgiBuf, Str);
  sprintf(Str, "\"SharpnessNight\":%d,", vfmt->Sharpness); strcat(cgiBuf, Str);

  sprintf(Str, "\"IsMirror\":%d,", vfmt->IsMirror); strcat(cgiBuf, Str);
  sprintf(Str, "\"IsFlip\":%d,", vfmt->IsFlip); strcat(cgiBuf, Str);

  sprintf(Str, "\"Width0\":%d,", vfmt->Width); strcat(cgiBuf, Str);
  sprintf(Str, "\"Height0\":%d,", vfmt->Height); strcat(cgiBuf, Str);
  sprintf(Str, "\"FrameRate0\":%d,", vfmt->FrameRate); strcat(cgiBuf, Str);
  sprintf(Str, "\"BitRate0\":%d,", vfmt->BitRate / 1024); strcat(cgiBuf, Str);

  GetWidthHeightFromStandard(vfmt->Sub.StandardEx, &w, &h);
  sprintf(Str, "\"Width1\":%d,", w); strcat(cgiBuf, Str);
  sprintf(Str, "\"Height1\":%d,", h); strcat(cgiBuf, Str);
  sprintf(Str, "\"FrameRate1\":%d,", vfmt->Sub.FrameRate); strcat(cgiBuf, Str);
  sprintf(Str, "\"BitRate1\":%d,", vfmt->Sub.BitRate / 1024); strcat(cgiBuf, Str);

  sprintf(Str, "\"BitRateType0\":%d,", vfmt->BitRateType); strcat(cgiBuf, Str);
  sprintf(Str, "\"BitRateType1\":%d,", vfmt->Sub.BitRateType); strcat(cgiBuf, Str);
  sprintf(Str, "\"BitRateQuant0\":%d,", vfmt->BitRateQuant); strcat(cgiBuf, Str);
  sprintf(Str, "\"BitRateQuant1\":%d,", vfmt->Sub.BitRateQuant); strcat(cgiBuf, Str);

  sprintf(Str, "\"IPInterval\":%d,", vfmt->IPInterval); strcat(cgiBuf, Str);//20171215 add
  sprintf(Str, "\"IsWDR\":%d,", vfmt->IsWDR); strcat(cgiBuf, Str);//20171215 add
  sprintf(Str, "\"WDRLevel\":%d,", vfmt->WDRLevel); strcat(cgiBuf, Str);//20171215 add

  sprintf(Str, "\"IsShowTime\":%d,", vfmt->IsShowTime); strcat(cgiBuf, Str);//20171215 add
  sprintf(Str, "\"TimeX\":\"%0.2f\",", vfmt->TimeX); strcat(cgiBuf, Str);//20171215 add
  sprintf(Str, "\"TimeY\":\"%0.2f\",", vfmt->TimeY); strcat(cgiBuf, Str);//20171215 add

  sprintf(Str, "\"IsShowTitle\":%d,", vfmt->IsShowTitle); strcat(cgiBuf, Str);//20171215 add
  sprintf(Str, "\"TitleX\":\"%0.2f\",", vfmt->TitleX); strcat(cgiBuf, Str);//20171215 add
  sprintf(Str, "\"TitleY\":\"%0.2f\",", vfmt->TitleY); strcat(cgiBuf, Str);//20171215 add

  sprintf(Str, "\"IsShowFrameRate\":%d,", vfmt->IsShowFrameRate); strcat(cgiBuf, Str);//20171215 add
  sprintf(Str, "\"FrameRateX\":\"%0.2f\",", vfmt->FrameRateX); strcat(cgiBuf, Str);//20171215 add
  sprintf(Str, "\"FrameRateY\":\"%0.2f\",", vfmt->FrameRateY); strcat(cgiBuf, Str);//20171215 add

  sprintf(Str, "\"IsShowLogo\":%d,", vfmt->IsShowLogo); strcat(cgiBuf, Str);//20171215 add
  sprintf(Str, "\"LogoX\":\"%0.2f\",", vfmt->LogoX); strcat(cgiBuf, Str);//20171215 add
  sprintf(Str, "\"LogoY\":\"%0.2f\",", vfmt->LogoY); strcat(cgiBuf, Str);//20171215 add

  sprintf(Str, "\"IsOSDBigFont\":%d,", vfmt->IsOSDBigFont); strcat(cgiBuf, Str);//20171215 add
  sprintf(Str, "\"IsOsdTransparent\":%d,", vfmt->IsOsdTransparent); strcat(cgiBuf, Str);//20171215 add
  sprintf(Str, "\"IRCutSensitive\":%d,", vfmt->IRCutSensitive); strcat(cgiBuf, Str);//20171215 add
  sprintf(Str, "\"IsNewSendStyle\":%d", vfmt->IsNewSendStyle); strcat(cgiBuf, Str);//20171215 add

  sprintf(Str, "}"); strcat(cgiBuf, Str);
}
//-----------------------------------------------------------------------------
void Json_cgi_GetAudioCfg(char* cgiBuf, TDevCfg* DevCfg)//通过网页调用，取得音频配置
{
  int len;
  char Str[1024];
  TAlmCfgPkt* almPkt = &DevCfg->AlmCfgPkt;
  //http://192.168.0.169/cfg1.cgi?User=admin&Psd=admin&MsgID=41
  sprintf(Str, "{"); strcat(cgiBuf, Str);
  sprintf(Str, "\"AUDIO_Active\":%d,", DevCfg->AudioCfgPkt.Active); strcat(cgiBuf, Str);
  sprintf(Str, "\"AUDIO_InputType\":%d,", DevCfg->AudioCfgPkt.InputType); strcat(cgiBuf, Str);
  //sprintf(Str, "\"AUDIO_VolumeMicIn\":%d,", DevCfg->AudioCfgPkt.VolumeMicIn); strcat(cgiBuf, Str);//20171215 del
  sprintf(Str, "\"AUDIO_VolumeLineIn\":%d,", DevCfg->AudioCfgPkt.VolumeLineIn); strcat(cgiBuf, Str);
  sprintf(Str, "\"AUDIO_VolumeLineOut\":%d,", DevCfg->AudioCfgPkt.VolumeLineOut); strcat(cgiBuf, Str);
  sprintf(Str, "\"AUDIO_wFormatTag\":%d,", DevCfg->AudioCfgPkt.AudioFormat.wFormatTag); strcat(cgiBuf, Str);
  sprintf(Str, "\"AUDIO_nChannels\":%d,", DevCfg->AudioCfgPkt.AudioFormat.nChannels); strcat(cgiBuf, Str);
  sprintf(Str, "\"AUDIO_nSamplesPerSec\":%d,", DevCfg->AudioCfgPkt.AudioFormat.nSamplesPerSec); strcat(cgiBuf, Str);
  sprintf(Str, "\"AUDIO_wBitsPerSample\":%d,", DevCfg->AudioCfgPkt.AudioFormat.wBitsPerSample); strcat(cgiBuf, Str);
  sprintf(Str, "\"AUDIO_IsAudioGain\":%d,", DevCfg->AudioCfgPkt.IsAudioGain); strcat(cgiBuf, Str);//20171215 add

  sprintf(Str, "\"AUDIO_SoundTriggerActive\":%d,", DevCfg->AudioCfgPkt.SoundTriggerActive); strcat(cgiBuf, Str);
  sprintf(Str, "\"AUDIO_SoundTriggerSensitive\":%d,", DevCfg->AudioCfgPkt.SoundTriggerSensitive); strcat(cgiBuf, Str);

  sprintf(Str, "\"AUDIO_IsAlmRec\":%d,", almPkt->SoundAlm.IsAlmRec); strcat(cgiBuf, Str);
  sprintf(Str, "\"AUDIO_IsFTPUpload\":%d,", almPkt->SoundAlm.IsFTPUpload); strcat(cgiBuf, Str);
  sprintf(Str, "\"AUDIO_ActiveDO\":%d,", almPkt->SoundAlm.ActiveDO); strcat(cgiBuf, Str);
  sprintf(Str, "\"AUDIO_IsSendEmail\":%d,", almPkt->SoundAlm.IsSendEmail); strcat(cgiBuf, Str);

  len = strlen(cgiBuf);
  if (cgiBuf[len - 1] == ',') cgiBuf[len - 1] = 0x00;
  sprintf(Str, "}"); strcat(cgiBuf, Str);
}
//-----------------------------------------------------------------------------
void Json_cgi_GetHideArea(char* cgiBuf, TDevCfg* DevCfg)//通过网页调用，取得隐藏区域配置
{
  int len;
  char Str[1024];
  //http://192.168.0.169/cfg1.cgi?User=admin&Psd=admin&MsgID=43
  sprintf(Str, "{"); strcat(cgiBuf, Str);
  sprintf(Str, "\"HA_Active\":%d,", DevCfg->HideAreaCfgPkt.Active); strcat(cgiBuf, Str);
  sprintf(Str, "\"HA_left\":\"%0.2f\",", DevCfg->HideAreaCfgPkt.NewRect.left); strcat(cgiBuf, Str);
  sprintf(Str, "\"HA_top\":\"%0.2f\",", DevCfg->HideAreaCfgPkt.NewRect.top); strcat(cgiBuf, Str);
  sprintf(Str, "\"HA_right\":\"%0.2f\",", DevCfg->HideAreaCfgPkt.NewRect.right); strcat(cgiBuf, Str);
  sprintf(Str, "\"HA_bottom\":\"%0.2f\",", DevCfg->HideAreaCfgPkt.NewRect.bottom); strcat(cgiBuf, Str);
  len = strlen(cgiBuf);
  if (cgiBuf[len - 1] == ',') cgiBuf[len - 1] = 0x00;
  sprintf(Str, "}"); strcat(cgiBuf, Str);
}
//-----------------------------------------------------------------------------
void Json_cgi_GetMDCfg(char* cgiBuf, TDevCfg* DevCfg)//通过网页调用，取得移动侦测配置
{
  //http://192.168.0.169/cfg1.cgi?User=admin&Psd=admin&MsgID=45
  int len;
  char Str[1024];
  TMDCfgPkt* aPkt = &DevCfg->MDCfgPkt;
  TAlmCfgPkt* almPkt = &DevCfg->AlmCfgPkt;
  sprintf(Str, "{"); strcat(cgiBuf, Str);

  sprintf(Str, "\"MD_Active\":%d,", aPkt->Active); strcat(cgiBuf, Str);
  sprintf(Str, "\"MD_Sensitive\":%d,", aPkt->Sensitive); strcat(cgiBuf, Str);
  sprintf(Str, "\"MD_left\":\"%0.2f\",", aPkt->NewRect.left); strcat(cgiBuf, Str);
  sprintf(Str, "\"MD_top\":\"%0.2f\",", aPkt->NewRect.top); strcat(cgiBuf, Str);
  sprintf(Str, "\"MD_right\":\"%0.2f\",", aPkt->NewRect.right); strcat(cgiBuf, Str);
  sprintf(Str, "\"MD_bottom\":\"%0.2f\",", aPkt->NewRect.bottom); strcat(cgiBuf, Str);

  sprintf(Str, "\"MD_IsAlmRec\":%d,", almPkt->MDAlm.IsAlmRec); strcat(cgiBuf, Str);
  sprintf(Str, "\"MD_IsFTPUpload\":%d,", almPkt->MDAlm.IsFTPUpload); strcat(cgiBuf, Str);
  sprintf(Str, "\"MD_ActiveDO\":%d,", almPkt->MDAlm.ActiveDO); strcat(cgiBuf, Str);
  sprintf(Str, "\"MD_IsSendEmail\":%d,", almPkt->MDAlm.IsSendEmail); strcat(cgiBuf, Str);

  len = strlen(cgiBuf);
  if (cgiBuf[len - 1] == ',') cgiBuf[len - 1] = 0x00;
  sprintf(Str, "}"); strcat(cgiBuf, Str);
}
//-----------------------------------------------------------------------------
void Json_cgi_GetDiskCfg(char* cgiBuf, TDevCfg* DevCfg)//通过网页调用，取得TF卡信息
{
  //http://192.168.0.169/cfg1.cgi?User=admin&Psd=admin&MsgID=53
  char Str[1024];

  dm_GetDiskSpaceInfo(&DevCfg->DiskCfgPkt);

  sprintf(Str, "{"); strcat(cgiBuf, Str);
  sprintf(Str, "\"DiskName\":\"%s\",", DevCfg->DiskCfgPkt.DiskName); strcat(cgiBuf, Str);
  sprintf(Str, "\"DiskSize\":%d,", DevCfg->DiskCfgPkt.DiskSize); strcat(cgiBuf, Str);
  sprintf(Str, "\"FreeSize\":%d,", DevCfg->DiskCfgPkt.FreeSize); strcat(cgiBuf, Str);
  sprintf(Str, "\"MinFreeSize\":%d", DevCfg->DiskCfgPkt.MinFreeSize); strcat(cgiBuf, Str);
  sprintf(Str, "}"); strcat(cgiBuf, Str);
}
//-----------------------------------------------------------------------------
void Json_cgi_GetRecCfg(char* cgiBuf, TDevCfg* DevCfg)//通过网页调用，取得录像配置
{
  int len;
  char Str[1024];
  //http://192.168.0.169/cfg1.cgi?User=admin&Psd=admin&MsgID=55
  TRecCfgPkt* PPkt = &DevCfg->RecCfgPkt;
  sprintf(Str, "{"); strcat(cgiBuf, Str);
  sprintf(Str, "\"Rec_RecStyle\":%d,", PPkt->RecStyle); strcat(cgiBuf, Str);  
  sprintf(Str, "\"Rec_IsRecAudio\":%d,", PPkt->IsRecAudio); strcat(cgiBuf, Str);  
  sprintf(Str, "\"Rec_RecStreamType\":%d,", PPkt->RecStreamType); strcat(cgiBuf, Str);
  sprintf(Str, "\"Rec_AlmTimeLen\":%d,", PPkt->Rec_AlmTimeLen); strcat(cgiBuf, Str);
  sprintf(Str, "\"Rec_NmlTimeLen\":%d,", PPkt->Rec_NmlTimeLen); strcat(cgiBuf, Str);
  len = strlen(cgiBuf);
  if (cgiBuf[len - 1] == ',') cgiBuf[len - 1] = 0x00;
  sprintf(Str, "}"); strcat(cgiBuf, Str);
}
//-----------------------------------------------------------------------------
void Json_cgi_GetFTPCfg(char* cgiBuf, TDevCfg* DevCfg)//通过网页调用，取得FTP配置
{
  int len;
  char Str[1024];
  //http://192.168.0.169/cfg1.cgi?User=admin&Psd=admin&MsgID=57     
  sprintf(Str, "{"); strcat(cgiBuf, Str);
  sprintf(Str, "\"FTP_Active\":%d,", DevCfg->FTPCfgPkt.Active); strcat(cgiBuf, Str);
  sprintf(Str, "\"FTP_FTPServer\":\"%s\",", DevCfg->FTPCfgPkt.FTPServer); strcat(cgiBuf, Str);
  sprintf(Str, "\"FTP_FTPPort\":%d,", DevCfg->FTPCfgPkt.FTPPort); strcat(cgiBuf, Str);
  sprintf(Str, "\"FTP_Account\":\"%s\",", DevCfg->FTPCfgPkt.Account); strcat(cgiBuf, Str);
  sprintf(Str, "\"FTP_Password\":\"%s\",", DevCfg->FTPCfgPkt.Password); strcat(cgiBuf, Str);
  sprintf(Str, "\"FTP_UploadPath\":\"%s\",", DevCfg->FTPCfgPkt.UploadPath); strcat(cgiBuf, Str);
  len = strlen(cgiBuf);
  if (cgiBuf[len - 1] == ',') cgiBuf[len - 1] = 0x00;
  sprintf(Str, "}"); strcat(cgiBuf, Str);
}
//-----------------------------------------------------------------------------
void Json_cgi_GetSMTPCfg(char* cgiBuf, TDevCfg* DevCfg)//通过网页调用，取得邮件配置
{
  int len;
  char Str[1024];
  //http://192.168.0.169/cfg1.cgi?User=admin&Psd=admin&MsgID=59
  sprintf(Str, "{"); strcat(cgiBuf, Str);
  sprintf(Str, "\"SMTP_Active\":%d,", DevCfg->SMTPCfgPkt.Active); strcat(cgiBuf, Str);
  sprintf(Str, "\"SMTP_SMTPServer\":\"%s\",", DevCfg->SMTPCfgPkt.SMTPServer); strcat(cgiBuf, Str);
  sprintf(Str, "\"SMTP_SMTPPort\":%d,", DevCfg->SMTPCfgPkt.SMTPPort); strcat(cgiBuf, Str);
  sprintf(Str, "\"SMTP_Account\":\"%s\",", DevCfg->SMTPCfgPkt.Account); strcat(cgiBuf, Str);
  sprintf(Str, "\"SMTP_Password\":\"%s\",", DevCfg->FTPCfgPkt.Password); strcat(cgiBuf, Str);
  sprintf(Str, "\"SMTP_FromAddress\":\"%s\",", DevCfg->SMTPCfgPkt.FromAddress); strcat(cgiBuf, Str);
  sprintf(Str, "\"SMTP_ToAddress\":\"%s\",", DevCfg->SMTPCfgPkt.ToAddress); strcat(cgiBuf, Str);
  len = strlen(cgiBuf);
  if (cgiBuf[len - 1] == ',') cgiBuf[len - 1] = 0x00;
  sprintf(Str, "}"); strcat(cgiBuf, Str);
}
//-----------------------------------------------------------------------------
void Json_cgi_GetP2PCfg(char* cgiBuf, TDevCfg* DevCfg)//通过网页调用，取得P2P配置
{
  int len;
  char Str[1024];
  //http://192.168.0.169/cfg1.cgi?User=admin&Psd=admin&MsgID=61
  sprintf(Str, "{"); strcat(cgiBuf, Str);
  sprintf(Str, "\"P2P_Active\":%d,", DevCfg->p2pCfgPkt.Active); strcat(cgiBuf, Str);
  sprintf(Str, "\"P2P_UID\":\"%s\",", DevCfg->p2pCfgPkt.UID); strcat(cgiBuf, Str);
  //sprintf(Str, "\"P2P_Password\":\"%s\",", DevCfg->p2pCfgPkt.Password); strcat(cgiBuf, Str);
  //sprintf(Str, "\"P2P_StreamType\":%d,", DevCfg->p2pCfgPkt.StreamType); strcat(cgiBuf, Str);//20171215 del
  sprintf(Str, "\"P2P_Version\":%d,", DevCfg->p2pCfgPkt.Version); strcat(cgiBuf, Str);//20171215 del
  
  len = strlen(cgiBuf);
  if (cgiBuf[len - 1] == ',') cgiBuf[len - 1] = 0x00;
  sprintf(Str, "}"); strcat(cgiBuf, Str);
}
//-----------------------------------------------------------------------------
char* DevCfg_to_Json(TDevCfg* DevCfg)
{
  static char tmpBuf[1024*64];
  char Str[1024];
  int len;
  tmpBuf[0] = 0x00;
  //http://192.168.0.169/cfg1.cgi?User=admin&Psd=admin&MsgID=28
  sprintf(Str, "{"); strcat(tmpBuf, Str);

  sprintf(Str, "\"DevInfo\":"); strcat(tmpBuf, Str);
  Json_cgi_GetDevInfo(tmpBuf, DevCfg);
  sprintf(Str, ","); strcat(tmpBuf, Str);

  sprintf(Str, "\"Net\":"); strcat(tmpBuf, Str);
  Json_cgi_GetNetCfg(tmpBuf, DevCfg);
  sprintf(Str, ","); strcat(tmpBuf, Str);

  sprintf(Str, "\"Wifi\":"); strcat(tmpBuf, Str);
  Json_cgi_GetWiFiCfg(tmpBuf, DevCfg);
  sprintf(Str, ","); strcat(tmpBuf, Str);

  sprintf(Str, "\"User\":"); strcat(tmpBuf, Str);
  Json_cgi_GetUserLst(tmpBuf, DevCfg);
  sprintf(Str, ","); strcat(tmpBuf, Str);

  sprintf(Str, "\"Video\":"); strcat(tmpBuf, Str);
  Json_cgi_GetVideoCfg(tmpBuf, DevCfg);
  sprintf(Str, ","); strcat(tmpBuf, Str);

  sprintf(Str, "\"Audio\":"); strcat(tmpBuf, Str);
  Json_cgi_GetAudioCfg(tmpBuf, DevCfg);
  sprintf(Str, ","); strcat(tmpBuf, Str);

  sprintf(Str, "\"MD\":"); strcat(tmpBuf, Str);
  Json_cgi_GetMDCfg(tmpBuf, DevCfg);
  sprintf(Str, ","); strcat(tmpBuf, Str);

  sprintf(Str, "\"HideArea\":"); strcat(tmpBuf, Str);
  Json_cgi_GetHideArea(tmpBuf, DevCfg);
  sprintf(Str, ","); strcat(tmpBuf, Str);

  sprintf(Str, "\"Disk\":"); strcat(tmpBuf, Str);
  Json_cgi_GetDiskCfg(tmpBuf, DevCfg);
  sprintf(Str, ","); strcat(tmpBuf, Str);

  sprintf(Str, "\"Rec\":"); strcat(tmpBuf, Str);
  Json_cgi_GetRecCfg(tmpBuf, DevCfg);
  sprintf(Str, ","); strcat(tmpBuf, Str);

  sprintf(Str, "\"FTP\":"); strcat(tmpBuf, Str);
  Json_cgi_GetFTPCfg(tmpBuf, DevCfg);
  sprintf(Str, ","); strcat(tmpBuf, Str);

  sprintf(Str, "\"P2P\":"); strcat(tmpBuf, Str);
  Json_cgi_GetP2PCfg(tmpBuf, DevCfg);
  sprintf(Str, ","); strcat(tmpBuf, Str);

  sprintf(Str, "\"SMTP\":"); strcat(tmpBuf, Str);
  Json_cgi_GetSMTPCfg(tmpBuf, DevCfg);
  sprintf(Str, ","); strcat(tmpBuf, Str);

  len = strlen(tmpBuf);
  if (tmpBuf[len - 1] == ',') tmpBuf[len - 1] = 0x00;
  sprintf(Str, "}"); strcat(tmpBuf, Str);
  return tmpBuf;
}
//-----------------------------------------------------------------------------
char* NewDevCfg_to_Json(TNewDevCfg* NewDevCfg)
{
  TDevCfg DevCfg;
  memset(&DevCfg, 0, sizeof(DevCfg));
  NewDevCfg_to_DevCfg(NewDevCfg, &DevCfg);
  return DevCfg_to_Json(&DevCfg);
}
