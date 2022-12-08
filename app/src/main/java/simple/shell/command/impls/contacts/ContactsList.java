package simple.shell.command.impls.contacts;

import java.util.*;
import simple.shell.*;
import simple.shell.command.*;
import simple.shell.loaders.*;
import simple.shell.models.*;
import android.content.*;
import simple.shell.utils.*;

public final class ContactsList extends LeafCommand {

	private final ContentResolver resolver;
	private final AppExecutors executors;

	public ContactsList(AppExecutors executors, ContentResolver resolver) {
		super(new Metadata.Builder("ls").build());
		this.executors = executors;
		this.resolver = resolver;
	}

	@Override
	public Cancellable execute(final Shell shell, ArgsList args, final Continuation<Void> executionCompletion) {
		
		final Cancellable cancellable = new Cancellable();
		
		ContactsLoader.load(shell, executors, resolver, cancellable, "", new Continuation<Result<List<Contact>>>(executors.main) {
				@Override
				protected void resume(Result<List<Contact>> value) {
					
					if (value.message != null) {
						shell.print(name() + ": " + value.message);
						executionCompletion.resumeWith(null);
						return;
					}
					
					final List<Contact> contacts = value.value;

					for (int i = 0; i < contacts.size(); i++) {
						if (i > 0) shell.print("");
						shell.print("Name : " + contacts.get(i).name);
						shell.print("Phone: " + contacts.get(i).phone);
					}
					
					executionCompletion.resumeWith(null);
				}
			});
			
		return cancellable;
	}
}

