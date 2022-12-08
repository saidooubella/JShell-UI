package simple.shell.utils;

public abstract class ContResult<T> {
	
	private static final ContResult CANCELLATION = new Cancellation();
	
	public static <T> ContResult<T> success(T value) {
		return new Success<T>(value);
	}
	
	public static <T> ContResult<T> failure(String message) {
		return new Failure<T>(message);
	}
	
	public static <T> ContResult<T> cancellation() {
		return (ContResult<T>) CANCELLATION;
	}
	
	public static final class Success<T> extends ContResult<T> {
		
		public final T value;

		private Success(T value) {
			this.value = value;
		}
	}
	
	public static final class Failure<T> extends ContResult<T> {

		public final String message;

		private Failure(String message) {
			this.message = message;
		}
	}
	
	public static final class Cancellation extends ContResult {
		private Cancellation() { }
	}
}
