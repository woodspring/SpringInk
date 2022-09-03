package woodspring.springink.EventBus;

import java.util.Set;

public interface Subscribable<E> {
	void onEvent(Event<E> event);
	Set<Subscribable> supports();

}
