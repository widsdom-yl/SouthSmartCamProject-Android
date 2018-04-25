#bin/sh
#export PATH=/opt/android-ndk-r9c:$PATH
# rm -rf ../obj/
# rm -rf ../libs/
export PATH=/opt/android/android-ndk-r11c:$PATH
#ndk-build clean
ndk-build

#cp -f lib.android/libSDL2.so ../libs/armeabi/
#cp -f lib.android/libSDL2main.so ../libs/armeabi/
#cp -f lib.android/libAVAPIs.so ../libs/armeabi/
#cp -f lib.android/libIOTCAPIs.so ../libs/armeabi/
