package com.deviantce.serial_bulletin_library;

import android.util.Log;

import java.util.Arrays;

/**
 * <p>
 * <b>data byte-1</b>
 *
 * YELP	    0x01
 * WAIL	    0x02
 * H_L		0x04
 * AUX		0x08
 * WL1		0x10
 * WL2		0x20
 * WL3		0x40
 * WNK		0x80
 * </p>
 *
 * <p>
 * <b>data byte-2</b>
 *
 * POWER ON/OFF		0x01
 * UP		0x02
 * DN		0x04
 * 비상      0x08
 *
 * 수신(숫자표시)
 * 데이터 받아서  가운데하단 숫자 표시
 * </p>
 *
 * <p>
 * <b>data byte-3</b>
 *
 * 볼륨 0	0x00
 * 볼륨 1	0x01
 * 볼륨 2	0x02
 * 볼륨 3	0x03
 * 볼륨 4	0x04
 * 볼륨 5	0x05
 * 볼륨 6	0x06
 * 볼륨 7	0x07
 * 볼륨 8	0x08
 * 볼륨 9	0x09
 * 볼륨 10	0x0a
 * 볼륨 11	0x0b
 * 볼륨 12	0x0c
 * 볼륨 13	0x0d
 * 볼륨 14	0x0e
 * 볼륨 15	0x0f
 * </p>
 */
public class LightItem{
    private final String TAG = getClass().getSimpleName();

    char[] send_data;
    private boolean power;
    private boolean yelp;
    private boolean wail;
    private boolean hi_lo;
    private boolean wl_1;
    private boolean wl_2;
    private boolean wl_3;
    private boolean wnk;
    private int volume;

    final char CODE_WL1 = 0x10;
    final char CODE_WL2 = 0x20;
    final char CODE_WL3 = 0x40;
    final char CODE_WNK = 0x80;

    final char CODE_YELP = 0x01;
    final char CODE_WAIL = 0x02;
    final char CODE_HILO = 0x04;

    final char CODE_POWER = 0x01;
    final char CODE_VOLUME_UP = 0x02;
    final char CODE_VOLUME_DOWN = 0x04;

    final int OFF = 0;
    final int YELP = 1;
    final int WAIL = 2;
    final int HILO = 3;

    public LightItem(){
        send_data = new char[3];
    }

    public LightItem(char[] send_data){

        this.send_data = send_data;
        makeByteData();

    }

    public char getCh(int i){
        if(i<=2 && i>=0)
            return send_data[i];
        else
            return 0;
    }

    public boolean isWl_1() {
        if((send_data[0] & CODE_WL1) == 0){
            this.wl_1 = false;
        }
        else
            this.wl_1 = true;
        return wl_1;
    }


    private void setSoundStatus(int type){
        if(type==YELP){
            send_data[0] |= CODE_YELP;
            send_data[0] &= (~CODE_WAIL);
            send_data[0] &= (~CODE_HILO);
        }
        else if(type==WAIL){
            send_data[0] |= CODE_WAIL;
            send_data[0] &= (~CODE_YELP);
            send_data[0] &= (~CODE_HILO);
        }
        else if(type==HILO){
            send_data[0] |= CODE_HILO;
            send_data[0] &= (~CODE_WAIL);
            send_data[0] &= (~CODE_YELP);
        }
        else if(type==OFF){
            send_data[0] &= (~CODE_HILO);
            send_data[0] &= (~CODE_WAIL);
            send_data[0] &= (~CODE_YELP);
        }
    }

    private void setWLStatus(int type){
        if(type==0){ // 전체 끄기
            send_data[0] &= (~CODE_WL1);
            send_data[0] &= (~CODE_WL2);
            send_data[0] &= (~CODE_WL3);
            send_data[0] &= (~CODE_WNK);
        }
        else if(type==1){ // WL-1
            send_data[0] |= CODE_WL1;
            send_data[0] &= (~CODE_WL2);
            send_data[0] &= (~CODE_WL3);
            send_data[0] &= (~CODE_WNK);
        }
        else if(type==2){ // WL-2
            send_data[0] |= CODE_WL2;
            send_data[0] &= (~CODE_WL1);
            send_data[0] &= (~CODE_WL3);
            send_data[0] &= (~CODE_WNK);
        }
        else if(type==3){ // WL-3
            send_data[0] |= CODE_WL3;
            send_data[0] &= (~CODE_WL2);
            send_data[0] &= (~CODE_WL1);
            send_data[0] &= (~CODE_WNK);
        }
        else if(type==4){ // WNK
            send_data[0] |= CODE_WNK;
            send_data[0] &= (~CODE_WL2);
            send_data[0] &= (~CODE_WL3);
            send_data[0] &= (~CODE_WL1);
        }

    }

    public boolean isWl_2() {
        if((send_data[0] & CODE_WL2) == 0){
            this.wl_2 = false;
        }
        else
            this.wl_2 = true;

        return wl_2;
    }


    public void setWl_1(boolean wl_1) {
        this.wl_1 = wl_1;
        if(wl_1){
            setWLStatus(1);
        }
        else{
            setWLStatus(0);
        }
    }

    public void setWl_2(boolean wl_2) {
        this.wl_2 = wl_2;
        if(wl_2){
            setWLStatus(2);
        }
        else{
            setWLStatus(0);
        }
    }


    public boolean isWl_3() {
        if((send_data[0] & CODE_WL3) == 0){
            this.wl_3 = false;
        }
        else
            this.wl_3 = true;

        return wl_3;
    }

    public void setWl_3(boolean wl_3) {
        this.wl_3 = wl_3;
        if(wl_3){
            setWLStatus(3);
        }
        else{
            setWLStatus(0);
        }
    }

    public boolean isWnk() {
        if((send_data[0] & CODE_WNK) == 0){
            this.wnk = false;
        }
        else
            this.wnk = true;

        return wnk;
    }

    public void setWnk(boolean wnk) {
        this.wnk = wnk;
        if(wnk){
            setWLStatus(4);
        }
        else{
            setWLStatus(0);
        }
    }

    public int getVolume() {
        char v = (char) (send_data[2] & 0xff);
        String hex = String.format("%02x",v& 0xff);
        Log.d(TAG,"volum e v: "  + String.format("%02x",v& 0xff));
        //this.volume = Character.digit(v,16);
        this.volume = Integer.parseInt(hex,16);
        Log.d(TAG,"volume: "  + volume);
        return volume;
    }

    public void setVolume(int volume) {
        this.volume = volume;
    }

    public boolean isPower() {
        if((send_data[1] & CODE_POWER) == 0){
            this.power = false;
        }
        else
            this.power = true;

        return power;
    }

    public void setPower(boolean power) {
        if(power)
            send_data[1] |= CODE_POWER;
        else
            send_data[1] &= (~CODE_POWER);
        this.power = power;
    }

    public boolean isYelp() {

        if((send_data[0] & CODE_YELP) == 0){
            this.yelp = false;
        }
        else
            this.yelp = true;

        return yelp;
    }

    public void setYelp(boolean yelp) {
        this.yelp = yelp;
        if(yelp){
            setSoundStatus(YELP);
        }
        else{
            setSoundStatus(OFF);
        }
    }

    public boolean isWail() {
        if((send_data[0] & CODE_WAIL) == 0){
            this.wail = false;
        }
        else
            this.wail = true;

        return wail;
    }

    public void setWail(boolean wail) {
        this.wail = wail;
        if(wail){
            setSoundStatus(WAIL);
        }
        else{
            setSoundStatus(OFF);
        }
    }

    public boolean isHi_lo() {
        if((send_data[0] & CODE_HILO) == 0){
            this.hi_lo = false;
        }
        else
            this.hi_lo = true;

        return hi_lo;
    }

    public void setHi_lo(boolean hi_lo) {
        this.hi_lo = hi_lo;
        if(hi_lo){
            setSoundStatus(HILO);
        }
        else{
            setSoundStatus(OFF);
        }
    }

    public void makeByteData() {
        isWl_1();
        isWl_2();
        isWl_3();
        isWnk();
        isYelp();
        isWail();
        isHi_lo();
        getVolume();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LightItem)) return false;
        LightItem lightItem = (LightItem) o;
        return ( (send_data[0]==lightItem.send_data[0]) &&
                (send_data[1]==lightItem.send_data[1]) &&
                (send_data[2]==lightItem.send_data[2]));
        //return Arrays.equals(send_data, lightItem.send_data);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(send_data);
    }

    @Override
    public String toString() {
        return "LightItem{" +
                "send_data 0=" +String.format("%02x",getCh(0) & 0xff) +
                "send_data 1=" +String.format("%02x",getCh(1) & 0xff) +
                "send_data 2=" +String.format("%02x",getCh(2) & 0xff) +
                ", power=" + power +
                ", yelp=" + yelp +
                ", wail=" + wail +
                ", hi_lo=" + hi_lo +
                ", wl_1=" + wl_1 +
                ", wl_2=" + wl_2 +
                ", wl_3=" + wl_3 +
                ", wnk=" + wnk +
                ", volume=" + volume +
                '}';
    }

    public void setVolumeUpEnable() {
        send_data[1] |= CODE_VOLUME_UP;
    }

    public void setVolumeUpDisable() {
        send_data[1] &= (~CODE_VOLUME_UP);
    }

    public void setVolumeDownEnable() {
        send_data[1] |= CODE_VOLUME_DOWN;
    }

    public void setVolumeDownDisable() {
        send_data[1] &= (~CODE_VOLUME_DOWN);
    }
}