package woodspring.springink.EventBusSynch121;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import woodspring.springink.EventBus.EventBus;
import woodspring.springink.EventBus.EventType;
import woodspring.springink.EventBus.Subscribable;

public class MsgEventBus implements EventBus<MessageEvent, MessageClient>{
	private final static Logger logger = LoggerFactory.getLogger(MsgEventBus.class);
	
	private static MsgEventBus eventBus = new MsgEventBus();
	List<Subscribable> ecList = new ArrayList<>();
	ConcurrentSkipListMap<String, List<MessageClient>> ecKeyMap = new ConcurrentSkipListMap<>();
	EnumMap<EventType, List<MessageClient>> filterMap = new EnumMap<>(EventType.class);
	
	public static MsgEventBus EVENTBUS() {
		return eventBus;
	}

	private MsgEventBus() {
	}
	

	@Override
	public void publishEvent(EventType eventType, MessageEvent event) {
		// filter if Client get filter
		if ( filterMap.containsKey( eventType)) {
			List<MessageClient> filterList = filterMap.get( eventType);
			ecList.stream().filter( item -> !filterList.contains( item))
						.forEach(doIt -> doIt.onEvent(event));
		} else {
			filterMap.computeIfAbsent( eventType, sub -> { 
								publish( event);
								return null;
							});
		}			
	}

	@Override
	public void publish(MessageEvent event) {
		// without topic; means publish to all Client;
		ecList.stream().forEach(  sub -> sub.onEvent(event));		
	}

	@Override
	public void addSubscriber(String name, MessageClient sub) {
		ecKeyMap.computeIfAbsent(name,v -> new ArrayList<>()).add( sub);
		if (!ecList.contains( sub))  ecList.add( sub ); 
	}

	@Override
	public void removeSubscriber(String name) {
		if (ecKeyMap.containsKey( name)) {
			var theList = ecKeyMap.get(name);
			if ( theList.size() == 1){
				ecKeyMap.put (name, new ArrayList<>() );
			} else
				theList.remove(0);
		}		
	}

	@Override
	public void addSubscriberForFilteredEvents(String name, MessageClient sub, EventType eventType) {
		ecKeyMap.computeIfAbsent(name,v -> new ArrayList<>()).add( sub);
		filterMap.computeIfAbsent(eventType, v -> new ArrayList<>()).add( sub);	
	}	

}
