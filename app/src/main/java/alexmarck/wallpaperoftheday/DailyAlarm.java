package alexmarck.wallpaperoftheday;

/**
 * Created by Alex on 6/6/2017.
 */
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;
import android.os.StrictMode;

import java.util.Calendar;
import java.util.TimeZone;

public class DailyAlarm extends BroadcastReceiver {

    public static boolean alarmStatus = false;

    SharedPreferences pref;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        setThreadPolicy();
        pref = context.getSharedPreferences("MyPref", context.MODE_PRIVATE);
        if(pref.getInt("alarmStatus", 0) == 0) cancelAlarm(context);

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        Calendar gmtCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        new DownloadAndSetWallpaper(context).execute(gmtCal.get(Calendar.DAY_OF_MONTH), gmtCal.get(Calendar.MONTH)+1, gmtCal.get(Calendar.YEAR), 0, 0);

        wl.release();
    }

    public void setAlarm(Context context)
    {
        pref = context.getSharedPreferences("MyPref", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("alarmStatus", 1);
        editor.commit();

        alarmStatus = true;
        AlarmManager am =(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent("alexmarck.wallpaperoftheday.START_ALARM");
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 86400000, pi); // Millisec, one day
    }

    public void cancelAlarm(Context context)
    {
        pref = context.getSharedPreferences("MyPref", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("alarmStatus", 0);
        editor.commit();

        alarmStatus = false;

        //Intent intent = new Intent(context, DailyAlarm.class);
        Intent intent = new Intent("alexmarck.wallpaperoftheday.START_ALARM"); //let's try this
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    public void setThreadPolicy() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }
}