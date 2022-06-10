package com.deviantce.serial_bulletin_library;

public class SignboardItem {

    private static SignboardItem instance = new SignboardItem();

    char[] send_data;
    // byte 1
    final public static char SIGNBOARD = 0x01;

    // byte 2
    final public static char SIGNBOARD_ON = 0x01;
    final public static char SIGNBOARD_OFF = 0x02;

    // byte 3 - 4bit LSB
    final public static char LEFT = 0x01;
    final public static char TWOWAY = 0x02;
    final public static char RIGHT = 0x04;
    final public static char X = 0x08;

    // byte 3 - 2bit MSB / LSB
    final public static char DAY = 0x01;
    final public static char CLOUDY = 0x02;
    final public static char NIGHT = 0x03;

    // byte 3 - 2bit MSB / MSB
    final public static char FAST = 0x01;
    final public static char MID = 0x02;
    final public static char SLOW = 0x03;

    // byte 4
    final public static char SIMUL = 0x01;
    final public static char CONTIN = 0x02;

    public char current_signboard_status = 0;
    public char current_brighness = 0;
    public char current_speed = 0;
    public char current_simulcontin = 0;


    public SignboardItem(){
        send_data = new char[4];
    }

    public static SignboardItem getInstance(){
        return instance;
    }

    public void setLeft(){
        setSignboard(LEFT);
    }

    public void setTwoway() {
        setSignboard(TWOWAY);
    }

    public void setRight() {
        setSignboard(RIGHT);
    }


    public void setX() {
        setSignboard(X);
    }

    public void setDay(){
        setBrightness(DAY);
    }

    public void setCloudy(){
        setBrightness(CLOUDY);
    }

    public void setNight(){
        setBrightness(NIGHT);
    }

    public void setFast(){
        setSpeed(FAST);
    }
    public void setMid(){
        setSpeed(MID);
    }
    public void setSlow(){
        setSpeed(SLOW);
    }

    public void setSimul(){
        send_data[3] = SIMUL;
        current_simulcontin = 1;
    }

    public void setContin(){
        send_data[3] = CONTIN;
        current_simulcontin = 2;
    }

    private void setBrightness(char status) {
        send_data[0] = SIGNBOARD;
        send_data[1] = SIGNBOARD_ON;
        if(status == DAY){
            send_data[2] = bitMask(send_data[2],4);
            send_data[2] = bitUnMask(send_data[2],5);
            current_brighness = 1;
        }
        if(status == CLOUDY){
            send_data[2] = bitMask(send_data[2],5);
            send_data[2] = bitUnMask(send_data[2],4);
            current_brighness = 2;
        }
        if(status == NIGHT){
            send_data[2] = bitMask(send_data[2],4);
            send_data[2] = bitMask(send_data[2],5);
            current_brighness = 3;
        }
    }

    private void setSpeed(char status){
        send_data[0] = SIGNBOARD;
        send_data[1] = SIGNBOARD_ON;
        if(status == FAST){
            send_data[2] = bitMask(send_data[2],6);
            send_data[2] = bitUnMask(send_data[2],7);
            current_speed = 1;
        }
        if(status == MID){
            send_data[2] = bitMask(send_data[2],7);
            send_data[2] = bitUnMask(send_data[2],6);
            current_speed = 2;
        }
        if(status == SLOW){
            send_data[2] = bitMask(send_data[2],6);
            send_data[2] = bitMask(send_data[2],7);
            current_speed = 3;
        }
    }


    private void setSignboard(char status) {
        current_signboard_status = status;
        send_data[0] = SIGNBOARD;
        send_data[1] = SIGNBOARD_ON;
        if(status == LEFT){
            send_data[2] = bitMask(send_data[2],0);
            send_data[2] = bitUnMask(send_data[2],1);
            send_data[2] = bitUnMask(send_data[2],2);
            send_data[2] = bitUnMask(send_data[2],3);
        }
        if(status == TWOWAY){
            send_data[2] = bitMask(send_data[2],1);
            send_data[2] = bitUnMask(send_data[2],0);
            send_data[2] = bitUnMask(send_data[2],2);
            send_data[2] = bitUnMask(send_data[2],3);
        }
        if(status == RIGHT){
            send_data[2] = bitMask(send_data[2],2);
            send_data[2] = bitUnMask(send_data[2],1);
            send_data[2] = bitUnMask(send_data[2],0);
            send_data[2] = bitUnMask(send_data[2],3);
        }
        if(status == X){
            send_data[2] = bitMask(send_data[2],3);
            send_data[2] = bitUnMask(send_data[2],1);
            send_data[2] = bitUnMask(send_data[2],2);
            send_data[2] = bitUnMask(send_data[2],0);
        }
        send_data[3] = 0x02;
    }

    public char bitMask(char dest,int bit){
        int src = 0x01 << bit;
        dest = (char)((int)dest | src);
        return dest;
    }

    public char bitUnMask(char dest,int bit){
        int src = 0x01 << bit;
        dest = (char)((int)dest & ~src);
        return dest;
    }


    public char getCh(int i){
        if(i<=3 && i>=0)
            return send_data[i];
        else
            return 0;
    }


    public char getCurrentSignboardStatus() {
        return current_signboard_status;
    }
    public char getCurrentSpeed(){
        return current_speed;
    }
    public char getCurrentBrighness(){
        return current_brighness;
    }
    public char getCurrentSimulContin(){
        return current_simulcontin;
    }
}
