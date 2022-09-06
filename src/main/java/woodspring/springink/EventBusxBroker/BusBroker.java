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
	
	private static NotificationEventBus noticeBus = null;
	private static Throttler throttler = null;
	private String busNo;
	private static Queue<NewsEvent> eventQueue;
	private int onRetryCount = 10;
	public BusBroker(String busId) {
		this.busNo = busId;
		noticeBus = NotificationEventBus.EVENTBUS();
		throttler = Throttler.THROTTLER();
		eventQueue = new LinkedList<>();
		logger.info("BusBroker, busNO:{}, throttler time:{}", busNo, throttler.getStartTime());
	}
	
	public Boolean onRetry(int numOfEvent) {
		// callback funtion from Throttler
		Boolean bRet = true;
		int reTrySuccess =0;
		logger.info("onRetry BusBroker {}, get a call, queue size:{}", busNo, eventQueue.size());
		while( !eventQueue.isEmpty()) {
			if ( publish(eventQueue.peek())) {
				//synchronized (throttler) {
					eventQueue.poll();
					throttler.release();
					reTrySuccess++;
					logger.info("onRetry BusBroker {},    poll sucess, eventQueue, size:{}", 
							busNo, eventQueue.size()); 
				//}
			} else {
				bRet = false;
				logger.info("onRetry false BusBroker {},    poll sucess, eventQueue, size:{}", 
						busNo, eventQueue.size()); 
				break;
			}
			if ( !(reTrySuccess < numOfEvent)) break; 
		}		
		return bRet;
	}
	

	public boolean publish( NewsEvent news) {
		boolean bRet = true;
		logger.info("--->BusBroker {}, publish news:{} ", busNo, news.getData());
		if (throttler.isProceed( this) == ThrottleResult.OVER) {
			// need to push the news into queue
			eventQueue.add(news);	
			bRet = false;
			logger.info("<--- Not OK, BusBroker {}, publish news:{} ", busNo, news.getData());
		} else {
			noticeBus.publish( news);
			throttler.release();
			logger.info("<--- OK, BusBroker {}, publish news:{} ", busNo, news.getData());
		}		
		return bRet;
	}
	
	public int numInQueue() {
		return eventQueue.size();
	}

}
