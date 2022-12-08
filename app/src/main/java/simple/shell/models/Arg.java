package simple.shell.models;

public final class Arg {

	public final String content;
	public final int start;
	public final int end;

	public Arg(String content, int start, int end) {
		this.content = content;
		this.start = start;
		this.end = end;
	}
}
