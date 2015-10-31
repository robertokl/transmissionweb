package androidapps.robertokl.transmissionweb.web;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.util.Log;

import com.goebl.david.Request;
import com.goebl.david.Response;
import com.goebl.david.Webb;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import androidapps.robertokl.transmissionweb.R;
import androidapps.robertokl.transmissionweb.activities.RPCActivity;
import androidapps.robertokl.transmissionweb.activities.SettingsActivity;
import androidapps.robertokl.transmissionweb.db.Entry;

/**
 * Created by klein on 3/8/15.
 */
public class TransmissionService extends IntentService {

    private static final int NOTIFICATION_ID = 2;
    private static String transmissionSession;
    private String transmissionUrl;
    private String username;
    private String password;

    public TransmissionService() {
        super("TransmissionService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            transmissionUrl = sharedPref.getString(SettingsActivity.KEY_PREF_TRANSMISSION_URL, "") + "/transmission/rpc";
            username = sharedPref.getString(SettingsActivity.KEY_PREF_TRANSMISSION_USERNAME, "");
            password = sharedPref.getString(SettingsActivity.KEY_PREF_TRANSMISSION_PASSWORD, "");

            if(transmissionSession == null) {
                setTransmissionSession();
            }

            Request request = transmissionConnection();
            JSONObject argumentsJson = new JSONObject();
            argumentsJson.put("paused", false);
            argumentsJson.put("download-dir", intent.getStringExtra("folder"));
            argumentsJson.put("filename", intent.getStringExtra("url"));
            JSONObject json = new JSONObject();
            json.put("method", "torrent-add");
            json.put("arguments", argumentsJson);
            request.body(json.toString());

            Log.d("myLog", "Sending json to \"" + transmissionUrl + "/transmission/rpc\" with: " + json.toString());
            Response<JSONObject> response = request.ensureSuccess().asJsonObject();

            Log.d("myLog", "Setting " + intent.getStringExtra("entry_id") + " as downloaded");
            Entry.find(this, intent.getStringExtra("entry_id")).downloaded();
            Log.d("myLog", "Server MSG: " + response.getResponseMessage());
        } catch (Exception e) {
            showFailNotification();
            e.printStackTrace();
        }
    }

    private void showFailNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.notification_icon_error)
                .setContentTitle("Error communicating with transmission")
                .setContentText("Click here to try again.");
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

        Intent settingIntent = new Intent(this, SettingsActivity.class);
        PendingIntent resultSettingIntent = PendingIntent.getActivity(
                this,
                0,
                settingIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.addAction(R.drawable.settings, "Settings", resultSettingIntent);

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        IntentFilter intentFilter = new IntentFilter(RPCActivity.STARTED);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        ResponseReceiver receiver = new ResponseReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);
    }

    private Request transmissionConnection() throws IOException {
        Webb webb = Webb.create();
        webb.setDefaultHeader("X-Transmission-Session-Id", transmissionSession);
        webb.setDefaultHeader("Content-Type", "application/json");
        webb.setDefaultHeader("Accept", "application/json");
        byte[] credentials = (username + ":" + password).getBytes("UTF-8");
        String auth = "Basic " + Base64.encodeToString(credentials, 0);
        webb.setDefaultHeader(Webb.HDR_AUTHORIZATION, auth);

        return webb.post(transmissionUrl);
    }

    private void setTransmissionSession() throws IOException {
        Request webb = transmissionConnection();
        Response<JSONObject> response = webb.param("method", "session-get").asJsonObject();
        transmissionSession = response.getHeaderField("X-Transmission-Session-Id");
    }

    private class ResponseReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()) {
                case RPCActivity.STARTED:
                    NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mNotificationManager.cancel(NOTIFICATION_ID);
            }
        }
    }
}
