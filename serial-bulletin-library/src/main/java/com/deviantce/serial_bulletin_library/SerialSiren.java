package com.deviantce.serial_bulletin_library;

public class SerialSiren {
    private String default_port_name = SerialConstants.DEFAULT_SERIAL_PORT;
    private int default_port_baud_rate = SerialConstants.DEFAULT_SERIAL_BAUD_RATE;

    SerialItem serialItem;

    private String TAG = getClass().getSimpleName();
    int fd = -1;
    NativeLib nativeLib;
    private SirenSerialListener mListener; // Synchronized by 'this'

    public boolean isOn() {
        return serialItem.isOn();
    }


    public interface SirenSerialListener {

        /**
         * <p>새로운 데이터가 들어왔을 때</p>
         */
        void onNewData( );
        void onRunError(Exception e);
    }


    public SerialSiren(SirenSerialListener listener){
        nativeLib  = new NativeLib();
        this.fd = -1;
        this.serialItem = SerialItem.getInstance();
        this.mListener = listener;
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

    public void setPolice(){
        serialItem.setPolice();
        sendSirenProtocol(serialItem);
    }

    public void setFire(){
        serialItem.setFire();
        sendSirenProtocol(serialItem);
    }

    public void setAmbulance(){
        serialItem.setAmbulance();
        sendSirenProtocol(serialItem);
    }

    public void setVoice1(){
        serialItem.setVOICE_1();
        sendSirenProtocol(serialItem);
    }

    public void setVoice2(){
        serialItem.setVOICE_2();
        sendSirenProtocol(serialItem);
    }

    public void setVoice3(){
        serialItem.setVOICE_3();
        sendSirenProtocol(serialItem);
    }

    public void setVoice4(){
        serialItem.setVOICE_4();
        sendSirenProtocol(serialItem);
    }

    public void setVoice5(){
        serialItem.setVOICE_5();
        sendSirenProtocol(serialItem);
    }

    public void setVolume(int volume){
        serialItem.setVolume(volume);
        sendSirenProtocol(serialItem);
    }

    public void setEmergencyOn() {
        nativeLib.sendCharEmergency(this.fd,(char)0xaa,(char)0x04,(char)0x01);
    }

    public void setEmergencyOff() {
        nativeLib.sendCharEmergency(this.fd,(char)0xaa,(char)0x04,(char)0x02);
    }

    public void offSiren() {
        serialItem.offSiren();
        nativeLib.sendCharSiren(this.fd,(char)0xaa,serialItem.getCh(0),serialItem.getCh(1),serialItem.getCh(2),serialItem.getCh(3));

    }



    private void sendSirenProtocol(SerialItem serialItem) {
        serialItem.OnSiren();
        nativeLib.sendCharSiren(this.fd,(char)0xaa,serialItem.getCh(0),serialItem.getCh(1),serialItem.getCh(2),serialItem.getCh(3));
    }

    public char getCurrent_siren_status(){
        return serialItem.getCurrent_siren_status();
    }

    public int getCurrentVolume(){
        return serialItem.getCurrentVolume();
    }
}
