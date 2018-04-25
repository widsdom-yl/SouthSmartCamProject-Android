#-----------------------------------------------------------------------------
# Author      : ÷Ï∫Ï≤®
# Date        : 2013.04.20
# Version     : V 2.00
# Description :
#-----------------------------------------------------------------------------
#CORSS_COMPILE=arm-linux-
CC=$(CORSS_COMPILE)gcc
AR=$(CORSS_COMPILE)ar
STRIP=$(CORSS_COMPILE)strip
LD=$(CORSS_COMPILE)ld

Filelib= \
TFun.o skt.o audio_g711.o thSDKlib.o

LibName = thSDK
ExeName = demo
Paramlib = -s -lpthread -lm -lrt -l$(LibName) -L.

all: 
	make lib
	
lib: $(Filelib)
	$(AR) -cvr ../lib$(LibName).a $(Filelib)

dll: $(Filelib)
	$(CC) -shared -o ../lib$(LibName).so $(Filelib)	
	$(STRIP) -s ../lib$(LibName).so
	rm -rf *.o
	
clean: 
	rm -f *.o


	
