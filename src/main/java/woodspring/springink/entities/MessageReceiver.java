package woodspring.springink.entities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import woodspring.springink.EventBusSynch121.MessageClient;

@Component
public class MessageReceiver {
	private final static Logger logger = LoggerFactory.getLogger(MessageReceiver.class);
	
	public MessageClient msgClient = new MessageClient();
	public int recCount =0;
	
	private StringBuilder strBuilder;
	
	public MessageReceiver() {
		strBuilder = new StringBuilder();
		msgClient.register(this);

	}
	
	public String receive(String msg) {
		String retStr = String.format("MessageReceiver %s receive msg:%s\n", recCount++, msg);
		logger.info( retStr);
		strBuilder.append(retStr);
		return retStr;
	}
	
	public String getString() {
		logger.info("MessageReciever::getString:{}", strBuilder.toString());
		return strBuilder.toString();
	}
	

}
