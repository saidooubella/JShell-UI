package simple.shell.command;

import java.util.*;

public final class CommandSet implements Command {

	public final CommandList commands;
	public final String name;

	public CommandSet(String name, CommandList commands) {
		this.commands = commands;
		this.name = name;
	}

	@Override
	public String name() {
		return name;
	}
}
