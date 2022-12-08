package simple.shell.command.impls.notes;

import simple.shell.*;
import simple.shell.command.*;
import simple.shell.data.*;
import simple.shell.utils.*;

public final class NotesClear extends LeafCommand {

	private final NotesDatabase notes;

	public NotesClear(NotesDatabase notes) {
		super(new Metadata.Builder("clear").build());
		this.notes = notes;
	}

	@Override
	public Cancellable execute(Shell shell, ArgsList args, final Continuation<Void> executionCompletion) {
		notes.clear(new Continuation<Void>(null) {
				@Override
				protected void resume(Void value) {
					executionCompletion.resumeWith(null);
				}
			});
		return null;
	}
}
