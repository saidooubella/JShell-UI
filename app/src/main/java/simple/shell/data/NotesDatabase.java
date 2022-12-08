package simple.shell.data;

import android.database.*;
import android.database.sqlite.*;
import java.util.*;
import simple.shell.*;
import simple.shell.utils.*;

public final class NotesDatabase {

	private static final String[] PROJECTION = {
		NoteContract.NoteEntry.COLUMN_NOTE,
	};

	private static final String SORT_ORDER = NoteContract.NoteEntry._ID;

	private static final String NOTE_GET_QUERY =
	"SELECT " + NoteContract.NoteEntry.COLUMN_NOTE + " " +
	"FROM " + NoteContract.NoteEntry.TABLE_NAME + " " +
	"ORDER BY " + NoteContract.NoteEntry._ID + " " +
	"LIMIT 1 OFFSET ?";

	private final SQLiteStatement deleteStatement;
	private final SQLiteStatement insertStatement;
	private final SQLiteStatement clearStatement;

	private final SQLiteDatabase writeDB;
	private final SQLiteDatabase readDB;

	private final AppExecutors executors;

	public NotesDatabase(NotesDBHelper helper, AppExecutors executors) {

		this.executors = executors;

		this.writeDB = helper.getWritableDatabase();
		this.readDB = helper.getReadableDatabase();

		this.deleteStatement = writeDB.compileStatement(
			"DELETE FROM " + NoteContract.NoteEntry.TABLE_NAME + " " +
			"WHERE " + NoteContract.NoteEntry._ID + " " +
			"IN ( SELECT " + NoteContract.NoteEntry._ID + " " +
			"FROM " + NoteContract.NoteEntry.TABLE_NAME + " " +
			"ORDER BY " + NoteContract.NoteEntry._ID + " " +
			"LIMIT 1 OFFSET ? )"
		);

		this.insertStatement = writeDB.compileStatement(
			"INSERT INTO " + NoteContract.NoteEntry.TABLE_NAME + " " +
			"(" + NoteContract.NoteEntry.COLUMN_NOTE + ") VALUES (?)"
		);

		this.clearStatement = writeDB.compileStatement(
			"DELETE FROM " + NoteContract.NoteEntry.TABLE_NAME
		);
	}

	public void add(final String note, final Continuation<Void> callback) {
		executors.io.execute(new Runnable() {
				@Override
				public void run() {
					insertStatement.bindString(1, note);
					insertStatement.execute();
					callback.resumeWith(null);
				}
			});
	}

	public void clear(final Continuation<Void> callback) {
		executors.io.execute(new Runnable() {
				@Override
				public void run() {
					clearStatement.execute();
					callback.resumeWith(null);
				}
			});
	}

	public Cancellable list(final Continuation<List<String>> callback) {

		final Cancellable cancellable = new Cancellable();

		executors.io.execute(new Runnable() {
				@Override
				public void run() {

					try (final Cursor cursor = readDB.query(NoteContract.NoteEntry.TABLE_NAME, PROJECTION, null, null, null, null, SORT_ORDER)) {

						final List<String> notes = new ArrayList<>(cursor.getCount());

						final int noteColumn = cursor.getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_NOTE);

						while (!cancellable.isCancelled() && cursor.moveToNext()) {
							notes.add(cursor.getString(noteColumn));
						}

						if (cancellable.isCancelled()) {
							callback.resumeWith(Collections.<String>emptyList());
							return;
						}

						callback.resumeWith(Collections.unmodifiableList(notes));
					}
				}
			});

		return cancellable;
	}

	public Cancellable get(final long index, final Continuation<ContResult<String>> callback) {

		final Cancellable cancellable = new Cancellable();

		executors.io.execute(new Runnable() {
				@Override
				public void run() {
					try (final Cursor cursor = readDB.rawQuery(NOTE_GET_QUERY, new String[] { Long.toString(index) })) {

						final int noteColumn = cursor.getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_NOTE);

						if (cancellable.isCancelled()) {
							callback.resumeWith(ContResult.<String>cancellation());
							return;
						}

						if (cursor.moveToFirst()) {
							callback.resumeWith(ContResult.success(cursor.getString(noteColumn)));
						} else {
							callback.resumeWith(ContResult.<String>failure(index + ": Invalid note index"));
						}
					}
				}
			});

		return cancellable;
	}

	public Cancellable removeNode(final long index, final Continuation<Boolean> callback) {
		
		final Cancellable cancellable = new Cancellable();

		executors.io.execute(new Runnable() {
				@Override
				public void run() {
					deleteStatement.bindLong(1, index);
					callback.resumeWith(deleteStatement.executeUpdateDelete() != 0);
				}
			});

		return cancellable;
	}

	public void close() {
		writeDB.close();
		readDB.close();
	}
}
