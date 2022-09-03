package woodspring.springink.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import woodspring.springink.modules.SlidingStatistics.SlidingStatisticsImpl;
import woodspring.springink.modules.SlidingStatistics.SlidingWindowStatistics;
import woodspring.springink.modules.SlidingStatistics.StatisticsClient;

@Service
public class YellowService {	
	private final static Logger logger = LoggerFactory.getLogger(SlidingStatisticsImpl.class);
	
	@Autowired
	 SlidingWindowStatistics<StatisticsClient> winStats;
	
	
	public String yellowService(int loop) {
		StringBuilder strB = new StringBuilder();
		List<StatisticsClient> clientList = new ArrayList<>();
		StatisticsClient client1 = new StatisticsClient();
		client1.setName("CANADA"); clientList.add(client1);
		var client2  = new StatisticsClient(); client2.setName("Ontario"); clientList.add(client2);
		var client3  = new StatisticsClient(); client3.setName("Toronto"); clientList.add(client3);
		List<Integer> list1 = new ArrayList<>();
		Random rand = new Random();

		IntStream.rangeClosed(1,  loop).forEach( item -> {
			list1.add(100);list1.add(rand.nextInt(97*item));list1.add(rand.nextInt(23*item));list1.add(13*item);list1.add(31*item);
			list1.add(rand.nextInt(21*item));list1.add(rand.nextInt(5*item));list1.add(rand.nextInt(7*item));
			list1.add(rand.nextInt(13*item));list1.add(rand.nextInt(17*item));list1.add(rand.nextInt(19*item));
			winStats.loadData( list1);
			list1.clear();
			});
		clientList.stream().forEach(item -> strB.append(item.getData()));
		
		return strB.toString();
	}
	

}
