package simple.shell.command.impls;

import java.io.*;
import java.nio.file.*;
import simple.shell.*;
import simple.shell.command.*;
import simple.shell.suggestion.*;
import simple.shell.utils.*;

public final class Write extends LeafCommand {

	private final AppExecutors executors;

	public Write(AppExecutors executors) {
		super(new Metadata.Builder("write")
			  .addRequiredArg("text", Suggestions.None)
			  .addRequiredArg("file", Suggestions.Files)
			  .build());
		this.executors = executors;
	}

	@Override
	public Cancellable execute(Shell shell, final ArgsList args, final Continuation<Void> executionCompletion) throws ShellException {
		// TODO : Inspect
		final Path file = shell.getRegularFile(args.asString(1));
		executors.io.execute(new Runnable() {
				@Override
				public void run() {
					try {
						try (final Writer writer = Files.newBufferedWriter(file)) {
							writer.write(args.asString(0));
						}
					} catch (IOException e) {}
					executionCompletion.resumeWith(null);
				}
			});
		return null;
	}
}
