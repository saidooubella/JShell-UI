package simple.shell.suggestion;

import java.util.*;

public final class SuggestionsResult {
	
	private static final SuggestionsResult EMPTY = new SuggestionsResult(Collections.<Suggestion>emptyList(), MergeAction.Append.instance());
	
	public final List<Suggestion> suggestions;
	public final MergeAction mergeAction;

	public SuggestionsResult(List<Suggestion> suggestions, MergeAction mergeAction) {
		this.suggestions = suggestions;
		this.mergeAction = mergeAction;
	}

	public static SuggestionsResult empty() {
		return EMPTY;
	}
	
	public static abstract class MergeAction {
		
		private MergeAction() { }
		
		public static final class Replace extends MergeAction {
			
			public final int start, end;

			public Replace(int start, int end) {
				this.start = start;
				this.end = end;
			}
		}
		
		public static final class Append extends MergeAction {
			
			private static final Append instance = new Append();

			private Append() { }

			public static Append instance() {
				return instance;
			}
		}
	}
}
