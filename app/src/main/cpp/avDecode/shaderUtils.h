//
//  shaderUtils.hpp
//  Copyright © 2018 jdf. All rights reserved.
//

#ifndef shaderUtils_hpp
#define shaderUtils_hpp

#include <GLES2/gl2.h>

GLuint createProgram(const char *vertexSource, const char *fragmentSource);

GLuint loadShader(GLenum shaderType, const char *source);


#endif /* shaderUtils_hpp */
