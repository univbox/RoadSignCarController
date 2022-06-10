#include <jni.h>
#include <string>
#include <termios.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <string.h>
#include <android/log.h>

static const char * g_lpszClassNameTag = "NativeDebug";

#define ANDROID_LOG
//#undef ANDROID_LOG

#ifdef ANDROID_LOG

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, g_lpszClassNameTag, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, g_lpszClassNameTag, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, g_lpszClassNameTag, __VA_ARGS__)

#else

#define LOGI(...)
#define LOGD(...)
#define LOGE(...)

#endif


char TOHEX(int i){
    return static_cast<char>(i <= 9 ? '0' + i : 'A' - 10 + i);
}

int TOINT(char a){
    if(a<='9')
        return a-'0';
    else if(a>='a' && a<='z')
        return a-'a'+10;
    return a-'A'+10;
    return (a<='9' ? a-'0' : a-'A'+10);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_deviantce_serial_1bulletin_1library_NativeLib_getBulletinFd(JNIEnv *env, jobject thiz) {
    struct termios tios;

    int fd = open("/dev/ttyS1", O_RDWR | O_NOCTTY | O_NONBLOCK);
    tcgetattr(fd, &tios);

    tios.c_cflag = B115200 | CS8 | CLOCAL | CREAD;
    tios.c_iflag = IGNPAR;
    tios.c_oflag = 0;
    tios.c_lflag = 0;
    tios.c_cc[VMIN]= 0;
    tios.c_cc[VTIME]= 0;

    tcflush(fd, TCIFLUSH);
    tcsetattr(fd, TCSANOW,&tios);

    return fd;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_deviantce_serial_1bulletin_1library_NativeLib_getBulletinFdWithPortAndBaud(JNIEnv *env,
                                                                                    jobject thiz,
                                                                                    jstring port_name,
                                                                                    jint baud_rate) {
    struct termios tios;

    const char*  port = env->GetStringUTFChars(port_name,0);
    int fd = open(port, O_RDWR | O_NOCTTY | O_NONBLOCK);
    tcgetattr(fd, &tios);

    switch(baud_rate){
        case 9600:
            tios.c_cflag = B9600 | CS8 | CLOCAL | CREAD;
            break;
        case 19200:
            tios.c_cflag = B19200 | CS8 | CLOCAL | CREAD;
            break;
        case 38400:
            tios.c_cflag = B38400 | CS8 | CLOCAL | CREAD;
            break;
        case 115200:
            tios.c_cflag = B115200 | CS8 | CLOCAL | CREAD;
            break;
        default:
            tios.c_cflag = B115200 | CS8 | CLOCAL | CREAD;
    }

    tios.c_iflag = IGNPAR;
    tios.c_oflag = 0;
    tios.c_lflag = 0;
    tios.c_cc[VMIN]= 0;
    tios.c_cc[VTIME]= 0;

    tcflush(fd, TCIFLUSH);
    tcsetattr(fd, TCSANOW,&tios);

    return fd;
}
extern "C"
JNIEXPORT jboolean JNICALL
Java_com_deviantce_serial_1bulletin_1library_NativeLib_defaultTest(JNIEnv *env, jobject thiz,
                                                                   jint fd) {
    tcflush(fd, TCIFLUSH);
    const char16_t* msg = u"RST=1,LNE=1,YSZ=2,SPD=3,DLY=3,EFF=090009000900,TXT=$f00$c03국도우회";
    int msg_len = 0;
    while(msg[msg_len++]!=0){}
    msg_len-=1;
    int x = msg_len*2;
    char res[4];

    res[0] = TOHEX(((x & 0xF000) >> 12));
    res[1] = TOHEX(((x & 0x0F00) >> 8));
    res[2] = TOHEX(((x & 0x00F0) >> 4));
    res[3] = TOHEX((x & 0x000F));


    int len = 0;
    char smsg[1000] = {'0','2','8','4',res[2],res[3],res[0],res[1]};
    len=8;
    for(int i=0;i<msg_len;i++)
    {
        x = msg[i];

        smsg[len] = TOHEX(((x & 0x00F0) >> 4));
        smsg[len+1] = TOHEX((x & 0x000F));
        smsg[len+2] = TOHEX(((x & 0xF000) >> 12));
        smsg[len+3] = TOHEX(((x & 0x0F00) >> 8));
        len+=4;
    }

    int total = 0;

    for(int i=0;i<len;i+=2){
        int y =(TOINT(smsg[i])*16 + TOINT(smsg[i+1]));
        total +=y;
    }


    x = total;
    x %= 16*16;

    res[2] = TOHEX(((x & 0x00F0) >> 4));
    res[3] = TOHEX((x & 0x000F));

    smsg[len++] = res[2];
    smsg[len++] = res[3];
    smsg[len++] = '0';
    smsg[len++] = '3';
    smsg[len] = 0;

    //const char* smsg = "028470005200530054003D0031002C004C004E0045003D0031002C00590053005A003D0032002C004500460046003D003000340030003000300034003000300030003400300030002C0044004C0059003D0033002C005400580054003D00240063003000330090C7E0ACDCC27CD32000210021000703";
    //int msg_len=0;
    //while(smsg[msg_len++]!=0){}


    unsigned char bu_msg[1600];
    int bu_len = 0;
    for(int i=0;i<len;i+=2)
    {
        bu_msg[i/2] = (unsigned char)(TOINT(smsg[i])*16 + TOINT(smsg[i+1]));
        bu_len++;
    }
    tcflush(fd, TCIFLUSH);
    write(fd,bu_msg,bu_len);

    return len;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_deviantce_serial_1bulletin_1library_NativeLib_sendTextBulletinWithEFF(JNIEnv *env, jobject instance, jint fd,
                                                                               jcharArray msgX_,jint msgX_len,jint msgX_text_len) {
    jchar *msgX = env->GetCharArrayElements(msgX_, NULL);
    // c04->하늘색 c03->녹색 c02->노
    const char* msg = "LNE=1,YSZ=2";

    tcflush(fd, TCIFLUSH);

    int msg_len = 0;
    while(msg[msg_len++]!=0){}
    msg_len-=1;

    msgX_text_len = msgX_text_len/2 -1;
    int x = (msg_len+msgX_text_len)*2;
    char res[4];

    res[0] = TOHEX(((x & 0xF000) >> 12));
    res[1] = TOHEX(((x & 0x0F00) >> 8));
    res[2] = TOHEX(((x & 0x00F0) >> 4));
    res[3] = TOHEX((x & 0x000F));


    int len = 0;
    char smsg[1000] = {'0','2','8','4',res[2],res[3],res[0],res[1]};
    len=8;
    for(int i=0;i<msg_len;i++)
    {
        x = msg[i];

        smsg[len] = TOHEX(((x & 0x00F0) >> 4));
        smsg[len+1] = TOHEX((x & 0x000F));
        smsg[len+2] = TOHEX(((x & 0xF000) >> 12));
        smsg[len+3] = TOHEX(((x & 0x0F00) >> 8));
        len+=4;
    }

    for(int i=0;i<msgX_len;i++)
    {
        smsg[len] = msgX[i];
        len+=1;
    }

    int total = 0;

    for(int i=0;i<len;i+=2){
        int y =(TOINT(smsg[i])*16 + TOINT(smsg[i+1]));
        total +=y;
    }

    x = total;
    x %= 16*16;

    res[2] = TOHEX(((x & 0x00F0) >> 4));
    res[3] = TOHEX((x & 0x000F));

    smsg[len++] = res[2];
    smsg[len++] = res[3];
    smsg[len++] = '0';
    smsg[len++] = '3';
    smsg[len] = 0;

    unsigned char bu_msg[800];
    int bu_len = 0;
    for(int i=0;i<len;i+=2)
    {
        bu_msg[i/2] = (unsigned char)(TOINT(smsg[i])*16 + TOINT(smsg[i+1]));
        bu_len++;
    }

    char tmp[16];
    char tmp1[800];
    for (int i = 0; i < bu_len; i ++ )
    {
        sprintf(tmp, "%02X ", bu_msg[i]);
        strcat(tmp1, tmp);
    }
    LOGD("%s", tmp1);




    tcflush(fd, TCIFLUSH);
    write(fd,bu_msg,bu_len);

    //tcflush(fd,TCOFLUSH);
    env->ReleaseCharArrayElements(msgX_, msgX, 0);
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_deviantce_serial_1bulletin_1library_NativeLib_getLightFd(JNIEnv *env, jobject thiz) {
    struct termios tios;

    int fd = open("/dev/ttyMAX1", O_RDWR | O_NOCTTY | O_NONBLOCK);
    tcgetattr(fd, &tios);

    tios.c_cflag = B9600 | CS8 | CLOCAL | CREAD;
    tios.c_iflag = IGNPAR;
    tios.c_oflag = 0;
    tios.c_lflag = 0;
    tios.c_cc[VMIN]= 0;
    tios.c_cc[VTIME]= 0;

    tcflush(fd, TCIFLUSH);
    tcsetattr(fd, TCSANOW,&tios);


    return fd;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_deviantce_serial_1bulletin_1library_NativeLib_getLightFdWithPortAndBaud(JNIEnv *env,
                                                                                 jobject thiz,
                                                                                 jstring port_name,
                                                                                 jint baud_rate) {
    struct termios tios;

    const char*  port = env->GetStringUTFChars(port_name,0);
    int fd = open(port, O_RDWR | O_NOCTTY | O_NONBLOCK);
    tcgetattr(fd, &tios);

    switch(baud_rate){
        case 9600:
            tios.c_cflag = B9600 | CS8 | CLOCAL | CREAD;
            break;
        case 19200:
            tios.c_cflag = B19200 | CS8 | CLOCAL | CREAD;
            break;
        case 38400:
            tios.c_cflag = B38400 | CS8 | CLOCAL | CREAD;
            break;
        case 115200:
            tios.c_cflag = B115200 | CS8 | CLOCAL | CREAD;
            break;
        default:
            tios.c_cflag = B115200 | CS8 | CLOCAL | CREAD;
    }


    tios.c_iflag = IGNPAR;
    tios.c_oflag = 0;
    tios.c_lflag = 0;
    tios.c_cc[VMIN]= 0;
    tios.c_cc[VTIME]= 0;

    tcflush(fd, TCIFLUSH);
    tcsetattr(fd, TCSANOW,&tios);


    return fd;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_deviantce_serial_1bulletin_1library_NativeLib_sendChar(JNIEnv *env, jobject thiz, jint fd,
                                                                jchar ch1, jchar ch2, jchar ch3) {
    char ch_1 = ch1;
    char ch_2 = ch2;
    char ch_3 = ch3;

    char ch[3] = {ch_1,ch_2,ch_3};

//    char tmp[16];
//    char tmp1[128];
//    for (int i = 0; i < 3; i ++ )
//    {
//        sprintf(tmp, "%02X ", ch[i]);
//        strcat(tmp1, tmp);
//    }
    //LOGD("%s", tmp1);


    write(fd,ch,sizeof(ch));
    tcflush(fd, TCIFLUSH);
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_deviantce_serial_1bulletin_1library_NativeLib_readData(JNIEnv *env, jobject thiz,
                                                                jint fd) {

    char read_buf[3];
    int ret_value = 0;
    int read_size;
//    read_size = read(fd,read_buf,sizeof(read_buf));
//    if(read_size == 3)
//    {
//        ret_value = 0;
//        int n1 = (int)read_buf[0];
//        int n2 = (int)read_buf[1];
//        int n3 = (int)read_buf[2];
//        ret_value |= (n1<<16);
//        ret_value |= (n2<<8);
//        ret_value |= (n3);
//    }


//    do
//    {
//        read_size = read(fd,read_buf,sizeof(read_buf));
//        if(read_size == 3)
//        {
//            ret_value = 0;
//            int n1 = (int)read_buf[0];
//            int n2 = (int)read_buf[1];
//            int n3 = (int)read_buf[2];
//            ret_value |= (n1<<16);
//            ret_value |= (n2<<8);
//            ret_value |= (n3);
//        }
//    }while(read_size==3);

    read_size = read(fd,read_buf,sizeof(read_buf));

        if(read_size == 3)
        {
            ret_value = 0;
            int n1 = (int)read_buf[0];
            int n2 = (int)read_buf[1];
            int n3 = (int)read_buf[2];
            ret_value |= (n1<<16);
            ret_value |= (n2<<8);
            ret_value |= (n3);
        }

    tcflush(fd, TCIFLUSH);

    return ret_value;
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_deviantce_serial_1bulletin_1library_NativeLib_getSerialFdWithPortAndBaud(JNIEnv *env,
                                                                                  jobject thiz,
                                                                                  jstring port_name,
                                                                                  jint baud_rate) {
    struct termios tios;

    const char*  port = env->GetStringUTFChars(port_name,0);
    int fd = open(port, O_RDWR | O_NOCTTY | O_NONBLOCK);
    tcgetattr(fd, &tios);

    switch(baud_rate){
        case 9600:
            tios.c_cflag = B9600 | CS8 | CLOCAL | CREAD;
            break;
        case 19200:
            tios.c_cflag = B19200 | CS8 | CLOCAL | CREAD;
            break;
        case 38400:
            tios.c_cflag = B38400 | CS8 | CLOCAL | CREAD;
            break;
        case 115200:
            tios.c_cflag = B115200 | CS8 | CLOCAL | CREAD;
            break;
        default:
            tios.c_cflag = B115200 | CS8 | CLOCAL | CREAD;
    }


    tios.c_iflag = IGNPAR;
    tios.c_oflag = 0;
    tios.c_lflag = 0;
    tios.c_cc[VMIN]= 0;
    tios.c_cc[VTIME]= 0;

    tcflush(fd, TCIFLUSH);
    tcsetattr(fd, TCSANOW,&tios);


    return fd;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_deviantce_serial_1bulletin_1library_NativeLib_sendCharSiren(JNIEnv *env, jobject thiz,
                                                                     jint fd,jchar st, jchar ch1, jchar ch2,
                                                                     jchar ch3, jchar ch4) {
    char ch_st = st;
    char ch_1 = ch1;
    char ch_2 = ch2;
    char ch_3 = ch3;
    char ch_4 = ch4;

    char ch[5] = {ch_st,ch_1,ch_2,ch_3,ch_4};


    write(fd,ch,sizeof(ch));
    tcflush(fd, TCIFLUSH);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_deviantce_serial_1bulletin_1library_NativeLib_sendCharBulletin(JNIEnv *env, jobject thiz,
                                                                        jint fd, jchar st,
                                                                        jchar ch1, jchar ch2,
                                                                        jchar ch3) {
    char ch_st = st;
    char ch_1 = ch1;
    char ch_2 = ch2;
    char ch_3 = ch3;

    char ch[4] = {ch_st,ch_1,ch_2,ch_3};


    write(fd,ch,sizeof(ch));
    tcflush(fd, TCIFLUSH);
}
extern "C"
JNIEXPORT void JNICALL
Java_com_deviantce_serial_1bulletin_1library_NativeLib_sendCharEmergency(JNIEnv *env, jobject thiz,
                                                                         jint fd, jchar st,
                                                                         jchar ch1, jchar ch2) {
    char ch_st = st;
    char ch_1 = ch1;
    char ch_2 = ch2;

    char ch[3] = {ch_st,ch_1,ch_2};


    write(fd,ch,sizeof(ch));
    tcflush(fd, TCIFLUSH);
}