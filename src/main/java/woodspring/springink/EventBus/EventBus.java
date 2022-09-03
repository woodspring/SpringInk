package woodspring.springink.EventBus;

public interface EventBus<E, S> {
	 // Feel free to replace Object with something more specific,
	 // but be prepared to justify it
	 void publishEvent(EventType eventType, E event);
	 void publish(E event);
	 // How would you denote the subscriber?
	 void addSubscriber(String name, S sub );
	 void removeSubscriber(String name);
	 // Would you allow clients to filter the events they receive? How would the interface look like?
	 void addSubscriberForFilteredEvents(String name, S sub, EventType filter);
	 
}
