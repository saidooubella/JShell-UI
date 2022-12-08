package simple.shell.adapters;

import android.support.v7.widget.*;
import android.view.*;
import java.util.*;
import simple.shell.bindings.*;

public final class LogsAdapter extends RecyclerView.Adapter<LogsAdapter.ViewHolder> {

	private final List<String> logs = new ArrayList<>();

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewItem) {
		return new ViewHolder(new LineItemBinding(parent));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		holder.bind(logs.get(position));
	}

	@Override
	public int getItemCount() {
		return logs.size();
	}
	
	public void addLog(String log) {
		logs.add(log);
		notifyDataSetChanged();
	}
	
	public void clearLogs() {
		final int size = getItemCount();
		logs.clear();
		notifyItemRangeRemoved(0, size);
	}

	public static final class ViewHolder extends RecyclerView.ViewHolder {

		private final LineItemBinding binding;

		public ViewHolder(LineItemBinding binding) {
			super(binding.root);
			this.binding = binding;
		}

		public void bind(String line) {
			binding.line.setText(line);
		}
	}
}

