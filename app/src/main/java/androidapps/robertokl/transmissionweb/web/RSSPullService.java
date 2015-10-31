package androidapps.robertokl.transmissionweb.web;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import androidapps.robertokl.transmissionweb.R;
import androidapps.robertokl.transmissionweb.activities.RPCActivity;
import androidapps.robertokl.transmissionweb.db.Entry;

/**
 * Created by klein on 3/1/15.
 */
public class RSSPullService extends IntentService {

    public static final String RSS_PULLSERVICE_BROADCAST = "androidapps.robertokl.transmission.broadcasts.RSSPULLSERVICE";
    private static final int NOTIFICATION_ID = 1;
    private static AlarmManager alarmManager;
    private static PendingIntent alarmIntent;
    private PendingIntent pendingAddAllIntent;
    private PendingIntent pendingSkipAllIntent;

    public RSSPullService() {
        super("RSSPullService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean status = true;
        try {
            IntentFilter intentFilter = new IntentFilter(RPCActivity.EMPTY_ENTRIES);
            intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
            ResponseReceiver receiver = new ResponseReceiver();
            LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);

            String url = intent.getDataString();

            Log.d("myLog", "Requesting to: " + url);

            InputStream resp = downloadUrl(url);

            List<Entry> newEntries = new ArrayList<Entry>();
            for (Entry entry: (new ShowRSSParser()).parse(resp, getApplicationContext())) {
                if(entry.isNew("AND (downloaded = 1 OR ignored = 1)")) {
                    newEntries.add(entry);
                }
            }

            Log.d("myLog", "Found " + newEntries.size() + " new entries");

            if(newEntries.size() > 0) {
                for (Entry newEntry : newEntries) {
                    Log.d("myLog", "Saving " + newEntry.title);
                    newEntry.save();
                }
                showNotification(newEntries);
            }
        } catch (IOException e) {
            status = false;
            Log.e("myLog", "IOException", e);
        } catch (XmlPullParserException e) {
            status = false;
            Log.e("myLog", "XmlPullParserException", e);
        }
        Intent localIntent = new Intent(RSS_PULLSERVICE_BROADCAST).putExtra("status", status);
        localIntent.addCategory(Intent.CATEGORY_DEFAULT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void showNotification(List<Entry> newEntries) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle("New feeds available")
                .setContentText(newEntries.size() + " items available for download.");
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent(this, RPCActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        for (int i=0; i < newEntries.size(); i++) {
            inboxStyle.addLine(newEntries.get(i).title);
        }
        mBuilder.setStyle(inboxStyle);

        mBuilder.addAction(R.drawable.ic_stat_av_playlist_add, "Add All", getAddAllPendingIntent());
        mBuilder.addAction(R.drawable.ic_stat_navigation_cancel, "Skip All", getSkipAllPendingIntent());

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public static void setAlarm(Context context) {
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, RSSPullServiceAlarmReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        long interval = 3 * AlarmManager.INTERVAL_HOUR;
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, interval, interval, alarmIntent);
    }

    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000 /* milliseconds */);
        conn.setConnectTimeout(15000 /* milliseconds */);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        // Starts the query
        conn.connect();
        return conn.getInputStream();
    }

    private PendingIntent getAddAllPendingIntent() {
        if(null == pendingAddAllIntent) {
            Intent addAllIntent = new Intent(this, MultipleRSSIntentHandler.class);
            addAllIntent.putExtra(MultipleRSSIntentHandler.ACTION, MultipleRSSIntentHandler.ADD_ALL);
            pendingAddAllIntent = PendingIntent.getService(this, 0, addAllIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return pendingAddAllIntent;
    }

    private PendingIntent getSkipAllPendingIntent() {
        if(null == pendingSkipAllIntent) {
            Intent skipAllIntent = new Intent(this, MultipleRSSIntentHandler.class);
            skipAllIntent.putExtra(MultipleRSSIntentHandler.ACTION, MultipleRSSIntentHandler.SKIP_ALL);
            pendingSkipAllIntent = PendingIntent.getService(this, 1, skipAllIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        return pendingSkipAllIntent;
    }

    private class ResponseReceiver extends BroadcastReceiver {

        private ResponseReceiver() {
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case RPCActivity.EMPTY_ENTRIES:
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(NOTIFICATION_ID);
            }
        }
    }
}
