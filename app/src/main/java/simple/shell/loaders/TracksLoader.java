package simple.shell.loaders;

import android.*;
import android.content.*;
import android.database.*;
import android.net.*;
import android.provider.*;
import java.util.*;
import simple.shell.*;
import simple.shell.models.*;
import simple.shell.utils.*;

public final class TracksLoader {

	private static final String[] PROJECTION = {
		MediaStore.Audio.Media._ID,
		MediaStore.Audio.Media.DISPLAY_NAME,
		MediaStore.Audio.Media.ARTIST,
	};

	private static final String SELECTION =
	MediaStore.Audio.Media.IS_MUSIC + " = 1 AND " +
	MediaStore.Audio.Media.DISPLAY_NAME + " LIKE ?";

	private static final String SORT_ORDER = MediaStore.Audio.Media.DISPLAY_NAME + " ASC";

	public static void load(
		final Shell shell,
		final AppExecutors executors,
		final ContentResolver resolver,
		final Cancellable cancellable,
		final String query,
		final Continuation<Result<List<Track>>> callback
	) {

		if (!shell.hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
			shell.requestPermissions(new String[] {
					Manifest.permission.READ_EXTERNAL_STORAGE
				}, new Continuation<Boolean>(null) {
					@Override
					protected void resume(Boolean granted) {
						if (granted) {
							load0(query, cancellable, resolver, executors, callback);
						} else if (!cancellable.isCancelled()) {
							callback.resumeWith(Result.<List<Track>>message("Permission denied"));
						} else {
							callback.resumeWith(Result.of(Collections.<Track>emptyList()));
						}
					}
				});
			return;
		}

		load0(query, cancellable, resolver, executors, callback);
	}

	private static void load0(final String query, final Cancellable cancellable, final ContentResolver resolver, final AppExecutors executors, final Continuation<Result<List<Track>>> callback) throws IllegalArgumentException {
		
		executors.io.execute(new Runnable() {
				@Override
				public void run() {
					
					final String[] selectionArgs = { '%' + query + '%' };

					try (final Cursor cursor = resolver.query(
						MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
						PROJECTION, SELECTION, selectionArgs, SORT_ORDER
					)) {

						final List<Track> musicList = new ArrayList<>(cursor.getCount());

						final int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
						final int nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME);
						final int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);

						while (!cancellable.isCancelled() && cursor.moveToNext()) {
							final Uri uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cursor.getLong(idColumn));
							final String artist = cursor.getString(artistColumn);
							final String name = cursor.getString(nameColumn);
							musicList.add(new Track(name, artist, uri));
						}
						
						if (cancellable.isCancelled()) {
							callback.resumeWith(Result.of(Collections.<Track>emptyList()));
							return;
						}

						callback.resumeWith(Result.of(Collections.unmodifiableList(musicList)));
					}
				}
			});
	}
}
