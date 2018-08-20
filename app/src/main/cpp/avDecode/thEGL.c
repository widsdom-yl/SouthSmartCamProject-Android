#include <jni.h>
#include <EGL/egl.h>
#include <android/native_window_jni.h>
#include <cm_types.h>

#include "thEGL.h"
#include "thffmpeg.h"

#include <malloc.h>
#include <GLES2/gl2.h>
//-----------------------------------------------------------------------------
#define GET_STR(x) #x
const char *vertexShaderString1 = GET_STR(attribute
                                            vec4 aPosition;
                                            attribute
                                            vec2 aTexCoord;
                                            varying
                                            vec2 vTexCoord;
                                            void main()
                                            {
                                              vTexCoord = vec2(aTexCoord.x, 1.0 - aTexCoord.y);
                                              gl_Position = aPosition;
                                            });

const char *fragmentShaderString1 = GET_STR(precision
                                              mediump float;
                                              varying
                                              vec2 vTexCoord;
                                              uniform
                                              sampler2D yTexture;
                                              uniform
                                              sampler2D uTexture;
                                              uniform
                                              sampler2D vTexture;
                                              void main()
                                              {
                                                vec3 yuv;
                                                vec3 rgb;
                                                yuv.r = texture2D(yTexture, vTexCoord).r;
                                                yuv.g = texture2D(uTexture, vTexCoord).r - 0.5;
                                                yuv.b = texture2D(vTexture, vTexCoord).r - 0.5;
                                                rgb =
                                                  mat3(1.0, 1.0, 1.0, 0.0, -0.39465, 2.03211, 1.13983, -0.58060, 0.0) *
                                                  yuv;
                                                gl_FragColor = vec4(rgb, 1.0);
                                              });

//-----------------------------------------------------------------------------
GLuint loadShader(GLenum shaderType, const char *source)
{
  GLuint shader = glCreateShader(shaderType);
  if (shader != 0)
  {
    glShaderSource(shader, 1, &source, NULL);
    glCompileShader(shader);
    GLint compiled = 0;
    glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
    if (!compiled)
    {
      GLint info_length = 0;
      glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &info_length);
      if (info_length)
      {
        char *buf = (char *) malloc(info_length * sizeof(char));
        if (buf)
        {
          glGetShaderInfoLog(shader, info_length, NULL, buf);
        }
        free(buf);
      }
      glDeleteShader(shader);
      shader = 0;
    }
  }
  return shader;
}

//-----------------------------------------------------------------------------
GLuint createProgram()
{
  GLuint vertexShader = loadShader(GL_VERTEX_SHADER, vertexShaderString1);
  if (!vertexShader)
  {
    return 0;
  }
  GLuint pixelShader = loadShader(GL_FRAGMENT_SHADER, fragmentShaderString1);
  if (!pixelShader)
  {
    return 0;
  }

  GLuint program = glCreateProgram();
  if (program != 0)
  {
    glAttachShader(program, vertexShader);
    glAttachShader(program, pixelShader);
    glLinkProgram(program);
    GLint linkStatus = 0;
    glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
    if (!linkStatus)
    {
      GLint info_length = 0;
      glGetProgramiv(program, GL_INFO_LOG_LENGTH, &info_length);
      if (info_length)
      {
        char *buf = (char *) malloc(info_length * sizeof(char));
        glGetProgramInfoLog(program, info_length, NULL, buf);
        free(buf);
      }
      glDeleteProgram(program);
      program = 0;
    }
  }
  return program;
}

//-----------------------------------------------------------------------------
EGLConfig eglConf;
EGLSurface eglWindow;
EGLContext eglCtx;
EGLDisplay eglDisp;
GLuint yTextureId;
GLuint uTextureId;
GLuint vTextureId;
AVFrame Frame420;
ANativeWindow *eglnativeWindow;

bool eglCreate(int ImgWidth, int ImgHeight)
{
  EGLint configSpec[] = {EGL_RED_SIZE, 8, EGL_GREEN_SIZE, 8, EGL_BLUE_SIZE, 8, EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
                         EGL_NONE};

  int windowWidth, windowHeight;
  eglDisp = eglGetDisplay(EGL_DEFAULT_DISPLAY);
  EGLint eglMajVers, eglMinVers;
  EGLint numConfigs;
  eglInitialize(eglDisp, &eglMajVers, &eglMinVers);
  eglChooseConfig(eglDisp, configSpec, &eglConf, 1, &numConfigs);
  eglWindow = eglCreateWindowSurface(eglDisp, eglConf, eglnativeWindow, NULL);
  eglQuerySurface(eglDisp, eglWindow, EGL_WIDTH, &windowWidth);
  eglQuerySurface(eglDisp, eglWindow, EGL_HEIGHT, &windowHeight);
  const EGLint ctxAttr[] = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE};
  eglCtx = eglCreateContext(eglDisp, eglConf, EGL_NO_CONTEXT, ctxAttr);
  eglMakeCurrent(eglDisp, eglWindow, eglWindow, eglCtx);

  GLuint programId = createProgram();
  GLuint aPositionHandle = (GLuint) glGetAttribLocation(programId, "aPosition");
  GLuint aTextureCoordHandle = (GLuint) glGetAttribLocation(programId, "aTexCoord");

  GLuint textureSamplerHandleY = (GLuint) glGetUniformLocation(programId, "yTexture");
  GLuint textureSamplerHandleU = (GLuint) glGetUniformLocation(programId, "uTexture");
  GLuint textureSamplerHandleV = (GLuint) glGetUniformLocation(programId, "vTexture");

  int left, top, viewWidth, viewHeight;
  if (windowHeight > windowWidth)
  {
    left = 0;
    viewWidth = windowWidth;
    viewHeight = (int) (ImgHeight * 1.0f / ImgWidth * viewWidth);
    top = (windowHeight - viewHeight) / 2;
  } else
  {
    top = 0;
    viewHeight = windowHeight;
    viewWidth = (int) (ImgWidth * 1.0f / ImgHeight * viewHeight);
    left = (windowWidth - viewWidth) / 2;
  }
  glViewport(left, top, viewWidth, viewHeight);

  glUseProgram(programId);
  glEnableVertexAttribArray(aPositionHandle);
  float vertexData[12] = {1.0f, -1.0f, 0.0f, -1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f};
  glVertexAttribPointer(aPositionHandle, 3, GL_FLOAT, GL_FALSE, 12, vertexData);
  glEnableVertexAttribArray(aTextureCoordHandle);
  float textureVertexData[8] = {1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};
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
  return true;
}

//-----------------------------------------------------------------------------
bool eglRender()
{
  glActiveTexture(GL_TEXTURE0);
  glBindTexture(GL_TEXTURE_2D, yTextureId);
  glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, Frame420.linesize[0], Frame420.height, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE,
               Frame420.data[0]);

  glActiveTexture(GL_TEXTURE1);
  glBindTexture(GL_TEXTURE_2D, uTextureId);
  glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, Frame420.linesize[1], Frame420.height / 2, 0, GL_LUMINANCE,
               GL_UNSIGNED_BYTE, Frame420.data[1]);

  glActiveTexture(GL_TEXTURE2);
  glBindTexture(GL_TEXTURE_2D, vTextureId);
  glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, Frame420.linesize[2], Frame420.height / 2, 0, GL_LUMINANCE,
               GL_UNSIGNED_BYTE, Frame420.data[2]);

  glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
  glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
  eglSwapBuffers(eglDisp, eglWindow);
  return true;
}
//-----------------------------------------------------------------------------
bool eglFree()
{
  eglMakeCurrent(eglDisp, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
  eglDestroyContext(eglDisp, eglCtx);
  eglDestroySurface(eglDisp, eglWindow);
  eglTerminate(eglDisp);
  eglDisp = EGL_NO_DISPLAY;
  eglWindow = EGL_NO_SURFACE;
  eglCtx = EGL_NO_CONTEXT;
  return true;
}

//-----------------------------------------------------------------------------
void videoPlay1111(char *Path, ANativeWindow *nativeWindow)
{
  av_register_all();
  AVFormatContext *fmt_ctx = avformat_alloc_context();
  if (avformat_open_input(&fmt_ctx, Path, NULL, NULL) < 0)
  {
    return;
  }
  if (avformat_find_stream_info(fmt_ctx, NULL) < 0)
  {
    return;
  }
  AVStream *avStream = NULL;
  int video_stream_index = -1;
  for (int i = 0; i < fmt_ctx->nb_streams; i++)
  {

    if (fmt_ctx->streams[i]->codec->codec_type == AVMEDIA_TYPE_VIDEO)
    {
      avStream = fmt_ctx->streams[i];
      video_stream_index = i;
      break;
    }
  }
  if (video_stream_index == -1)
  {
    return;
  }

  ///查找解码器
  AVCodecContext *codec_ctx = fmt_ctx->streams[video_stream_index]->codec;

  AVCodec *avCodec = avcodec_find_decoder(codec_ctx->codec_id);
  if (avcodec_open2(codec_ctx, avCodec, NULL) < 0)
  {
    return;
  }
  int y_size = codec_ctx->width * codec_ctx->height;
  AVPacket *pkt = (AVPacket *) malloc(sizeof(AVPacket));
  av_new_packet(pkt, y_size);
#define IS_EGLTEST

  eglnativeWindow = nativeWindow;

#ifdef IS_EGLTEST
  eglCreate(codec_ctx->width, codec_ctx->height);
#else
  int ImgWidth = codec_ctx->width;
  int ImgHeight = codec_ctx->height;
  EGLint configSpec[] = {EGL_RED_SIZE, 8, EGL_GREEN_SIZE, 8, EGL_BLUE_SIZE, 8, EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
                         EGL_NONE};

  int windowWidth, windowHeight;
  eglDisp = eglGetDisplay(EGL_DEFAULT_DISPLAY);
  EGLint eglMajVers, eglMinVers;
  EGLint numConfigs;
  eglInitialize(eglDisp, &eglMajVers, &eglMinVers);
  eglChooseConfig(eglDisp, configSpec, &eglConf, 1, &numConfigs);
  eglWindow = eglCreateWindowSurface(eglDisp, eglConf, eglnativeWindow, NULL);
  eglQuerySurface(eglDisp, eglWindow, EGL_WIDTH, &windowWidth);
  eglQuerySurface(eglDisp, eglWindow, EGL_HEIGHT, &windowHeight);
  const EGLint ctxAttr[] = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE};
  eglCtx = eglCreateContext(eglDisp, eglConf, EGL_NO_CONTEXT, ctxAttr);
  eglMakeCurrent(eglDisp, eglWindow, eglWindow, eglCtx);

  GLuint programId = createProgram();
  GLuint aPositionHandle = (GLuint) glGetAttribLocation(programId, "aPosition");
  GLuint aTextureCoordHandle = (GLuint) glGetAttribLocation(programId, "aTexCoord");

  GLuint textureSamplerHandleY = (GLuint) glGetUniformLocation(programId, "yTexture");
  GLuint textureSamplerHandleU = (GLuint) glGetUniformLocation(programId, "uTexture");
  GLuint textureSamplerHandleV = (GLuint) glGetUniformLocation(programId, "vTexture");

  int left, top, viewWidth, viewHeight;
  if (windowHeight > windowWidth)
  {
    left = 0;
    viewWidth = windowWidth;
    viewHeight = (int) (ImgHeight * 1.0f / ImgWidth * viewWidth);
    top = (windowHeight - viewHeight) / 2;
  } else
  {
    top = 0;
    viewHeight = windowHeight;
    viewWidth = (int) (ImgWidth * 1.0f / ImgHeight * viewHeight);
    left = (windowWidth - viewWidth) / 2;
  }
  glViewport(left, top, viewWidth, viewHeight);

  glUseProgram(programId);
  glEnableVertexAttribArray(aPositionHandle);
  float vertexData[12] = {1.0f, -1.0f, 0.0f, -1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f};
  glVertexAttribPointer(aPositionHandle, 3, GL_FLOAT, GL_FALSE, 12, vertexData);
  glEnableVertexAttribArray(aTextureCoordHandle);
  float textureVertexData[8] = {1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f};
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
#endif

  int ret, got_picture;

  //Frame420 = av_frame_alloc();
  while (av_read_frame(fmt_ctx, pkt) >= 0)
  {
    if (pkt->stream_index == video_stream_index)
    {
      ret = avcodec_decode_video2(codec_ctx, &Frame420, &got_picture, pkt);

      if (ret < 0)
      {

        return;
      }

      if (got_picture)
      {
#ifdef IS_EGLTEST
        eglRender();
#else
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, yTextureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, Frame420.linesize[0], Frame420.height, 0, GL_LUMINANCE, GL_UNSIGNED_BYTE,
                     Frame420.data[0]);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, uTextureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, Frame420.linesize[1], Frame420.height / 2, 0, GL_LUMINANCE,
                     GL_UNSIGNED_BYTE, Frame420.data[1]);
        glActiveTexture(GL_TEXTURE2);
        glBindTexture(GL_TEXTURE_2D, vTextureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, Frame420.linesize[2], Frame420.height / 2, 0, GL_LUMINANCE,
                     GL_UNSIGNED_BYTE, Frame420.data[2]);
        glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
        glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
        eglSwapBuffers(eglDisp, eglWindow);
#endif
      }
    }
    //av_free_packet(packet);
  }
#ifdef IS_EGLTEST
  eglFree();
#else
  eglMakeCurrent(eglDisp, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
  eglDestroyContext(eglDisp, eglCtx);
  eglDestroySurface(eglDisp, eglWindow);
  eglTerminate(eglDisp);
  eglDisp = EGL_NO_DISPLAY;
  eglWindow = EGL_NO_SURFACE;
  eglCtx = EGL_NO_CONTEXT;
#endif
  avcodec_close(codec_ctx);
  avformat_close_input(&fmt_ctx);

}

