package alexmarck.wallpaperoftheday;

//app id: ca-app-pub-9244211598141649~1990368210

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;

import org.json.*;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;

import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private TextView topText;

    private JSONObject json;
    private JSONObject query;
    private URL returnUrl;
    private Bitmap wallpaperBitmap;

    //Context context; //testing

    //START PREF MENU TESTING

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        switch(item.getItemId())
        {
            case R.id.preferences:
            {
                Intent intent = new Intent();
                intent.setClassName(this, "alexmarck.wallpaperoftheday.MyPreferenceActivity");
                startActivity(intent);
                return true;
            }

            case R.id.imageinformation:
            {
                Intent viewLicenseIntent  = new Intent(this, LicenseActivity.class); //second parameter is activity you want to navigate to
                startActivity(viewLicenseIntent);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);


    }
    //END PREF MENU TESTING

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //context = getApplicationContext(); //tersting
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", getApplicationContext().MODE_PRIVATE);
        if(pref.getBoolean("hasRunOnce", false) == false) {
            onFirstRun();
            SharedPreferences.Editor editor = pref.edit();
            editor.putBoolean("hasRunOnce", true);
            editor.commit();
        };

    }

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

    private void onFirstRun() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        builder.setTitle("Use Mobile Data?");
        builder.setMessage("Wallpapers can be very large in size and use a significant amount of data. Do you want to download wallpapers over mobile data? You can change this option later in the settings.");

        AlertDialog.Builder builder1 = builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("pref_mobileDataEnabled", true);
                editor.commit();
                // Do nothing, but close the dialog
                dialog.dismiss();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                //SharedPreferences pref = getApplicationContext().getSharedPreferences("fragment_preference", getApplicationContext().MODE_PRIVATE);
                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = pref.edit();
                editor.putBoolean("pref_mobileDataEnabled", false);
                editor.commit();
                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    public void showDownloadToast() {
        Toast.makeText(this, "Fetching wallpaper...", Toast.LENGTH_LONG).show();
    }

    public void setTodayImage(View view) {
        Calendar gmtCal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        showDownloadToast();
        new DownloadAndSetWallpaper(getApplicationContext()).execute(gmtCal.get(Calendar.DAY_OF_MONTH), gmtCal.get(Calendar.MONTH)+1, gmtCal.get(Calendar.YEAR), 1, 0);
    }

    public void changeWallpaperRandom(View view) {
        Calendar cal = getRandomDayMonthYear();
        showDownloadToast();
        new DownloadAndSetWallpaper(getApplicationContext()).execute(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR), 1, 1);
    }

    public void pickDay(View view) {
        Intent pickDayIntent  = new Intent(this, PickDayActivity.class); //second parameter is activity you want to navigate to
        startActivity(pickDayIntent);
    }

    public void setAutomaticChanging(View view) {
        Intent automaticChangingIntent  = new Intent(this, AutomaticChanging.class); //second parameter is activity you want to navigate to
        startActivity(automaticChangingIntent);
    }

    public void setDailyAutomaticChanging(View view) {
        Intent dailyAutomaticChangingIntent  = new Intent(this, AutomaticDailyChanging.class); //second parameter is activity you want to navigate to
        startActivity(dailyAutomaticChangingIntent);
    }

    /*
    public void viewLicense(View view) {
        Intent viewLicenseIntent  = new Intent(this, LicenseActivity.class); //second parameter is activity you want to navigate to
        startActivity(viewLicenseIntent);
    }
    */

}
