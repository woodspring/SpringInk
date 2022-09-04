package woodspring.springink.EventBusAsynchN2M;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import woodspring.springink.EventBus.Event;
import woodspring.springink.EventBus.EventType;


public class NewsReader implements Reader<String>{
	private final static Logger logger = LoggerFactory.getLogger(NewsReader.class);
	
	AtomicInteger aInt = new AtomicInteger(0);
	private String readerName = null; 
	private StringBuilder strB = new StringBuilder();
	private NewsEventBus newsBus;
	
	public NewsReader(String name, EventType eventType) {
		readerName = name;
		newsBus = NewsEventBus.EVENTBUS();
		newsBus.addSubscriberForFilteredEvents(readerName, this, eventType);
	}	

	@Override
	public String onEvent(Event<String> event) {
		// TODO Auto-generated method stub
		String retStr = "NewReader " + readerName+" "+ aInt.getAndIncrement()+" news:"+ event.getData();
		logger.info("{} read news:{} -{}-",readerName, retStr, aInt );
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
	public String getName() {
		return readerName;
	}

}