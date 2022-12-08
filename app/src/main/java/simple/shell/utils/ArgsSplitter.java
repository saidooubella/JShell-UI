package simple.shell.utils;

import java.util.*;
import simple.shell.models.*;

public final class ArgsSplitter {

	public static List<Arg> split(final String line) {

		final List<Arg> args = new ArrayList<>();
		final StringBuilder builder = new StringBuilder();

		for (int i = 0; i < line.length();) {

			while (isSpace(line, i))
				i++;

			boolean escape = false;
			boolean quote = false;

			final int start = i;

			builder.delete(0, builder.length());

			for (; i < line.length(); i++) {

				final char c = line.charAt(i);

				if (escape) {
					escape = false;
					builder.append(c);
					continue;
				}

				if (!quote && isSpace(c))
					break;

				switch (c) {
					case '\\': escape = true;     break;
					case '"':  quote  = !quote;   break;
					default:   builder.append(c); break;
				}
			}

			final int end = i;

			if (start >= end) {
				continue;
			}

			args.add(new Arg(builder.toString(), start, end));
		}

		return args;
	}

	private static boolean isSpace(final String line, final int i) {
		return i < line.length() && isSpace(line.charAt(i));
	}

	private static boolean isSpace(final char c) {
		return Character.isWhitespace(c);
	}
}
