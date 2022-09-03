package woodspring.springink.entities;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import woodspring.springink.EventBusSynch121.MessageEvent;
import woodspring.springink.EventBusSynch121.MsgEventBus;

@Component
public class MessageSender {
	private final static Logger logger = LoggerFactory.getLogger(MessageSender.class);
	private String senderName;
	private MsgEventBus msgBus = null;
	public MessageSender() {
		msgBus = MsgEventBus.EVENTBUS();
	}
	
	public void setName(String name) {
		this.senderName = name;
	}
	
	public String publishMessage(String theMsg) {
		MessageEvent aEvent = new MessageEvent(theMsg);
		
		String retStr = String.format("%s sent %s",senderName, theMsg);
		logger.info("{}",retStr);
		msgBus.publish(aEvent);
		return retStr;
	}
	
	public String publishSomeMessage() {
		List<String> strList = new ArrayList<>();
		StringBuilder strB = new StringBuilder();
		strList.add("FIRST ONE");strList.add("Second");strList.add("Third");strList.add("Four");strList.add("Five");
		strList.add("Six");strList.add("Seven");strList.add("Eight");strList.add("Nine");strList.add("Ten");
		strList.stream().forEach( item -> strB.append( publishMessage(item)));
		return strB.toString();
	}
}
