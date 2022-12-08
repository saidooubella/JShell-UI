package simple.shell.bindings;

import android.view.*;
import android.widget.*;
import simple.shell.*;
import android.support.v7.widget.*;

public final class ActivityMainBinding {

	public final RecyclerView suggestionsList;
	public final ProgressBar inProgressBar;
	public final RecyclerView logList;
	public final EditText inputField;
	public final View root;

	public ActivityMainBinding(LayoutInflater inflater) {
		this.root = inflater.inflate(R.layout.activity_main, null, false);
		this.suggestionsList = root.findViewById(R.id.suggestions_list);
		this.inProgressBar = root.findViewById(R.id.in_progress_bar);
		this.inputField = root.findViewById(R.id.input_field);
		this.logList = root.findViewById(R.id.log_list);
	}
}
