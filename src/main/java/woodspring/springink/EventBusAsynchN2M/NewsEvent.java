package woodspring.springink.EventBusAsynchN2M;

import woodspring.springink.EventBus.Event;

public class NewsEvent implements Event<String> {
	String msgData;
	public NewsEvent(String msg) {
		this.msgData = msg; 
	}
	@Override
	public String getData() {
		return msgData;
	}

}