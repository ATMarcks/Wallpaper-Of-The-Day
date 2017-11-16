package alexmarck.wallpaperoftheday;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class LicenseActivity extends AppCompatActivity {

    TextView licenseTextView;
    TextView authorTextView;
    TextView nameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license);

        licenseTextView = (TextView) findViewById(R.id.licenseTextView);
        authorTextView = (TextView) findViewById(R.id.authorTextView);
        nameTextView = (TextView) findViewById(R.id.nameTextView);

        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", getApplicationContext().MODE_PRIVATE);

        if(pref.contains("mostRecentImageLicense") == false) {
            licenseTextView.setText("N/A");
        } else {
            licenseTextView.setText(pref.getString("mostRecentImageLicense", "DEFAULT"));
        }

        if(pref.contains("mostRecentImageAuthor") == false) {
            authorTextView.setText("N/A");
        } else {
            authorTextView.setText(pref.getString("mostRecentImageAuthor", "DEFAULT"));
        }

        if(pref.contains("mostRecentImageName") == false) {
            nameTextView.setText("N/A");
        } else {
            //sometimes, in the case of painting names, etc.
            //italicized text is used via html tags, so this adds support for that
            String nameString = pref.getString("mostRecentImageName", "DEFAULT");
            nameTextView.setText(Html.fromHtml(nameString));
        }
    }

    public void returnBack(View view) {
        Intent mainActivityIntent  = new Intent(this, MainActivity.class); //second parameter is activity you want to navigate to
        startActivity(mainActivityIntent);
    }

    public void viewMoreInformation(View view) {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", getApplicationContext().MODE_PRIVATE);

        if(pref.contains("mostRecentImageDescriptionURL") == false) {
            Toast.makeText(this, "You must have selected/downloaded an image before viewing its license", Toast.LENGTH_LONG).show();
        } else {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(pref.getString("mostRecentImageDescriptionURL", "DEFAULT")));
            startActivity(browserIntent);
        }
    }

}
