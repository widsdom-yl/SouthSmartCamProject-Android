#include "../include/TFun.h"

struct timeval tv0, tv1;

void WINAPI mmtimerCallBack(HANDLE mmHandle, u32 uMsg, void* dwUser, u32 dw1, u32 dw2)
{
  int idec;
  gettimeofday(&tv1, NULL);
  idec = timeval_dec(&tv1, &tv0);
  tv0 = tv1;
  printf("mmHandle:%d idec:%d uMsg:%d dwUser:%p\n", mmHandle, idec, uMsg, dwUser);
}

int main()
{
  HANDLE mmHandle;
  mmHandle = mmTimeSetEvent(33, mmtimerCallBack, (void*)11);
  mmTimeKillEvent(mmHandle);

  mmHandle = mmTimeSetEvent(33, mmtimerCallBack, (void*)11);
  mmTimeKillEvent(mmHandle);
  mmHandle = mmTimeSetEvent(33, mmtimerCallBack, (void*)11);
  mmTimeKillEvent(mmHandle);
  mmHandle = mmTimeSetEvent(33, mmtimerCallBack, (void*)11);
  mmTimeKillEvent(mmHandle);

  mmHandle = mmTimeSetEvent(33, mmtimerCallBack, (void*)11);
  while (1)
  {
    usleep(1000*10);
  }
  mmTimeKillEvent(mmHandle);
  printf("2222222222222222\n");
  return 1;
}
