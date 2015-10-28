package org.dayup.inotes;

import java.util.ArrayList;
import java.util.List;

import org.dayup.activities.BaseActivity;
import org.dayup.common.Analytics;
import org.dayup.common.Log;
import org.dayup.inotes.INotesPreferences.PK;
import org.dayup.inotes.adapter.NoteListAdapter;
import org.dayup.inotes.constants.Constants.RequestCode;
import org.dayup.inotes.data.Note;
import org.dayup.inotes.provider.NoteSuggestionProvider;
import org.dayup.inotes.utils.AudioUtils;
import org.dayup.inotes.views.SearchLayoutView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.MenuItem;

public class INotesSearchResultActivity extends BaseActivity {
    private static final String TAG = INotesSearchResultActivity.class.getSimpleName();
    private static final int REQUEST_CODE_VOICE_RECOGNITION_SEARCH = 30;

    private List<Note> noteslist = new ArrayList<Note>();
    private String query;

    private TextView searchCount, searchHead;
    private ListView mListView;
    private SearchLayoutView mSearchLayoutView;
    private NoteListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_list);
        initViews();
        parseIntent();
    }

    private void initViews() {
        mListView = (ListView) findViewById(R.id.list);
        searchCount = (TextView) findViewById(R.id.search_count);
        searchHead = (TextView) findViewById(R.id.search_header_text);
        adapter = new NoteListAdapter(this, noteslist);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note note = adapter.getItem(position);
                if (note == null) {
                    Toast.makeText(INotesSearchResultActivity.this, R.string.toast_note_not_exist,
                            Toast.LENGTH_SHORT).show();
                } else {
                    startDetailActivityForResult(note);
                }
            }
        });
        initActionBar();
    }

    private void initActionBar() {
        ActionBar bar = getSupportActionBar();
        bar.setHomeButtonEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowCustomEnabled(true);
        bar.setCustomView(R.layout.search_layout);
        mSearchLayoutView = (SearchLayoutView) bar.getCustomView();
        mSearchLayoutView.setRecognizClick(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (AudioUtils.checkRecAvailable(INotesSearchResultActivity.this)) {
                    startVoiceRecognitionActivity(null, REQUEST_CODE_VOICE_RECOGNITION_SEARCH);
                } else {
                    voiceSearchMarketDialog.show();
                }
            }
        });
        mSearchLayoutView.setTitleOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                doSearch(mSearchLayoutView.getTitleText());
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                        .hideSoftInputFromWindow(mSearchLayoutView.getTitleEdit().getWindowToken(),
                                0);
                return true;
            }
        });

    }

    private void parseIntent() {
        String queryAction = getIntent().getAction();
        if (TextUtils.equals(Intent.ACTION_SEARCH, queryAction)) {
            String query = getIntent().getStringExtra(SearchManager.QUERY);
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putString(PK.LAST_QUERY_STRING, query).commit();
            doSearch(query);
        } else if (TextUtils.equals(Intent.ACTION_VIEW, queryAction)) {
            String noteId = getIntent().getData().getLastPathSegment();
            Note note = Note.getNoteById(noteId, dbHelper);
            if (note == null) {
                Toast.makeText(INotesSearchResultActivity.this, R.string.toast_note_not_exist,
                        Toast.LENGTH_SHORT).show();
            } else {
                startDetailActivity(note);
            }
            finish();
        }

    }

    private void doSearch(String titleText) {
        query = titleText;
        SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                NoteSuggestionProvider.AUTHORITY, NoteSuggestionProvider.DATABASE_MODE_QUERIES);
        suggestions.saveRecentQuery(query, null);
        Log.i(TAG, "search....  " + query);
        requery(query);
    }

    private void requery(String queryString) {
        List<Note> noteList = Note.getNotes4SuggestionSearch(
                iNotesApplication.getCurrentAccountId(), queryString, dbHelper);

        int count = noteList.size();

        String countString = getResources().getQuantityString(R.plurals.search_results, count,
                queryString);

        mSearchLayoutView.setTitleText(query);
        searchCount.setText(count + "");
        searchHead.setText(countString);
        adapter.setData(noteList);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void requery() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.getString(PK.LAST_QUERY_STRING, "");
        requery(sp.getString(PK.LAST_QUERY_STRING, ""));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_VOICE_RECOGNITION_SEARCH) {
            String str = "";
            if (data != null) {
                ArrayList<String> matches = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                str = matches.size() > 0 ? matches.get(0) : "";
            }
            mSearchLayoutView.getTitleEdit().requestFocus();
            mSearchLayoutView.appendTitleText(str);
            doSearch(mSearchLayoutView.getTitleText());
        } else if (requestCode == RequestCode.NOTE_EDIT) {
            requery();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        final String queryAction = intent.getAction();
        if (Intent.ACTION_SEARCH.equals(queryAction)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            doSearch(query);
        }
    }

    private void startDetailActivity(Note note) {
        Intent i = new Intent(this, INotesDetailActivity.class);
        i.putExtra(INotesDetailActivity.NOTE_ID, note.id);
        i.putExtra(INotesDetailActivity.FOLDER_ID, note.folderId);
        startActivity(i);
    }

    private void startDetailActivityForResult(Note note) {
        Intent i = new Intent(this, INotesDetailActivity.class);
        i.putExtra(INotesDetailActivity.NOTE_ID, note.id);
        i.putExtra(INotesDetailActivity.FOLDER_ID, note.folderId);
        startActivityForResult(i, RequestCode.NOTE_EDIT);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            Analytics.startFlurry(this);
        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Analytics.endFlurry(this);
    }
}
