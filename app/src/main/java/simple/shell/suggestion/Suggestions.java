package simple.shell.suggestion;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import simple.shell.*;
import simple.shell.utils.*;

public final class Suggestions {

	public static final Suggestions Directories = new Suggestions(null);
	public static final Suggestions Recursive = new Suggestions(null);
	public static final Suggestions Commands = new Suggestions(null);
	public static final Suggestions Files = new Suggestions(null);
	public static final Suggestions Apps = new Suggestions(null);
	public static final Suggestions None = new Suggestions(null);

	public final SuggestionsSupplier supplier;

	private Suggestions(SuggestionsSupplier supplier) {
		this.supplier = supplier;
	}

	public static Suggestions custom(final Suggestion... suggestions) {
		final SuggestionsSupplier supplier = new SuggestionsSupplier() {
			@Override
			public Cancellable load(final Shell shell, final String hint, final Continuation<List<Suggestion>> callback) {
				final List<Suggestion> result = Stream.of(suggestions).filter(new Predicate<Suggestion>() {
						@Override
						public boolean test(Suggestion item) {
							return item.label.indexOf(hint) != -1;
						}
					}).collect(Collectors.toList());
				callback.resumeWith(result);
				return null;
			}
		};
		return new Suggestions(supplier);
	}
}
