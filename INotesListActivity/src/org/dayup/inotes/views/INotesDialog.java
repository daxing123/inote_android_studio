package org.dayup.inotes.views;

import java.util.ArrayList;

import org.dayup.inotes.R;
import org.dayup.inotes.constants.Constants.Themes;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spanned;
import android.text.method.MovementMethod;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

public class INotesDialog extends Dialog {
    private Context context;
    private TextView title, message;
    private LinearLayout currentView;
    private ListView listView;
    private View btn_msg_divider;
    private Button confirm_btn, middle_btn, cancel_btn;

    public INotesDialog(Context context, int theme) {
        super(context, theme == Themes.THEME_LIGHT ? R.style.INotesDialog_Light
                : R.style.INotesDialog);
        this.context = context;
        setContentView(R.layout.inotes_dialog_layout);
        this.setCanceledOnTouchOutside(true);
        this.setCancelable(true);
        initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowManager m = this.getWindow().getWindowManager();
        Display d = m.getDefaultDisplay();
        android.view.WindowManager.LayoutParams p = this.getWindow().getAttributes();
        p.width = (int) ((d.getWidth() < d.getHeight() ? d.getWidth() : d.getHeight()) * 0.92);
        getWindow().setAttributes(p);
        super.onCreate(savedInstanceState);
        if (currentView.getChildCount() > 0) {
            currentView.getChildAt(0).setLayoutParams(
                    new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,
                            LayoutParams.WRAP_CONTENT));
        }
    }

    private void initView() {
        title = (TextView) findViewById(R.id.dialog_title);
        message = (TextView) findViewById(R.id.dialog_message);
        currentView = (LinearLayout) findViewById(R.id.dialog_setview);
        listView = (ListView) findViewById(android.R.id.list);
        btn_msg_divider = findViewById(R.id.dialog_btn_msg_divider);
        confirm_btn = (Button) findViewById(R.id.dialog_btn_confirm);
        middle_btn = (Button) findViewById(R.id.dialog_btn_middle);
        cancel_btn = (Button) findViewById(R.id.dialog_btn_cancel);
    }

    public void setTitle(int textId) {
        title.setVisibility(View.VISIBLE);
        title.setText(textId);
    }

    public void setTitle(String str) {
        title.setVisibility(View.VISIBLE);
        title.setText(str);
    }

    public void setTitle(CharSequence chars) {
        title.setVisibility(View.VISIBLE);
        title.setText(chars);
    }

    public void setMessage(int textId) {
        message.setVisibility(View.VISIBLE);
        message.setText(textId);
    }

    public void setMessage(String str) {
        message.setVisibility(View.VISIBLE);
        message.setText(str);
    }

    public void setMessage(Spanned spa) {
        message.setVisibility(View.VISIBLE);
        message.setText(spa);
    }

    public void setMessageMovementMethod(MovementMethod movement) {
        message.setMovementMethod(movement);
    }

    public void setView(int layoutId) {
        currentView.setVisibility(View.VISIBLE);
        currentView.removeAllViews();
        currentView.addView(View.inflate(context, layoutId, null));
    }

    public void setView(View view) {
        currentView.setVisibility(View.VISIBLE);
        currentView.removeAllViews();
        currentView.addView(view);
        currentView.invalidate();
    }

    public void setPositiveButton(int textId, View.OnClickListener onClickListener) {
        setButtonOnClick(confirm_btn, textId, onClickListener);
    }

    public void setNeutralButton(int textId, View.OnClickListener onClickListener) {
        setButtonOnClick(middle_btn, textId, onClickListener);
    }

    public void setNegativeButton(int textId, View.OnClickListener onClickListener) {
        setButtonOnClick(cancel_btn, textId, onClickListener);
    }

    private void setButtonOnClick(Button btn, int textId, View.OnClickListener onClickListener) {
        btn_msg_divider.setVisibility(View.VISIBLE);
        if (btn == middle_btn) {
            findViewById(R.id.dialog_btn_middle_divider).setVisibility(View.VISIBLE);
        } else if (btn == cancel_btn) {
            findViewById(R.id.dialog_btn_cancel_divider).setVisibility(View.VISIBLE);
        }

        btn.setVisibility(View.VISIBLE);
        btn.setText(textId);
        if (onClickListener != null) {
            btn.setOnClickListener(onClickListener);
        } else {
            btn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dismiss();
                }
            });
        }
    }

    public void setSingleChoiceItems(CharSequence[] items, int checkedItem,
            INotesDialogListItemOnClickListener listener) {
        ArrayList<String> list = new ArrayList<String>();
        for (CharSequence item : items) {
            list.add(item.toString());
        }
        itemOnClickListener = listener;
        listView.setVisibility(View.VISIBLE);
        listView.setAdapter(new DialogListAdapter(context, list, checkedItem));
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((RadioButton) view.findViewById(R.id.radio)).setChecked(true);

                View child = null;
                for (int i = 0, j = parent.getChildCount(); i < j; i++) {
                    child = parent.getChildAt(i);
                    if (child != null) {
                        ((RadioButton) child.findViewById(R.id.radio)).setChecked(i == position);
                    }
                }
                if (itemOnClickListener != null) {
                    itemOnClickListener.onClick(INotesDialog.this, position);
                }
            }
        });
    }

    public void setCustomListView(CharSequence[] items, INotesDialogListItemOnClickListener listener) {
        ArrayList<String> list = new ArrayList<String>();
        for (CharSequence item : items) {
            list.add(item.toString());
        }
        itemOnClickListener = listener;
        listView.setVisibility(View.VISIBLE);
        listView.setAdapter(new DialogListAdapter(context, list, -1));
        listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (itemOnClickListener != null) {
                    itemOnClickListener.onClick(INotesDialog.this, position);
                }
            }
        });

    }

    class DialogListAdapter extends BaseAdapter {
        private Context context;
        private ArrayList<String> list;
        private int checkedItem;

        public DialogListAdapter(Context context, ArrayList<String> list, int checkedItem) {
            this.context = context;
            this.list = list;
            this.checkedItem = checkedItem;
        }

        @Override
        public int getCount() {
            return list != null ? list.size() : 0;
        }

        @Override
        public String getItem(int position) {
            if (list != null) {
                return list.get(position);
            }
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (position < 0 || position > list.size()) {
                return null;
            }
            String item = getItem(position);
            if (item == null) {
                return null;
            }
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.inotes_dialog_list_item, null);
            }

            ((TextView) convertView.findViewById(R.id.text)).setText(item);
            if (checkedItem == -1) {
                ((RadioButton) convertView.findViewById(R.id.radio)).setVisibility(View.GONE);
            } else {
                ((RadioButton) convertView.findViewById(R.id.radio))
                        .setChecked(position == checkedItem);
            }

            convertView.findViewById(R.id.divider).setVisibility(
                    (position == getCount() - 1) ? View.GONE : View.VISIBLE);
            return convertView;
        }

    }

    @Override
    public void show() {
        super.show();
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                if (showListener != null) {
                    showListener.dialogShow(INotesDialog.this);
                }
            }

        }, 100);
    }

    private INotesDialogShowListener showListener;

    public void setGTasksDialogShowListener(INotesDialogShowListener listener) {
        this.showListener = listener;
    }

    public interface INotesDialogShowListener {
        void dialogShow(Dialog dialog);
    }

    private INotesDialogListItemOnClickListener itemOnClickListener;

    public interface INotesDialogListItemOnClickListener {
        void onClick(Dialog dialog, int position);
    }
}
