package com.deviantce.roadsigncarcontroller.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.deviantce.roadsigncarcontroller.IndexActivity;
import com.deviantce.roadsigncarcontroller.MainActivity;

public class BootUpReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

//        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        PendingIntent pi = PendingIntent.getService(context, 0, new Intent(context, IndexActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
//        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1500, 500, pi);
//
//
//        Intent i = new Intent(context, Index.class);
//
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        //i.putExtra("start",'O');
//        context.startActivity(i);
//

        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Intent it = new Intent(context, IndexActivity.class);
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(it);
        }

    }
}
