package woodspring.springink.EventBusAsynchN2M;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import woodspring.springink.EventBus.EventBus;
import woodspring.springink.EventBus.EventType;


public class NewsEventBus implements EventBus< NewsEvent, NewsReader>{
	private final static Logger logger = LoggerFactory.getLogger(NewsEventBus.class);
	
	private static NewsEventBus eventBus = null;
	static List<Reader> readers =  new ArrayList<>();;
	ConcurrentSkipListMap<String, List<NewsReader>> newsReaderMap = null;
	EnumMap<EventType, List<NewsReader>> filterMap = null;
	static Lock lock = new ReentrantLock();
	
	public static NewsEventBus EVENTBUS() {
		if ( eventBus == null) {
			try {
				lock.lock();
				if ( eventBus == null) {
					synchronized( lock) {
						//readers = new ArrayList<>();
						eventBus = new NewsEventBus();
					}
				}
			} catch (Exception ex) {
				logger.error("NewsEventBus can not create due to"+ ex.getMessage());
			} finally {
				lock.unlock();
			}
		}			
		return eventBus;
	}
		
	private NewsEventBus() {
		//readers = new ArrayList<>();
		newsReaderMap = new ConcurrentSkipListMap<>();
		filterMap = new EnumMap<>(EventType.class);
	}

	@Override
	public void publishEvent(EventType eventType, NewsEvent event) {
		// filter if Client get filter
		if ( filterMap.containsKey( eventType)) {
			List<NewsReader> filterList = filterMap.get( eventType);
			logger.info("readers size:{}", readers.size());
			readers.stream().filter( item -> !filterList.contains( item))
						.map(reader -> 
							CompletableFuture.supplyAsync(() -> reader.onEvent( event)))
						.collect(Collectors.collectingAndThen( Collectors.toList(),
								cfList -> cfList.stream().map( CompletableFuture::join)))
						.collect(Collectors.toList());
		} else {
			filterMap.computeIfAbsent( eventType, 	sub -> {
							publish(event);
							return null;
			});
		}		
	}

	@Override
	public void publish(NewsEvent event) {
		// without topic; means publish to all Client;
		logger.info("readers size:{}, publish event:{}", readers.size(), event.getData());
		List<CompletableFuture<Object>> comFList = readers.stream().map( reader ->  
							CompletableFuture.supplyAsync(() -> reader.onEvent(event)))
		.collect( Collectors.toList());	
		comFList.stream().map( CompletableFuture::join).collect(Collectors.toList());

	}

	@Override
	public void addSubscriber(String name, NewsReader reader) {
		newsReaderMap.computeIfAbsent(name,v -> new ArrayList<>()).add( reader);
		if (!readers.contains( reader))  readers.add( reader ); 
		logger.info("addSubscriber readers size:{}, name:{} readerName:{}", readers.size(), name, reader.toString());
	}

	@Override
	public void removeSubscriber(String name) {
		if (newsReaderMap.containsKey( name)) {
			var theList = newsReaderMap.get(name);
			if ( theList.size() == 1){
				newsReaderMap.put (name, new ArrayList<>() );
			} else
				theList.remove(0);
		}	
		
	}

	@Override
	public void addSubscriberForFilteredEvents(String name, NewsReader reader, EventType eventType) {
		newsReaderMap.computeIfAbsent(name,v -> new ArrayList<>()).add( reader);
		filterMap.computeIfAbsent(eventType, v -> new ArrayList<>()).add( reader);	
		if (!readers.contains( reader))  readers.add( reader ); 
		logger.info("addSubscriberForFilteredEvents readers size:{}, name:{} readerName:{} eventType:{}", readers.size(), name, reader.toString(), eventType);
	}	
}
