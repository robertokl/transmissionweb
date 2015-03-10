package androidapps.robertokl.transmissionweb.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by klein on 3/8/15.
 */
public class DBModel extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;
    private static final String DB_NAME = "transmissionweb";

    public DBModel(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + Rule.TABLE_NAME + " (" + Rule.COLUMNS + ");");
        db.execSQL("CREATE TABLE " + Entry.TABLE_NAME + " (" + Entry.COLUMNS + ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
