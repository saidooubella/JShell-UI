package simple.shell.models;

public final class App implements Comparable<App> {

	public final String packageName;
	public final String name;

	public App(String name, String packageName) {
		this.packageName = packageName;
		this.name = name;
	}

	@Override
	public int compareTo(App that) {
		return name.compareToIgnoreCase(that.name);
	}
}
