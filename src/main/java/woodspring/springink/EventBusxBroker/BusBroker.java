package woodspring.springink.EventBusxBroker;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import woodspring.springink.EventBusAsynchN2M.NewsEvent;
import woodspring.springink.EventBusAsynchN2M.NewsEventBus;

public class BusBroker  {
	private final static Logger logger = LoggerFactory.getLogger(BusBroker.class);
	
	private NewsEventBus newsBus = null;
	private Throttler throttler = null;
	private String busNo;
	private Queue<NewsEvent> eventQueue;
	public BusBroker(String busId) {
		this.busNo = busId;
		newsBus = NewsEventBus.EVENTBUS();
		throttler = Throttler.THROTTLER();
		eventQueue = new LinkedList<>();
	}
	
	public Boolean onEvent() {
		// callback funtion from Throttler
		Boolean bRet = true;
		logger.info("BusBroker {}, get a call, queue size:{}", busNo, eventQueue.size());
		while( !eventQueue.isEmpty()) {
			if ( publish(eventQueue.peek())) {
				eventQueue.poll();
			} else {
				bRet = false;
				break;
			}			
		}		
		return bRet;
	}
	public boolean publish( NewsEvent news) {
		boolean bRet = true;
		logger.info("BusBroker {}, publish news:{} ", busNo, news.getData());
		if (throttler.isProceed( this) == ThrottleResult.OVER) {
			// need to push the news into queue
			eventQueue.add(news);	
			bRet = false;
		} else {
			newsBus.publish( news);
			throttler.release();
		}		
		return bRet;
	}

}
