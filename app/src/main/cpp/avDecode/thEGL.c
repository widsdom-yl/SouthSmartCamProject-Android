#include <cm_types.h>
#include "thEGL.h"
#include "shaderUtils.h"




ANativeWindow * mWindow;
int initEGL;

pthread_mutex_t th_mutex_lock;

GLuint yTextureId;
GLuint uTextureId;
GLuint vTextureId;

static EGLConfig eglConf;
static EGLSurface eglWindow;
static EGLContext eglCtx;
static EGLDisplay eglDisp;

int left,top,viewWidth,viewHeight;

enum RenderEvent {
    RE_NONE,
    RE_SURFACE_CREATED,
    RE_SURFACE_CHANGED,
    RE_DRAW_FRAME,
    RE_EXIT
};


enum RenderEvent mEnumRenderEvent;

#define GET_STR(x) #x
const char *vertexShaderString = GET_STR(
                                         attribute vec4 aPosition;
                                         attribute vec2 aTexCoord;
                                         varying vec2 vTexCoord;
                                         void main() {
                                             vTexCoord=vec2(aTexCoord.x,1.0-aTexCoord.y);
                                             gl_Position = aPosition;
                                         }
                                         );
const char *fragmentShaderString = GET_STR(
                                           precision mediump float;
                                           varying vec2 vTexCoord;
                                           uniform sampler2D yTexture;
                                           uniform sampler2D uTexture;
                                           uniform sampler2D vTexture;
                                           void main() {
                                               vec3 yuv;
                                               vec3 rgb;
                                               yuv.r = texture2D(yTexture, vTexCoord).r;
                                               yuv.g = texture2D(uTexture, vTexCoord).r - 0.5;
                                               yuv.b = texture2D(vTexture, vTexCoord).r - 0.5;
                                               rgb = mat3(1.0,       1.0,         1.0,
                                                          0.0,       -0.39465,  2.03211,
                                                          1.13983, -0.58060,  0.0) * yuv;
                                               gl_FragColor = vec4(rgb, 1.0);
                                           }
                                           );



//-------------------------------------------------------------------------

int requestEGLRenderFrame(AVFrame* Frame422,int videoWidth,int videoHeight)
{


    LOGE("requestEGLRenderFrame,wid is %d,height is %d,%s(%d)\n",videoWidth,videoHeight, __FUNCTION__, __LINE__);
    //pthread_mutex_lock(&th_mutex_lock);

    switch (mEnumRenderEvent){
        case  RE_SURFACE_CREATED:
            LOGE("RE_SURFACE_CREATED,%s(%d)\n", __FUNCTION__, __LINE__);
            requestInitEGL(mWindow,videoWidth,videoHeight);
            initEGL = 1;
            mEnumRenderEvent = RE_NONE;
            break;
        case  RE_SURFACE_CHANGED:

            LOGE("RE_SURFACE_CHANGED,%s(%d)\n", __FUNCTION__, __LINE__);
            eglSurfaceDestory();
            requestEGLSurfaceChanged(mWindow,videoWidth,videoHeight);
            initEGL = 1;
            mEnumRenderEvent = RE_NONE;
            break;
        case RE_EXIT:
            mEnumRenderEvent = RE_NONE;
            eglSurfaceDestory();
            //pthread_mutex_unlock(&th_mutex_lock);
            return 0;
        default:
            break;

    }


    if (initEGL == 0){
        return 0;
    }
    LOGE("requestEGLRenderFrame,wid is %d,height is %d,%s(%d)\n",videoWidth,videoHeight, __FUNCTION__, __LINE__);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D, yTextureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, Frame422->linesize[0], Frame422->height,0, GL_LUMINANCE, GL_UNSIGNED_BYTE, Frame422->data[0]);
    
    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D, uTextureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE, Frame422->linesize[1], Frame422->height/2,0, GL_LUMINANCE, GL_UNSIGNED_BYTE, Frame422->data[1]);
    
    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D, vTextureId);
    glTexImage2D(GL_TEXTURE_2D, 0, GL_LUMINANCE,  Frame422->linesize[2], Frame422->height/2,0, GL_LUMINANCE, GL_UNSIGNED_BYTE, Frame422->data[2]);
    
    
    /***
     * 纹理更新完成后开始绘制
     ***/
    glClear(GL_DEPTH_BUFFER_BIT | GL_COLOR_BUFFER_BIT);
    
    glDrawArrays(GL_TRIANGLE_STRIP, 0, 4);
    
    eglSwapBuffers(eglDisp, eglWindow);
    
  //  av_free(&yuvFrame);
    
  // pthread_mutex_unlock(&th_mutex_lock);
    
   

}



int requestInitEGL(ANativeWindow * nativeWindow,int videoWidth,int videoHeight){
    /**
     *初始化egl
     **/
    
    //pthread_mutex_lock(&th_mutex_lock);




    
    //ANativeWindow *nativeWindow = ANativeWindow_fromSurface(env, surface);
    
    
    EGLint configSpec[] =
    {
        EGL_RED_SIZE, 8,
        
        EGL_GREEN_SIZE, 8,
        
        EGL_BLUE_SIZE, 8,
        
        EGL_ALPHA_SIZE, 8,
        
        EGL_BUFFER_SIZE, 16,
        
        EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
        
        EGL_DEPTH_SIZE, 16,
        
        EGL_STENCIL_SIZE, 8,
        
        EGL_SAMPLE_BUFFERS, 0,
        
        EGL_SAMPLES, 0,
#ifdef _IRR_COMPILE_WITH_OGLES1_
        EGL_RENDERABLE_TYPE, EGL_OPENGL_ES_BIT,
#else
        EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
#endif
        EGL_NONE, 0
    };

    
    
//    EGLint configSpec[] = { EGL_RED_SIZE, 8,
//        EGL_GREEN_SIZE, 8,
//        EGL_BLUE_SIZE, 8,
//        EGL_SURFACE_TYPE, EGL_WINDOW_BIT, EGL_NONE };
    
    eglDisp = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    EGLint eglMajVers, eglMinVers;
    EGLint numConfigs;
    eglInitialize(eglDisp, &eglMajVers, &eglMinVers);
    eglChooseConfig(eglDisp, configSpec, &eglConf, 1, &numConfigs);
    
    eglWindow = eglCreateWindowSurface(eglDisp, eglConf,nativeWindow, NULL);
    
   
    const EGLint ctxAttr[] = {
        EGL_CONTEXT_CLIENT_VERSION, 2,
        EGL_NONE
    };
    eglCtx = eglCreateContext(eglDisp, eglConf,EGL_NO_CONTEXT, ctxAttr);
    
    
    
    eglMakeCurrent(eglDisp, eglWindow, eglWindow, eglCtx);
    
   
    /**
     * 设置opengl 要在egl初始化后进行
     * **/
    float vertexData[12] = {1.0f, -1.0f, 0.0f,-1.0f, -1.0f, 0.0f,1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f};
    
    float textureVertexData[8] = {
        1.0f, 0.0f,//右下
        0.0f, 0.0f,//左下
        1.0f, 1.0f,//右上
        0.0f, 1.0f//左上
    };


//    ShaderUtils *shaderUtils = new ShaderUtils();

    GLuint programId = createProgram(vertexShaderString,fragmentShaderString );
//    delete shaderUtils;

    GLuint aPositionHandle = (GLuint) glGetAttribLocation(programId, "aPosition");
    GLuint aTextureCoordHandle = (GLuint) glGetAttribLocation(programId, "aTexCoord");
    
    GLuint textureSamplerHandleY = (GLuint) glGetUniformLocation(programId, "yTexture");
    GLuint textureSamplerHandleU = (GLuint) glGetUniformLocation(programId, "uTexture");
    GLuint textureSamplerHandleV = (GLuint) glGetUniformLocation(programId, "vTexture");
    
    int windowWidth;
    int windowHeight;
    eglQuerySurface(eglDisp,eglWindow,EGL_WIDTH,&windowWidth);
    eglQuerySurface(eglDisp,eglWindow,EGL_HEIGHT,&windowHeight);
//    LOGE("window width is %d, height is %d ,%s(%d) \n",windowWidth,windowHeight, __FUNCTION__, __LINE__);
    

    
    if(windowHeight > windowWidth){
        left = 0;
        viewWidth = windowWidth;
        viewHeight = (int)(videoHeight*1.0f/videoWidth*viewWidth);
        top = (windowHeight - viewHeight)/2;
    }else{
        top = 0;
        viewHeight = windowHeight;
        viewWidth = (int)(videoWidth*1.0f/videoHeight*viewHeight);
        left = (windowWidth - viewWidth)/2;
    }
//     LOGE("left is %d, top is %d ,viewWidth is %d,viewheight is %d,%s(%d) \n",left,top,viewWidth,viewHeight, __FUNCTION__, __LINE__);
    glViewport(left, top, viewWidth, viewHeight);
    //glViewport(0, 0, windowWidth, windowHeight);
    
    
    glDisableVertexAttribArray(aPositionHandle);
    glDisableVertexAttribArray(aTextureCoordHandle);
    glUseProgram(programId);
    glEnableVertexAttribArray(aPositionHandle);
    glVertexAttribPointer(aPositionHandle, 3, GL_FLOAT, GL_FALSE,
                          12, vertexData);
    glEnableVertexAttribArray(aTextureCoordHandle);
    glVertexAttribPointer(aTextureCoordHandle,2,GL_FLOAT,GL_FALSE,8,textureVertexData);
    /***
     * 初始化空的yuv纹理
     * **/
   
    glGenTextures(1,&yTextureId);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D,yTextureId);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,
                    GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    
    glUniform1i(textureSamplerHandleY,0);
    
    glGenTextures(1,&uTextureId);
    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D,uTextureId);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,
                    GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    
    glUniform1i(textureSamplerHandleU,1);
    
    glGenTextures(1,&vTextureId);
    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D,vTextureId);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,
                    GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    
    glUniform1i(textureSamplerHandleV,2);
//pthread_mutex_unlock(&th_mutex_lock);
    return 1;
}
int requestEGLSurfaceChanged(ANativeWindow * nativeWindow,int videoWidth,int videoHeight){
    
   // pthread_mutex_lock(&th_mutex_lock);


    eglSurfaceDestory();
    

//    LOGE("jopenglInit begin");
    

    EGLint configSpec[] =
    {
        EGL_RED_SIZE, 8,
        
        EGL_GREEN_SIZE, 8,
        
        EGL_BLUE_SIZE, 8,
        
        EGL_ALPHA_SIZE, 8,
        
        EGL_BUFFER_SIZE, 16,
        
        EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
        
        EGL_DEPTH_SIZE, 16,
        
        EGL_STENCIL_SIZE, 8,
        
        EGL_SAMPLE_BUFFERS, 0,
        
        EGL_SAMPLES, 0,
#ifdef _IRR_COMPILE_WITH_OGLES1_
        EGL_RENDERABLE_TYPE, EGL_OPENGL_ES_BIT,
#else
        EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
#endif
        EGL_NONE, 0
    };
    
    
    
    //    EGLint configSpec[] = { EGL_RED_SIZE, 8,
    //        EGL_GREEN_SIZE, 8,
    //        EGL_BLUE_SIZE, 8,
    //        EGL_SURFACE_TYPE, EGL_WINDOW_BIT, EGL_NONE };
    
    eglDisp = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    EGLint eglMajVers, eglMinVers;
    EGLint numConfigs;
    eglInitialize(eglDisp, &eglMajVers, &eglMinVers);
    eglChooseConfig(eglDisp, configSpec, &eglConf, 1, &numConfigs);
    
    eglWindow = eglCreateWindowSurface(eglDisp, eglConf,nativeWindow, NULL);
    
    
    const EGLint ctxAttr[] = {
        EGL_CONTEXT_CLIENT_VERSION, 2,
        EGL_NONE
    };
    eglCtx = eglCreateContext(eglDisp, eglConf,EGL_NO_CONTEXT, ctxAttr);
    
    
    
    eglMakeCurrent(eglDisp, eglWindow, eglWindow, eglCtx);
    
    
    /**
     * 设置opengl 要在egl初始化后进行
     * **/
    float vertexData[12] = {1.0f, -1.0f, 0.0f,-1.0f, -1.0f, 0.0f,1.0f, 1.0f, 0.0f, -1.0f, 1.0f, 0.0f};

    float textureVertexData[8] = {
            1.0f, 0.0f,//右下
            0.0f, 0.0f,//左下
            1.0f, 1.0f,//右上
            0.0f, 1.0f//左上
    };
//    ShaderUtils *shaderUtils = new ShaderUtils();
    
    GLuint programId = createProgram(vertexShaderString,fragmentShaderString );
//    delete shaderUtils;
    GLuint aPositionHandle = (GLuint) glGetAttribLocation(programId, "aPosition");
    GLuint aTextureCoordHandle = (GLuint) glGetAttribLocation(programId, "aTexCoord");
    
    GLuint textureSamplerHandleY = (GLuint) glGetUniformLocation(programId, "yTexture");
    GLuint textureSamplerHandleU = (GLuint) glGetUniformLocation(programId, "uTexture");
    GLuint textureSamplerHandleV = (GLuint) glGetUniformLocation(programId, "vTexture");
    
    int windowWidth;
    int windowHeight;
    eglQuerySurface(eglDisp,eglWindow,EGL_WIDTH,&windowWidth);
    eglQuerySurface(eglDisp,eglWindow,EGL_HEIGHT,&windowHeight);
//    LOGE("window width is %d, height is %d ,%s(%d) \n",windowWidth,windowHeight, __FUNCTION__, __LINE__);
    
    //因为没有用矩阵所以就手动自适应

    
    if(windowHeight > windowWidth){
        left = 0;
        viewWidth = windowWidth;
        viewHeight = (int)(videoHeight*1.0f/videoWidth*viewWidth);
        top = (windowHeight - viewHeight)/2;
    }else{
        top = 0;
        viewHeight = windowHeight;
        viewWidth = (int)(videoWidth*1.0f/videoHeight*viewHeight);
        left = (windowWidth - viewWidth)/2;
    }
//    LOGE("left is %d, top is %d ,viewWidth is %d,viewheight is %d,%s(%d) \n",left,top,viewWidth,viewHeight, __FUNCTION__, __LINE__);
    glViewport(left, top, viewWidth, viewHeight);
    //glViewport(0, 0, windowWidth, windowHeight);
    
    
    glDisableVertexAttribArray(aPositionHandle);
    glDisableVertexAttribArray(aTextureCoordHandle);
    glUseProgram(programId);
    glEnableVertexAttribArray(aPositionHandle);
    glVertexAttribPointer(aPositionHandle, 3, GL_FLOAT, GL_FALSE,
                          12, vertexData);
    glEnableVertexAttribArray(aTextureCoordHandle);
    glVertexAttribPointer(aTextureCoordHandle,2,GL_FLOAT,GL_FALSE,8,textureVertexData);
    /***
     * 初始化空的yuv纹理
     * **/
    
    glGenTextures(1,&yTextureId);
    glActiveTexture(GL_TEXTURE0);
    glBindTexture(GL_TEXTURE_2D,yTextureId);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,
                    GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    
    glUniform1i(textureSamplerHandleY,0);
    
    glGenTextures(1,&uTextureId);
    glActiveTexture(GL_TEXTURE1);
    glBindTexture(GL_TEXTURE_2D,uTextureId);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,
                    GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    
    glUniform1i(textureSamplerHandleU,1);
    
    glGenTextures(1,&vTextureId);
    glActiveTexture(GL_TEXTURE2);
    glBindTexture(GL_TEXTURE_2D,vTextureId);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,
                    GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    
    glUniform1i(textureSamplerHandleV,2);
   // pthread_mutex_unlock(&th_mutex_lock);
}

void eglSurfaceDestory(){
    eglMakeCurrent(eglDisp, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
    eglDestroyContext(eglDisp, eglCtx);
    eglDestroySurface(eglDisp, eglWindow);
    eglTerminate(eglDisp);
    eglDisp = EGL_NO_DISPLAY;
    eglWindow = EGL_NO_SURFACE;
    eglCtx = EGL_NO_CONTEXT;
}

void nativeRequestInitEGL(ANativeWindow * nativeWindow){
    pthread_mutex_init(&th_mutex_lock, NULL);
   // pthread_mutex_lock(&th_mutex_lock);
    mWindow = nativeWindow;
    initEGL = 0;
    mEnumRenderEvent = RE_SURFACE_CREATED;
  //  pthread_mutex_unlock(&th_mutex_lock);
}
void nativeRequestSurfaceChangeEGL(ANativeWindow * nativeWindow){
   // pthread_mutex_unlock(&th_mutex_lock);
    mWindow = nativeWindow;
    initEGL = 0;
    mEnumRenderEvent = RE_SURFACE_CHANGED;
   // pthread_mutex_unlock(&th_mutex_lock);
}
void nativeRequestDestoryEGL(){
    pthread_mutex_destroy(&th_mutex_lock);
    mEnumRenderEvent = RE_EXIT;
}



