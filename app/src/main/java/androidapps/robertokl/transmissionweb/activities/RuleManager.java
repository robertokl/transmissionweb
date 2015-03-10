package androidapps.robertokl.transmissionweb.activities;


import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidapps.robertokl.transmissionweb.R;
import androidapps.robertokl.transmissionweb.db.RuleContentProvider;

public class RuleManager extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private SimpleCursorAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule_manager);

        ListView listView = (ListView) findViewById(R.id.ruleListView);
        this.adapter = new SimpleCursorAdapter(this,
                R.layout.fragment_ruleitem_list,
                null,
                new String[]{ "rule", "folder" },
                new int[] { R.id.txtRule, R.id.txtFolder },
                0
        );
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                prepareDialog(NewRuleFragment.EDIT_RULE, position, id);
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                prepareDialog(NewRuleFragment.DELETE_RULE, position, id);

                return true;
            }
        });

        /** Creating a loader for populating listview from sqlite database */
        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_rule_manager, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.new_rule:
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction ft = fm.beginTransaction();

                Bundle args = new Bundle();
                args.putInt("action", NewRuleFragment.ADD_RULE);
                NewRuleFragment ruleFragment = new NewRuleFragment();
                ruleFragment.setArguments(args);

                ft.add(ruleFragment, "RULE_DIALOG");
                ft.commit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = RuleContentProvider.CONTENT_URI;
        return new CursorLoader(this, uri, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    private void prepareDialog(int action, int position, long id) {
        adapter.getCursor().moveToPosition(position);

        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        Bundle args = new Bundle();
        args.putInt("action", action);
        args.putLong("rule_id", id);
        args.putString("rule_name", adapter.getCursor().getString(1));
        args.putString("rule_folder", adapter.getCursor().getString(2));
        NewRuleFragment ruleFragment = new NewRuleFragment();
        ruleFragment.setArguments(args);

        ft.add(ruleFragment, "RULE_DIALOG");
        ft.commit();
    }
}
