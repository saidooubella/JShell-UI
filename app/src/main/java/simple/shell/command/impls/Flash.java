package simple.shell.command.impls;

import android.*;
import android.content.*;
import android.content.pm.*;
import android.hardware.camera2.*;
import simple.shell.*;
import simple.shell.command.*;
import simple.shell.suggestion.*;
import simple.shell.utils.*;

public final class Flash extends LeafCommand {

	private final CameraManager camera;

	public Flash(CameraManager camera) {
		super(new Metadata.Builder("flash")
			  .addRequiredArg("facing", Suggestions.custom(Suggestion.of("back"), Suggestion.of("front")))
			  .addRequiredArg("mode", Suggestions.custom(Suggestion.of("off"), Suggestion.of("on")))
			  .build());
		this.camera = camera;
	}

	@Override
	public Cancellable execute(final Shell shell, ArgsList args, final Continuation<Void> executionCompletion) throws ShellException {

		final boolean mode;
		final int facing;

		switch (args.asString(0)) {
			case "back" : facing = 0; break;
			case "front": facing = 1; break;
			default: throw new ShellException(args.asString(0) + ": Invalid facing value");
		}

		switch (args.asString(1)) {
			case "off": mode = false; break;
			case "on" : mode = true;  break;
			default: throw new ShellException(args.asString(1) + ": Invalid mode value");
		}

		if (!shell.hasPermission(Manifest.permission.CAMERA)) {
			shell.requestPermissions(new String[] {
					Manifest.permission.CAMERA
				}, new Continuation<Boolean>(null) {
					@Override
					protected void resume(Boolean granted) {
						if (granted) {
							flash0(shell, facing, mode, executionCompletion);
						} else {
							shell.print(name() + ": Permission denied");
							executionCompletion.resumeWith(null);
						}
					}
				});
			return null;
		}

		flash0(shell, facing, mode, executionCompletion);
		return null;
	}

	private void flash0(Shell shell, int facing, boolean mode, Continuation<Void> executionCompletion) {
			
		try {
			final String[] cameraIds = camera.getCameraIdList();
			if (cameraIds.length >= facing + 1 && hasFlash(camera, cameraIds[facing])) {
				camera.setTorchMode(cameraIds[facing], mode);
				executionCompletion.resumeWith(null);
				return;
			}
		} catch (CameraAccessException e) {}

		shell.print(name() + ": Flash unavailable");
		executionCompletion.resumeWith(null);
	}

	private boolean hasFlash(CameraManager manager, String cameraId) throws CameraAccessException {
		return manager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
	}
}
