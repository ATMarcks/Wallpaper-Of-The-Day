package alexmarck.wallpaperoftheday;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by alex on 7/6/17.
 */

public class MyPreferenceFragment extends PreferenceFragment
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.fragment_preference);
    }
}