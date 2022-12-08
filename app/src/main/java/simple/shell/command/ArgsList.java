package simple.shell.command;

import java.util.*;
import simple.shell.models.*;
import simple.shell.utils.*;

public final class ArgsList {
	
	private final List<Arg> args;

	public ArgsList(List<Arg> args) {
		this.args = args;
	}
	
	public List<Arg> asArgsList() { 
		return args;
	}
	
	public String asString(int index) {
		return args.get(index).content;
	}
	
	public long asLong(int index) throws ShellException {
		final String number = asString(index);
		try {
			return Long.parseLong(number);
		} catch (NumberFormatException e) {
			throw new ShellException(number + ": Malformed int64 number");
		}
	}
	
	public int asInt(int index) throws ShellException {
		final String number = asString(index);
		try {
			return Integer.parseInt(number);
		} catch (NumberFormatException e) {
			throw new ShellException(number + ": Malformed int32 number");
		}
	}
	
	public boolean isNotEmpty() {
		return !args.isEmpty();
	}

	public int size() {
		return args.size();
	}
}
