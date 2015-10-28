package org.dayup.inotes.adapter;

import java.util.List;
import java.util.TreeMap;

import org.dayup.common.Log;
import org.dayup.inotes.INotesListActivity.SortByTypes;
import org.dayup.inotes.R;
import org.dayup.inotes.data.Note;
import org.dayup.inotes.utils.DateUtils;
import org.dayup.inotes.utils.DateUtils.DatePattern;
import org.dayup.inotes.views.NoteListItemView;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * @author Nicky
 * 
 */
public class NoteListAdapter extends BaseAdapter {

    private final static String TAG = "NoteListAdapter";

    private Context context;
    private List<Note> notes;
    private String timeFormat = DatePattern.HM_COLON_12;
    private int sortBy;
    private boolean flingState = false;
    private boolean isSelectMode = false;
    private TreeMap<Integer, Note> selectItems = new TreeMap<Integer, Note>();

    public NoteListAdapter(Context context, List<Note> notes) {
        this.context = context;
        this.notes = notes;
        initDateAndTimeFormat(context);
    }

    /************* Select mode **************/
    public boolean isSelectMode() {
        return isSelectMode;
    }

    public void setSelectMode(boolean isSelectMode) {
        this.isSelectMode = isSelectMode;
    }

    public void resetSelectedItems(int position) {
        if (selectItems.containsKey(position)) {
            selectItems.remove(position);
        } else {
            selectItems.put(position, getItem(position));
        }
        notifyDataSetChanged();
    }

    public TreeMap<Integer, Note> getSelectItems() {
        return selectItems;
    }

    public boolean isSelected(Note item) {
        return isSelectMode() && selectItems.containsValue(item);
    }

    public void clearSelection() {
        TreeMap<Integer, Note> selectItems = getSelectItems();
        if (selectItems.size() > 0) {
            selectItems.clear();
        }
    }

    private void initDateAndTimeFormat(Context context) {
        timeFormat = android.text.format.DateFormat.is24HourFormat(context) ? DatePattern.HM_COLON_24
                : DatePattern.HM_COLON_12;
    }

    public void setData(List<Note> notes, int sortBy) {
        this.sortBy = sortBy;
        setData(notes);
    }

    public void setData(List<Note> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    public void setFlingState(boolean flag) {
        flingState = flag;
        if (!flingState) {
            notifyDataSetChanged();
        }
    }

    @Override
    public int getCount() {
        return notes == null ? 0 : notes.size();
    }

    @Override
    public Note getItem(int position) {
        return position < getCount() ? notes.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public String getItemIdByPosition(int position) {
        if (notes.size() <= 0) {
            return null;
        }
        Note note = notes.get(position);
        if (note == null) {

        }
        return note == null ? null : note.id;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i(TAG, "getView..." + position);

        NoteListItemView itemView;
        ViewHolder holder;
        final Note item = getItem(position);
        if (item == null)
            return null;
        if (convertView == null) {
            itemView = (NoteListItemView) View.inflate(context, R.layout.note_list_item, null);
            holder = new ViewHolder();
            holder.title = (TextView) itemView.findViewById(R.id.nli_notes_title);
            holder.date = (TextView) itemView.findViewById(R.id.item_date);
            itemView.setTag(holder);
        } else {
            itemView = (NoteListItemView) convertView;
            holder = (ViewHolder) convertView.getTag();
        }
        if (holder == null) {
            itemView = (NoteListItemView) View.inflate(context, R.layout.note_list_item, null);
            holder = new ViewHolder();
            holder.title = (TextView) itemView.findViewById(R.id.nli_notes_title);
            holder.date = (TextView) itemView.findViewById(R.id.item_date);
            itemView.setTag(holder);
        }
        long lastTime = 0;
        if (sortBy == SortByTypes.CREATE_DOWN || sortBy == SortByTypes.CREATE_UP) {
            lastTime = item.createdTime;
        } else {
            lastTime = item.modifiedTime;
        }
        holder.date.setText(DateUtils.formatTimeStampString(context, lastTime));
        holder.title.setText(extractNoteTitle(item.content));
        itemView.setNoteSelected(isSelected(item));
        return itemView;
    }

    private CharSequence extractNoteTitle(String content) {
        if (TextUtils.isEmpty(content)) {
            return content;
        }
        if (content.contains("\n")) {
            return content.substring(0, content.indexOf("\n"));
        }
        return content;
    }

    static class ViewHolder {
        TextView title, date;
    }

}
