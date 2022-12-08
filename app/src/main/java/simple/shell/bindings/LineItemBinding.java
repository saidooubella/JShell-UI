package simple.shell.bindings;

import android.view.*;
import android.widget.*;
import simple.shell.*;

public final class LineItemBinding {

	public final TextView line;
	public final View root;

	public LineItemBinding(ViewGroup parent) {
		this.root = LayoutInflater.from(parent.getContext()).inflate(R.layout.line_item, parent, false);
		this.line = root.findViewById(R.id.line);
	}
}
