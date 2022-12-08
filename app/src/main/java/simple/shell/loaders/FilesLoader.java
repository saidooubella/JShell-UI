package simple.shell.loaders;

import android.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import simple.shell.*;
import simple.shell.models.*;

import simple.shell.models.File;
import simple.shell.utils.*;

public final class FilesLoader {

	public static void load(
		final Shell shell,
		final AppExecutors executors,
		final Cancellable cancellable,
		final Path root,
		final String query,
		final boolean onlyDirs,
		final Continuation<Result<List<File>>> callback
	) {

		if (!shell.hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
			shell.requestPermissions(new String[] {
					Manifest.permission.READ_EXTERNAL_STORAGE
				}, new Continuation<Boolean>(null) {
					@Override
					protected void resume(Boolean granted) {
						if (granted) {
							load0(root, executors, cancellable, onlyDirs, query, callback);
						} else if (!cancellable.isCancelled()) {
							callback.resumeWith(Result.<List<File>>message("Permission denied"));
						} else {
							callback.resumeWith(Result.of(Collections.<File>emptyList()));
						}
					}
				});
			return;
		}

		load0(root, executors, cancellable, onlyDirs, query, callback);
	}

	private static void load0(final Path root, final AppExecutors executors, final Cancellable cancellable, final boolean onlyDirs, final String query, final Continuation<Result<List<File>>> callback) {
		
		executors.io.execute(new Runnable() {
				@Override
				public void run() {
					
					final List<File> result;

					try {
						try (final Stream<Path> files = Files.list(root)) {
							result = files.filter(new Predicate<Path>() {
									@Override
									public boolean test(Path path) {
										if (path.getFileName().toString().indexOf(query) != -1)
											return onlyDirs ? Files.isDirectory(path) : true;
										return false;
									}
								}).map(new Function<Path, File>() {
									@Override
									public File apply(Path path) {
										return new File(path.getFileName().toString(), path);
									}
								}).sorted().collect(Collectors.toList());
						}
					} catch (IOException | UncheckedIOException e) {
						result = Collections.<File>emptyList();
					}
					
					if (cancellable.isCancelled()) {
						callback.resumeWith(Result.of(Collections.<File>emptyList()));
						return;
					}
					
					callback.resumeWith(Result.of(result));
				}
			});
	}
}
