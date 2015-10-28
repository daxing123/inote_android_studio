package org.dayup.inotes.db;

import org.dayup.inotes.data.Folder;

public enum AccountField implements Field {
    _id("INTEGER primary key autoincrement"), email, password, activity("INTEGER NOT NULL DEFAULT "
            + Status.ACCOUNT_ACTIVITY_FREEZE), default_folder_id("INTEGER NOT NULL DEFAULT "
            + Folder.ALL_FOLDER_ID), modifyTime("INTEGER"), createdTime("INTEGER"), imap, smtp, port, security_type, auth_type;

    private String type;

    private AccountField() {
        this("TEXT");
    }

    private AccountField(String type) {
        this.type = type;
    }

    @Override
    public int index() {
        return this.ordinal();
    }

    @Override
    public String type() {
        return type;
    }

    public static final String TABLE_NAME = "account";

}
