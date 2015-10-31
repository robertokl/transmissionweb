package androidapps.robertokl.transmissionweb.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.SyncStateContract;

/**
 * Created by klein on 3/3/15.
 */
public class Rule {
    public static final String TABLE_NAME = "rules";
    public static final String COLUMNS = " _id INTEGER PRIMARY KEY AUTOINCREMENT," +
            " rule VARCHAR(200)," +
            " folder VARCHAR(200)";
    public String rule;
    public String folder;
    public int id;
    private DBModel db;

    public Rule(Context context) {
        db = new DBModel(context);
    }

    public static Cursor all(Context context) {
        SQLiteDatabase db = new DBModel(context).getReadableDatabase();
        Cursor query = db.query(TABLE_NAME,
                new String[]{"_id", "rule", "folder"},
                null,
                null,
                null,
                null,
                null);

        return query;
    }

    public long save() {
        ContentValues v = new ContentValues(2);
        v.put("rule", this.rule);
        v.put("folder", this.folder);
        return this.save(v);
    }

    public long save(ContentValues values) {
        long dbId = db.getWritableDatabase().insert(TABLE_NAME, null, values);

        db.close();

        return dbId;
    }

    public int delete() {
        int dbId = db.getWritableDatabase().delete(TABLE_NAME, "_id = ?", new String[]{String.valueOf(id)});

        db.close();

        return dbId;
    }

    public static Rule find(Integer idToFind, Context context) {
        SQLiteDatabase db = new DBModel(context).getReadableDatabase();
        Cursor query = db.query(TABLE_NAME,
                new String[]{"_id", "rule", "folder"},
                "_id = ?",
                new String[]{idToFind.toString()},
                null,
                null,
                null);

        query.moveToFirst();

        Rule rule = new Rule(context);
        rule.id = query.getInt(0);
        rule.rule = query.getString(1);
        rule.folder = query.getString(2);

        db.close();

        return rule;
    }

    public int update() {
        ContentValues values = new ContentValues(2);
        values.put("rule", this.rule);
        values.put("folder", this.folder);
        return this.update(values);
    }

    public int update(ContentValues values) {
        int dbId = db.getWritableDatabase().update(TABLE_NAME, values, "_id = ?", new String[]{String.valueOf(id)});

        db.close();

        return dbId;
    }

    public static Rule ruleFor(Context context, String entryTitle) {
        Cursor rules = Rule.all(context);
        while(rules.moveToNext()) {
            if(entryTitle.matches(".*" + rules.getString(1) + ".*")){
                Rule rule = new Rule(context);
                rule.id = rules.getInt(0);
                rule.rule = rules.getString(1);
                rule.folder = rules.getString(2);

                return rule;
            }
        }
        rules.close();
        return null;
    }
}
