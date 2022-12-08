package simple.shell.command.impls.notes;

import android.content.*;
import simple.shell.*;
import simple.shell.command.*;
import simple.shell.data.*;
import simple.shell.suggestion.*;
import simple.shell.utils.*;

public final class NoteCopy extends LeafCommand {

	private final AppExecutors executors;
	private final ClipboardManager clipboard;
	private final NotesDatabase notes;
	
	public NoteCopy(AppExecutors executors, NotesDatabase notes, ClipboardManager clipboard) {
		super(new Metadata.Builder("cp")
			  .addRequiredArg("index", Suggestions.None)
			  .build());
		this.clipboard = clipboard;
		this.executors = executors;
		this.notes = notes;
	}

	@Override
	public Cancellable execute(final Shell shell, final ArgsList args, final Continuation<Void> executionCompletion) throws ShellException {
		
		final long noteId = args.asLong(0);
		
		return notes.get(noteId, new Continuation<ContResult<String>>(executors.main) {
				@Override
				protected void resume(ContResult<String> content) {
					
					if (content instanceof ContResult.Cancellation) {
						executionCompletion.resumeWith(null);
						return;
					}
					
					if (content instanceof ContResult.Failure) {
						shell.print(name() + ": " + ((ContResult.Failure) content).message);
						executionCompletion.resumeWith(null);
						return;
					}

					clipboard.setPrimaryClip(ClipData.newPlainText("", ((ContResult.Success<String>) content).value));
					shell.print(name() + ": The note was copied");
					
					executionCompletion.resumeWith(null);
				}
			});
	}
}
