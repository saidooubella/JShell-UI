package simple.shell.suggestion;

public final class Suggestion {

	public final String label;
	public final String value;

	private Suggestion(String label, String value) {
		this.label = label;
		this.value = value;
	}
	
	public static Suggestion of(String label) {
		return of(label, label + ' ');
	}

	public static Suggestion of(String label, String value) {
		return new Suggestion(label, wrap(value));
	}

	private static String wrap(final String value) {

		if (hasNoSpaces(value.trim()))
			return value;

		final int start = afterLeadingSpaces(value);
		final int end = beforeTrailingSpaces(value, start);

		final String leading  = value.substring(0, start);
		final String content  = value.substring(start, end);
		final String trailing = value.substring(end, value.length());

		return leading + '"' + content + '"' + trailing;
	}

	private static int beforeTrailingSpaces(String value, int start) {
		int end = value.length() - 1;
		while (end > start && Character.isWhitespace(value.charAt(end)))
			end--;
		return end + 1;
	}

	private static int afterLeadingSpaces(String value) {
		int start = 0;
		while (start < value.length() && Character.isWhitespace(value.charAt(start)))
			start++;
		return start;
	}

	private static boolean hasNoSpaces(final String value) {
		for (int i = 0; i < value.length(); i++)
			if (Character.isWhitespace(value.charAt(i)))
				return false;
		return true;
	}
}
