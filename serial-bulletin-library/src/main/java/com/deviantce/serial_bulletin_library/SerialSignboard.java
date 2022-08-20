package com.deviantce.serial_bulletin_library;

public class SerialSignboard {
    private String default_port_name = SerialConstants.DEFAULT_SERIAL_PORT;
    private int default_port_baud_rate = SerialConstants.DEFAULT_SERIAL_BAUD_RATE;

    SignboardItem signboardItem;

    private String TAG = getClass().getSimpleName();
    int fd = -1;
    NativeLib nativeLib;

    public SerialSignboard(){
        nativeLib  = new NativeLib();
        this.fd = -1;
        this.signboardItem = SignboardItem.getInstance();
    }

    public boolean connect(){
        this.fd = nativeLib.getSerialFdWithPortAndBaud(default_port_name,default_port_baud_rate);
        if(this.fd != -1)
            return true;
        return false;
    }

    public boolean isConnected(){
        return this.fd != -1;
    }

    public void setLeft(){
        signboardItem.setLeft();
        sendSignboardProtocol(signboardItem);
    }

    public void setTwoway(){
        signboardItem.setTwoway();
        sendSignboardProtocol(signboardItem);
    }

    public void setRight(){
        signboardItem.setRight();
        sendSignboardProtocol(signboardItem);
    }

    public void setX(){
        signboardItem.setX();
        sendSignboardProtocol(signboardItem);
    }

    public void setDay(){
        signboardItem.setDay();
        sendSignboardProtocol(signboardItem);
    }

    public void setCloudy(){
        signboardItem.setCloudy();
        sendSignboardProtocol(signboardItem);
    }
    public void setNight(){
        signboardItem.setNight();
        sendSignboardProtocol(signboardItem);
    }

    public void setFast(){
        signboardItem.setFast();
        sendSignboardProtocol(signboardItem);
    }
    public void setMid(){
        signboardItem.setMid();
        sendSignboardProtocol(signboardItem);
    }
    public void setSlow(){
        signboardItem.setSlow();
        sendSignboardProtocol(signboardItem);
    }

    public void setSimul() {
        signboardItem.setSimul();
        sendSignboardProtocol(signboardItem);
    }
    public void setContin(){
        signboardItem.setContin();
        sendSignboardProtocol(signboardItem);
    }
    private void sendSignboardProtocol(SignboardItem signboardItem) {
        signboardItem.onSignboard();
        nativeLib.sendCharSiren(this.fd,(char)0xaa,signboardItem.getCh(0),signboardItem.getCh(1),signboardItem.getCh(2),signboardItem.getCh(3));

    }


    public char getCurrentSignboardStatus() {
        return signboardItem.getCurrentSignboardStatus();
    }

    public char getCurrentBrighness() {
        return signboardItem.getCurrentBrighness();
    }

    public char getCurrentSpeed(){
        return signboardItem.getCurrentSpeed();
    }

    public char getCurrentSimulContin(){
        return signboardItem.getCurrentSimulContin();
    }

    public void offSignboard() {
        signboardItem.offSignboard();
        nativeLib.sendCharSiren(this.fd,(char)0xaa,signboardItem.getCh(0),signboardItem.getCh(1),signboardItem.getCh(2),signboardItem.getCh(3));
    }

    public boolean isSignboardOn() {
        return signboardItem.isSignboardOn();
    }
}
