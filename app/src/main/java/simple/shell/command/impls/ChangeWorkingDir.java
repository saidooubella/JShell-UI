package simple.shell.command.impls;

import simple.shell.*;
import simple.shell.command.*;
import simple.shell.suggestion.*;
import simple.shell.utils.*;

public final class ChangeWorkingDir extends LeafCommand {

	public ChangeWorkingDir() {
		super(new Metadata.Builder("cd")
			  .addOptionalArg("dir", Suggestions.Directories)
			  .build());
	}

	@Override
	public Cancellable execute(Shell shell, ArgsList args, final Continuation<Void> executionCompletion) throws ShellException {
		shell.workingDir = shell.getDirectory(args.size() == 1 ? args.asString(0) : "/sdcard").normalize();
		executionCompletion.resumeWith(null);
		return null;
	}
}
