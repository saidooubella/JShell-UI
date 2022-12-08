package simple.shell.bindings;

import android.view.*;
import android.widget.*;
import simple.shell.*;

public final class SuggestionItemBinding {

	public final Button suggestion;
	public final View root;

	public SuggestionItemBinding(ViewGroup parent) {
		this.root = LayoutInflater.from(parent.getContext()).inflate(R.layout.suggestion_item, parent, false);
		this.suggestion = root.findViewById(R.id.suggestion_button);
	}
}
