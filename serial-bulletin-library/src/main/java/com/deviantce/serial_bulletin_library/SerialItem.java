package com.deviantce.serial_bulletin_library;



public class SerialItem {

    private static SerialItem instance = new SerialItem();
    char[] send_data;
    // byte 1
    final public static char SIREN = 0x03;

    // byte 2
    final public static char SIREN_ON = 0x01;
    final public static char SIREN_OFF = 0x02;

    // byte 3
    final public static char CODE_POLICE = 0x01;
    final public static char CODE_FIRE = 0x02;
    final public static char CODE_AMBULANCE = 0x04;

    final public static char VOICE_1 = 0x06;
    final public static char VOICE_2 = 0x07;
    final public static char VOICE_3 = 0x08;
    final public static char VOICE_4 = 0x09;
    final public static char VOICE_5 = 0x0a;

    public char current_siren_status = 0;


    char[] SIREN_CODES;


    // byte 4

    int siren_volume = 3;

    public SerialItem(){
        send_data = new char[4];
        SIREN_CODES = new char[]{0x00,CODE_POLICE,CODE_FIRE,0x03,CODE_AMBULANCE,0x05,VOICE_1,VOICE_2,VOICE_3,
        VOICE_4,VOICE_5};
    }

    public static SerialItem getInstance(){
        return instance;
    }

    public void setPolice() {
        setSirenStatus(CODE_POLICE);
    }

    public void setFire(){
        setSirenStatus(CODE_FIRE);
    }

    public void setAmbulance(){
        setSirenStatus(CODE_AMBULANCE);
    }

    public void setVOICE_1(){
        setSirenStatus(VOICE_1);
    }

    public void setVOICE_2(){
        setSirenStatus(VOICE_2);
    }

    public void setVOICE_3(){
        setSirenStatus(VOICE_3);
    }

    public void setVOICE_4(){
        setSirenStatus(VOICE_4);
    }

    public void setVOICE_5(){
        setSirenStatus(VOICE_5);
    }

    public void setVolume(int volume){
        if(volume >=1 && volume <= 5)
            setSirenVolume(volume);
    }


    private void setSirenStatus(int type){
        current_siren_status = (char)type;
        send_data[0] = SIREN;
        if(type==0){// 끄기
            send_data[1] = SIREN_OFF;
        }
        else {
            send_data[1] = SIREN_ON;
            send_data[2] = SIREN_CODES[type];
            send_data[3] = (char)siren_volume;
        }
    }

    private void setSirenVolume(int volume){
        siren_volume = volume;
        send_data[0] = SIREN;
        send_data[3] = (char)volume;
    }

    public char getCh(int i){
        if(i<=3 && i>=0)
            return send_data[i];
        else
            return 0;
    }

    public char getCurrent_siren_status(){
        return current_siren_status;
    }

    public int getCurrentVolume(){
        return siren_volume;
    }

}
