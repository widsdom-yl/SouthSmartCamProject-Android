#ifndef SOUTHSMARTCAMPROJECT_ANDROID_THEGLTEST_H
#define SOUTHSMARTCAMPROJECT_ANDROID_THEGLTEST_H

#include <jni.h>
#include <EGL/egl.h>
#include <android/native_window_jni.h>
#include <cm_types.h>

#include "thEGL.h"
#include "thffmpeg.h"

#include <malloc.h>
#include <GLES2/gl2.h>

GLuint createProgram();
void videoPlay1111(char* Path, ANativeWindow *nativeWindow);

#endif //SOUTHSMARTCAMPROJECT_ANDROID_THEGLTEST_H
