package alexmarck.wallpaperoftheday;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class AutomaticChanging extends AppCompatActivity {

    private EditText hoursEditText;
    private TextView serviceStatusTextView;

    Intent backgroundIntent;

    //testing
    Handler h = new Handler();
    int serviceCheckInterval = 1000; //1 second
    Runnable runnable;
    //testing

    @Override
    protected void onStart() {
    //start handler as activity become visible

        h.postDelayed(new Runnable() {
            public void run() {
                updateServiceStatus();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automatic_changing);

        hoursEditText = (EditText) findViewById(R.id.hoursEditText);
        serviceStatusTextView = (TextView) findViewById(R.id.serviceStatusTextView);
        backgroundIntent = new Intent(this, BackgroundService.class);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        if(pref.contains("timeBetweenChanges") == false) {
            hoursEditText.setText("0");
        } else {
            hoursEditText.setText(Integer.toString(pref.getInt("timeBetweenChanges", 0)));
        }

        updateServiceStatus();
    }

    public void saveAndStart(View view) {
        int hoursSelectionInt;

        if(hoursEditText.getText().toString().isEmpty()) {
            hoursSelectionInt = 10;
        } else {
            hoursSelectionInt = Integer.parseInt(hoursEditText.getText().toString());
        }

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("timeBetweenChanges", hoursSelectionInt);
        editor.commit();

        startService(backgroundIntent);

        updateServiceStatus();
    }

    public void stopBackgroundService(View view){
        stopService(backgroundIntent);
        updateServiceStatus();
    }

    public void updateServiceStatus() {
        if(BackgroundService.serviceStatus == true){
            serviceStatusTextView.setText("Service is currently running");
            serviceStatusTextView.setTextColor(Color.GREEN);
        } else {
            serviceStatusTextView.setText("Service is not running");
            serviceStatusTextView.setTextColor(Color.RED);
        }
    }

    public void goBack(View view) {
        Intent mainActivityIntent  = new Intent(this, MainActivity.class); //second parameter is activity you want to navigate to
        startActivity(mainActivityIntent);
    }
}
