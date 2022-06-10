package com.deviantce.serial_bulletin_library;

import android.util.Log;

import com.deviantce.serial_bulletin_library.NativeLib;

public class SerialLight{
    private String default_port_name = SerialConstants.DEFAULT_LIGHT_PORT;
    private int default_port_baud_rate = SerialConstants.DEFAULT_LIGHT_BAUD_RATE;


    private String TAG = getClass().getSimpleName();
    int fd = -1;
    NativeLib nativeLib;
    LightItem prev_light_item = new LightItem();
    LightItem lightItem = new LightItem();

    private LightSerialListener mListener; // Synchronized by 'this'

    private LightSerialThread readThread;

    boolean thread_lock = false;




    public interface LightSerialListener {

        /**
         * <p>새로운 데이터가 들어왔을 때</p>
         */
        void onNewData( );
        void onRunError(Exception e);
    }


    public SerialLight(LightSerialListener listener){
        nativeLib  = new NativeLib();
        this.fd = -1;
        this.lightItem = new LightItem();
        this.mListener = listener;
    }

    public boolean connect(){
        this.fd = nativeLib.getLightFdWithPortAndBaud(default_port_name,default_port_baud_rate);
        startThread();
        if(this.fd != -1)
            return true;
        return false;
    }
    public boolean connect(String port){

        return true;
    }
    public void setLightStatus(LightItem item){
        nativeLib.sendChar(this.fd,item.getCh(0),item.getCh(1),item.getCh(2));
    }
    public LightItem getLightStatus(){
        return prev_light_item;
    }

    public void setWl_1(boolean status){
        thread_lock  = true;
        lightItem.setWl_1(status);
        setLightStatus(lightItem);
        thread_lock = false;
    }

    public boolean isWl_1(){
        return lightItem.isWl_1();
    }

    public void setWl_2(boolean status){
        thread_lock = true;
        lightItem.setWl_2(status);
        setLightStatus(lightItem);
        thread_lock = false;
    }

    public boolean isWl_2(){
        return lightItem.isWl_2();
    }

    public void setWl_3(boolean status){
        thread_lock = true;
        lightItem.setWl_3(status);
        setLightStatus(lightItem);
        thread_lock = false;
    }

    public boolean isWl_3(){
        return lightItem.isWl_3();
    }

    public boolean isPower(){
        return lightItem.isPower();
    }

    public void setWnk(boolean status){
        thread_lock = true;
        lightItem.setWnk(status);
        setLightStatus(lightItem);
        thread_lock = false;
    }

    public boolean isWnk(){
        return lightItem.isWnk();
    }

    public void setYelp(boolean status){
        lightItem.setYelp(status);
        setLightStatus(lightItem);
    }

    public boolean isYelp(){
        return lightItem.isYelp();
    }

    public void setWail(boolean status){
        lightItem.setWail(status);
        setLightStatus(lightItem);
    }

    public void setPower(boolean power){
        lightItem.setPower(power);
        setLightStatus(lightItem);
    }

    public boolean isWail(){
        return lightItem.isWail();
    }

    public void setHi_lo(boolean status){
        lightItem.setHi_lo(status);
        setLightStatus(lightItem);
    }

    public boolean isHi_lo(){
        return lightItem.isHi_lo();
    }

    public int getVolume() {
        return lightItem.getVolume();
    }

    public void volumeUp( ){
        lightItem.setVolumeUpEnable();
        nativeLib.sendChar(this.fd,lightItem.getCh(0),lightItem.getCh(1),lightItem.getCh(2));

        lightItem.setVolumeUpDisable();
        nativeLib.sendChar(this.fd,lightItem.getCh(0),lightItem.getCh(1),lightItem.getCh(2));
    }
    public void volumeDown( ){
        lightItem.setVolumeDownEnable();
        nativeLib.sendChar(this.fd,lightItem.getCh(0),lightItem.getCh(1),lightItem.getCh(2));

        lightItem.setVolumeDownDisable( );
        nativeLib.sendChar(this.fd,lightItem.getCh(0),lightItem.getCh(1),lightItem.getCh(2));
    }
    public void sendVoice(int no){

    }
    public void stopVoice(){

    }



    public int readData(){
        Log.d(TAG,"this.fd: " + fd);

            int read_data = nativeLib.readData(this.fd);
            char ch1 = (char) (read_data >> 16 & 0xFF);
            char ch2 = (char) (read_data >> 8 & 0xFF);
            char ch3 = (char) (read_data & 0xFF);

            char[] send_data = new char[3];
            send_data[0] = ch1;
            send_data[1] = ch2;
            send_data[2] = ch3;



            //if(read_data!=0 &&  prev_light_item.getCh(0) != send_data[0] || prev_light_item.getCh(1)!=send_data[1] || prev_light_item.getCh(2) != send_data[2]){
            //if(read_data!=0 &&  !prev_light_item.equals(new LightItem(send_data))){

            if(read_data!=0 && !thread_lock) {
//                prev_light_item = new LightItem(send_data);
                lightItem = new LightItem(send_data);
                mListener.onNewData();
                return read_data;
            }

        return 0;
    }

    public void sendChar(char ch1, char ch2, char ch3) {
        nativeLib.sendChar(this.fd,ch1,ch2,ch3);
    }

    private void startThread(){

        if(readThread == null){
            readThread = new LightSerialThread();
            readThread.start();
        }

    }

    private void stopThread(){
        if(readThread!=null && readThread.isAlive())
            readThread.interrupt();
        if(readThread!=null){
            readThread.setKillSign(true);
            readThread = null;
        }
    }

    private class LightSerialThread extends Thread {
        private boolean kill = false;

        @Override
        public synchronized void run() {

            while(!Thread.interrupted()){
              if(fd!=-1) {
                      try {
                          Thread.sleep(70);
                      } catch (InterruptedException ignore) {
                      }

                      try {
                              int read_data = readData();
                              if (read_data != 0) {
                                  char ch1 = (char) (read_data >> 16 & 0xFF);
                                  char ch2 = (char) (read_data >> 8 & 0xFF);
                                  char ch3 = (char) (read_data & 0xFF);


                                  if(!thread_lock)
                                    sendChar(ch1, ch2, ch3);
                                  Log.d("LightDebug", "Thread ch1 " + String.format("%02x", ch1 & 0xff));
                                  Log.d("LightDebug", "Thread ch2 " + String.format("%02x", ch2 & 0xff));
                                  Log.d("LightDebug", "Thread ch3 " + String.format("%02x", ch3 & 0xff));
                              } else {
                                  Log.d("LightDebug", "read data 0");
                              }


                      } catch (Exception e) {
                          Log.e("LightDebug", e.getLocalizedMessage());
                      }

              }

            }

        }

        public void setKillSign(boolean kill){
            this.kill = kill;
        }
    }

    @Override
    public String toString() {
        return "SerialLight{" +
                "default_port_name='" + default_port_name + '\'' +
                ", default_port_baud_rate=" + default_port_baud_rate +
                ", TAG='" + TAG + '\'' +
                ", fd=" + fd +
                ", nativeLib=" + nativeLib +
                ", prev_light_item=" + prev_light_item +
                ", lightItem=" + lightItem +
                ", mListener=" + mListener +
                ", readThread=" + readThread +
                ", thread_lock=" + thread_lock +
                '}';
    }
}