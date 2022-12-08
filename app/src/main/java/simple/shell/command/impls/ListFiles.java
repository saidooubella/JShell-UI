package simple.shell.command.impls;

import java.nio.file.*;
import java.util.*;
import simple.shell.*;
import simple.shell.command.*;
import simple.shell.loaders.*;
import simple.shell.models.*;
import simple.shell.suggestion.*;
import simple.shell.utils.*;

public final class ListFiles extends LeafCommand {

	private final AppExecutors executors;
	
	public ListFiles(AppExecutors executors) {
		super(new Metadata.Builder("ls")
			  .addOptionalNArgs("files", Suggestions.Directories)
			  .build());
		this.executors = executors;
	}

	@Override
	public Cancellable execute(final Shell shell, ArgsList args, final Continuation<Void> executionCompletion) throws ShellException {
		
		final Cancellable cancellable = new Cancellable();
		
		if (args.size() <= 1) {
			final Path path = shell.getDirectory(args.isNotEmpty() ? args.asString(0) : ".");
			FilesLoader.load(shell, executors, cancellable, path, "", false, new Continuation<Result<List<File>>>(executors.main) {
					@Override
					protected void resume(Result<List<File>> value) {
						
						if (value.message != null) {
							shell.print(name() + ": " + value.message);
							executionCompletion.resumeWith(null);
							return;
						}
						
						for (final File file : value.value) {
							shell.print(file.name);
						}
						
						executionCompletion.resumeWith(null);
					}
				});
		} else {
			printFiles(shell, executors, cancellable, args, 0, executionCompletion);
		}
		
		return cancellable;
	}

	private void printFiles(final Shell shell, final AppExecutors executors, final Cancellable cancellable, final ArgsList args, final int index, final Continuation<Void> executionCompletion) throws ShellException {
		
		if (index >= args.size()) {
			executionCompletion.resumeWith(null);
			return;
		}
		
		if (index > 0) shell.print("");
		shell.print(args.asString(index));
		final Path path = shell.getDirectory(args.asString(index));
		FilesLoader.load(shell, executors, cancellable, path, "", false, new Continuation<Result<List<File>>>(executors.main) {
				@Override
				protected void resume(final Result<List<File>> value) {
					
					if (value.message != null) {
						shell.print(name() + ": " + value.message);
						executionCompletion.resumeWith(null);
						return;
					}
					
					for (final File file : value.value) {
						shell.print(file.name);
					}
					
					try {
						printFiles(shell, executors, cancellable, args, index + 1, executionCompletion);
					} catch (ShellException e) {
						shell.print(name() + ": " + e.getMessage());
						executionCompletion.resumeWith(null);
					}
				}
			});
	}
}
