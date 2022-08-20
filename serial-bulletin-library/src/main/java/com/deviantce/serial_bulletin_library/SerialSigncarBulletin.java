package com.deviantce.serial_bulletin_library;

public class SerialSigncarBulletin {
    private String default_port_name = SerialConstants.DEFAULT_SERIAL_PORT;
    private int default_port_baud_rate = SerialConstants.DEFAULT_SERIAL_BAUD_RATE;

    SignbarBulletinItem signbarBulletinItem;
    private String TAG = getClass().getSimpleName();
    int fd = -1;
    NativeLib nativeLib;

    public SerialSigncarBulletin(){
        nativeLib  = new NativeLib();
        this.fd = -1;
        this.signbarBulletinItem = SignbarBulletinItem.getInstance();
    }

    public boolean connect(){
        this.fd = nativeLib.getSerialFdWithPortAndBaud(default_port_name,default_port_baud_rate);
        if(this.fd != -1)
            return true;
        return false;
    }

    public void setBulletinNumber(int number){
        signbarBulletinItem.setBulletinNumber(number);
        sendBulletinProtocol(signbarBulletinItem);
    }

    private void sendBulletinProtocol(SignbarBulletinItem signbarBulletinItem) {
        signbarBulletinItem.onBulletin();
        nativeLib.sendCharBulletin(this.fd,(char)0xaa,signbarBulletinItem.getCh(0),signbarBulletinItem.getCh(1),signbarBulletinItem.getCh(2));
        nativeLib.sendCharBulletin(this.fd,(char)0xaa,signbarBulletinItem.getCh(0),signbarBulletinItem.getCh(1),(char)signbarBulletinItem.getCurrentBrighness());

    }

    public void offBulletin() {
        signbarBulletinItem.offBulletin();
        nativeLib.sendCharBulletin(this.fd,(char)0xaa,signbarBulletinItem.getCh(0),signbarBulletinItem.getCh(1),signbarBulletinItem.getCh(2));

    }
}
