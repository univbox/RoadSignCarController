package com.deviantce.serial_bulletin_library;


public class BulletinItem {
    private String send_text;
    private String color;
    private int speed;
    private int delay;
    private String blink;
    private int icon_id;
    private String direction;

    public BulletinItem(){


    }


    /**
     * <p>전광판 보내는 문자열 하나</p>
     * @param text 보낼 문자열
     * @param color 문자열 색상 황 , 녹 , 적 , 청
     * @param speed 전송 속도 1 ~ 5
     * @param delay 전송 딜레이 1 ~ 5
     * @param blink 점멸 Y , N
     * @param icon_id 이미지 id
     * @param direction 방향 좌("0"), 우("1")
     */
    public BulletinItem(String send_text,String color,int speed,int delay,String blink,int icon_id, String direction){
        this.icon_id = icon_id;
        this.send_text = send_text;
        this.color = color;
        this.speed = speed;
        this.delay = delay;
        this.blink = blink;
        this.direction = direction;
    }



    public String getSend_textText() {
        return send_text;
    }

    public void setSend_text(String text) {
        this.send_text = text;
    }


    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getDelay() {
        return delay;
    }

    public void setDelay(int delay) {
        this.delay = delay;
    }

    public String getBlink() {
        return blink;
    }

    public void setBlink(String blink) {
        this.blink = blink;
    }

    public int getIcon_id() {
        return icon_id;
    }

    public void setIcon_id(int icon_id) {
        this.icon_id = icon_id;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }
}
