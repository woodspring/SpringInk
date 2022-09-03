package woodspring.springink.EventBusSynch121;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import woodspring.springink.EventBus.Event;
import woodspring.springink.EventBus.Subscribable;
import woodspring.springink.entities.MessageReceiver;
public class MessageClient implements Subscribable<String>{
	private final static Logger logger = LoggerFactory.getLogger(MessageClient.class);
	
	private MessageReceiver msgReceiver = null;

	@Override
	public void onEvent(Event<String> event) {
		String aStr = event.getData(); 
		logger.info("MessageClient, onEvent:{}", aStr);
		msgReceiver.receive(aStr);
		
	}

	@Override
	public Set<Subscribable> supports() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void register(MessageReceiver msgRec) {
		this.msgReceiver = msgRec;
		MsgEventBus.EVENTBUS().addSubscriber("MESSAGE RVR", this);
		
	}

}
