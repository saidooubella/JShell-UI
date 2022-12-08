package simple.shell.models;

import simple.shell.suggestion.*;

public final class ArgInfo {

	public final Suggestions suggestions;
	public final boolean required;
	public final boolean variadic;
	public final String name;

	public ArgInfo(String name, Suggestions suggestions, boolean required, boolean variadic) {
		this.suggestions = suggestions;
		this.required = required;
		this.variadic = variadic;
		this.name = name;
	}
}
