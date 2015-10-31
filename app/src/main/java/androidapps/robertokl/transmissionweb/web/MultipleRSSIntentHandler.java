package androidapps.robertokl.transmissionweb.web;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;

import androidapps.robertokl.transmissionweb.R;
import androidapps.robertokl.transmissionweb.activities.RPCActivity;
import androidapps.robertokl.transmissionweb.db.Entry;
import androidapps.robertokl.transmissionweb.db.Rule;

/**
 * Created by klein on 10/31/15.
 */
public class MultipleRSSIntentHandler extends IntentService {
    public static final String ACTION = "androidapps.robertokl.transmission.actions.MULTIPLERSSINTENTHANDLER.Actions";
    public static final String ADD_ALL = "androidapps.robertokl.transmission.actions.MULTIPLERSSINTENTHANDLER.AddAll";
    public static final String SKIP_ALL = "androidapps.robertokl.transmission.actions.MULTIPLERSSINTENTHANDLER.SkipAll";

    private ArrayList<Entry> entries;

    public MultipleRSSIntentHandler() { super("MultipleRSSIntentHandler"); }

    @Override
    protected void onHandleIntent(Intent intent) {
        String action = intent.getStringExtra(ACTION);
        Log.d("myLog", "MultipleRSSIntent received: " + action);

        entries = Entry.newEntries(getApplication());
        if(action.equals(ADD_ALL)) {
            addAll();
        } else if(action.equals(SKIP_ALL)) {
            skipAll();
        }

        Intent localIntent = new Intent(RPCActivity.EMPTY_ENTRIES);
        localIntent.addCategory(Intent.CATEGORY_DEFAULT);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private void skipAll() {
        Log.d("myLog", "Starting Ignoring All..");

        for (Entry entry: entries) {
            entry.ignored();
        }
    }

    private void addAll() {
        Log.d("myLog", "Starting Download All..");

        for (Entry entry: entries) {
            Rule rule = Rule.ruleFor(this, entry.title);
            if(rule == null) {
                Log.d("myLog", "Skipping \"" + entry.title + "\" because there's no set rule.");
                continue;
            }

            Log.d("myLog", "NOT Skipping \"" + entry.title + "\"");
            Intent transmissionService = new Intent(getApplicationContext(), TransmissionService.class);
            transmissionService.putExtra("url", entry.link);
            transmissionService.putExtra("entry_id", String.valueOf(entry.id));
            transmissionService.putExtra("folder", rule.folder);
            getApplicationContext().startService(transmissionService);
        }
    }
}
