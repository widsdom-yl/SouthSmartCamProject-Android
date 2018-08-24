#ifndef jpeg_h
#define jpeg_h

#include <stdio.h>
#include <stdlib.h>


//-----------------------------------------------------------------------------
int rgb24_jpg(char* FileName, char* rgbBuf, int w, int h);
int bmp_jpg(char* srcFile, char* dstFile);

#endif
