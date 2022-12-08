package simple.shell.utils;

public final class Result<T> {
	
	public final T value;
	public final String message;

	private Result(T value, String message) {
		this.value = value;
		this.message = message;
	}
	
	public static <T> Result<T> of(T value) {
		return new Result<T>(value, null);
	}
	
	public static <T> Result<T> message(String value) {
		return new Result<T>(null, value);
	}
}
