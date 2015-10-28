package org.dayup.inotes.provider;

import org.dayup.inotes.INotesApplication;
import org.dayup.inotes.data.Note;
import org.dayup.inotes.db.NoteField;

import android.app.SearchManager;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class NoteSuggestionProvider extends SearchRecentSuggestionsProvider {

    public static final String TAG = NoteSuggestionProvider.class.getSimpleName();
    public static final String AUTHORITY = "org.dayup.inotes.provider.NoteSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public static final String[] PROJECTION_SEARCH = new String[] {
            BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA_ID,
    };

    public NoteSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }

    private INotesApplication getApplication() {
        return (INotesApplication) getContext().getApplicationContext();
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        MatrixCursor cursor = new MatrixCursor(PROJECTION_SEARCH);
        INotesApplication application = getApplication();
        String queryString = "";
        if (selectionArgs.length > 0) {
            queryString = selectionArgs[0];
        }
        Cursor c = null;
        try {
            c = Note.getNoteCursor4SuggestionSearch(application.getCurrentAccountId(), queryString,
                    null, null, application.getDBHelper());
            c.moveToFirst();
            int idIndex = c.getColumnIndex(BaseColumns._ID);
            int contentIndex = c.getColumnIndex(NoteField.content.name());
            int noteIdIndex = c.getColumnIndex(NoteField._id.name());
            while (!c.isAfterLast()) {
                cursor.addRow(new Object[] {
                        c.getLong(idIndex), c.getString(contentIndex), c.getString(noteIdIndex)
                });
                c.moveToNext();
            }
            return cursor;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }
}