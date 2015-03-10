package androidapps.robertokl.transmissionweb.web;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import com.goebl.david.Request;
import com.goebl.david.Response;
import com.goebl.david.Webb;

import org.json.JSONObject;

import java.io.IOException;

import androidapps.robertokl.transmissionweb.activities.SettingsActivity;

/**
 * Created by klein on 3/8/15.
 */
public class TransmissionService extends IntentService {

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

            Response<JSONObject> response = request.ensureSuccess().asJsonObject();
            Log.d("myLog", "Server MSG: " + response.getResponseMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}
