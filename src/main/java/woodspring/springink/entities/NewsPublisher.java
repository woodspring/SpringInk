package woodspring.springink.entities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import woodspring.springink.EventBusAsynchN2M.NewsEvent;
import woodspring.springink.EventBusAsynchN2M.NewsEventBus;


public class NewsPublisher implements Publisher{
	private final static Logger logger = LoggerFactory.getLogger(MessageSender.class);
	private String publisherName;
	private NewsEventBus newsBus = null;
	public NewsPublisher() {
		newsBus = NewsEventBus.EVENTBUS();
	}
	@Override
	public void setName(String name) {
		this.publisherName = name;
	}
	@Override
	public String publisherName() {

		return publisherName;
	}
	
	@Override
	public String publish(String theMsg) {
		NewsEvent newsEvent = new NewsEvent(theMsg);
		
		String retStr = String.format("%s sent %s",publisherName, theMsg);
		logger.info("{}",retStr);
		newsBus.publish(newsEvent);
		return retStr;
	}
	
	public String publishMessages(List<String> strList) {
		strList = new ArrayList<>();
		String pName = publisherName+": ";
		StringBuilder strB = new StringBuilder();
		strList.add(pName+"Solay System");strList.add(pName+"SUN");strList.add(pName+"Mercury");strList.add(pName+"Vnus");strList.add(pName+"EARTH");
		strList.add(pName+"Mars");strList.add(pName+"Jupiter");strList.add(pName+"Saturn");strList.add(pName+"Uranus");strList.add(pName+"Neptune");
		strList.stream().map( 
						event-> CompletableFuture.supplyAsync(() -> this.publish(event)
				))
				.collect(Collectors.collectingAndThen( Collectors.toList(),
						compFuture -> compFuture.stream().map( CompletableFuture::join)))
				.forEach(itm -> strB.append( itm));
		return strB.toString();
	}

	
	
	
	

}
