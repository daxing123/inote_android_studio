package org.dayup.inotes.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Table {
    private final String tableName;
    private final Field[] fields;
    private final Field lastModifyDate;
    private final Field createdDate;

    public Table(String tableName, Field[] fields, Field lastModifyDate, Field createdDate) {
        this.tableName = tableName;
        this.fields = fields;
        this.lastModifyDate = lastModifyDate;
        this.createdDate = createdDate;
    }

    public Field[] fields() {
        return fields;
    }

    public String tableName() {
        return tableName;
    }

    public String sql2Drop() {
        return "DROP TABLE IF EXISTS " + tableName() + ";";
    }

    public String sql2Create() {
        StringBuffer sb = new StringBuffer();
        sb.append("CREATE TABLE ").append(tableName()).append(" (");
        boolean first = true;
        for (Field t : fields) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append(t.name()).append(" ").append(t.type());
        }
        return sb.append(")").toString();
    }

    public String[] columns = null;

    public String[] columns() {
        if (columns == null) {
            String[] temp = new String[fields.length];
            for (int i = 0; i < fields.length; i++) {
                temp[i] = fields[i].name();
            }
            columns = temp;
        }
        return columns;
    }

    public void dropTable(SQLiteDatabase db) {
        db.execSQL(sql2Drop());
    }

    public void createTable(SQLiteDatabase db) {
        db.execSQL(sql2Create());
    }

    public long getSqliteSequence(INotesDBHelper dbHelper) {
        Cursor c = null;
        long seq = 0;
        try {
            StringBuffer sql = new StringBuffer("select seq from sqlite_sequence where name='")
                    .append(tableName).append("'");
            c = dbHelper.getReadableDatabase().rawQuery(sql.toString(), null);
            c.moveToFirst();
            while (!c.isAfterLast()) {
                seq = c.getLong(c.getColumnIndex("seq"));
                c.moveToNext();
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return seq;
    }

    public long create(ContentValues values, INotesDBHelper dbHelper) {
        if (values.get(lastModifyDate.name()) == null) {
            values.put(lastModifyDate.name(), System.currentTimeMillis());
        }
        if (values.get(createdDate.name()) == null) {
            values.put(createdDate.name(), System.currentTimeMillis());
        }
        return dbHelper.getWritableDatabase().insert(tableName, null, values);
    }

    public int update(ContentValues values, String whereClause, String[] whereArgs,
            INotesDBHelper dbHelper) {
        values.put(lastModifyDate.name(), System.currentTimeMillis());
        return dbHelper.getWritableDatabase().update(tableName, values, whereClause, whereArgs);
    }

    public int updateWithoutModifyDate(ContentValues values, String whereClause,
            String[] whereArgs, INotesDBHelper dbHelper) {
        return dbHelper.getWritableDatabase().update(tableName, values, whereClause, whereArgs);
    }

    public Cursor query(String selection, String[] selectionArgs, Field orderBy,
            INotesDBHelper dbHelper) {
        return dbHelper.getWritableDatabase().query(tableName(), columns(), selection,
                selectionArgs, null, null, orderBy == null ? null : orderBy.name());
    }

    public Cursor query(String selection, String[] selectionArgs, String orderBy,
            INotesDBHelper dbHelper) {
        return dbHelper.getWritableDatabase().query(tableName(), columns(), selection,
                selectionArgs, null, null, orderBy == null ? null : orderBy);
    }

    public void deleteById(Field idField, String id, INotesDBHelper dbHelper) {
        String sql = "DELETE from " + tableName() + " where " + idField.name() + " = '" + id + "'";
        dbHelper.getWritableDatabase().execSQL(sql);
    }

    public void deleteByIdAndUserId(Field idField, String id, Field userIdField, long userId,
            INotesDBHelper dbHelper) {
        StringBuffer sql = new StringBuffer();
        sql.append("DELETE from ").append(tableName()).append(" where ");
        sql.append(idField.name()).append(" = '").append(id).append("' and ");
        sql.append(userIdField.name()).append(" = '").append(userId).append("'");

        dbHelper.getWritableDatabase().execSQL(sql.toString());
    }

}
