package androidapps.robertokl.transmissionweb.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

import androidapps.robertokl.transmissionweb.web.RSSPullService;

import static androidapps.robertokl.transmissionweb.R.xml.preferences;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String KEY_PREF_RSS_URL = "pref_rss_url";
    public static final String KEY_PREF_TRANSMISSION_URL = "pref_server";
    public static final String KEY_PREF_TRANSMISSION_USERNAME = "pref_username";
    public static final String KEY_PREF_TRANSMISSION_PASSWORD = "pref_password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPref.registerOnSharedPreferenceChangeListener(this);

        addPreferencesFromResource(preferences);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(KEY_PREF_RSS_URL)) {
            Log.d("myLog", "RSS URL Changed. Running sync");
            String syncConnPref = sharedPreferences.getString(SettingsActivity.KEY_PREF_RSS_URL, "");
            if(!syncConnPref.equals("")) {
                Intent mServiceIntent = new Intent(getApplicationContext(), RSSPullService.class);
                mServiceIntent.setData(Uri.parse(syncConnPref));
                getApplicationContext().startService(mServiceIntent);
            }
        }
    }
}
