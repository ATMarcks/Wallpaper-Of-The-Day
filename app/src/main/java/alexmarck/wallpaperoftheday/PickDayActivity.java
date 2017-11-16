package alexmarck.wallpaperoftheday;

import android.app.DatePickerDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;

public class PickDayActivity extends AppCompatActivity {

    Calendar cal = Calendar.getInstance();
    private int currentYear= cal.get(Calendar.YEAR);
    private int currentMonth = cal.get(Calendar.MONTH);
    private int currentDay = cal.get(Calendar.DAY_OF_MONTH);

    private int previewYear = 0;
    private int previewMonth = 0;
    private int previewDay = 0;

    DatePicker datePicker;
    ImageView previewImageView;

    private URL returnUrl;
    private Bitmap wallpaperBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_day);

        datePicker = (DatePicker) findViewById(R.id.mainDatePicker);

        Calendar minDate = Calendar.getInstance();
        minDate.set(2005, 4, 14); //Year, Month -1,Day
        datePicker.setMinDate(minDate.getTimeInMillis());

        Calendar maxDate = Calendar.getInstance();
        maxDate.set(currentYear, currentMonth, currentDay-1); //Year, Month -1,Day
        datePicker.setMaxDate(maxDate.getTimeInMillis());

    }

    public void previewWallpaper(View view) {
        Toast.makeText(this, "Fetching preview image...", Toast.LENGTH_LONG).show();
        new PreviewWallpaper(this).execute(datePicker.getDayOfMonth(), datePicker.getMonth() + 1, datePicker.getYear());

        previewMonth = datePicker.getMonth() + 1;
        previewDay = datePicker.getDayOfMonth();
        previewYear = datePicker.getYear();
    }

    public void setWallpaper(View view) {
        Toast.makeText(this, "Fetching wallpaper...", Toast.LENGTH_LONG).show();
        if(previewDay == 0) { //if no preview is selected
            new DownloadAndSetWallpaper(getApplicationContext()).execute(datePicker.getDayOfMonth(), datePicker.getMonth() + 1, datePicker.getYear(), 1, 0);
            //update wallpaper to currently selected date
        } else {
            //set wallpaper to preview
            new DownloadAndSetWallpaper(getApplicationContext()).execute(previewDay, previewMonth, previewYear, 1, 0);//otherwise set to preview choice
        }
    }

    public void returnToMain(View view) {
        Intent mainIntent  = new Intent(this, MainActivity.class); //second parameter is activity you want to navigate to
        startActivity(mainIntent);
    }
}
