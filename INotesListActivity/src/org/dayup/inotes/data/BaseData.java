package org.dayup.inotes.data;

import org.dayup.inotes.db.Field.Status;

import android.text.TextUtils;

public class BaseData {
    public String sid;
    public long modifiedTime;
    public long createdTime;
    public int status;
    public int deleted;

    public boolean hasSynced() {
        return !TextUtils.isEmpty(sid);
    }

    public boolean isLocalAdded() {
        if (deleted == Status.DELETED_YES) {
            return false;
        }
        return status == Status.SYNC_NEW || (status == Status.SYNC_UPDATE && !hasSynced());
    }

}
