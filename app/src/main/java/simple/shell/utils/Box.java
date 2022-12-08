package simple.shell.utils;

public final class Box<T> {
	
	private T value = null;
	
	public void set(T value) {
		this.value = value;
	}
	
	public T take() {
		final T temp = value;
		value = null;
		return temp;
	}

	public boolean isNotEmpty() {
		return value != null;
	}

	public boolean isEmpty() {
		return value == null;
	}
}
