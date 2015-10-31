package androidapps.robertokl.transmissionweb.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

/**
 * Created by klein on 3/1/15.
 */
public class Entry {
    public static final String TABLE_NAME = "entries";
    public static final String COLUMNS = "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
            " title varchar(250)," +
            " link text," +
            " guid varchar(100)," +
            " description text," +
            " pubDate date," +
            " downloaded boolean default f," +
            " ignored boolean default f";
    public String title;
    public String link;
    public String guid;
    public String description;
    public Date pubDate;
    public Integer id;
    private DBModel db;

    public Entry(Context context) {
        db = new DBModel(context);
    }

    public boolean isNew(String criterias) {
        String criteria = "guid = '" + this.guid + "' " + criterias;

        Cursor query = db.getReadableDatabase().query(TABLE_NAME,
                new String[]{"guid"},
                criteria,
                null,
                null,
                null,
                null);
        int count = query.getCount();

        db.close();

        return count == 0;
    }

    public static ArrayList<Entry> newEntries(Context context) {
        SQLiteDatabase db = new DBModel(context).getReadableDatabase();
        Cursor query = db.query(TABLE_NAME,
                new String[]{"_id", "title", "link"},
                "downloaded = 0 AND ignored = 0",
                null,
                null,
                null,
                null
        );
        Log.d("myLog", query.getCount() + " new entries found");

        ArrayList<Entry> entries = new ArrayList<Entry>();
        while (query.moveToNext()) {
            Entry entry = new Entry(context);
            entry.id = query.getInt(0);
            entry.title = query.getString(1);
            entry.link = query.getString(2);
            entries.add(entry);
        }

        db.close();

        return entries;
    }

    public static Entry find(Context context, String id) {
        SQLiteDatabase db = new DBModel(context).getReadableDatabase();
        Cursor query = db.query(TABLE_NAME,
                new String[]{"_id"},
                "_id = ?",
                new String[] { id },
                null,
                null,
                null
        );

        query.moveToFirst();
        Entry entry = new Entry(context);
        entry.id = query.getInt(0);

        db.close();

        return entry;
    }

    public long save() {
        if(!this.isNew("")) {
            Log.d("myLog", "Already Saved: " + this.title);
            return -1;
        }
        ContentValues v = new ContentValues(6);
        v.put("title", this.title);
        v.put("link", this.link);
        v.put("guid", this.guid);
        v.put("description", this.description);
        v.put("downloaded", false);
        v.put("ignored", false);
        long id = db.getWritableDatabase().insert(TABLE_NAME, null, v);

        db.close();

        return id;
    }

    public void ignored() {
        ContentValues values = new ContentValues(1);
        values.put("ignored", true);
        db.getWritableDatabase().update(TABLE_NAME, values, "_id = ?", new String[] { String.valueOf(id) });
        db.close();
    }

    public void downloaded() {
        ContentValues values = new ContentValues(1);
        values.put("downloaded", true);
        db.getWritableDatabase().update(TABLE_NAME, values, "_id = ?", new String[] { String.valueOf(id) });
        db.close();
    }
}