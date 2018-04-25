#ifndef mtk_SmartConfig_H
#define mtk_SmartConfig_H

int InitSmartConnection(void);
int StartSmartConnection(const char* SSID, const char* Password, unsigned char* Tlv, int TlvLen, const char* Target, char AuthMode);
int StopSmartConnection(void);

#endif
