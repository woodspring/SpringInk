package woodspring.springink.entities;

import java.util.List;

public interface Publisher {

	void setName(String name);
	String publisherName();
	String publish(String theMsg);	
	String publishMessages(List<String> strList);

}
