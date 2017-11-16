package alexmarck.wallpaperoftheday;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class AutomaticDailyChanging extends AppCompatActivity {

    private TextView alarmStatusTextView;

    Handler h = new Handler();
    int serviceCheckInterval = 1000; //how often to check alarm status, currently every 1 second
    Runnable runnable;

    DailyAlarm alarm = new DailyAlarm();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_automatic_daily_changing);
        alarmStatusTextView = (TextView) findViewById(R.id.alarmStatusTextView);
    }

    @Override
    protected void onStart() {
        //start handler as activity become visible

        h.postDelayed(new Runnable() {
            public void run() {
                updateAlarmStatus();

                runnable=this;

                h.postDelayed(runnable, serviceCheckInterval);
            }
        }, serviceCheckInterval);

        super.onStart();
    }

    @Override
    protected void onPause() {
        h.removeCallbacks(runnable); //stop handler when activity not visible
        super.onPause();
    }

    public void startAlarm(View view) {
        alarm.setAlarm(getApplicationContext()); //getapplicationcontext was "this"
        updateAlarmStatus();
    }

    public void stopAlarm(View view) {
        alarm.cancelAlarm(getApplicationContext());
        updateAlarmStatus();
    }

    private void updateAlarmStatus() {
        SharedPreferences prefs = getSharedPreferences("MyPref", MODE_PRIVATE);
        int alarmStatus = prefs.getInt("alarmStatus", 0);

        if(alarmStatus == 1){
            alarmStatusTextView.setText("Daily Changing: On");
            alarmStatusTextView.setTextColor(Color.GREEN);
        } else {
            alarmStatusTextView.setText("Daily Changing: Off");
            alarmStatusTextView.setTextColor(Color.RED);
        }
    }
}
