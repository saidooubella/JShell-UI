package simple.shell.loaders;

import android.*;
import android.content.*;
import android.database.*;
import android.provider.*;
import java.util.*;
import simple.shell.*;
import simple.shell.models.*;
import simple.shell.utils.*;

public final class ContactsLoader {

	private static final String[] PROJECTION = {
		ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY,
		ContactsContract.CommonDataKinds.Phone.NUMBER,
	};

	private static final String SELECTION =
	ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " LIKE ? AND " +
	ContactsContract.CommonDataKinds.Phone.HAS_PHONE_NUMBER + " = 1 AND " +
	"is_sdn_contact = 0";

	private static final String SORT_ORDER = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY + " ASC";

	public static void load(
		final Shell shell,
		final AppExecutors executors,
		final ContentResolver resolver,
		final Cancellable cancellable,
		final String query,
		final Continuation<Result<List<Contact>>> callback
	) {

		if (!shell.hasPermission(Manifest.permission.READ_CONTACTS) || !shell.hasPermission(Manifest.permission.READ_PHONE_NUMBERS)) {
			shell.requestPermissions(new String[] {
					Manifest.permission.READ_PHONE_NUMBERS,
					Manifest.permission.READ_CONTACTS,
				}, new Continuation<Boolean>(null) {
					@Override
					protected void resume(Boolean granted) {
						if (granted) {
							load0(cancellable, executors, query, resolver, callback);
						} else if (!cancellable.isCancelled()) {
							callback.resumeWith(Result.<List<Contact>>message("Permission denied"));
						} else {
							callback.resumeWith(Result.of(Collections.<Contact>emptyList()));
						}
					}
				});
			return;
		}

		load0(cancellable, executors, query, resolver, callback);

		return;
	}

	private static void load0(final Cancellable cancellable, final AppExecutors executors, final String query, final ContentResolver resolver, final Continuation<Result<List<Contact>>> callback) {

		executors.io.execute(new Runnable() {
				@Override
				public void run() {

					final String[] selectionArgs = { '%' + query + '%' };

					try {

						try (final Cursor cursor = resolver.query(
							ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
							PROJECTION, SELECTION, selectionArgs, SORT_ORDER
						)) {

							final List<Contact> contactsList = new ArrayList<>(cursor.getCount());

							final int phoneColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER);
							final int nameColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME_PRIMARY);
							
							while (!cancellable.isCancelled() && cursor.moveToNext()) {
								final String phone = cursor.getString(phoneColumn);
								final String name = cursor.getString(nameColumn);
								contactsList.add(new Contact(name, phone));
							}

							if (cancellable.isCancelled()) {
								callback.resumeWith(Result.of(Collections.<Contact>emptyList()));
								return;
							}

							callback.resumeWith(Result.of(Collections.unmodifiableList(contactsList)));
						}
					} catch (Exception e) {
						callback.resumeWith(Result.of(Collections.<Contact>emptyList()));
					}
				}
			});
	}
}
