package simple.shell.loaders;

import android.content.*;
import android.content.pm.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import simple.shell.*;
import simple.shell.models.*;
import simple.shell.utils.*;

public final class AppsLoader {

	private static final Intent FILTER = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);

	public static void load(
		final AppExecutors executors,
		final PackageManager pm,
		final Cancellable cancellable,
		final String query,
		final boolean exactFilter,
		final Continuation<List<App>> callback
	) {
		
		executors.io.execute(new Runnable() {
				@Override
				public void run() {
					
					final List<App> result = pm.queryIntentActivities(FILTER, 0).stream().filter(new Predicate<ResolveInfo>() {
							@Override
							public boolean test(ResolveInfo app) {
								if (exactFilter)
									return app.loadLabel(pm).toString().equals(query);
								return app.loadLabel(pm).toString().indexOf(query) != -1;
							}
						}).map(new Function<ResolveInfo, App>() {
							@Override
							public App apply(ResolveInfo app) {
								final String name = app.loadLabel(pm).toString();
								final String packageName = app.activityInfo.packageName;
								return new App(name, packageName);
							}
						}).sorted().collect(Collectors.toList());
						
					if (cancellable.isCancelled()) {
						callback.resumeWith(Collections.<App>emptyList());
						return;
					}

					callback.resumeWith(result);
				}
			});
	}
}
