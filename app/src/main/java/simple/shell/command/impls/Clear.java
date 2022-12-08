package simple.shell.command.impls;

import simple.shell.*;
import simple.shell.command.*;
import simple.shell.utils.*;

public final class Clear extends LeafCommand {

	public Clear() {
		super(new Metadata.Builder("clear").build());
	}

	@Override
	public Cancellable execute(Shell shell, ArgsList args, final Continuation<Void> executionCompletion) {
		shell.clear();
		executionCompletion.resumeWith(null);
		return null;
	}
}
