package androidapps.robertokl.transmissionweb.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidapps.robertokl.transmissionweb.db.Rule;

/**
 * Created by klein on 3/6/15.
 */
public class RuleContentProvider extends ContentProvider {

    public static final String PROVIDER_NAME = "androidapps.robertokl.transmissionweb.rules";
    public static final Uri CONTENT_URI = Uri.parse("content://" + PROVIDER_NAME + "/rules" );

    private static final int RULES = 1;
    private static final int RULE_ID = 2;

    private static final UriMatcher uriMatcher ;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "rule", RULES);
        uriMatcher.addURI(PROVIDER_NAME, "rules/#", RULE_ID);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return Rule.all(getContext());
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Rule rule = new Rule(getContext());
        long rowId = rule.save(values);
        return ContentUris.withAppendedId(CONTENT_URI, rowId);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if(uriMatcher.match(uri) == RULE_ID){
            Rule rule = new Rule(getContext());
            rule.id = Integer.valueOf(uri.getPathSegments().get(1));
            return rule.delete();
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if(uriMatcher.match(uri) == RULE_ID){
            Rule rule = Rule.find(Integer.valueOf(uri.getPathSegments().get(1)), getContext());
            return rule.update(values);
        }
        return 0;
    }
}
