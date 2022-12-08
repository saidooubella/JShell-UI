package simple.shell.command;

import java.util.*;

public final class CommandList {

	private final Map<String, Command> commands;

	private CommandList(Map<String, Command> commands) {
		this.commands = commands;
	}

	public Command get(String name) {
		return commands.get(name);
	}
	
	public Collection<String> names() {
		return commands.keySet();
	}
	
	public Collection<Command> commands() {
		return commands.values();
	}

	public static final class Builder {

		private final Map<String, Command> commands;

		public Builder() {
			this.commands = new TreeMap<>();
		}

		public Builder add(Command command) {
			commands.put(command.name(), command);
			return this;
		}

		public CommandList build() {
			return new CommandList(commands);
		}
	}
}
