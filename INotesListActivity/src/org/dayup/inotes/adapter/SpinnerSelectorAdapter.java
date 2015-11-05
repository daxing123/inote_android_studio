package org.dayup.inotes.adapter;

import java.util.ArrayList;

import android.app.LauncherActivity;
import org.dayup.inotes.INotesApplication;
import org.dayup.inotes.R;
import org.dayup.inotes.utils.ThemeUtils;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * @author Nicky
 *
 */
public class SpinnerSelectorAdapter extends BaseAdapter {

    private ArrayList<SpinnerSelecter> mData = new ArrayList<SpinnerSelecter>();
    /** View is of a separator row */
    private static final int ITEM_VIEW_TYPE_HEADER = AdapterView.ITEM_VIEW_TYPE_HEADER_OR_FOOTER;

    /** View is of a "normal" row */
    private static final int ITEM_VIEW_TYPE_NORMAL = 0;

    private static final int FIRST_FOLDER_POSITION = 1;

    private INotesApplication mApplication;
    private ThemeUtils themeUtils;

    private int labelTextColor, itemTextColor;

    public SpinnerSelectorAdapter(Context context) {
        this.mApplication = (INotesApplication) context.getApplicationContext();
        themeUtils = new ThemeUtils(mApplication);
        Resources r = mApplication.getResources();
        labelTextColor = r.getColor(themeUtils.getActionBarListSectionLabelText());
        itemTextColor = r.getColor(themeUtils.getGLargeTextColor());
    }

    public ArrayList<SpinnerSelecter> getData() {
        return mData;
    }

    public void clearData() {
        mData.clear();
        notifyDataSetChanged();
    }

    public void setData(ArrayList<SpinnerSelecter> data) {
        mData = (data == null) ? new ArrayList<SpinnerSelecter>() : data;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        mData.remove(position);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public SpinnerSelecter getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        long id = SpinnerSelecter.NO_ID;
        if (mData.size() != 0) {
            id = getItem(position).id;
        }
        return id;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public boolean isEnabled(int position) {
        return !isLabel(position);
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public int getItemViewType(int position) {
        return isLabel(position) ? ITEM_VIEW_TYPE_HEADER : ITEM_VIEW_TYPE_NORMAL;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent);
    }


    private View createViewFromResource(int position, View convertView, ViewGroup parent) {
        if (position < 0 || position >= mData.size()) {
            return null;
        }

        SpinnerSelecter item = mData.get(position);

        if (isLabel(position)) {
            convertView = View.inflate(mApplication, R.layout.spinner_item_label, null);
            final TextView separatorView = (TextView) convertView
                    .findViewById(R.id.listSeparator_label);
            separatorView.setTextColor(labelTextColor);
            separatorView.setBackgroundResource(themeUtils.getActionBarListSectionDividerBg());
            separatorView.setText("  " + item.displayName.toUpperCase());
        } else {
            convertView = View.inflate(mApplication, R.layout.spinner_selector_item, null);
            TextView listName = (TextView) convertView.findViewById(R.id.selector_name);
            listName.setTextColor(itemTextColor);
            listName.setText(item.displayName);
        }

        return convertView;
    }

    private boolean isLabel(int position) {
        if (position < 0 || position >= getCount()) {
            return false;
        }
        return getItem(position).type == SpinnerSelecter.LABEL_TYPE_HEADER
                || getItem(position).type == SpinnerSelecter.LABEL_TYPE_MIDDLE;
    }

    public boolean isAccount(int position) {
        if (position < 0 || position >= getCount()) {
            return false;
        }
        return getItem(position).type == SpinnerSelecter.TYPE_ACCOUNT;
    }

    public boolean isFolder(int position) {
        if (position < 0 || position >= getCount()) {
            return false;
        }
        return getItem(position).type == SpinnerSelecter.TYPE_FOLDER;
    }

}
