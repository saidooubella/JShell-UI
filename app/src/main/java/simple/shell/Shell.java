package simple.shell;

import android.content.*;
import java.nio.file.*;
import java.util.*;
import simple.shell.command.*;
import simple.shell.models.*;
import simple.shell.utils.*;

public abstract class Shell {

	public final CommandList commands;
	
	public Path workingDir;

	public Shell(Path workingDir, CommandList commands) {
		this.workingDir = workingDir;
		this.commands = commands;
	}
	
	public abstract boolean hasPermission(String permission);
	public abstract void requestPermissions(String[] permission, Continuation<Boolean> callback);
	
	public abstract void startActivity(Intent intent);
	public abstract void prompt(String prompt, Continuation<String> result);
	public abstract void print(String line);
	public abstract void clear();

	public final String removeWorkingDir(final String path) {
		final String root = workingDir.toString();
		return path.startsWith(root) ? path.substring(root.length() + 1, path.length()) : path;
	}

	public final Path resolvePath(final String other) {

		if (other.isEmpty()) return workingDir;

		final Path path = other.startsWith(FileUtils.separator)
			? Paths.get(other) : workingDir.resolve(other);

		return path;
	}

	public final Path getFile(String other) throws ShellException {
		
		final Path path = resolvePath(other);

		if (Files.notExists(path))
			throw new ShellException(other + ": No such file or directory");

		return path;
	}
	
	public final Path getRegularFile(final String other) throws ShellException {
		
		final Path path = getFile(other);

		if (!Files.isRegularFile(path))
			throw new ShellException(other + ": Not a file");

		return path;
	}

	public final Path getDirectory(String other) throws ShellException {
		
		final Path path = getFile(other);

		if (!Files.isDirectory(path))
			throw new ShellException(other + ": Not a directory");

		return path;
	}

	public final Cancellable run(final List<Arg> args, final Continuation<Void> executionCompletion) {

		if (args.isEmpty()) {
			executionCompletion.resumeWith(null);
			return null;
		}

		final Command parent = commands.get(args.get(0).content);

		if (parent == null) {
			print("shell: " + args.get(0).content + ": Command not found");
			executionCompletion.resumeWith(null);
			return null;
		}

		return proceedCommand(parent, parent, args, executionCompletion);
	}

	private final Cancellable proceedCommand(final Command parent, Command command, List<Arg> args, final Continuation<Void> executionCompletion) {

		for (;;) {

			args = args.subList(1, args.size());

			if (command instanceof CommandSet) {

				if (args.isEmpty()) {
					print(parent.name() + ": Too few args");
					executionCompletion.resumeWith(null);
					return null;
				}

				final CommandSet set = (CommandSet) command;
				command = set.commands.get(args.get(0).content);

				if (command == null) {
					print(parent.name() + ": " + args.get(0).content + ": Option not found");
					executionCompletion.resumeWith(null);
					return null;
				}

			} else if (command instanceof LeafCommand) {

				final LeafCommand leaf = (LeafCommand) command;

				if (args.size() < leaf.metadata.minArgs) {
					print(parent.name() + ": Too few args");
					executionCompletion.resumeWith(null);
					return null;
				}

				if (args.size() > leaf.metadata.maxArgs) {
					print(parent.name() + ": Too many args");
					executionCompletion.resumeWith(null);
					return null;
				}

				try {
					return leaf.execute(this, new ArgsList(args), executionCompletion);
				} catch (ShellException e) {
					print(parent.name() + ": " + e.getMessage());
				}

				executionCompletion.resumeWith(null);
				return null;
			} else {
				throw new IllegalStateException();
			}
		}
	}
}
