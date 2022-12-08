package simple.shell;

import android.os.*;
import java.util.concurrent.*;

public final class AppExecutors {
	
	public final ScheduledExecutorService io;
	public final MainExecutor main;
	
	public AppExecutors() {
		this.main = new MainExecutor();
		this.io = Executors.newScheduledThreadPool(3);
	}
	
	public static final class MainExecutor implements Executor {

		public final Handler handler = new Handler(Looper.getMainLooper());
		
		@Override
		public void execute(Runnable runnable) {
			handler.post(runnable);
		}
	}
}
