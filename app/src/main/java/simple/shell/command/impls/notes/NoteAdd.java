package simple.shell.command.impls.notes;

import simple.shell.*;
import simple.shell.command.*;
import simple.shell.data.*;
import simple.shell.suggestion.*;
import simple.shell.utils.*;

public final class NoteAdd extends LeafCommand {

	private final NotesDatabase notes;

	public NoteAdd(NotesDatabase notes) {
		super(new Metadata.Builder("add")
			  .addRequiredArg("text", Suggestions.None)
			  .build());
		this.notes = notes;
	}

	@Override
	public Cancellable execute(Shell shell, ArgsList args, final Continuation<Void> executionCompletion) {
		notes.add(args.asString(0), new Continuation<Void>(null) {
				@Override
				protected void resume(Void value) {
					executionCompletion.resumeWith(null);
				}
			});
		return null;
	}
}
