package woodspring.springink.EventBusAsynchN2M;

import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import woodspring.springink.EventBus.Event;


public class NewsReader implements Reader<String>{
	private final static Logger logger = LoggerFactory.getLogger(NewsReader.class);
	
	AtomicInteger aInt = new AtomicInteger(0);
	private String readerName = null; 
	
	public NewsReader(String name) {
		readerName = name;
	}

	@Override
	public String onEvent(Event<String> event) {
		// TODO Auto-generated method stub
		String retStr = "NewReader " + readerName+ aInt.getAndIncrement()+" news:"+ event;
		logger.info(" get news:"+ retStr);
		return retStr;
	}

	@Override
	public Set<Reader> supports() {
		// TODO Auto-generated method stub
		return null;
	}

}