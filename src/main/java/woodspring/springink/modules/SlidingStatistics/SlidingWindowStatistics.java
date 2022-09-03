package woodspring.springink.modules.SlidingStatistics;

import java.util.List;

public interface SlidingWindowStatistics<U> {
	void add( int measurement);
	void loadData( List<Integer> measurement);
	void noticeClient();
	
	void subscribForStatistics(U observer);
	
	public interface Statistics {
		double getMean();
		int getMode();
		double getPctile( int pctile);
	}

}
