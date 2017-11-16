package alexmarck.wallpaperoftheday;

/**
 * Created by Alex on 6/3/2017.
 */

import android.app.Service;
import android.app.WallpaperManager;
import android.content.*;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.*;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Random;

public class BackgroundService extends Service {

    public Context context = this;
    public Handler handler = null;
    public static Runnable runnable = null;

    public static boolean serviceStatus = false;

    public Calendar getRandomDayMonthYear(){
        Random rand = new Random();
        Calendar cal = Calendar.getInstance();

        int currentDay = cal.get(Calendar.DAY_OF_MONTH);
        int currentMonth = cal.get(Calendar.MONTH);
        int currentYear = cal.get(Calendar.YEAR);

        int randomDay = rand.nextInt(27) + 1;

        int randomYear = rand.nextInt(currentYear-2004) + 2005; //get a random year between 2005 and current year
        int randomMonth = rand.nextInt(11) + 1;

        if(randomYear == currentYear) { //if the random year is the current year
            while(currentMonth >= randomMonth) { //keep trying until random month is on or before current month
                randomMonth = rand.nextInt(11) + 1; //this makes sure it doesn't select a future month
            }

            if(currentMonth == randomMonth) { //same with days
                while(currentDay >= randomDay-1) {
                    randomDay = rand.nextInt(27) + 1;
                }
            }
        }

        cal.set(randomYear, randomMonth-1, randomDay);
        return cal;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "Service created", Toast.LENGTH_LONG).show();
        serviceStatus = true;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        final int input = pref.getInt("timeBetweenChanges", 0);

            handler = new Handler();

            runnable = new Runnable() {
                public void run() {

                    if(serviceStatus) {
                        MainActivity mainA = new MainActivity();
                        Calendar cal = getRandomDayMonthYear();

                       // Bitmap wallpaperBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.diamondrock); //initialize

                        new DownloadAndSetWallpaper(getApplicationContext()).execute(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR), 0, 1);

                        handler.postDelayed(runnable, input*1000*60);
                    }
                }
            };

        handler.postDelayed(runnable, input*1000*60);
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacks(runnable);
        Toast.makeText(this, "Service stopped", Toast.LENGTH_LONG).show();
        serviceStatus = false;
        stopSelf();
        super.onDestroy();
    }

    @Override
    public void onStart(Intent intent, int startid) {
        //Toast.makeText(this, "Service started by user.", Toast.LENGTH_LONG).show();
        //return START_NOT_STICKY;
    }
}