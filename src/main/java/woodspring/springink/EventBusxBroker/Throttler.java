package woodspring.springink.EventBusxBroker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import woodspring.springink.EventBusAsynchN2M.NewsEventBus;

public class Throttler {
	private final static Logger logger = LoggerFactory.getLogger(Throttler.class);

	private static final int count = 10;
	private AtomicInteger currentCount = new AtomicInteger(0);
	private long timePeriod = 2 * 10L; // 1000L;
	private long startTime = 0L;
	private long lastOkTime = 0L, lastNotOkTime = 0L;
	//private static long currTime;
	private int onRetryTimes = 5;
	List<BusBroker> notOkList;
	private static Throttler instance = null;
	static Lock lock = new ReentrantLock();

	public static Throttler THROTTLER() {
		logger.info("THROTTLER static");
		if (instance == null) {
			try {
				lock.lock();
				if (instance == null) {
					synchronized (lock) {
						instance = new Throttler();
					}
				}
			} catch (Exception ex) {
				logger.error("THROTTLER can not be created:{}", ex.getMessage());
			} finally {
				lock.unlock();
			}
		}
		return instance;
	}

	private Throttler() {
		notOkList = new ArrayList<>();
		startTime = System.currentTimeMillis();

	}

	public ThrottleResult isProceed(BusBroker bBroker) {
		ThrottleResult thRet = ThrottleResult.PROCEED;
		//currTime = System.currentTimeMillis();
		currentCount.getAndIncrement();
		if (!((System.currentTimeMillis() - startTime) > timePeriod)) {
			// it is inTime
			logger.info("------1-------- currTime - startTime > tPeriod");
			if (!(currentCount.get() > count)) {
				// PROCEED
				logger.info("-------1.good------- it is good, still get space to publish, currentCount:{}", currentCount.get());
				lastOkTime = System.currentTimeMillis();;
			} else {
				// NOT good
				logger.info("-------1.NotGood but in time period, no space to publish, currentCount:{}", currentCount.get());
				thRet = ThrottleResult.OVER;
				lastNotOkTime = System.currentTimeMillis();;
				if (!notOkList.contains(bBroker))
					notOkList.add(bBroker);
//				try {
//					// if ( (ind % 10) == 0) {
//					logger.info(
//							" ===============================going to sleep 5 seconds");
//					Thread.sleep(50 );
//					// }
//				} catch (InterruptedException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} finally {
//					logger.info(" =================Finally wake up ===================");
//				}
			}
		} else {
			logger.info("------2-------- out of Time period: currTime - startTime > tPeriod, time:{}", (System.currentTimeMillis() - startTime));
			logger.info(
					"!!--+2.1+--!!!! isProceed: result:{}, notOkList size:{}, currentCount:{} count:{} time:{} timePeriod:{}",
					thRet, notOkList.size(), currentCount.get(), count, (System.currentTimeMillis() - startTime), timePeriod);
			if (!(currentCount.get() > count)) {
				// 2. else
				// 2.1 delive current notice; currCount--;
				// 2.2 deleive count -1 - curretCount = count - 1 -currentCount
				//

				// available so PROCEED
				
				int space = count -1 - currentCount.get();
				logger.info("-----2.1----- out ogf time period but still get space to publish space:{}, currentCount:{}",space, currentCount.get());
				
				if (!notOkList.isEmpty()) {
						notifyToProceed(space);
						logger.info(
								"!!--+2.2+--!!!! isProceed: result:{}, notOkList size:{}, currentCount:{} count:{} time:{} timePeriod:{}",
								thRet, notOkList.size(), currentCount.get(), count, (System.currentTimeMillis() - startTime), timePeriod);
					}
				currentCount.set( currentCount.get() - space);
				restThrottle();
				startTime = System.currentTimeMillis();;
				logger.info(
						"!!--+2.3+--!!!! pub {} isProceed: result:{}, notOkList size:{}, currentCount:{} count:{} time:{} timePeriod:{}",
						space, thRet, notOkList.size(), currentCount.get(), count, (System.currentTimeMillis() - startTime), timePeriod);
			} else {
				// NOT good
				// 1. check if ( currentCount - count > 0)
				// 1.1deleive count notification from NoOkList
				// 1.2 currentCount -= count
				// need to wait time period.
				logger.info("-----3.1----- out of time period and new public . currentCount:{}", currentCount.get());
				thRet = ThrottleResult.OVER;
				lastNotOkTime = System.currentTimeMillis();
				if (!notOkList.contains(bBroker)) {
					notOkList.add(bBroker);
				}
				logger.info("-----3.2----- out of time period and new public . currentCount:{}", currentCount.get());
				notifyToProceed( count);
				currentCount.set(  currentCount.get() - count);
				
				startTime = lastNotOkTime = System.currentTimeMillis();
				logger.info("-----3.3----- out of time period and new public . currentCount:{} startTime:{}, timePeriod:{}",
						currentCount.get(), startTime, System.currentTimeMillis()-startTime);
			}

		}
		logger.info("!!!!!! isProceed: result:{}, notOkList size:{}, currentCount:{} count:{} time:{} timePeriod:{}",
				thRet, notOkList.size(), currentCount.get(), count, (System.currentTimeMillis() - startTime), timePeriod);
		return thRet;
	}

	public boolean notifyToProceed() {
		boolean bRet = true;
		logger.info("!!!!!!notifyToProceed== notOkList size:{}, currentCount:{} count:{} time:{} timePeriod:{}",
				notOkList.size(), currentCount.get(), count, (System.currentTimeMillis() - startTime), timePeriod);
		// lock.lock();
		try {
			for (int ind = 0; ind < onRetryTimes; ind++) {

				List<CompletableFuture<Boolean>> cfbList = notOkList.stream()
						.map(item -> CompletableFuture.supplyAsync(() -> item.onRetry(1))).collect(Collectors.toList());
				List<Boolean> boolList = cfbList.stream().map(CompletableFuture::join).collect(Collectors.toList());
				if (!boolList.contains(false)) {
					logger.info("notifyToProceed--> all success. notOkList size:{}", notOkList.size());
					restThrottle();
					break;
				} else {
					logger.info("notifyToProceed--> notifyToProcess not all success. notOkList size:{}",
							notOkList.size());
				}
			}
		} catch (Exception ex) {
			logger.error("THROTTLER can not notify broker:{}", ex.getMessage());
			bRet = false;
		} finally {
			logger.info(
					"!!!unlock!!!notifyToProceed== notOkList size:{}, currentCount:{} count:{} time:{} timePeriod:{}",
					notOkList.size(), currentCount.get(), count, (System.currentTimeMillis() - startTime), timePeriod);
			// lock.unlock();
		}
		return bRet;
	}
	
	public boolean notifyToProceed(int numOfNotice) {
		boolean bRet = true;
		logger.info("!!!!!!notifyToProceed== notOkList size:{}, numOfNotice:{}, currentCount:{} count:{} time:{} timePeriod:{}",
				notOkList.size(), numOfNotice, currentCount.get(), count, (System.currentTimeMillis() - startTime), timePeriod);
		// lock.lock();
		try {
			for (int ind = 0; ind < onRetryTimes; ind++) {
				
				List<CompletableFuture<Boolean>> cfbList = notOkList.stream()
						//.limit(numOfNotice)
						.map(item -> CompletableFuture.supplyAsync(() -> item.onRetry(numOfNotice)))
						.collect(Collectors.toList());
				List<Boolean> boolList = cfbList.stream().map(CompletableFuture::join).collect(Collectors.toList());
				if (!boolList.contains(false)) {
					logger.info("notifyToProceed--> all success. notOkList size:{}", notOkList.size());
					restThrottle();
					break;
				} else {
					logger.info("notifyToProceed--> notifyToProcess not all success. notOkList size:{}",
							notOkList.size());
				}
			}
		} catch (Exception ex) {
			logger.error("THROTTLER can not notify broker:{}", ex.getMessage());
			bRet = false;
		} finally {
			logger.info(
					"!!!unlock!!!notifyToProceed== notOkList size:{}, currentCount:{} count:{} time:{} timePeriod:{}",
					notOkList.size(), currentCount.get(), count, (System.currentTimeMillis() - startTime), timePeriod);
			// lock.unlock();
		}
		return bRet;
	}

	public int release() {
		try {
			lock.lock();
			currentCount.getAndDecrement();
			logger.info("====================================>Throttler release!! {}", currentCount.get());
			if (currentCount.get() == 0) {
				startTime = lastOkTime;
			}
		} catch (Exception ex) {
			logger.error("Throttler cant release!! {}", ex.getMessage());
		} finally {
			lock.unlock();
		}

		//if (!(currentCount.get() > count)) {
		if ( !notOkList.isEmpty()) {
			logger.info(
					"==========currentCount:{} > count:{}======notOkList.size:{}====================>Throttler release!! {}",
					currentCount.get(), count, notOkList.size(), currentCount.get());
			if (notOkList.size() > 0) {
				notifyToProceed();
			}
		}
		return currentCount.get();

	}

	public boolean restThrottle() {
		boolean bRet = true;
		currentCount.set(0);
		notOkList.clear();
		startTime  = System.currentTimeMillis();
		lastOkTime = lastNotOkTime = 0L;
		logger.info(" reset Throttler-----");
		return bRet;
	}

	public long getStartTime() {
		return startTime;
	}

}
