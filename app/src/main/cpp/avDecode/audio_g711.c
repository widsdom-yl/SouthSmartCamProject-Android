//-----------------------------------------------------------------------------
// Author      : ÷Ï∫Ï≤®
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#include <math.h>
#include "../include/avDecode.h"
//-----------------------------------------------------------------------------
int pcm_DB(unsigned char* buf, int size)
{
  int ndb = 0;
  short value;
  int i;
  long v = 0;
  for(i=0; i<size; i+=2)
  {   
    memcpy((char*)&value, buf + i, 1); 
    memcpy((char*)&value + 1, buf + i + 1, 1); 
    v += abs(value);
  }   

  v = v / (size / 2);

  if(v != 0)
  {
    ndb = (int)(20.0*log10((double)v / 65535.0 )); 
  }   
  else
  {
    ndb = -96;
  }   

  return ndb;
}
//-----------------------------------------------------------------------------
unsigned char encode(short pcm)
{
#define MAX_32635 32635
  int exponent = 7;
  int expMask;
  int mantissa;
  unsigned char alaw;
  int sign = (pcm & 0x8000) >> 8;
  if (sign != 0) pcm = -pcm;
  if (pcm > MAX_32635) pcm = MAX_32635;
  for (expMask = 0x4000; (pcm & expMask) == 0 && exponent>0; exponent--, expMask >>= 1) {}
  mantissa = (pcm >> ((exponent == 0) ? 4 : (exponent + 3))) & 0x0f;
  alaw = (unsigned char)(sign | exponent << 4 | mantissa);
  return (unsigned char)(alaw^0xD5);
}
//-----------------------------------------------------------------------------
int g711_encode(unsigned char* dstBuf, const char* srcBuf, int srcBufLen)
{
  int i;
  short* tmpBuf = (short*)srcBuf;
  for(i=0; i<srcBufLen/2; i++) dstBuf[i] = encode(tmpBuf[i]);
  return srcBufLen/2;
}
//-----------------------------------------------------------------------------
short decode(unsigned char alaw)
{
  int sign, exponent, data;
  alaw ^= 0xD5;
  sign = alaw & 0x80;
  exponent = (alaw & 0x70) >> 4;
  data = alaw & 0x0f;
  data <<= 4;
  data += 8;
  if (exponent != 0) data += 0x100;
  if (exponent > 1) data <<= (exponent - 1);

  return (short)(sign == 0 ? data : -data);
} 
//-----------------------------------------------------------------------------
int g711_decode(char* dstBuf, const unsigned char* srcBuf, int srcBufLen)
{
  int i;
  short* tmpBuf = (short*)dstBuf;
  for(i=0; i<srcBufLen; i++) tmpBuf[i] = decode(srcBuf[i]);
  return srcBufLen*2;
}
//-----------------------------------------------------------------------------
