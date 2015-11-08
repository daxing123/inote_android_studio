package org.dayup.inotes;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.dayup.inotes.adapter.SpinnerSelecter;

import java.nio.channels.Pipe;
import java.util.ArrayList;

/**
 * Created by myatejx on 15/11/8.
 */
public class MyDrawerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<SpinnerSelecter> mData;

    public void setData(ArrayList<SpinnerSelecter> data) {
        mData = (data == null) ? new ArrayList<SpinnerSelecter>() : data;
        notifyDataSetChanged();
    }

    public interface DrawerRvListener {
        void drawerItemClick(int position);
    }

    public DrawerRvListener drawerRvListener;

    public void setDrawerRvListener(DrawerRvListener drawerRvListener) {
        this.drawerRvListener = drawerRvListener;
    }

    @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = null;
        RecyclerView.ViewHolder holder = null;

        switch (viewType) {
        case SpinnerSelecter.LABEL_TYPE_HEADER:
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.drawer_item_label, parent, false);
            holder = new ViewHolderLabel(view);

            break;
        case SpinnerSelecter.TYPE_FOLDER:
            view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.drawer_item_folder, parent, false);
            holder = new ViewHolderFolder(view);

            break;
        }

        return holder;
    }

    @Override public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        switch (getItemViewType(position)) {
        case SpinnerSelecter.LABEL_TYPE_HEADER:
            ViewHolderLabel viewHolderLabel = (ViewHolderLabel) holder;
            viewHolderLabel.tv_label.setText(mData.get(position).displayName);

            break;
        case SpinnerSelecter.TYPE_FOLDER:
            ViewHolderFolder viewHolderFolder = (ViewHolderFolder) holder;
            viewHolderFolder.tv_folder.setText(mData.get(position).displayName);
            viewHolderFolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    drawerRvListener.drawerItemClick(position);
                }
            });

            break;
        }
    }

    @Override public int getItemViewType(int position) {
        return mData.get(position).type;
    }

    @Override public int getItemCount() {

        return mData.size();
    }

    class ViewHolderLabel extends RecyclerView.ViewHolder {

        TextView tv_label;

        public ViewHolderLabel(View itemView) {
            super(itemView);

            tv_label = (TextView) itemView.findViewById(R.id.tv_label);
        }
    }

    class ViewHolderFolder extends RecyclerView.ViewHolder {

        TextView tv_folder;

        public ViewHolderFolder(View itemView) {
            super(itemView);

            tv_folder = (TextView) itemView.findViewById(R.id.tv_folder);
        }
    }

}
