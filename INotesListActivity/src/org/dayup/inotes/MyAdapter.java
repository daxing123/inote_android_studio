package org.dayup.inotes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.dayup.inotes.data.Note;
import org.dayup.inotes.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by myatejx on 15/11/2.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

    private int layout;
    private Context context;
    private List<Note> notes;
    private String timeFormat = DateUtils.DatePattern.HM_COLON_12;
    private int sortBy;
    private boolean flingState = false;

    private TreeMap<Integer, Boolean> selectedItemIds;
    private LayoutInflater inflater;

    public MyAdapter(Context context, int layout, ArrayList<Note> notes) {
        this.context = context;
        this.layout = layout;
        this.notes = notes;
        inflater = LayoutInflater.from(this.context);
        this.selectedItemIds = new TreeMap<>();
        initDateAndTimeFormat(context);
    }

    public interface OnRvItemClickListener {
        void onItemClick(int position);

        void onItemLongClick(int position);
    }

    public OnRvItemClickListener onRvItemClickListener;

    public void SetOnRvItemClickListener(OnRvItemClickListener onRvItemClickListener) {
        this.onRvItemClickListener = onRvItemClickListener;
    }

    @Override public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        MyViewHolder myViewHolder = new MyViewHolder(
                inflater.inflate(layout, parent, false));

        return myViewHolder;
    }

    @Override public void onBindViewHolder(MyViewHolder holder, final int position) {

        final Note item = getItem(position);

        long lastTime = 0;
        if (sortBy == INotesListActivity.SortByTypes.CREATE_DOWN
                || sortBy == INotesListActivity.SortByTypes.CREATE_UP) {
            lastTime = item.createdTime;
        } else {
            lastTime = item.modifiedTime;
        }

        holder.tv_title.setText(extractNoteTitle(item.content));
        holder.tv_time.setText(DateUtils.formatTimeStampString(context, lastTime));

        if (onRvItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    onRvItemClickListener.onItemClick(position);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override public boolean onLongClick(View v) {
                    onRvItemClickListener.onItemLongClick(position);

                    return true;
                }
            });
        }

        if (!selectedItemIds.isEmpty()) {
            holder.itemView.setBackgroundResource(
                    selectedItemIds.get(position) ?
                            R.drawable.item_press_in_actionmode :
                            R.drawable.item_press);
            Toast.makeText(MyAdapter.this, String.valueOf(se), Toast.LENGTH_LONG).show();
        }
    }

    @Override public int getItemCount() {

        return notes.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tv_title, tv_time;

        public MyViewHolder(View view) {
            super(view);
            tv_title = (TextView) view.findViewById(R.id.tv_title);
            tv_time = (TextView) view.findViewById(R.id.tv_time);

        }
    }

    public void removeData(int position) {
        notes.remove(position);
        notifyItemRemoved(position);
    }

    //切换选择状态
    public void toggleSelected(int position) {
        if (!selectedItemIds.isEmpty()) {
            selectView(position, !selectedItemIds.get(position));
        }
    }

    //
    public void selectView(int position, boolean value) {
        if (value)
            selectedItemIds.put(position, value);
        else {
            //            selectedItemIds.delete(position);
            selectedItemIds.remove(position);
        }
        notifyDataSetChanged();//对rv来说这个还有用么？
    }

    //移除选中项记录，简单的说就是退出选中状态
    public void removeSelection() {
        selectedItemIds = new TreeMap<Integer, Boolean>();//用空对象给自己赋值，相当于消除了之前所选
        notifyDataSetChanged();
    }

    //
    public int getSelectedCount() {
        return selectedItemIds.size();
    }

    //
    public TreeMap<Integer, Boolean> getSelectedItemIds() {
        return selectedItemIds;
    }

    ///----------------------------

    private void initDateAndTimeFormat(Context context) {
        timeFormat = android.text.format.DateFormat.is24HourFormat(context) ?
                DateUtils.DatePattern.HM_COLON_24
                :
                DateUtils.DatePattern.HM_COLON_12;
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

    ///----------------------------

    public int getCount() {
        return notes == null ? 0 : notes.size();
    }

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

    ///-----------------------------

    private CharSequence extractNoteTitle(String content) {
        if (TextUtils.isEmpty(content)) {
            return content;
        }
        if (content.contains("\n")) {
            return content.substring(0, content.indexOf("\n"));
        }
        return content;
    }
}
