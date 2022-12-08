package simple.shell.command.impls;

import simple.shell.*;
import simple.shell.command.*;
import simple.shell.utils.*;

public final class PrintWorkingDir extends LeafCommand {

	public PrintWorkingDir() {
		super(new Metadata.Builder("pwd").build());
	}

	@Override
	public Cancellable execute(Shell shell, ArgsList args, final Continuation<Void> executionCompletion) {
		shell.print(shell.workingDir.toString());
		executionCompletion.resumeWith(null);
		return null;
	}
}
