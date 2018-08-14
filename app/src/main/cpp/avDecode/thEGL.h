#ifndef thEGL_h
#define thEGL_h
#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <assert.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/time.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <dlfcn.h>
#include <pthread.h>
extern "C"{


#include <ffmpeg/libavutil/frame.h>
#include <GLES2/gl2.h>
#include <EGL/egl.h>
#include <android/native_window_jni.h>
}

int requestInitEGL(ANativeWindow * nativeWindow,int videoWidth,int videoHeight);
int requestEGLSurfaceChanged(ANativeWindow * nativeWindow,int videoWidth,int videoHeight);
int requestEGLRender(AVFrame* Frame422);
void eglSurfaceDestory();
#endif /* thEGL_h */