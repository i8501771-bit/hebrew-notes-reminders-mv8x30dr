package com.generated.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "notes_database.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NOTES = "notes";
    public static final String COL_ID = "_id";
    public static final String COL_TITLE = "title";
    public static final String COL_CONTENT = "content";
    public static final String COL_CATEGORY = "category";
    public static final String COL_COLOR = "color";
    public static final String COL_IS_PINNED = "is_pinned";
    public static final String COL_IS_CHECKLIST = "is_checklist";
    public static final String COL_REMINDER_TIME = "reminder_time";
    public static final String COL_UPDATED_AT = "updated_at";

    private static DatabaseHelper sInstance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new DatabaseHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createQuery = "CREATE TABLE " + TABLE_NOTES + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_TITLE + " TEXT, "
                + COL_CONTENT + " TEXT, "
                + COL_CATEGORY + " TEXT, "
                + COL_COLOR + " INTEGER, "
                + COL_IS_PINNED + " INTEGER DEFAULT 0, "
                + COL_IS_CHECKLIST + " INTEGER DEFAULT 0, "
                + COL_REMINDER_TIME + " INTEGER DEFAULT 0, "
                + COL_UPDATED_AT + " INTEGER"
                + ");";
        db.execSQL(createQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }

    public long saveNote(Note note) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_TITLE, note.title != null ? note.title : "");
        cv.put(COL_CONTENT, note.content != null ? note.content : "");
        cv.put(COL_CATEGORY, note.category != null ? note.category : "כללי");
        cv.put(COL_COLOR, note.color);
        cv.put(COL_IS_PINNED, note.isPinned ? 1 : 0);
        cv.put(COL_IS_CHECKLIST, note.isChecklist ? 1 : 0);
        cv.put(COL_REMINDER_TIME, note.reminderTime);
        cv.put(COL_UPDATED_AT, note.updatedAt);

        if (note.id > 0) {
            db.update(TABLE_NOTES, cv, COL_ID + " = ?", new String[]{String.valueOf(note.id)});
            return note.id;
        } else {
            long newId = db.insert(TABLE_NOTES, null, cv);
            note.id = newId;
            return newId;
        }
    }

    public Note getNote(long id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTES, null, COL_ID + " = ?", new String[]{String.valueOf(id)},
                null, null, null);
        Note note = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                note = cursorToNote(cursor);
            }
            cursor.close();
        }
        return note;
    }

    public void deleteNote(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NOTES, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public List<Note> getAllNotes(String categoryFilter, String searchQuery) {
        List<Note> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList<>();

        if (categoryFilter != null && !categoryFilter.equals("הכל")) {
            selection.append(COL_CATEGORY).append(" = ?");
            selectionArgs.add(categoryFilter);
        }

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            if (selection.length() > 0) {
                selection.append(" AND ");
            }
            selection.append("(").append(COL_TITLE).append(" LIKE ? OR ")
                    .append(COL_CONTENT).append(" LIKE ?)");
            String queryArg = "%" + searchQuery.trim() + "%";
            selectionArgs.add(queryArg);
            selectionArgs.add(queryArg);
        }

        String sel = selection.length() > 0 ? selection.toString() : null;
        String[] args = selectionArgs.isEmpty() ? null : selectionArgs.toArray(new String[0]);
        String orderBy = COL_IS_PINNED + " DESC, " + COL_UPDATED_AT + " DESC";

        Cursor cursor = db.query(TABLE_NOTES, null, sel, args, null, null, orderBy);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                list.add(cursorToNote(cursor));
            }
            cursor.close();
        }
        return list;
    }

    private Note cursorToNote(Cursor c) {
        Note n = new Note();
        n.id = c.getLong(c.getColumnIndexOrThrow(COL_ID));
        n.title = c.getString(c.getColumnIndexOrThrow(COL_TITLE));
        n.content = c.getString(c.getColumnIndexOrThrow(COL_CONTENT));
        n.category = c.getString(c.getColumnIndexOrThrow(COL_CATEGORY));
        n.color = c.getInt(c.getColumnIndexOrThrow(COL_COLOR));
        n.isPinned = c.getInt(c.getColumnIndexOrThrow(COL_IS_PINNED)) == 1;
        n.isChecklist = c.getInt(c.getColumnIndexOrThrow(COL_IS_CHECKLIST)) == 1;
        n.reminderTime = c.getLong(c.getColumnIndexOrThrow(COL_REMINDER_TIME));
        n.updatedAt = c.getLong(c.getColumnIndexOrThrow(COL_UPDATED_AT));
        return n;
    }
}
