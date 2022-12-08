package simple.shell.command.impls;

import java.io.*;
import java.nio.file.*;
import simple.shell.*;
import simple.shell.command.*;
import simple.shell.suggestion.*;
import simple.shell.utils.*;

public final class Read extends LeafCommand {

	private final AppExecutors executors;

	public Read(AppExecutors executors) {
		super(new Metadata.Builder("read")
			  .addRequiredNArgs("files", Suggestions.Files)
			  .build());
		this.executors = executors;
	}

	@Override
	public Cancellable execute(final Shell shell, final ArgsList args, final Continuation<Void> executionCompletion) {
		// TODO : Inspect
		final Cancellable cancellable = new Cancellable();
		
		executors.io.execute(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; !cancellable.isCancelled() && i < args.size(); i++) {
						try {
							try (final BufferedReader file = Files.newBufferedReader(shell.getRegularFile(args.asString(i)))) {
								for (String line; !cancellable.isCancelled() && (line = file.readLine()) != null; ) {
									shell.print(line);
								}
							}
						} catch (IOException | ShellException e) {
							shell.print(name() + ": " + e.getMessage());
						}
					}
					executionCompletion.resumeWith(null);
				}
			});
		
		return cancellable;
	}
}
