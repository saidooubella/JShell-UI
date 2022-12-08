package simple.shell.command.impls;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import simple.shell.*;
import simple.shell.command.*;
import simple.shell.suggestion.*;
import simple.shell.utils.*;

public final class Remove extends LeafCommand {

	private static final FileVisitor<Path> DeleteVisitor = new SimpleFileVisitor<Path>() {

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			Files.delete(file);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			Files.delete(dir);
			return FileVisitResult.CONTINUE;
		}
	};

	private final AppExecutors executors;

	public Remove(AppExecutors executors) {
		super(new Metadata.Builder("rm")
			  .addRequiredNArgs("files", Suggestions.Files)
			  .build());
		this.executors = executors;
	}

	@Override
	public Cancellable execute(final Shell shell, final ArgsList args, final Continuation<Void> executionCompletion) {
		// TODO : Inspect
		executors.io.execute(new Runnable() {
				@Override
				public void run() {
					for (int i = 0; i < args.size(); i++) {
						try {
							Files.walkFileTree(shell.getFile(args.asString(i)).normalize(), DeleteVisitor);
						} catch (IOException | ShellException e) {
							shell.print(name() + ": " + e.getMessage());
						}
					}
					executionCompletion.resumeWith(null);
				}
			});
		return null;
	}
}
