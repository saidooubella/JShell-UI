package simple.shell.utils;

public final class Observable<T> {

	private final Observer<T> observer;

	private T value;

	public Observable(T value, Observer<T> observer) {
		this.observer = observer;
		this.set(value);
	}

	public void set(T value) {
		this.value = value;
		this.observer.onChange(value);
	}

	public T get() {
		return value;
	}

	public interface Observer<T> {
		void onChange(T value);
	}
}
