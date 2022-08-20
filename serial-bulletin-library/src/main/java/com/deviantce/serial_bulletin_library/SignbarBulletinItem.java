package com.deviantce.serial_bulletin_library;

public class SignbarBulletinItem {
    private static SignbarBulletinItem instance = new SignbarBulletinItem();

    char[] send_data;

    // byte 1
    final public static char BULLETIN = 0x02;

    // byte 2
    final public static char BULLETIN_ON = 0x01;
    final public static char BULLETIN_OFF = 0x02;

    // byte 3


    public int current_bulletin_status = 0;
    public int current_brighness = 3;

    public SignbarBulletinItem(){
        send_data = new char[3];
    }

    public static SignbarBulletinItem getInstance(){
        return instance;
    }

    public void setBulletinNumber(int number){
        send_data[0] = BULLETIN;
        send_data[1] = BULLETIN_ON;
        send_data[2] = (char)number;
        current_bulletin_status = number;
    }

    public int getCurrent_bulletin_status(){
        return current_bulletin_status;
    }

    public char getCh(int i){
        if(i<=2 && i>=0)
            return send_data[i];
        else
            return 0;
    }


    public int getCurrentBrighness() {
        return current_brighness;
    }

    public void offBulletin() {
        send_data[1] = BULLETIN_OFF;
    }

    public void onBulletin() {
        send_data[1] = BULLETIN_ON;
    }
}
