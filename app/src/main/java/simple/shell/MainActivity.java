package simple.shell;

import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.*;
import android.hardware.camera2.*;
import android.os.*;
import android.support.v7.widget.*;
import android.text.*;
import android.view.*;
import android.view.inputmethod.*;
import android.widget.*;
import java.nio.file.*;
import java.util.concurrent.*;
import simple.shell.adapters.*;
import simple.shell.bindings.*;
import simple.shell.command.*;
import simple.shell.command.impls.*;
import simple.shell.command.impls.apps.*;
import simple.shell.command.impls.contacts.*;
import simple.shell.command.impls.notes.*;
import simple.shell.data.*;
import simple.shell.suggestion.*;
import simple.shell.suggestion.SuggestionsResult.*;
import simple.shell.utils.*;

import android.content.ClipboardManager;

public final class MainActivity extends Activity {

	private static final int PERMISSIONS_REQUEST_CODE = 123;

	private final Box<Continuation<Boolean>> permissionCallback = new Box<>();
	private final Box<Continuation<String>> promptCallback = new Box<>();

	private SuggestionsAdapter suggestionsAdapter;
	private ActivityMainBinding binding;
	private NotesDatabase notesDB;
	private LogsAdapter logsAdapter;

	private Cancellable suggestionTask;
	private Cancellable shellTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		binding = new ActivityMainBinding(getLayoutInflater());
        setContentView(binding.root);

		suggestionsAdapter = new SuggestionsAdapter(new SuggestionsAdapter.OnClickListener() {

				@Override
				public void onClick(Suggestion item, MergeAction action) {
					if (action instanceof MergeAction.Append) {
						final Editable text = binding.inputField.getText();
						if (shouldAddSpace(text.toString()))
							text.append(' ');
						text.append(item.value);
					} else if (action instanceof MergeAction.Replace) {
						final MergeAction.Replace replace = (MergeAction.Replace) action;
						binding.inputField.getText().replace(replace.start, replace.end, item.value);
					} else {
						throw new IllegalStateException();
					}
				}

				private boolean shouldAddSpace(final String text) {
					return !text.trim().isEmpty() && !Character.isWhitespace(text.charAt(text.length() - 1));
				}
			});

		logsAdapter = new LogsAdapter();

		binding.suggestionsList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
		binding.suggestionsList.setAdapter(suggestionsAdapter);

		final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
		layoutManager.setStackFromEnd(true);

		binding.logList.setLayoutManager(layoutManager);
		binding.logList.setAdapter(logsAdapter);

		binding.inputField.setInputType(InputType.TYPE_CLASS_TEXT);
		binding.inputField.setImeOptions(EditorInfo.IME_ACTION_GO);
		binding.inputField.setSingleLine(true);

		final BluetoothManager bluetooth = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
		final ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		final CameraManager    camera    = (CameraManager)    getSystemService(CAMERA_SERVICE);

		final BluetoothAdapter adapter = bluetooth.getAdapter();
		final AppExecutors executors = new AppExecutors();

		notesDB = new NotesDatabase(new NotesDBHelper(this), executors);

		final CommandList.Builder commandsBuilder = new CommandList.Builder()
			.add(new CommandSet("notes", new CommandList.Builder()
								.add(new NoteCopy(executors, notesDB, clipboard))
								.add(new NoteRemove(executors, notesDB))
								.add(new NotesList(executors, notesDB))
								.add(new NotesClear(notesDB))
								.add(new NoteAdd(notesDB))
								.build()))
			.add(new CommandSet("contacts", new CommandList.Builder()
								.add(new ContactsList(executors, getContentResolver()))
								.build()))
			.add(new CommandSet("apps", new CommandList.Builder()
								.add(new AppRemove(executors, getPackageManager()))
								.add(new AppsList(executors, getPackageManager()))
								.add(new AppOpen(executors, getPackageManager()))
								.build()))
			.add(new ListFiles(executors))
			.add(new Bluetooth(adapter))
			.add(new ChangeWorkingDir())
			.add(new PrintWorkingDir())
			.add(new Remove(executors))
			.add(new Write(executors))
			.add(new Read(executors))
			.add(new Flash(camera))
			.add(new MakeFile())
			.add(new MakeDir())
			.add(new Clear());

		if (BuildConfig.DEBUG) {
			commandsBuilder.add(
				new CommandSet(
					"devutils", 
					new CommandList.Builder()
					.add(new DevUtilWait(executors))
					.add(new DevUtilTime(executors))
					.add(new DevUtilPrompt())
					.add(new DevUtilPrint())
					.build()
				)
			);
		}

		final CommandList commands = commandsBuilder.build();

		final Shell shell = new Shell(Paths.get("/sdcard"), commands) {

			@Override
			public void requestPermissions(String[] permission, Continuation<Boolean> callback) {

				if (permissionCallback.isNotEmpty()) {
					throw new IllegalStateException("You can't request permissions more than once at the time.");
				}

				permissionCallback.set(callback);
				MainActivity.this.requestPermissions(permission, PERMISSIONS_REQUEST_CODE);
			}

			@Override
			public void startActivity(Intent intent) {
				MainActivity.this.startActivity(intent);
			}

			@Override
			public boolean hasPermission(String permission) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
					return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
				return true;
			}

			@Override
			public void prompt(String prompt, final Continuation<String> result) {

				if (promptCallback.isNotEmpty()) {
					throw new IllegalStateException("You can't prompt more than once at the time.");
				}

				binding.inputField.setHint(prompt);
				promptCallback.set(result);
			}

			@Override
			public void print(String line) {
				final int lastIndex = logsAdapter.getItemCount();
				logsAdapter.addLog(line);
				binding.logList.scrollToPosition(lastIndex);
			}

			@Override
			public void clear() {
				logsAdapter.clearLogs();
			}
		};

		final SuggestionsProvider provider = new SuggestionsProvider(commands.names(), executors, getPackageManager());
		final SuggestionsGenerator generator = new SuggestionsGenerator(shell, provider);

		final Runnable submitEmptyList = new Runnable() {
			@Override
			public void run() {
				suggestionsAdapter.submit(SuggestionsResult.empty());
			}
		};

		final Observable<String> inputText = new Observable<String>(binding.inputField.getText().toString(), new Observable.Observer<String>() {
				@Override
				public void onChange(String value) {

					if (suggestionTask != null) suggestionTask.cancel();

					if (promptCallback.isNotEmpty()) {
						suggestionsAdapter.submit(SuggestionsResult.empty());
						return;
					}

					executors.main.handler.postDelayed(submitEmptyList, 18);

					suggestionTask = generator.suggestions(ArgsSplitter.split(value), value.length(), new Continuation<SuggestionsResult>(executors.main) {
							@Override
							protected void resume(SuggestionsResult value) {
								executors.main.handler.removeCallbacks(submitEmptyList);
								suggestionsAdapter.submit(value);
							}
						});
				}
			});

		final Observable<Boolean> inExecution = new Observable<Boolean>(false, new Observable.Observer<Boolean>() {
				@Override
				public void onChange(Boolean value) {
					binding.suggestionsList.setVisibility(value ? View.GONE : View.VISIBLE);
					binding.inProgressBar.setVisibility(value ? View.VISIBLE : View.GONE);
				}
			});

		binding.inputField.addTextChangedListener(new TextWatcher() {

				@Override
				public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {

				}

				@Override
				public void onTextChanged(CharSequence content, int p2, int p3, int p4) {
					inputText.set(content.toString());
				}

				@Override
				public void afterTextChanged(Editable p1) {

				}
			});

		binding.inputField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
				@Override
				public boolean onEditorAction(TextView p1, int action, KeyEvent p3) {

					if (action == EditorInfo.IME_ACTION_GO) {

						if (promptCallback.isEmpty()) {

							if (inExecution.get()) {
								return true;
							}

							shell.print(">> " + inputText.get());
							if (shellTask != null) shellTask.cancel();
							inExecution.set(true);
							shellTask = shell.run(ArgsSplitter.split(inputText.get()), new Continuation<Void>(executors.main) {
									@Override
									protected void resume(Void value) {
										inExecution.set(false);
									}
								});

						} else {
							binding.inputField.setHint(getString(R.string.type_a_command));
							promptCallback.take().resumeWith(inputText.get());
						}

						binding.inputField.setText(null);

						return true;
					}
					return false;
				}
			});
    }

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (requestCode == PERMISSIONS_REQUEST_CODE) {
			permissionCallback.take().resumeWith(areGranted(grantResults));
		}
	}

	private boolean areGranted(int[] grantResults) {
		for (final int grantResult : grantResults) {
			if (grantResult == PackageManager.PERMISSION_DENIED) {
				return false;
			}
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		notesDB.close();
		super.onDestroy();
	}
}

final class DevUtilPrompt extends LeafCommand {

	public DevUtilPrompt() {
		super(new Metadata.Builder("prompt").build());
	}

	@Override
	public Cancellable execute(final Shell shell, ArgsList args, final Continuation<Void> executionCompletion) throws ShellException {
		shell.prompt("Type something...", new Continuation<String>(null) {
				@Override
				protected void resume(String value) {
					shell.print(value);
					executionCompletion.resumeWith(null);
				}
			});
		return null;
	}
}

final class DevUtilTime extends LeafCommand {

	private final AppExecutors executors;

	public DevUtilTime(AppExecutors executors) {
		super(new Metadata.Builder("time")
			  .addRequiredNArgs("command", Suggestions.Recursive)
			  .build());
		this.executors = executors;
	}

	@Override
	public Cancellable execute(final Shell shell, ArgsList args, final Continuation<Void> executionCompletion) throws ShellException {

		final long start = System.currentTimeMillis();
		shell.run(args.asArgsList(), new Continuation<Void>(executors.main) {
				@Override
				protected void resume(Void value) {
					final long end = System.currentTimeMillis();
					shell.print("It took: " + (end - start) + " ms");
					executionCompletion.resumeWith(null);
				}
			});

		return null;
	}
}

final class DevUtilPrint extends LeafCommand {

	public DevUtilPrint() {
		super(new Metadata.Builder("print")
			  .addRequiredArg("what", Suggestions.None)
			  .addRequiredArg("times", Suggestions.None)
			  .build());
	}

	@Override
	public Cancellable execute(Shell shell, ArgsList args, final Continuation<Void> executionCompletion) throws ShellException {

		final String what = args.asString(0);
		final int times = args.asInt(1);

		for (int i = 0; i < times; i++) {
			shell.print(what);
		}

		executionCompletion.resumeWith(null);
		return null;
	}
}

final class DevUtilWait extends LeafCommand {

	private final AppExecutors executors;

	public DevUtilWait(AppExecutors executors) {
		super(new Metadata.Builder("wait")
			  .addRequiredArg("timeInMs", Suggestions.None)
			  .build());
		this.executors = executors;
	}

	@Override
	public Cancellable execute(Shell shell, ArgsList args, final Continuation<Void> executionCompletion) throws ShellException {

		final Runnable runnble = new Runnable() {
			@Override
			public void run() {
				executionCompletion.resumeWith(null);
			}
		};

		executors.io.schedule(runnble, args.asLong(0), TimeUnit.MILLISECONDS);
		return null;
	}
}
