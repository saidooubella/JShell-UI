package simple.shell.command.impls;

import java.io.*;
import java.nio.file.*;
import simple.shell.*;
import simple.shell.command.*;
import simple.shell.suggestion.*;
import simple.shell.utils.*;

public final class MakeDir extends LeafCommand {

	public MakeDir() {
		super(new Metadata.Builder("mkdir")
			  .addRequiredNArgs("dirs", Suggestions.None)
			  .build());
	}

	@Override
	public Cancellable execute(final Shell shell, final ArgsList args, final Continuation<Void> executionCompletion) {
		// TODO : Inspect
		for (int i = 0; i < args.size(); i++) {

			final Path file = shell.resolvePath(args.asString(i));

			if (Files.exists(file)) {
				shell.print(name() + ": " + args.asString(i) + ": File already exists");
				continue;
			}

			try {
				Files.createDirectories(file);
			} catch (IOException | FileAlreadyExistsException e) {
				shell.print(name() + ": " + args.asString(i) + ": Cannot create directory");
			}
		}
		
		executionCompletion.resumeWith(null);
		return null;
	}
}
