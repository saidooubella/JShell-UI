package simple.shell.command.impls.notes;

import simple.shell.*;
import simple.shell.command.*;
import simple.shell.data.*;
import simple.shell.suggestion.*;
import simple.shell.utils.*;

public final class NoteRemove extends LeafCommand {

	private final AppExecutors executors;
	private final NotesDatabase notes;

	public NoteRemove(AppExecutors executors, NotesDatabase notes) {
		super(new Metadata.Builder("rm")
			  .addRequiredArg("index", Suggestions.None)
			  .build());
		this.executors = executors;
		this.notes = notes;
	}

	@Override
	public Cancellable execute(final Shell shell, ArgsList args, final Continuation<Void> executionCompletion) throws ShellException {
		final long noteId = args.asLong(0);
		return notes.removeNode(noteId, new Continuation<Boolean>(executors.main) {
				@Override
				protected void resume(Boolean value) {
					if (!value) {
						shell.print(name() + ": " + noteId + ": Invalid note index");
					}
					executionCompletion.resumeWith(null);
				}
			});
	}
}
