package simple.shell.suggestion;

import android.content.pm.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import simple.shell.*;
import simple.shell.loaders.*;
import simple.shell.models.*;
import simple.shell.utils.*;

public final class SuggestionsProvider {

	private final Map<Suggestions, SuggestionsSupplier> suggestionsMap;

	public SuggestionsProvider(final Collection<String> commands, final AppExecutors executors, final PackageManager pm) {

		suggestionsMap = new IdentityHashMap<>();
		
		suggestionsMap.put(Suggestions.Recursive, new SuggestionsSupplier() {
				@Override
				public Cancellable load(Shell shell, String hint, final Continuation<List<Suggestion>> callback) {
//					return new SuggestionsGenerator(shell, SuggestionsProvider.this).suggestions(ArgsSplitter.split(hint), hint.length(), new Continuation<SuggestionsResult>(null) {
//							@Override
//							protected void resume(SuggestionsResult value) {
//								callback.resumeWith(value.suggestions);
//							}
//						});
					callback.resumeWith(Collections.<Suggestion>emptyList());
					return null;
				}
			});

		suggestionsMap.put(Suggestions.Directories, new SuggestionsSupplier() {
				@Override
				public Cancellable load(final Shell shell, final String hint, final Continuation<List<Suggestion>> callback) {
					
					final Cancellable cancellable = new Cancellable();
					
					try {
						
						final int index = hint.lastIndexOf(FileUtils.separator);
						final Path root = shell.getDirectory(index != -1 ? hint.substring(0, index + 1) : "");
						final String query = index != -1 ? hint.substring(index + 1) : hint;
						
						FilesLoader.load(shell, executors, cancellable, root, query, true, new Continuation<Result<List<File>>>(null) {
								@Override
								protected void resume(Result<List<File>> value) {
									
									if (value.message != null) {
										callback.resumeWith(Collections.<Suggestion>emptyList());
										return;
									}
									
									final List<Suggestion> results = new ArrayList<>();

									if (!hint.endsWith(FileUtils.separator)) {
										results.add(Suggestion.of("/",  hint + "/"));
									}
									
									results.add(Suggestion.of(".",  hint + "./"));
									results.add(Suggestion.of("..", hint + "../"));
									
									for (final File file : value.value) {
										results.add(Suggestion.of(file.name, shell.removeWorkingDir(file.path.toString()) + FileUtils.separator));
									}
									
									callback.resumeWith(results);
								}
							});
							
					} catch (ShellException e) {
						callback.resumeWith(Collections.<Suggestion>emptyList());
					}
					
					return cancellable;
				}
			});

		suggestionsMap.put(Suggestions.Commands, new SuggestionsSupplier() {
				@Override
				public Cancellable load(final Shell shell, final String hint, Continuation<List<Suggestion>> callback) {
					final List<Suggestion> suggestions = commands.stream().filter(new Predicate<String>() {
							@Override
							public boolean test(String item) {
								return item.indexOf(hint) != -1;
							}
						}).map(new Function<String, Suggestion>() {
							@Override
							public Suggestion apply(String item) {
								return Suggestion.of(item);
							}
						}).collect(Collectors.toList());
					callback.resumeWith(suggestions);
					return null;
				}
			});

		suggestionsMap.put(Suggestions.Files, new SuggestionsSupplier() {
				@Override
				public Cancellable load(final Shell shell, final String hint, final Continuation<List<Suggestion>> callback) {
					
					final Cancellable cancellable = new Cancellable();

					try {
						
						final int index = hint.lastIndexOf(FileUtils.separator);
						final Path root = shell.getDirectory(index != -1 ? hint.substring(0, index + 1) : "");
						final String query = index != -1 ? hint.substring(index + 1) : hint;
						
						FilesLoader.load(shell, executors, cancellable, root, query, false, new Continuation<Result<List<File>>>(null) {
								@Override
								protected void resume(Result<List<File>> value) {
									
									if (value.message != null) {
										callback.resumeWith(Collections.<Suggestion>emptyList());
										return;
									}
									
									final List<Suggestion> results = new ArrayList<>();

									if (!hint.endsWith(FileUtils.separator)) {
										results.add(Suggestion.of("/",  hint + "/"));
									}

									results.add(Suggestion.of(".",  hint + "./"));
									results.add(Suggestion.of("..", hint + "../"));

									for (final File file : value.value) {
										final String path = shell.removeWorkingDir(file.path.toString());
										final String ending = Files.isDirectory(file.path) ? FileUtils.separator : " ";
										results.add(Suggestion.of(file.name, path + ending));
									}
									
									callback.resumeWith(results);
								}
							});
					} catch (ShellException e) {
						callback.resumeWith(Collections.<Suggestion>emptyList());
					}
					
					return cancellable;
				}
			});
		
		suggestionsMap.put(Suggestions.Apps, new SuggestionsSupplier() {
				@Override
				public Cancellable load(Shell shell, String hint, final Continuation<List<Suggestion>> callback) {
					
					final Cancellable cancellable = new Cancellable();
					
					AppsLoader.load(executors, pm, cancellable, hint, false, new Continuation<List<App>>(null) {
							@Override
							protected void resume(List<App> value) {
								final List<Suggestion> suggestions = value.stream().map(new Function<App, Suggestion>() {
										@Override
										public Suggestion apply(App app) {
											return Suggestion.of(app.name, app.name + ' ');
										}
									}).collect(Collectors.toList());
								callback.resumeWith(suggestions);
							}
						});
					
					return cancellable;
				}
			});

		suggestionsMap.put(Suggestions.None, new SuggestionsSupplier() {
				@Override
				public Cancellable load(final Shell shell, final String hint, Continuation<List<Suggestion>> callback) {
					callback.resumeWith(Collections.<Suggestion>emptyList());
					return null;
				}
			});
	}

	public Cancellable load(Suggestions suggestions, Shell shell, String hint, Continuation<List<Suggestion>> callback) {
		if (suggestions.supplier == null)
			return suggestionsMap.get(suggestions).load(shell, hint, callback);
		return suggestions.supplier.load(shell, hint, callback);
	}
}
