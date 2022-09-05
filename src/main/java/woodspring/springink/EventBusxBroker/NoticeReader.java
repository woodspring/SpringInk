package woodspring.springink.EventBusxBroker;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import woodspring.springink.EventBus.Event;
import woodspring.springink.EventBus.EventType;
import woodspring.springink.EventBusAsynchN2M.NewsEventBus;
import woodspring.springink.EventBusAsynchN2M.NewsReader;
import woodspring.springink.EventBusAsynchN2M.Reader;

public class NoticeReader implements Reader<String>{
	private final static Logger logger = LoggerFactory.getLogger(NoticeReader.class);
	
	AtomicInteger aInt = new AtomicInteger(0);
	private String readerName = null; 
	private StringBuilder strB = new StringBuilder();
	private NotificationEventBus noticeBus;
	
	public NoticeReader(String name, EventType eventType) {
		readerName = name;
		noticeBus = NotificationEventBus.EVENTBUS();
		noticeBus.addSubscriberForFilteredEvents(readerName, this, eventType);
	}
	
	
	@Override
	public String onEvent(Event<String> event) {
		String retStr = "NoticeReader " + readerName+" "+ aInt.getAndIncrement()+" notification:"+ event.getData();
		logger.info("******************READER {} get notification:{} -{}-",readerName, retStr, aInt );
		strB.append( retStr);
		return retStr;
	}
	@Override
	public Set<Reader> supports() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String getData() {
		return strB.toString();
	}

}
