package com.deviantce.serial_bulletin_library;

public class NativeLib {

    static {
        System.loadLibrary("serial_bulletin_library");
    }

    /**
     * <p><b>전광판</b> 관련 네이티브 메소드</p>
     */
    public native int getBulletinFd();
    public native int getBulletinFdWithPortAndBaud(String port_name,int baud_rate);
    public native boolean defaultTest(int fd);
    public native void sendTextBulletinWithEFF(int fd,char[] msgX,int msg_len,int msg_text_len);

    /**
     * <p><b>경광등 </b> 관련 네이티브 메소드</p>
     */
    public native int getLightFd();
    public native int getLightFdWithPortAndBaud(String port_name,int baud_rate);
    public native void sendChar(int fd,char ch1,char ch2,char ch3);
    public native int readData(int fd);

    public native int getSerialFdWithPortAndBaud(String port_name,int baud_rate);
    public native void sendCharSiren(int fd,char st,char ch1,char ch2,char ch3,char ch4);

    public native void sendCharBulletin(int fd,char st,char ch1,char ch2,char ch3);
    public native void sendCharEmergency(int fd,char st,char ch1,char ch2);

}