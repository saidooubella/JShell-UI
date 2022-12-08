package simple.shell.command;

import java.util.*;
import simple.shell.*;
import simple.shell.models.*;
import simple.shell.suggestion.*;
import simple.shell.utils.*;

public abstract class LeafCommand implements Command {

	public final Metadata metadata;

	public LeafCommand(Metadata metadata) {
		this.metadata = metadata;
	}

	public abstract Cancellable execute(Shell shell, ArgsList args, final Continuation<Void> executionCompletion) throws ShellException;

	@Override
	public final String name() {
		return metadata.name;
	}

	public static final class Metadata {

		public final List<ArgInfo> args;
		public final String name;
		public final int minArgs;
		public final int maxArgs;

		private Metadata(String name, List<ArgInfo> args, int minArgs, int maxArgs) {
			this.minArgs = minArgs;
			this.maxArgs = maxArgs;
			this.name = name;
			this.args = args;
		}

		public static final class Builder implements RequiredArg {

			private final List<ArgInfo> args;
			private final String name;

			private int minArgs, maxArgs;

			public Builder(String name) {
				this.args = new ArrayList<>();
				this.name = name;
			}

			@Override
			public Metadata build() {
				return new Metadata(name, args, minArgs, maxArgs);
			}

			@Override
			public MetadataBuilder addOptionalNArgs(String name, Suggestions suggestions) {
				args.add(new ArgInfo(name, suggestions, false, true));
				maxArgs = Integer.MAX_VALUE;
				return this;
			}

			@Override
			public OptionalArg addOptionalArg(String name, Suggestions suggestions) {
				args.add(new ArgInfo(name, suggestions, false, false));
				maxArgs = maxArgs + 1;
				return this;
			}

			@Override
			public MetadataBuilder addRequiredNArgs(String name, Suggestions suggestions) {
				args.add(new ArgInfo(name, suggestions, true, true));
				maxArgs = Integer.MAX_VALUE;
				minArgs = minArgs + 1;
				return this;
			}

			@Override
			public RequiredArg addRequiredArg(String name, Suggestions suggestions) {
				args.add(new ArgInfo(name, suggestions, true, false));
				maxArgs = maxArgs + 1;
				minArgs = minArgs + 1;
				return this;
			}
		}

		public interface MetadataBuilder {
			Metadata build();
		}

		public interface OptionalNArgs extends MetadataBuilder {
			MetadataBuilder addOptionalNArgs(String name, Suggestions suggestions);
		}

		public interface OptionalArg extends OptionalNArgs {
			OptionalArg addOptionalArg(String name, Suggestions suggestions);
		}

		public interface RequiredNArgs extends OptionalArg {
			MetadataBuilder addRequiredNArgs(String name, Suggestions suggestions);
		}

		public interface RequiredArg extends RequiredNArgs {
			RequiredArg addRequiredArg(String name, Suggestions suggestions);
		}
	}
}
