package woodspring.springink.modules.SlidingStatistics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticsClient {
	private final static Logger logger = LoggerFactory.getLogger(StatisticsClient.class);
	private SlidingWindowStatistics<StatisticsClient> winStats = new SlidingStatisticsImpl();
	private String clientName;
	private StringBuffer strB = new StringBuffer();
	public StatisticsClient() {
		
	}
	public String setName(String name) {
		this.clientName = name;
		winStats.subscribForStatistics( this);
		return clientName;
	}
	public double onThreshold( double mean, int mode) {
		var retStr = String.format("%s : StatisticsClient get statistics notice: mean %f mode:%d", clientName, mean, mode);
		logger.info("{}", retStr);
strB.append( retStr);
		return mean;
		
	}
	public String getData() {
		return strB.toString();
	}

}
