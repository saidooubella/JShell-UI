package simple.shell.suggestion;

import java.util.*;
import simple.shell.*;
import simple.shell.utils.*;

public interface SuggestionsSupplier {
	Cancellable load(Shell shell, String hint, Continuation<List<Suggestion>> callback);
}
