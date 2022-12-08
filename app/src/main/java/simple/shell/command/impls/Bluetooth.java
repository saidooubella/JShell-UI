package simple.shell.command.impls;

import android.*;
import android.bluetooth.*;
import simple.shell.*;
import simple.shell.command.*;
import simple.shell.utils.*;

public final class Bluetooth extends LeafCommand {

	private final BluetoothAdapter adapter;

	public Bluetooth(BluetoothAdapter adapter) {
		super(new Metadata.Builder("bluetooth").build());
		this.adapter = adapter;
	}

	@Override
	public Cancellable execute(final Shell shell, ArgsList args, final Continuation<Void> executionCompletion) {

		if (adapter == null) {
			shell.print(name() + ": Bluetooth unavailable");
			executionCompletion.resumeWith(null);
			return null;
		}

		if (!shell.hasPermission(Manifest.permission.BLUETOOTH_ADMIN) || !shell.hasPermission(Manifest.permission.BLUETOOTH)) {
			shell.requestPermissions(new String[] {
					Manifest.permission.BLUETOOTH_ADMIN,
					Manifest.permission.BLUETOOTH
				}, new Continuation<Boolean>(null) {
					@Override
					protected void resume(Boolean granted) {
						if (granted) {
							bluetooth0(shell, executionCompletion);
						} else {
							shell.print(name() + ": Permission denied");
							executionCompletion.resumeWith(null);
						}
					}
				});
			return null;
		}

		bluetooth0(shell, executionCompletion);
		return null;
	}

	private void bluetooth0(Shell shell, Continuation<Void> executionCompletion) {
		final boolean shouldEnable = !adapter.isEnabled();
		if (shouldEnable) adapter.enable(); else adapter.disable();
		shell.print(name() + ": Bluetooth is " + (shouldEnable ? "enabled" : "disabled"));
		executionCompletion.resumeWith(null);
	}
}
