package alexmarck.wallpaperoftheday;

import android.preference.PreferenceActivity;

import java.util.List;

/**
 * Created by alex on 7/6/17.
 */

public class MyPreferenceActivity extends PreferenceActivity
{
    @Override
    public void onBuildHeaders(List<Header> target)
    {
        loadHeadersFromResource(R.xml.headers_preference, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName)
    {
        return MyPreferenceFragment.class.getName().equals(fragmentName);
    }
}