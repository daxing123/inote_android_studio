package org.dayup.inotes.db;

public enum NoteField implements Field {
    _id("TEXT primary key NOT NULL"), account_id("INTEGER NOT NULL DEFAULT "
            + Status.LOCAL_MODE_ACCOUNT_ID), sId, folder_id("INTEGER"), content, created_time(
            "INTEGER"), modified_time("INTEGER"), _deleted("INTEGER NOT NULL DEFAULT "
            + Status.DELETED_NO), _status("INTEGER NOT NULL DEFAULT " + Status.SYNC_NEW), content_old;

    private String type;

    private NoteField() {
        this("TEXT");
    }

    private NoteField(String type) {
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

    public static final String TABLE_NAME = "note";
}
