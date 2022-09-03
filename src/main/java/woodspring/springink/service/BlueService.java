package woodspring.springink.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import woodspring.springink.EventBus.EventType;
import woodspring.springink.EventBusAsynchN2M.NewsReader;
import woodspring.springink.EventBusAsynchN2M.Reader;
import woodspring.springink.entities.MessageReceiver;
import woodspring.springink.entities.MessageSender;
import woodspring.springink.entities.NewsPublisher;
import woodspring.springink.entities.Publisher;

@Service
public class BlueService {
	private final static Logger logger = LoggerFactory.getLogger(BlueService.class);
	
	@Autowired
	MessageReceiver msgReceiver;
	
	@Autowired
	MessageSender msgSender;
	
	public String doMessageSendReceive() {
		String retStr = "BlueService ";
		msgSender.setName( "Simple Event Bus");
		msgSender.publish("The FirstStep");
		msgSender.publishMessages(new ArrayList<String>());
		retStr += msgReceiver.getString();		
		return retStr;
	}
	
	public String doNewsPublishRead() {
		List<Publisher> pList = new ArrayList<>();
		Publisher newsP1 = new NewsPublisher();
		newsP1.setName("USA today"); pList.add( newsP1);
		Publisher newsP2 = new NewsPublisher();
		newsP2.setName("Toronto Start"); pList.add( newsP2);
		Publisher newsP3 = new NewsPublisher();
		newsP3.setName("The B.C. Catholic"); pList.add( newsP3);
		List<Reader> rList = new ArrayList<>();
		Reader reader1 = new NewsReader("Andrew", EventType.TOPIC_A);
		rList.add( reader1);
		Reader reader2 = new NewsReader("Babaries", EventType.TOPIC_B);
		rList.add( reader2);
		Reader reader3 = new NewsReader("Charles", EventType.TOPIC_C);
		rList.add( reader3);

		String retStr = "BlueService: publish news ";
		pList.stream().forEach(item -> item.publish(  "BlueService: publish news "));
		List<String> emptyNews = new ArrayList<>();
		StringBuilder strB = new StringBuilder();
		pList.stream().map( 
				 publer -> CompletableFuture.supplyAsync(() -> publer.publishMessages( emptyNews) 
				))
				.collect(Collectors.collectingAndThen( Collectors.toList(),
						compFuture -> compFuture.stream().map( CompletableFuture::join)))
				.forEach( item -> strB.append( item));
		rList.stream().forEach( reader -> strB.append( reader.getData()));
	logger.info(strB.toString());
		return strB.toString();
	}


}
