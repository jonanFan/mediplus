package es.mediplus.mediplus;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by inigo on 8/04/16.
 */

public class ReminderBootActivator extends BroadcastReceiver{

    private static String TAG=".ReminderBootActivator";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            //TODO: inigo: mirar aver si onBoot va bien
            Log.d (TAG, "inigo: me ha llegado el boot_completed");
            ReminderManager.setRemindersOnBoot(context);
        }
    }
}
