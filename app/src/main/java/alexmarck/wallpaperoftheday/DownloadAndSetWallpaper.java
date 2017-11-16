package alexmarck.wallpaperoftheday;

import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Alex on 6/5/2017.
 */

public class DownloadAndSetWallpaper extends AsyncTask<Integer, Void, Integer> {

    private JSONObject json;
    private JSONObject query;
    private JSONObject secondQuery;
    private JSONObject secondJson;

    private URL returnUrl;
    private URL wikimediaBaseURL;
    private Bitmap wallpaperBitmap;
    private Bitmap croppedWallpaperBitmap;

    private Integer returnInteger = 1; //1 means no errors

    private Context context; //testing

    public DownloadAndSetWallpaper(Context context) {
        this.context = context;
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

    private void clearBitmaps() {
        if(wallpaperBitmap != null) { //fixes memory errors
            wallpaperBitmap.recycle();
            wallpaperBitmap = null;
        }

        if(croppedWallpaperBitmap != null) { //fixes memory errors
            croppedWallpaperBitmap.recycle();
            croppedWallpaperBitmap = null;
        }
    }

    public String grabURL(int day, int month, int year, int isRandom) throws MalformedURLException, IOException {
        String monthString; String dayString;

        String imageURL = "Error finding URL";
        String imageDescriptionURL = "Error finding URL";
        String author = "Click 'view more information online' to view author";
        String license = "Click 'view more information online' to view license";
        String imageName = "Click 'view more information online' to view name";

        if(day < 10) {
            dayString = "0"+Integer.toString(day);
        } else {
            dayString = Integer.toString(day);
        }

        if(month < 10) {
            monthString = "0"+Integer.toString(month);
        } else {
            monthString = Integer.toString(month);
        }

        String yearString = Integer.toString(year);

        String url="https://commons.wikimedia.org/w/api.php?action=query&generator=images&titles=Template:Potd/"+yearString+"-"+monthString+"-"+dayString+"&prop=imageinfo&iiprop=size&format=json&formatversion=2&iiprop=url";
        String urlMetaData="https://commons.wikimedia.org/w/api.php?action=query&generator=images&titles=Template:Potd/"+yearString+"-"+monthString+"-"+dayString+"&prop=imageinfo&iiprop=size&format=json&formatversion=2&iiprop=extmetadata";
        //the above string can be modified to be any date

        //grab JSON from internet
        try {
            json = new JSONObject(IOUtils.toString(new URL(url), Charset.forName("UTF-8")));
            query = json.getJSONObject("query").getJSONArray("pages").getJSONObject(0).getJSONArray("imageinfo").getJSONObject(0);

            secondJson = new JSONObject(IOUtils.toString(new URL(urlMetaData), Charset.forName("UTF-8")));
            secondQuery = secondJson.getJSONObject("query").getJSONArray("pages").getJSONObject(0).getJSONArray("imageinfo").getJSONObject(0).getJSONObject("extmetadata");
            //end testing

        } catch (JSONException e){
            Log.e("MYAPP", "JSON Exception", e); returnInteger = 0;
        }

        try{
            imageURL = query.getString("url");
            imageDescriptionURL = query.getString("descriptionshorturl");
        } catch (JSONException e) {
            Log.e("MYAPP", "JSON Exception", e); returnInteger = 0;
        }

        //if image changing is random and random image has a .svg extension, get a new random image
        if((imageURL.substring(imageURL.lastIndexOf("."))).toLowerCase().equals(".svg") && isRandom == 1) {
            Log.d("SVG", "random has called SVG!");
            Calendar cal = getRandomDayMonthYear();
            grabURL(cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR), 1);
        } else if ((imageURL.substring(imageURL.lastIndexOf("."))).toLowerCase().equals(".svg") && isRandom == 0) {
            returnInteger = 3;
        }

        try {
            license = secondQuery.getJSONObject("LicenseShortName").getString("value");
        } catch (JSONException e) {
            Log.e("MYAPP", "JSON Exception", e);
        }

        try {
            imageName = secondQuery.getJSONObject("ObjectName").getString("value");
        } catch (JSONException e) {
            Log.e("MYAPP", "JSON Exception", e);
        }

        try {
            author = secondQuery.getJSONObject("Attribution").getString("value");
        } catch (JSONException e) {
            Log.e("MYAPP", "JSON Exception", e);
        }

        Log.d("Metadata URL", urlMetaData);
        Log.d("Image URL", imageURL);

        author = android.text.Html.fromHtml(author).toString();

        SharedPreferences pref = context.getSharedPreferences("MyPref", context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("mostRecentImageLicense", license);
        editor.putString("mostRecentImageName", imageName);
        editor.putString("mostRecentImageAuthor", author);
        editor.putString("mostRecentImageDescriptionURL", imageDescriptionURL);
        editor.commit();

        return imageURL;
    }

    protected Integer doInBackground(Integer... integers) {
        String urlString = "";
        clearBitmaps(); //fixes memory errors

        try {
            urlString = grabURL(integers[0],integers[1], integers[2], integers[4]);
            returnUrl = new URL(urlString);
        } catch(IOException e) {
            Log.e("MYAPP", "IO Exception", e); returnInteger = 0;
        }

        if(returnInteger == 3) return returnInteger;

        try {
            wallpaperBitmap = BitmapFactory.decodeStream(returnUrl.openConnection().getInputStream());
        } catch(IOException e) {
            Log.e("MYAPP", "IO Exception", e); returnInteger = 0;
        }

        WallpaperManager myWallpaperManager
                = WallpaperManager.getInstance(context);

        //below code is for centering wallpaper
        boolean wallpaperSmallerThanScreen = false;

        int screenWidth = 0; int screenHeight = 0;
        int wallpaperHeight = 0; int wallpaperWidth = 0;

        screenWidth = Resources.getSystem().getDisplayMetrics().widthPixels;
        screenHeight = Resources.getSystem().getDisplayMetrics().heightPixels;
        wallpaperHeight = wallpaperBitmap.getHeight();
        wallpaperWidth = wallpaperBitmap.getWidth(); //left, top, right, bottom

        if(screenWidth > wallpaperWidth || screenHeight > wallpaperHeight) {
            wallpaperSmallerThanScreen = true;
        }

        int rectLeft = (wallpaperWidth - screenWidth)/2;
        int rectRight = (screenWidth/2) + (wallpaperWidth/2);
        int rectTop = (wallpaperHeight - screenHeight)/2;
        int rectBottom = (screenHeight/2) + (wallpaperHeight/2);

        Rect rect = new Rect(rectLeft, rectTop, rectRight, rectBottom);

        //assert(rect.left < rect.right && rect.top < rect.bottom);
        croppedWallpaperBitmap = Bitmap.createBitmap(rect.right-rect.left, rect.bottom-rect.top, Bitmap.Config.ARGB_8888);
        //  draw source bitmap into resulting image at given position
        new Canvas(croppedWallpaperBitmap).drawBitmap(wallpaperBitmap, -rect.left, -rect.top, null);


        try {
            //if wallpaper is smaller than screen, edges get cropped off
            //we only want fullscreen wallpapers
            if(wallpaperSmallerThanScreen == false) {
                myWallpaperManager.setBitmap(croppedWallpaperBitmap);
            } else {
                myWallpaperManager.setBitmap(wallpaperBitmap);
            }
        } catch (IOException e) {
            Log.e("MYAPP", "IO Exception", e); returnInteger = 0;
        } catch (IllegalArgumentException e) {
            Log.e("MYAPP", "Illegal Argument Exception", e); returnInteger = 0;
        }

        clearBitmaps(); //fixes out of memory errors

        //at this point, returnInteger == 0 if there has been an error
        //integers[3] == 1 if process is background process, == 0 if not

        //if you want to display message and there has not been an error
        if(integers[3] == 1 && returnInteger != 0) {
            returnInteger = 1;
        }

        //if you do not want to display message
        if(integers[3] == 0) {
            returnInteger = 2;
        }

        return returnInteger;
    }

    protected void onProgressUpdate(Integer... progress) {
        //Toast.makeText(context, "Downloading Wallpaper...", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPostExecute(Integer displayMessage) {
        if (displayMessage == 0) Toast.makeText(context, "Error updating wallpaper; check internet connection", Toast.LENGTH_LONG).show();
        if (displayMessage == 1) Toast.makeText(context, "Wallpaper successfully updated", Toast.LENGTH_LONG).show();
        //if (displayMessage == 2) this is a background service, so display nothing
        if (displayMessage == 3) Toast.makeText(context, "Could not update wallpaper; image may be .svg file or not available", Toast.LENGTH_LONG).show();
    }

    //testing internet functionality
    @Override
    protected void onPreExecute() {
        boolean serverReachable = true;
        String networkStatus = checkNetworkStatus(context);

        /*
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        */

        try {
            wikimediaBaseURL = new URL("http://commons.wikimedia.org/");
        } catch (MalformedURLException e) {
            Log.e("MYAPP", "Malformed URL Exception", e);
        }

        try {
            HttpURLConnection conn = (HttpURLConnection) wikimediaBaseURL.openConnection();
            conn.setConnectTimeout(3000);
            conn.connect();
            conn.disconnect();
        } catch (java.net.SocketTimeoutException e) {
            Log.e("MYAPP", "SocketTimeoutException", e);
            serverReachable = false;
        } catch (IOException e) {
            Log.e("MYAPP", "IOException", e);
        }

        //if no internet connection, display toast and cancel execution
        if(networkStatus.equals("noNetwork")) {
            Toast.makeText(context, "Cannot update wallpaper; not connected to internet", Toast.LENGTH_LONG).show();
            cancel(true);
        }

        if(!serverReachable) {
            Toast.makeText(context, "Cannot update wallpaper; Wikimedia server unreachable", Toast.LENGTH_LONG).show();
            cancel(true);
        }


        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);

        //Log.d("MYAPP", Boolean.toString(pref.getBoolean("pref_mobileDataEnabled", false) ));
        if(pref.getBoolean("pref_mobileDataEnabled", false) == false && networkStatus.equals("mobileData")) {
            Toast.makeText(context, "Cannot update wallpaper; downloading wallpapers on mobile data is currently disabled. You can change this in the preferences.", Toast.LENGTH_LONG).show();
            cancel(true);
        }
    }

    private String checkNetworkStatus(Context context) {

        String networkStatus ="";
        final ConnectivityManager manager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //Check Wifi
        final android.net.NetworkInfo wifi = manager.getActiveNetworkInfo();
        //Check for mobile data
        final android.net.NetworkInfo mobile = manager.getActiveNetworkInfo();

        if( wifi.getType() == ConnectivityManager.TYPE_WIFI) {
            networkStatus = "wifi";
        }else if(mobile.getType() == ConnectivityManager.TYPE_MOBILE){
            networkStatus = "mobileData";
        }else{
            networkStatus="noNetwork";
        }
        return networkStatus;
    }
}