package woodspring.springink.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import woodspring.springink.entities.MessageReceiver;
import woodspring.springink.entities.MessageSender;

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
		msgSender.publishMessage("The FirstStep");
		msgSender.publishSomeMessage();
		retStr += msgReceiver.getString();
		
		
		return retStr;
	}

}
