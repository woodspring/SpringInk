package woodspring.springink.EventBusSynch121;

import woodspring.springink.EventBus.Event;

public class MessageEvent implements Event<String> {
	String msgData;
	public MessageEvent(String msg) {
		this.msgData = msg; 
	}
	@Override
	public String getData() {
		return msgData;
	}

}
