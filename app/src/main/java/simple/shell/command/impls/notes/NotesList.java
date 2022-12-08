package simple.shell.command.impls.notes;

import java.util.*;
import simple.shell.*;
import simple.shell.command.*;
import simple.shell.data.*;
import simple.shell.utils.*;

public final class NotesList extends LeafCommand {

	private final AppExecutors executors;
	private final NotesDatabase notes;

	public NotesList(AppExecutors executors, NotesDatabase notes) {
		super(new Metadata.Builder("ls").build());
		this.executors = executors;
		this.notes = notes;
	}

	@Override
	public Cancellable execute(final Shell shell, ArgsList args, final Continuation<Void> executionCompletion) {
		return notes.list(new Continuation<List<String>>(executors.main) {
				@Override
				protected void resume(List<String> listNotes) {
					for (int i = 0; i < listNotes.size(); i++) {
						shell.print(i + ": " + listNotes.get(i));
					}
					executionCompletion.resumeWith(null);
				}
			});
	}
}
