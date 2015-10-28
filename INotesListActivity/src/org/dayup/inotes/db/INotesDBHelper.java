package org.dayup.inotes.db;

import org.dayup.inotes.data.Account;
import org.dayup.inotes.data.Folder;
import org.dayup.inotes.data.Note;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * // * sqlite3 /data/data/org.dayup.inotes/databases/inotes
 * 
 * @author
 */
public class INotesDBHelper extends SQLiteOpenHelper {

    private final String TAG = INotesDBHelper.class.getSimpleName();

    private Context context;

    public static final String DATABASE_NAME = "inotes";
    public static final int DATABASE_VERSION = 2;

    public INotesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Account.table.dropTable(db);
        Account.table.createTable(db);
        Folder.table.dropTable(db);
        Folder.table.createTable(db);
        Note.table.dropTable(db);
        Note.table.createTable(db);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            addContentOldForDiff(db);
        }
    }

    public static interface Transactable<T> {
        T doIntransaction(INotesDBHelper dbHelper);
    }

    public <T> T doInTransaction(Transactable<T> t) {
        T result;
        getWritableDatabase().beginTransaction();
        try {
            result = t.doIntransaction(this);
            getWritableDatabase().setTransactionSuccessful();
        } finally {
            getWritableDatabase().endTransaction();
        }
        return result;
    }

    private boolean addContentOldForDiff(SQLiteDatabase db) {
        try {
            db.beginTransaction();
            db.execSQL("alter table note add content_old TEXT");
            db.setTransactionSuccessful();
            return true;
        } catch (SQLException e) {
            Log.d(TAG, e.getMessage(), e);
        } finally {
            db.endTransaction();
        }
        return false;
    }
}
