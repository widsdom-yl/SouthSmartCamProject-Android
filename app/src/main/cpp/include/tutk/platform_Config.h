
#ifndef _IOTCAPIs_charlie_H_Platform_Config_h
#define _IOTCAPIs_charlie_H_Platform_Config_h

// ************************ Platform Selection ************************  
//#define IOTC_Linux //±‡º≠”√

#ifndef WIN32
//#warning " IOTC_LinuxIOTC_LinuxIOTC_Linux "
#define IOTC_Linux
#else
//#warning " IOTC_Win32IOTC_Win32IOTC_Win32 "
#define IOTC_Win32
#endif

// ************************ OS Selection ******************************  
//#define OS_ANDROID

// ************************ Compilier Option **************************
//#define _ARC_COMPILER	 // for Arc compilier

// ************************ Debug Option **************************  
//#define OUTPUT_DEBUG_MESSAGE			// Define this to enable debug output.
// #ifdef OS_ANDROID
// 	#define LOG_FILE	"/sdcard/log_iotcapi.txt"	// Define this to redirect debug output into specified log file.
// #else
// 	#define LOG_FILE	"log_iotcapi.txt"			// Define this to redirect debug output into specified log file.
// #endif

// ************************ AVAPI Debug Option ***************************  
//#define OUTPUT_DEBUG_MESSAGE_AV			// Define this to enable av module debug output.

//#ifdef OS_ANDROID
//	#define LOG_FILE_AVAPI	"/sdcard/log_avapi.txt"
//#else
//	#define LOG_FILE_AVAPI	"log_avapi.txt"
//#endif


#endif //_IOTCAPIs_charlie_H_Platform_Config_h
