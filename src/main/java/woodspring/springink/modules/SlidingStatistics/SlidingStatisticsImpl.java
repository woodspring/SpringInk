package woodspring.springink.modules.SlidingStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Component
public class SlidingStatisticsImpl implements SlidingWindowStatistics<StatisticsClient> {
	private final static Logger logger = LoggerFactory.getLogger(SlidingStatisticsImpl.class);

	static StatisticsImpl statistics = null;
	static List<StatisticsClient> clientList = new ArrayList<>();
	private final double criteria = 101;

	public SlidingStatisticsImpl() {
		DescriptiveStatistics stats = new DescriptiveStatistics();
		statistics = new StatisticsImpl(stats);
	}

	@Override
	public void add(int measurement) {
		List<Integer> list = new ArrayList<>();
		list.add(measurement);
		statistics.loadData(list);
		noticeClient();
	}

	@Override
	public void loadData(List<Integer> measurements) {
		statistics.loadData(measurements);
		noticeClient();
	}

	@Override
	public void subscribForStatistics(StatisticsClient observer) {

		clientList.add(observer);
	}

	@Override
	public void noticeClient() {
		logger.info("noticeClient or not: criteria:{}",criteria );
		if (statistics.getMean() > criteria) {
			var value = statistics.getMean();
			int mode = statistics.getMode();
			logger.info("noticeClient start to notice; mean:{}", statistics.getMean() );
			clientList.stream().map(client -> CompletableFuture.supplyAsync(() -> client.onThreshold(value, mode)))
					.collect(Collectors.collectingAndThen(Collectors.toList(),
							statList -> statList.stream().map(CompletableFuture::join)))
					.collect(Collectors.toList());
		}
	}
}
