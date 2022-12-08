package simple.shell.command.impls.apps;

import android.content.pm.*;
import java.util.*;
import simple.shell.*;
import simple.shell.command.*;
import simple.shell.loaders.*;
import simple.shell.models.*;
import simple.shell.utils.*;

public final class AppsList extends LeafCommand {
	
	private final AppExecutors executors;
	private final PackageManager pm;

	public AppsList(AppExecutors executors, PackageManager pm) {
		super(new Metadata.Builder("ls").build());
		this.executors = executors;
		this.pm = pm;
	}

	@Override
	public Cancellable execute(final Shell shell, ArgsList args, final Continuation<Void> executionCompletion) {
		
		final Cancellable cancellable = new Cancellable();
		
		AppsLoader.load(executors, pm, cancellable, "", false, new Continuation<List<App>>(executors.main) {
				@Override
				protected void resume(List<App> value) {
					for (final App app : value) {
						shell.print(app.name);
					}
					executionCompletion.resumeWith(null);
				}
			});
		
		return cancellable;
	}
}
