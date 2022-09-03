package woodspring.springink.modules.SlidingStatistics;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatisticsImpl implements SlidingWindowStatistics.Statistics {
	private final static Logger logger = LoggerFactory.getLogger(StatisticsImpl.class);
	
	private DescriptiveStatistics stats = new DescriptiveStatistics();
	private int theMode = 0;
	public StatisticsImpl( DescriptiveStatistics statist) {
		this.stats = statist;
	}

	@Override
	public double getMean() {
		// TODO Auto-generated method stub
		return stats.getMean();
	}

	@Override
	public int getMode() {
			double[] values = stats.getSortedValues();
			List<Integer> iList = new ArrayList<>();
			for ( double item: values) {
				if ( item > 1) iList.add( ( item < 1) ? 1 :  (int)Math.round( item ));
			}
			Map<Integer, Long> freq = iList.stream()
					.collect( Collectors.groupingBy( Function.identity(), Collectors.counting()));
			this.theMode = freq.entrySet().stream()
					.max( Comparator.comparingLong( Map.Entry::getValue))
					.map(Map.Entry::getKey)
					.get();	
			
			//logger.info(" mode:{} \n keySet:[{}] \n fValue:[{}]\n values: [{}]", theMode, freq.keySet(), freq.values(), values);

		return theMode;
	}

	@Override
	public double getPctile(int pctile) {		
		return stats.getPercentile( pctile);
	}
	
	public void loadData(List<Integer> numbers ) {
		numbers.stream().forEach( item -> stats.addValue( item ));
		getMode();
	}

	
}
