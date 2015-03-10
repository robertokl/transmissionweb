package androidapps.robertokl.transmissionweb.web;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import androidapps.robertokl.transmissionweb.activities.SettingsActivity;

/**
 * Created by klein on 3/8/15.
 */
public class RSSPullServiceAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("myLog", "Alarm received");
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String syncConnPref = sharedPref.getString(SettingsActivity.KEY_PREF_RSS_URL, "");

        Intent mServiceIntent = new Intent(context, RSSPullService.class);
        mServiceIntent.setData(Uri.parse(syncConnPref));
        context.startService(mServiceIntent);
    }
}
