package org.dayup.inotes.db;

public enum FolderField implements Field {
    _id("INTEGER primary key autoincrement"), account_id("INTEGER NOT NULL DEFAULT "
            + Status.LOCAL_MODE_ACCOUNT_ID), name, created_time("INTEGER"), modified_time("INTEGER"), startCheckPoint(
            "INTEGER NOT NULL DEFAULT 0"), endCheckPoint("INTEGER NOT NULL DEFAULT 0");

    private String type;

    private FolderField() {
        this("TEXT");
    }

    private FolderField(String type) {
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

    public static final String TABLE_NAME = "folder";

}
