package simple.shell.models;

import java.nio.file.*;

public final class File implements Comparable<File> {

	public final String name;
	public final Path path;

	public File(String name, Path path) {
		this.name = name;
		this.path = path;
	}

	@Override
	public int compareTo(File that) {
		return name.compareToIgnoreCase(that.name);
	}
}
