package androidapps.robertokl.transmissionweb.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import androidapps.robertokl.transmissionweb.R;
import androidapps.robertokl.transmissionweb.db.Entry;
import androidapps.robertokl.transmissionweb.db.Rule;
import androidapps.robertokl.transmissionweb.web.RSSPullService;
import androidapps.robertokl.transmissionweb.web.TransmissionService;

public class RPCActivity extends ActionBarActivity {

    public static final String EMPTY_ENTRIES = "androidapps.robertokl.transmission.broadcasts.RSSPULLSERVICE.emptyEntries";
    private ArrayList<Entry> entries = new ArrayList<>();
    private int currentEntry = 0;
    private View.OnClickListener skipListener;
    private View.OnClickListener addListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        skipListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entries.get(currentEntry).downloaded();
                currentEntry++;
                showCurrentEntry();
            }
        };
        addListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                entries.get(currentEntry).ignored();
                Intent transmissionService = new Intent(getApplicationContext(), TransmissionService.class);
                transmissionService.putExtra("url", entries.get(currentEntry).link);
                transmissionService.putExtra("folder", ((TextView) findViewById(R.id.txtRuleFolder)).getText().toString());
                getApplicationContext().startService(transmissionService);

                currentEntry++;
                showCurrentEntry();
            }
        };
        RSSPullService.setAlarm(this);

        IntentFilter intentFilter = new IntentFilter(RSSPullService.RSS_PULLSERVICE_BROADCAST);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        ResponseReceiver receiver = new ResponseReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, intentFilter);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String rssUrl = sharedPref.getString(SettingsActivity.KEY_PREF_RSS_URL, "");
        String transmissionUrl = sharedPref.getString(SettingsActivity.KEY_PREF_TRANSMISSION_URL, "");
        if(rssUrl.equals("") || transmissionUrl.equals("")) {
            Log.w("myLog", "RSS URL is null. Showing settings screen.");
            startActivity(new Intent(this, SettingsActivity.class));
            return;
        }

        entries = Entry.newEntries(getApplication());
        if(entries.size() <= 0) {
            Intent mServiceIntent = new Intent(getApplicationContext(), RSSPullService.class);
            mServiceIntent.setData(Uri.parse(rssUrl));
            getApplicationContext().startService(mServiceIntent);

            setContentView(R.layout.empty_entries);
            findViewById(R.id.txtNoNewEntries).setVisibility(View.INVISIBLE);

            return;
        }

        startInteraction();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(entries.size() > 0) {
            if(null == findViewById(R.id.buttonSkip)){
                startInteraction();
            }
            currentEntry = 0;
            showCurrentEntry();
        }
    }

    private void showCurrentEntry() {
        if(currentEntry >= entries.size()) {
            setContentView(R.layout.empty_entries);
            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
            findViewById(R.id.txtNoNewEntries).setVisibility(View.VISIBLE);
            currentEntry = 0;
            entries = new ArrayList<>();

            Intent localIntent = new Intent(EMPTY_ENTRIES);
            localIntent.addCategory(Intent.CATEGORY_DEFAULT);
            LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
            return;
        }

        ((TextView) findViewById(R.id.txtEntryTitle)).setText(entries.get(currentEntry).title);

        Rule rule = Rule.ruleFor(this, entries.get(currentEntry).title);
        if(rule == null) {
            ((TextView) findViewById(R.id.txtRuleName)).setText("");
            ((TextView) findViewById(R.id.txtRuleFolder)).setText("");
        } else {
            ((TextView) findViewById(R.id.txtRuleName)).setText(rule.rule);
            ((TextView) findViewById(R.id.txtRuleFolder)).setText(rule.folder);
        }
    }

    private void startInteraction() {
        setContentView(R.layout.activity_rpc);
        findViewById(R.id.buttonSkip).setOnClickListener(skipListener);
        findViewById(R.id.buttonAdd).setOnClickListener(addListener);
        showCurrentEntry();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_rpc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.rule_activity:
                startActivity(new Intent(this, RuleManager.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private class ResponseReceiver extends BroadcastReceiver {

        private ResponseReceiver() {}

        @Override
        public void onReceive(Context context, Intent intent) {
            switch(intent.getAction()) {
                case RSSPullService.RSS_PULLSERVICE_BROADCAST:
                    entries = Entry.newEntries(getApplication());
                    if(null != findViewById(R.id.progressBar)) {
                        if(entries.size() <= 0) {
                            findViewById(R.id.progressBar).setVisibility(View.INVISIBLE);
                            findViewById(R.id.txtNoNewEntries).setVisibility(View.VISIBLE);
                        } else {
                            startInteraction();
                        }
                    }
                    break;
            }
        }
    }
}
