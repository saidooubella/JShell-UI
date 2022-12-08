package simple.shell.command.impls.apps;

import android.content.*;
import android.content.pm.*;
import android.net.*;
import java.util.*;
import simple.shell.*;
import simple.shell.command.*;
import simple.shell.loaders.*;
import simple.shell.models.*;
import simple.shell.suggestion.*;
import simple.shell.utils.*;

public final class AppRemove extends LeafCommand {

	private final AppExecutors executors;
	private final PackageManager pm;
	
	public AppRemove(AppExecutors executors, PackageManager pm) {
		super(new Metadata.Builder("rm")
			  .addRequiredArg("package", Suggestions.Apps)
			  .build());
		this.executors = executors;
		this.pm = pm;
	}

	@Override
	public Cancellable execute(final Shell shell, ArgsList args, final Continuation<Void> executionCompletion) {

		final String app = args.asString(0);
		
		final Cancellable cancellable = new Cancellable();
		
		AppsLoader.load(executors, pm, cancellable, app, true, new Continuation<List<App>>(executors.main) {
				@Override
				protected void resume(final List<App> apps) {
					
					if (apps.isEmpty()) {
						shell.print(name() + ": " + app + ": Package not found");
						executionCompletion.resumeWith(null);
						return;
					}

					if (apps.size() == 1) {
						uninstallPackage(shell, apps.get(0).packageName);
						executionCompletion.resumeWith(null);
						return;
					}

					shell.print(" > Which of the following is your target:");

					for (int i = 0; i < apps.size(); i++) {
						shell.print(i + ": " + apps.get(i).packageName);
					}

					shell.prompt("Select an index...", new Continuation<String>(null) {
							@Override
							protected void resume(String result) {
								try {
									final int index = Integer.parseInt(result);
									if (0 <= index && index < apps.size()) {
										uninstallPackage(shell, apps.get(index).packageName);
									} else {
										shell.print(name() + ": " + index + ": Invalid index");
									}
								} catch (NumberFormatException e) {
									shell.print(name() + ": " + result + ": Malformed number");
								}
								executionCompletion.resumeWith(null);
							}
						});
				}
			});
			
		return cancellable;
	}

	private void uninstallPackage(Shell shell, String packageName) {
		final Uri packageURI = Uri.parse("package:" + packageName);
		shell.startActivity(new Intent(Intent.ACTION_DELETE, packageURI));
	}
}
