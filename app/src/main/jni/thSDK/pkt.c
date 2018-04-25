//-----------------------------------------------------------------------------
// Author      : Öìºì²¨
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------

#include "th_protocol.h"
#include "axdll.h"
//------------------------------------------------------------------------------

//-----------------------------------------------------------------------------


void main_Pkt()//i32 a, char** bbb)
{
  TCmdPkt Pkt;
  memset(&Pkt,0,sizeof(Pkt));
  //  memcpy(&PktHead,SktConnPkt->RecvBuf, 8);

  printf("--------------------------------\n");
  printf("TRect            :%4d\n", sizeof(TRect));
  printf("TGroupType       :%4d\n", sizeof(TGroupType));
  printf("TFontColor       :%4d\n", sizeof(TFontColor));
  printf("TVideoType       :%4d\n", sizeof(TVideoType));
  printf("TStandard        :%4d\n", sizeof(TStandard));
  printf("TVideoFormat     :%4d\n", sizeof(TVideoFormat));
  printf("TVideoCfgPkt     :%4d\n", sizeof(TVideoCfgPkt));
  printf("TAudioType       :%4d\n", sizeof(TAudioType));
  printf("TAudioFormat     :%4d\n", sizeof(TAudioFormat));
  printf("TAudioCfgPkt     :%4d\n", sizeof(TAudioCfgPkt));
  printf("TRecFileIdx      :%4d\n", sizeof(TRecFileIdx));
  printf("TRecFileHead     :%4d\n", sizeof(TRecFileHead));
  printf("TFilePkt         :%4d\n", sizeof(TFilePkt));
  printf("TDataFrameInfo   :%4d\n", sizeof(TDataFrameInfo));
  printf("TAlmType         :%4d\n", sizeof(TAlmType));
  printf("TAlmSendPkt      :%4d\n", sizeof(TAlmSendPkt));
  printf("TDoControlPkt    :%4d\n", sizeof(TDoControlPkt));
  printf("TMDCfgPkt        :%4d\n", sizeof(TMDCfgPkt));

  printf("TAlmCfgItem      :%4d\n", sizeof(TAlmCfgItem));
  printf("TAlmCfgPkt       :%4d\n", sizeof(TAlmCfgPkt));
  printf("TUserCfgPkt      :%4d\n", sizeof(TUserCfgPkt));
  printf("TPTZCmd          :%4d\n", sizeof(TPTZCmd));
  printf("TPTZProtocol     :%4d\n", sizeof(TPTZProtocol));
  printf("TPTZPkt          :%4d\n", sizeof(TPTZPkt));
  printf("TLoginPkt        :%4d\n", sizeof(TLoginPkt));
  printf("TPlayLivePkt     :%4d\n", sizeof(TPlayLivePkt));
  printf("TPlayBackPkt     :%4d\n", sizeof(TPlayBackPkt));
  printf("TPlayCtrl        :%4d\n", sizeof(TPlayCtrl));
  printf("TRecFilePkt      :%4d\n", sizeof(TRecFilePkt));
  printf("TPlanRecPkt      :%4d\n", sizeof(TPlanRecPkt));
  printf("TRecCfgPkt       :%4d\n", sizeof(TRecCfgPkt));
  printf("THideAreaCfgPkt  :%4d\n", sizeof(THideAreaCfgPkt));
  printf("TAxInfo          :%4d\n", sizeof(TAxInfo));
  printf("TDevInfoPkt      :%4d\n", sizeof(TDevInfoPkt));
  printf("TNetCfgPkt       :%4d\n", sizeof(TNetCfgPkt));
  printf("TWiFiSearchPkt   :%4d\n", sizeof(TWiFiSearchPkt));
  printf("TWiFiCfgPkt      :%4d\n", sizeof(TWiFiCfgPkt));
  printf("TMulticastInfoPkt:%4d\n", sizeof(TMulticastInfoPkt));
  printf("THeadPkt         :%4d\n", sizeof(THeadPkt));
  printf("TMsgID           :%4d\n", sizeof(TMsgID));
  printf("TCmdPkt          :%4d\n", sizeof(TCmdPkt));
  printf("TNetCmdPkt       :%4d\n", sizeof(TNetCmdPkt));

  printf("TRecCfgPkt       :%4d\n", sizeof(TRecCfgPkt));
  printf("TDiskCfgPkt      :%4d\n", sizeof(TDiskCfgPkt));

  printf("TFTPCfgPkt       :%4d\n", sizeof(TFTPCfgPkt));
  printf("Tp2pCfgPkt       :%4d\n", sizeof(Tp2pCfgPkt));
  printf("TSMTPCfgPkt      :%4d\n", sizeof(TSMTPCfgPkt));

  /*
  printf("*********************\n");
  printf("TdecHeadPkt      :%4d\n", sizeof(TdecHeadPkt));
  printf("TencDevInfo      :%4d\n", sizeof(TencDevInfo));
  printf("TPlayItem        :%4d\n", sizeof(TPlayItem));
  printf("TdecPlayCfg      :%4d\n", sizeof(TdecPlayCfg));
  printf("TdecLoginPkt     :%4d\n", sizeof(TdecLoginPkt));
  printf("TdecSearchPkt    :%4d\n", sizeof(TdecSearchPkt));
  */


  printf("--------------------------------\n");
  printf("--------------------------------\n");
  printf("TDevCfg          :%4d\n", sizeof(TDevCfg));
  printf("--------------------------------\n");
}

i32 main(i32 argc, char** argv)
{

  main_Pkt();
  return 1;
}
