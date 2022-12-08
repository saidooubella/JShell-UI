package simple.shell.data;

import android.provider.*;

public final class NoteContract {

	public static final String SQL_CREATE_ENTRIES =
    "CREATE TABLE IF NOT EXISTS " + NoteEntry.TABLE_NAME + " (" +
    NoteEntry._ID + " INTEGER PRIMARY KEY," +
    NoteEntry.COLUMN_NOTE + " TEXT)";

	public static final String SQL_DELETE_ENTRIES =
    "DROP TABLE IF EXISTS " + NoteEntry.TABLE_NAME;

	public static class NoteEntry implements BaseColumns {
		public static final String TABLE_NAME = "notes";
		public static final String COLUMN_NOTE = "note";
	}
}
