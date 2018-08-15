# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)






set(SOURCE_DIR ${CMAKE_SOURCE_DIR}/src/main/cpp)
set(JPEG_DIR ${CMAKE_SOURCE_DIR}/src/main/cpp/avDecode/libjpeg)
set(LIB_DIR ${CMAKE_SOURCE_DIR}/src/main/cpp/lib)

add_library(AVAPIs SHARED IMPORTED)
set_target_properties(AVAPIs PROPERTIES IMPORTED_LOCATION ${LIB_DIR}/libAVAPIs.so)

add_library(ffmpeg SHARED IMPORTED)
set_target_properties(ffmpeg PROPERTIES IMPORTED_LOCATION ${LIB_DIR}/libffmpeg.so)

add_library(IOTCAPIs SHARED IMPORTED)
set_target_properties(IOTCAPIs PROPERTIES IMPORTED_LOCATION ${LIB_DIR}/libIOTCAPIs.so)

add_library(SDL2 SHARED IMPORTED)
set_target_properties(SDL2 PROPERTIES IMPORTED_LOCATION ${LIB_DIR}/libSDL2.so)

add_library(SDL2main SHARED IMPORTED)
set_target_properties(SDL2main PROPERTIES IMPORTED_LOCATION ${LIB_DIR}/libSDL2main.so)



add_library(SmartConnection STATIC IMPORTED)
set_target_properties(SmartConnection PROPERTIES IMPORTED_LOCATION ${LIB_DIR}/libSmartConnection.a)


add_library(vo-aacenc STATIC IMPORTED)
set_target_properties(vo-aacenc PROPERTIES IMPORTED_LOCATION ${LIB_DIR}/libvo-aacenc.a)

aux_source_directory(${JPEG_DIR} jpegSrc)

#ADD_DEFINITIONS(
#-O3 -g -W -Wall
# -Wunused-variable -Wunused-parameter -Wunused-function -Wunused
# -Wno-deprecated -Woverloaded-virtual -Wwrite-strings
#  -Wl,–no-warn-shared-textrel
#)

set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -Wl,--no-fatal-warnings")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} -fPIC")
add_library( # Sets the name of the library.
             thSDK

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             ${SOURCE_DIR}/jniMain.c
             ${SOURCE_DIR}/avDecode/shaderUtils.c
             ${SOURCE_DIR}/avDecode/thEGL.c
             ${SOURCE_DIR}/thSDK/thPlay.c ${SOURCE_DIR}/thSDK/axdllEx.c
             ${SOURCE_DIR}/common/av_Queue.c ${SOURCE_DIR}/common/List.c ${SOURCE_DIR}/common/skt.c ${SOURCE_DIR}/common/TFun.c ${SOURCE_DIR}/cJSON/cJSON.c
             ${SOURCE_DIR}/avDecode/audio_g711.c

            ${SOURCE_DIR}/avDecode/thSDL.c ${SOURCE_DIR}/avDecode/thOpenGL_SLES.c ${SOURCE_DIR}/avDecode/thRec.c
              ${SOURCE_DIR}/avDecode/thImg.c ${SOURCE_DIR}/avDecode/thVideoDec.c
              ${SOURCE_DIR}/avDecode/thRender.c
             ${jpegSrc}

             )


target_include_directories(thSDK PRIVATE
                           ${SOURCE_DIR}/include
                           ${SOURCE_DIR}/include/ffmpeg)



find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       thSDK

                       ffmpeg
                       SmartConnection
                       vo-aacenc
                        GLESv1_CM
                        GLESv2
                        EGL
                        OpenSLES
                        android
                        AVAPIs
                        IOTCAPIs
                        SDL2
                        SDL2main
                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )