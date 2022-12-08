package simple.shell.command.impls;

import java.io.*;
import java.nio.file.*;
import simple.shell.*;
import simple.shell.command.*;
import simple.shell.suggestion.*;
import simple.shell.utils.*;

public final class MakeFile extends LeafCommand {

	public MakeFile() {
		super(new Metadata.Builder("mkfile")
			  .addRequiredNArgs("files", Suggestions.None)
			  .build());
	}

	@Override
	public Cancellable execute(final Shell shell, final ArgsList args, final Continuation<Void> executionCompletion) {
		// TODO : Inspect
		for (int i = 0; i < args.size(); i++) {
			try {
				final Path file = shell.resolvePath(args.asString(i));
				if (Files.exists(file)) {
					shell.print(name() + ": " + args.asString(i) + ": File already exists");
				} else {
					Files.createFile(file);
				}
			} catch (FileAlreadyExistsException e) {
				shell.print(name() + ": " + args.asString(i) + ": Cannot create file");
			} catch (IOException e) {
				shell.print(name() + ": " + e.getMessage());
			}
		}
		
		executionCompletion.resumeWith(null);
		return null;
	}
}
