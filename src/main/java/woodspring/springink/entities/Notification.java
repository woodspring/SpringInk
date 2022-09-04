package woodspring.springink.entities;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import woodspring.springink.EventBusAsynchN2M.NewsEvent;
import woodspring.springink.EventBusxBroker.BusBroker;

public class Notification implements Publisher {
	private final static Logger logger = LoggerFactory.getLogger(Notification.class);
	
	
	private BusBroker noticeSrc;
	private String noticer;

	public Notification(String name) {	
		noticer = name;
		noticeSrc = new BusBroker( name);
	}
	
	@Override
	public void setName(String name) {
		noticer = name;
		//noticeSrc = new BusBroker( name);
	}

	@Override
	public String publisherName() {
		return noticer;
	}

	@Override
	public String publish(String theMsg) {
		NewsEvent msg = new NewsEvent( theMsg);
		if (noticeSrc.publish(msg)) 
			logger.info("noticer {} send msg:{} sucessfully", noticer, theMsg);
		else 
			logger.warn("noticer{} did NOT send msg:{} properly", noticer, theMsg );
		
		String retStr = String.format("noticer %s publish msg:%s..", noticer, theMsg);
		return retStr;
	}

	@Override
	public String publishMessages(List<String> strList) {
		var strB = new StringBuffer();
		strList.stream().map( str -> CompletableFuture.supplyAsync(() -> this.publish( str)))
						.collect(Collectors.collectingAndThen(
								Collectors.toList(), 
								cfList -> cfList.stream().map(CompletableFuture::join)))
						.forEach(item -> strB.append( item));
		return strB.toString();
	}

}
