package alexmarck.wallpaperoftheday;

/**
 * Created by Alex on 6/6/2017.
 */
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class AutoStartAlarm extends BroadcastReceiver
{
    DailyAlarm alarm = new DailyAlarm();
   // SharedPreferences pref;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        SharedPreferences pref = context.getSharedPreferences("MyPref", context.MODE_PRIVATE);

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
        {
            if (pref.contains("alarmStatus") == false || pref.getInt("alarmStatus", 0) == 0) {
                //dont start alarm if alarmstatuspref does not exist or if selected alarm status is off
            } else {
                alarm.setAlarm(context); //start alarm
            }
        }
    }

}