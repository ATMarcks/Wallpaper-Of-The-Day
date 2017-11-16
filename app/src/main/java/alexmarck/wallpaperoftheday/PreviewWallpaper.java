package alexmarck.wallpaperoftheday;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by Alex on 6/5/2017.
 */
public class PreviewWallpaper extends AsyncTask<Integer, Void, Integer> {
    private JSONObject json;
    private JSONObject query;
    private URL returnUrl;
    private Bitmap wallpaperBitmap;
    private Context context; //testing
    private URL wikimediaBaseURL;

    //Activity activity; //new testing!

    WeakReference<Activity> previewWeakActivity;

    public PreviewWallpaper(Context context) {
        this.context = context;
    }

    public String grabURL(int day, int month, int year) throws MalformedURLException, IOException {
        String monthString; String dayString;

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
        //the above string can be modified to be any date

        //grab JSON from internet
        try{
            json = new JSONObject(IOUtils.toString(new URL(url), Charset.forName("UTF-8")));
            query = json.getJSONObject("query").getJSONArray("pages").getJSONObject(0).getJSONArray("imageinfo").getJSONObject(0);

        } catch (JSONException e){
            //Log.e("e", e);
        }

        String imageURL = "Error finding URL";
        String thumbURL = "Error finding thumbnail";

        Log.w("date/month/year", dayString+", "+monthString+", "+yearString);

        try{
            imageURL = query.getString("url");
        } catch (JSONException e) {
        }

        String imageTitle = imageURL.substring(imageURL.lastIndexOf("/"));
        imageTitle = imageTitle.substring(1);
        String xmlURLstring = "https://tools.wmflabs.org/magnus-toolserver/commonsapi.php?image="+imageTitle+"&thumbwidth=240&thumbheight=240&versions&meta";
        URL xmlURL = new URL(xmlURLstring);

        Log.d("URL", xmlURLstring);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringElementContentWhitespace(true);

        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlURL.openStream());
            doc.getDocumentElement().normalize();

            thumbURL = doc.getElementsByTagName("thumbnail").item(0).getTextContent();

            // Do something with the document here.
        } catch (ParserConfigurationException e) {
        } catch (SAXException e) {
        } catch (IOException e) {
        }

        return thumbURL;
    }

    protected Integer doInBackground(Integer... integers) {
        //String urlString = "";

        if(wallpaperBitmap != null) { //fixes memory errors
            wallpaperBitmap.recycle();
            wallpaperBitmap = null;
        }

        String grabbedURL = "";

        try{
            grabbedURL = grabURL(integers[0], integers[1], integers[2]);
        } catch (IOException e) {
            //uh oh
        }

        if(grabbedURL.equals("Error finding thumbnail")) {
            return 2;
        }

        try {
            returnUrl = new URL(grabbedURL);
            wallpaperBitmap = BitmapFactory.decodeStream(returnUrl.openConnection().getInputStream());
        } catch(IOException e) {
            //empty!
        }

        return 1;
    }

    public PreviewWallpaper(Activity activity) {
        previewWeakActivity = new WeakReference<Activity>(activity);
    }

    protected void onProgressUpdate(Integer... progress) {
        //setProgressPercent(progress[0]);
    }

    @Override
    protected void onPostExecute(Integer result) {
        Activity activity = previewWeakActivity.get();

        if(result == 2) {
            Toast.makeText(activity, "Could not create preview; image may be .svg file or not available", Toast.LENGTH_LONG).show();
        } else {
            ImageView previewImageView;

            previewImageView = (ImageView) activity.findViewById(R.id.previewImageView);
            previewImageView.setImageBitmap(wallpaperBitmap);
        }
    }

    //testing internet functionality

    @Override
    protected void onPreExecute() {
        Activity activity = previewWeakActivity.get();
       // activity = previewWeakActivity.get();

        boolean serverReachable = true;
        ConnectivityManager cm = (ConnectivityManager)activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        try {
            wikimediaBaseURL = new URL("https://commons.wikimedia.org/");
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
        if(!isConnected) {
            Toast.makeText(activity, "Cannot update wallpaper; not connected to internet", Toast.LENGTH_LONG).show();
            cancel(true);
        }

        if(!serverReachable) {
            Toast.makeText(activity, "Cannot update wallpaper; Wikimedia server unreachable", Toast.LENGTH_LONG).show();
            cancel(true);
        }

    }

}