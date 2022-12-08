package simple.shell.adapters;

import android.support.v7.widget.*;
import android.view.*;
import java.util.*;
import simple.shell.bindings.*;
import simple.shell.suggestion.*;

import simple.shell.suggestion.SuggestionsResult.*;

public final class SuggestionsAdapter extends RecyclerView.Adapter<SuggestionsAdapter.ViewHolder> {

	private final OnClickListener listener;

	private List<Suggestion> suggestions = Collections.emptyList();
	private MergeAction mergeAction = null;

	public SuggestionsAdapter(OnClickListener listener) {
		this.listener = listener;
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new ViewHolder(new SuggestionItemBinding(parent));
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		holder.bind(suggestions.get(position));
	}

	@Override
	public int getItemCount() {
		return suggestions.size();
	}

	public void submit(SuggestionsResult result) {
		this.suggestions = result.suggestions;
		this.mergeAction = result.mergeAction;
		this.notifyDataSetChanged();
	}

	public final class ViewHolder extends RecyclerView.ViewHolder {

		private final SuggestionItemBinding binding;

		public ViewHolder(SuggestionItemBinding binding) {
			super(binding.root);
			this.binding = binding;
			this.binding.suggestion.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View p1) {
						listener.onClick(suggestions.get(getAdapterPosition()), mergeAction);
					}
				});
		}

		public void bind(final Suggestion item) {
			binding.suggestion.setText(item.label);
		}
	}
	
	public interface OnClickListener {
		void onClick(Suggestion item, MergeAction action);
	}
}
