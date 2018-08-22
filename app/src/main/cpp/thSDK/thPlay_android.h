//-----------------------------------------------------------------------------
// Author      : 朱红波
// Date        : 2017.09.14
// Version     : V 2.00
// Description : www.southipcam.com
//-----------------------------------------------------------------------------
#include "axdll.h"
#include "../include/avDecode.h"
#include "../include/libthSDK.h"

#include <EGL/egl.h>
#include <android/native_window_jni.h>
#include <GLES2/gl2.h>
#include "../avDecode/thEGL.h"

//-----------------------------------------------------------------------------
bool thOpenGL_RenderRGB565(HANDLE NetHandle)
{
  int i;
  int ret = false;
  TPlayParam *Play = (TPlayParam *) NetHandle;
  if (NetHandle == 0) return false;
  if (!Play->IsConnect) return false;
  if (Play->RenderHandle == 0) return false;

  struct timeval tm;
  struct timespec tnow;
  pthread_mutex_lock(&Play->Lock);
  gettimeofday(&tm, NULL);
  tnow.tv_sec = tm.tv_sec + 1;
  tnow.tv_nsec = tm.tv_usec * 1000;
  ret = pthread_cond_timedwait(&Play->SyncCond, &Play->Lock, &tnow);
  pthread_mutex_unlock(&Play->Lock);
  if (ret != 0) return false;

  for (i = 0; i < MAX_DSPINFO_COUNT; i++)
  {
    TDspInfo *PDspInfo = &Play->DspInfoLst[i];
    if (PDspInfo->DspHandle == NULL) continue;
    if (PDspInfo->DspRect.right - PDspInfo->DspRect.left > 0 && PDspInfo->DspRect.bottom - PDspInfo->DspRect.top > 0)
    {
      ret = thRender_Display(Play->RenderHandle, PDspInfo->DspHandle, PDspInfo->DspRect);
    }
  }
  return ret;
}

//-----------------------------------------------------------------------------
void thread_eglRender(HANDLE NetHandle)
{
  struct timeval tv1, tv2;
  int i, iPktCount, idec;
  TavNode *tmpNode;
  TavPicture *FrameV420;
  int ret;
  EGLConfig eglConf;
  EGLSurface eglWindow;
  EGLContext eglCtx;
  EGLDisplay eglDisp;
  GLuint yTextureId;
  GLuint uTextureId;
  GLuint vTextureId;
  GLuint textureSamplerHandleY, textureSamplerHandleU, textureSamplerHandleV;
  GLuint programId, aPositionHandle, aTextureCoordHandle;
  EGLint configSpec[] = {EGL_RED_SIZE, 8, EGL_GREEN_SIZE, 8, EGL_BLUE_SIZE, 8, EGL_SURFACE_TYPE, EGL_WINDOW_BIT, EGL_NONE};
  const EGLint ctxAttr[] = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE};
  float vertexData[12] = {1.0f, -1.0f, 0.0f, -1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f};
  float textureVertexData[8] = {1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};
  struct timeval tm;
  struct timespec tnow;
  int windowWidth, windowHeight;
  int ScreenWidth, ScreenHeight, left, top, viewWidth, viewHeight;

  TPlayParam *Play = (TPlayParam *) NetHandle;
  if (NetHandle == 0) return;

  while (1)
  {
    if (Play->ImgWidth > 0 && Play->ImgHeight > 0) break;
    usleep(1000 * 10);
  }
  //eglCreate
  eglDisp = eglGetDisplay(EGL_DEFAULT_DISPLAY);
  EGLint eglMajVers, eglMinVers;
  EGLint numConfigs;
  eglInitialize(eglDisp, &eglMajVers, &eglMinVers);
  eglChooseConfig(eglDisp, configSpec, &eglConf, 1, &numConfigs);
  eglWindow = eglCreateWindowSurface(eglDisp, eglConf, Play->Window, NULL);
  eglQuerySurface(eglDisp, eglWindow, EGL_WIDTH, &windowWidth);
  eglQuerySurface(eglDisp, eglWindow, EGL_HEIGHT, &windowHeight);
  eglCtx = eglCreateContext(eglDisp, eglConf, EGL_NO_CONTEXT, ctxAttr);
  eglMakeCurrent(eglDisp, eglWindow, eglWindow, eglCtx);

  programId = createProgram();
  aPositionHandle = (GLuint) glGetAttribLocation(programId, "aPosition");
  aTextureCoordHandle = (GLuint) glGetAttribLocation(programId, "aTexCoord");
  textureSamplerHandleY = (GLuint) glGetUniformLocation(programId, "yTexture");
  textureSamplerHandleU = (GLuint) glGetUniformLocation(programId, "uTexture");
  textureSamplerHandleV = (GLuint) glGetUniformLocation(programId, "vTexture");

  glUseProgram(programId);
  glEnableVertexAttribArray(aPositionHandle);
  glVertexAttribPointer(aPositionHandle, 3, GL_FLOAT, GL_FALSE, 12, vertexData);
  glEnableVertexAttribArray(aTextureCoordHandle);
  glVertexAttribPointer(aTextureCoordHandle, 2, GL_FLOAT, GL_FALSE, 8, textureVertexData);

  glGenTextures(1, &yTextureId);
  glActiveTexture(GL_TEXTURE0);
  glBindTexture(GL_TEXTURE_2D, yTextureId);
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
  glUniform1i(textureSamplerHandleY, 0);

  glGenTextures(1, &uTextureId);
  glActiveTexture(GL_TEXTURE1);
  glBindTexture(GL_TEXTURE_2D, uTextureId);
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
  glUniform1i(textureSamplerHandleU, 1);

  glGenTextures(1, &vTextureId);
  glActiveTexture(GL_TEXTURE2);
  glBindTexture(GL_TEXTURE_2D, vTextureId);
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
  glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
  glUniform1i(textureSamplerHandleV, 2);

  gettimeofday(&tv1, NULL);
  while (1)
  {
    if (Play->IsExit) break;
    if (Play->IsExitRender) break;
    if (Play->IsQueue)
    {
#ifdef ANDROID_IS_QUEUEDRAW
      iPktCount = avQueue_GetCount(Play->hQueueDraw);
      if (iPktCount == 0) continue;

      if (iPktCount > 5 && iPktCount <= 10)
      {
        tmpNode = avQueue_ReadBegin(Play->hQueueDraw);
        if (tmpNode != NULL)
        {
          if (tmpNode->Buf1 != NULL) free(tmpNode->Buf1);
        }
        avQueue_ReadEnd(Play->hQueueDraw, tmpNode);
      }
      //1
      iPktCount = avQueue_GetCount(Play->hQueueDraw);
      if (iPktCount == 0) continue;
      if (iPktCount > 10)
      {
        for (i = 0; i < 1; i++)
        {
          tmpNode = avQueue_ReadBegin(Play->hQueueDraw);
          if (tmpNode != NULL)
          {
            if (tmpNode->Buf1 != NULL) free(tmpNode->Buf1);
          }
          avQueue_ReadEnd(Play->hQueueDraw, tmpNode);
        }
      }
      //2
      tmpNode = avQueue_ReadBegin(Play->hQueueDraw);
      if (tmpNode == NULL) continue;
      if (tmpNode->Buf == NULL)
      {
        if (tmpNode->Buf1 != NULL) free(tmpNode->Buf1);
        avQueue_ReadEnd(Play->hQueueDraw, tmpNode);
        continue;
      }
#endif
      //3
#ifdef ANDROID_IS_QUEUEDRAW
      ThreadLock(&Play->Lock);
      FrameV420 = (TavPicture *) tmpNode->Buf;
#else
      ThreadLock(&Play->Lock);
      FrameV420 = &Play->FrameV420;
#endif
      if (Play->ImgWidth > 0 && Play->ImgHeight > 0)
      {
        ScreenWidth = Play->DspInfoLst[0].DspRect.right - Play->DspInfoLst[0].DspRect.left;
        ScreenHeight = Play->DspInfoLst[0].DspRect.bottom - Play->DspInfoLst[0].DspRect.top;
        if (ScreenHeight > ScreenWidth)
        {
          left = 0;
          viewWidth = ScreenWidth;
          viewHeight = (int) (Play->ImgHeight * 1.0f / Play->ImgWidth * viewWidth);
          top = (ScreenHeight - viewHeight) / 2;
        } else
        {
          top = 0;
          left = 0;
          viewHeight = ScreenHeight;
          viewWidth = ScreenWidth;
        }
        glViewport(left, top, viewWidth, viewHeight);

        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
        //eglRender
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, yTextureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, FrameV420->linesize[0], Play->ImgHeight, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE,
                     FrameV420->data[0]);

        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, uTextureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, FrameV420->linesize[1], Play->ImgHeight / 2, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE,
                     FrameV420->data[1]);

        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, vTextureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, FrameV420->linesize[2], Play->ImgHeight / 2, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE,
                     FrameV420->data[2]);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        eglSwapBuffers(eglDisp, eglWindow);
        Play->IsRenderSuccess = true;
        ThreadUnlock(&Play->Lock);
        gettimeofday(&tv2, NULL);
        idec = timeval_dec(&tv2, &tv1);
        tv1 = tv2;
        //PRINTF("iPktCount:%d idec:%03d\n", iPktCount, idec);
      }
#ifdef ANDROID_IS_QUEUEDRAW
      if (tmpNode->Buf1 != NULL) free(tmpNode->Buf1);
      avQueue_ReadEnd(Play->hQueueDraw, tmpNode);
#endif
    } else
    {
      FrameV420 = &Play->FrameV420;
      ThreadLock(&Play->Lock);
      //pthread_cond_wait(&Play->SyncCond, &Play->Lock);
      gettimeofday(&tm, NULL);
      tnow.tv_sec = tm.tv_sec + 1;
      tnow.tv_nsec = tm.tv_usec * 1000;
      ret = pthread_cond_timedwait(&Play->SyncCond, &Play->Lock, &tnow);
      ThreadUnlock(&Play->Lock);
      if (ret != 0) continue;

      ThreadLock(&Play->Lock);
      ScreenWidth = Play->DspInfoLst[0].DspRect.right - Play->DspInfoLst[0].DspRect.left;
      ScreenHeight = Play->DspInfoLst[0].DspRect.bottom - Play->DspInfoLst[0].DspRect.top;
      if (ScreenHeight > ScreenWidth)
      {
        left = 0;
        viewWidth = ScreenWidth;
        viewHeight = (int) (Play->ImgHeight * 1.0f / Play->ImgWidth * viewWidth);
        top = (ScreenHeight - viewHeight) / 2;
      } else
      {
        top = 0;
        left = 0;
        viewHeight = ScreenHeight;
        viewWidth = ScreenWidth;
      }
      glViewport(left, top, viewWidth, viewHeight);

      glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
      //eglRender
      glActiveTexture(GL_TEXTURE0);
      glBindTexture(GL_TEXTURE_2D, yTextureId);
      glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, FrameV420->linesize[0], Play->ImgHeight, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE,
                   FrameV420->data[0]);

      glActiveTexture(GL_TEXTURE1);
      glBindTexture(GL_TEXTURE_2D, uTextureId);
      glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, FrameV420->linesize[1], Play->ImgHeight / 2, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE,
                   FrameV420->data[1]);

      glActiveTexture(GL_TEXTURE2);
      glBindTexture(GL_TEXTURE_2D, vTextureId);
      glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, FrameV420->linesize[2], Play->ImgHeight / 2, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE,
                   FrameV420->data[2]);
      glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
      eglSwapBuffers(eglDisp, eglWindow);
      Play->IsRenderSuccess = true;
      ThreadUnlock(&Play->Lock);
    }
  }
  //eglFree
  eglMakeCurrent(eglDisp, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
  eglDestroyContext(eglDisp, eglCtx);
  eglDestroySurface(eglDisp, eglWindow);
  eglTerminate(eglDisp);
  eglDisp = EGL_NO_DISPLAY;
  eglWindow = EGL_NO_SURFACE;
  eglCtx = EGL_NO_CONTEXT;
}
//-----------------------------------------------------------------------------
bool thOpenGL_CreateEGL(HANDLE NetHandle, void *Window)
{
  TPlayParam *Play = (TPlayParam *) NetHandle;
  if (NetHandle == 0) return false;

  Play->Window = (ANativeWindow *) Window;
  Play->IsExitRender = false;
  Play->IsRenderSuccess = false;
  Play->thRenderEGL = ThreadCreate((void *) thread_eglRender, (HANDLE) NetHandle, false);
  return true;
}
//-----------------------------------------------------------------------------
bool thOpenGL_FreeEGL(HANDLE NetHandle)
{
  TPlayParam *Play = (TPlayParam *) NetHandle;
  if (NetHandle == 0) return false;
  ThreadLock(&Play->Lock);
  Play->IsExitRender = true;
  ThreadUnlock(&Play->Lock);
  ThreadExit(Play->thRenderEGL, 0);//1000;
  return true;
}
//-----------------------------------------------------------------------------
bool thOpenGL_IsRenderSuccess(HANDLE NetHandle)
{
  TPlayParam *Play = (TPlayParam *) NetHandle;
  if (NetHandle == 0) return false;
  return Play->IsRenderSuccess;
}
//-----------------------------------------------------------------------------

