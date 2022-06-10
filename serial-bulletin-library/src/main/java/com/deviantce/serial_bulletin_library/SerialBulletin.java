package com.deviantce.serial_bulletin_library;

import android.widget.Toast;

import androidx.annotation.RestrictTo;

import com.deviantce.serial_bulletin_library.NativeLib;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class SerialBulletin {

    private String default_port_name = SerialConstants.DEFAULT_BULLETIN_PORT;
    private int default_port_baud_rate = SerialConstants.DEFAULT_BULLETIN_BAUD_RATE;

    private NativeLib nativeLib;
    private int fd = -1;

    /**
     * baud rate 115200
     * port /dev/ttyMAX2
     * @return 전광판 연결 상태
     */
    public SerialBulletin(){
        nativeLib = new NativeLib();
        fd = -1;
    }

    public int connect(){
        this.fd =  nativeLib.getBulletinFd();
        //this.fd = nativeLib.getBulletinFdWithPortAndBaud(default_port_name,default_port_baud_rate);
        return this.fd;
    }

    public boolean connect(String port){

        return true;
    }

    public  boolean connect(String port,int baud_rate){
        this.fd = nativeLib.getBulletinFdWithPortAndBaud(port,baud_rate);
        return true;
    }

    public boolean isConnected(){
        if(this.fd != -1)
            return true;
        return false;
    }

    public boolean sendLetter(BulletinItem item){
        //nativeLib.defaultTest(this.fd);

        char[] smsg = new char[1000];
        String letter = "";
        letter += makeProtoStr(item,0);


        int letter_len = 0;
        try {
            byte []l = letter.getBytes("UTF-16");
            letter_len = l.length;
        } catch (UnsupportedEncodingException e) {
        }
        int len = 0;
        for(int j=0;j<letter.length();j++)
        {
            char x = letter.charAt(j);
            smsg[len] = TOHEX(((x & 0x00F0) >> 4));
            smsg[len+1] = TOHEX((x & 0x000F));
            smsg[len+2] = TOHEX(((x & 0xF000) >> 12));
            smsg[len+3] = TOHEX(((x & 0x0F00) >> 8));
            len+=4;
        }
        try {
            Thread.sleep(300);
            nativeLib.sendTextBulletinWithEFF(fd,smsg,len,letter_len);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return true;
    }

    public boolean sendMultipleLetter(ArrayList<BulletinItem> bulletin_items){
        char[] smsg = new char[1000];
        String send_text = "";
        for(int i=0;i<bulletin_items.size();i++){
            BulletinItem seletedItem = bulletin_items.get(i);

            String letter = "";
            letter += makeProtoStr(seletedItem,i);

            send_text += seletedItem.getSend_textText();



            int letter_len = 0;
            try {
                byte []l = letter.getBytes("UTF-16");
                letter_len = l.length;
            } catch (UnsupportedEncodingException e) {
            }
            int len = 0;
            for(int j=0;j<letter.length();j++)
            {
                char x = letter.charAt(j);
                smsg[len] = TOHEX(((x & 0x00F0) >> 4));
                smsg[len+1] = TOHEX((x & 0x000F));
                smsg[len+2] = TOHEX(((x & 0xF000) >> 12));
                smsg[len+3] = TOHEX(((x & 0x0F00) >> 8));
                len+=4;
            }
            try {
                Thread.sleep(300);
                nativeLib.sendTextBulletinWithEFF(fd,smsg,len,letter_len);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    private String makeProtoStr(BulletinItem seletedItem, int i) {
        String letter = "";
        if(i==0)
            letter+=",RST=1";

        letter += ",SPD=";
        letter += String.valueOf(seletedItem.getSpeed());
        letter += ",DLY=";
        letter += String.valueOf(seletedItem.getDelay());

        String blink = seletedItem.getBlink();

        if(blink.equals("N")){
            String direction = seletedItem.getDirection();
            letter += ",EFF=";
            letter += ("040"+direction);
            letter += ("040"+direction);
            letter += ("040"+direction);
        }
        else{
            letter += ",EFF=090009000900";
        }


        int icon_id = seletedItem.getIcon_id();
        if(icon_id!=0){
            letter += ",USM=0";
            if(icon_id>=10)
                letter += String.valueOf(icon_id);
            else{
                letter += "0"+String.valueOf(icon_id);
            }
            if(icon_id>=11)
                letter += "X0000Y0000W0128H0032F1,TSX=0032";
            else
                letter += "X0000Y0000W0032H0032F1,TSX=0032";
        }
        letter +=  ",TXT=$f00";
        if(blink.equals("O"))
        {
            letter += "$a01";
        }
        if(seletedItem.getColor().equals("황"))
            letter += "$c02";
        else if(seletedItem.getColor().equals("녹"))
            letter += "$c03";
        else if(seletedItem.getColor().equals("적"))
            letter += "$c01";
        else if(seletedItem.getColor().equals("청"))
            letter += "$c05";
        letter += seletedItem.getSend_textText();
        return letter;
    }

    private char TOHEX(int i){
        if (i<=9)
            return (char) ('0'+i);
        return (char) ('A'-10+i);
    }
}
