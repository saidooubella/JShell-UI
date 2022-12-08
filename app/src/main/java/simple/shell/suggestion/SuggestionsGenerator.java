package simple.shell.suggestion;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import simple.shell.*;
import simple.shell.command.*;
import simple.shell.models.*;
import simple.shell.suggestion.SuggestionsResult.*;
import simple.shell.utils.*;

public final class SuggestionsGenerator {

	public final SuggestionsProvider provider;
	public final Shell shell;

	public SuggestionsGenerator(Shell shell, SuggestionsProvider provider) {
		this.provider = provider;
		this.shell = shell;
	}

	public Cancellable suggestions(final List<Arg> args, final int lineLength, final Continuation<SuggestionsResult> callback) {

		if (args.isEmpty()) {
			return provider.load(Suggestions.Commands, shell, "", new Continuation<List<Suggestion>>(null) {
					@Override
					protected void resume(List<Suggestion> value) {
						callback.resumeWith(new SuggestionsResult(value, MergeAction.Append.instance()));
					}
				});
		}

		final Arg commandArg = args.get(0);
		
		final Command parent = shell.commands.get(commandArg.content);

		if (parent == null || commandArg.end == lineLength) {
			
			if (args.size() > 1 || commandArg.end != lineLength) {
				callback.resumeWith(SuggestionsResult.empty());
				return null;
			}
			
			return provider.load(Suggestions.Commands, shell, commandArg.content, new Continuation<List<Suggestion>>(null) {
					@Override
					protected void resume(List<Suggestion> value) {
						callback.resumeWith(new SuggestionsResult(value, new MergeAction.Replace(commandArg.start, commandArg.end)));
					}
				});
		}

		return proceedCommand(args, parent, lineLength, callback);
	}

	private Cancellable proceedCommand(List<Arg> args, Command command, int lineLength, final Continuation<SuggestionsResult> callback) {

		for (;;) {

			args = args.subList(1, args.size());

			if (command instanceof CommandSet) {

				final CommandSet set = (CommandSet) command;

				if (args.isEmpty()) {
					callback.resumeWith(new SuggestionsResult(filterSuggestions(set.commands.names(), ""), MergeAction.Append.instance()));
					return null;
				}

				final Arg optionArg = args.get(0);
				
				command = set.commands.get(optionArg.content);

				if (command == null || optionArg.end == lineLength) {
					
					if (args.size() > 1 || optionArg.end != lineLength) {
						callback.resumeWith(SuggestionsResult.empty());
						return null;
					}
					
					callback.resumeWith(new SuggestionsResult(filterSuggestions(set.commands.names(), optionArg.content), new MergeAction.Replace(optionArg.start, optionArg.end)));
					return null;
				}

			} else if (command instanceof LeafCommand) {
				return handleLeafCommand((LeafCommand) command, args, lineLength, callback);
			} else {
				throw new IllegalStateException();
			}
		}
	}

	private Cancellable handleLeafCommand(LeafCommand command, List<Arg> args, int lineLength, final Continuation<SuggestionsResult> callback) {

		if (args.isEmpty()) {
			
			if (command.metadata.args.isEmpty()){
				callback.resumeWith(SuggestionsResult.empty());
				return null;
			}
			
			return provider.load(command.metadata.args.get(0).suggestions, shell, "", new Continuation<List<Suggestion>>(null) {
					@Override
					protected void resume(List<Suggestion> value) {
						callback.resumeWith(new SuggestionsResult(value, MergeAction.Append.instance()));
					}
				});
		}

		if (args.size() > command.metadata.maxArgs) {
			callback.resumeWith(SuggestionsResult.empty());
			return null;
		}

		final Arg lastArg = args.get(args.size() - 1);
		final String hint = lastArg.end == lineLength ? lastArg.content : "";
		final int target = lastArg.end == lineLength ? args.size() - 1 : args.size();

		List<ArgInfo> argsInfo = command.metadata.args;

		for (int i = 0; i < target && !argsInfo.isEmpty(); i++) {
			if (!argsInfo.get(0).variadic) {
				argsInfo = argsInfo.subList(1, argsInfo.size());
			}
		}

		final MergeAction mergeAction = lastArg.end == lineLength ? new MergeAction.Replace(lastArg.start, lastArg.end) : MergeAction.Append.instance();
		
		if (argsInfo.isEmpty()) {
			callback.resumeWith(SuggestionsResult.empty());
			return null;
		}
		
		return provider.load(argsInfo.get(0).suggestions, shell, hint, new Continuation<List<Suggestion>>(null) {
				@Override
				protected void resume(List<Suggestion> value) {
					callback.resumeWith(new SuggestionsResult(value, mergeAction));
				}
			});
	}

	private List<Suggestion> filterSuggestions(final Collection<String> names, final String query) {
		return names.stream().filter(new Predicate<String>() {
				@Override
				public boolean test(String p1) {
					return p1.indexOf(query) != -1;
				}
			}).map(new Function<String, Suggestion>() {
				@Override
				public Suggestion apply(String label) {
					return Suggestion.of(label);
				}
			}).collect(Collectors.toList());
	}
}
