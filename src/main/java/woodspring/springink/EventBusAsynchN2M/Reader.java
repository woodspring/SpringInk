package woodspring.springink.EventBusAsynchN2M;

import java.util.Set;

import woodspring.springink.EventBus.Event;
import woodspring.springink.EventBus.Subscribable;

public interface Reader<E> {
	E onEvent(Event<E> event);
	Set<Reader> supports();

}
