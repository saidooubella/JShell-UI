package simple.shell.utils;

import java.util.concurrent.atomic.*;

public final class Cancellable {
	
	private final AtomicBoolean cancelled = new AtomicBoolean();

	public void cancel() {
		cancelled.set(true);
	}
	
	public boolean isCancelled() {
		return cancelled.get();
	}
}
