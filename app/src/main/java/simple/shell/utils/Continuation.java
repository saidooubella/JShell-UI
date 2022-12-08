package simple.shell.utils;

import java.util.concurrent.*;

public abstract class Continuation<T> {
	
	private final Executor executor;

	public Continuation(Executor executor) {
		this.executor = executor;
	}
	
	protected abstract void resume(T value);
	
	public final void resumeWith(final T value) {
		if (executor == null) {
			resume(value);
		} else {
			executor.execute(new Runnable() {
					@Override
					public void run() {
						resume(value);
					}
				});
		}
	}
}
