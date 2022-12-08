package simple.shell.data;

import android.content.*;
import android.database.sqlite.*;
import simple.shell.data.*;

public final class NotesDBHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "Notes.db";
	private static final int DATABASE_VERSION = 1;

	public NotesDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(NoteContract.SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(NoteContract.SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
