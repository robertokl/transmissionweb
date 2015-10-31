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
        if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            Log.d("myLog", "Boot Alarm Received.");
            RSSPullService.setAlarm(context);
        } else {
            Log.d("myLog", "Alarm Received.");

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
            String rssUrl = sharedPref.getString(SettingsActivity.KEY_PREF_RSS_URL, "");
            String transmissionUrl = sharedPref.getString(SettingsActivity.KEY_PREF_TRANSMISSION_URL, "");
            if(rssUrl.equals("") || transmissionUrl.equals("")) {
                Log.w("myLog", "RSS and/or TRANSMISSION URL are null. Not running alarm.");
                return;
            }
            Intent mServiceIntent = new Intent(context, RSSPullService.class);
            mServiceIntent.setData(Uri.parse(rssUrl));
            context.startService(mServiceIntent);

        }
    }
}
