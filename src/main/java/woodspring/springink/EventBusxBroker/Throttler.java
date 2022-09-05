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
	private long timePeriod = 2 * 100L; //1000L;
	private long startTime = 0L;
	private long lastOkTime =0L, lastNotOkTime=0L;
	private int onRetryTimes = 5;
	List<BusBroker> notOkList;
	private static Throttler instance = null;
	static Lock lock = new ReentrantLock();
	public static Throttler THROTTLER() {
		logger.info("THROTTLER static");
		if ( instance==null) {
			try {
				lock.lock();
				if ( instance==null) {
					synchronized( lock) {
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
	
	public ThrottleResult isProceed(BusBroker bBroker)  {
		ThrottleResult thRet = ThrottleResult.PROCEED;
		long currTime = System.currentTimeMillis();
		currentCount.getAndIncrement();
		if ( !((currTime - startTime) > timePeriod)) {
			// it is inTime
			if ( !(currentCount.get() > count)) {
				// PROCEED
				lastOkTime = currTime;
			} else {
				// NOT good
				thRet = ThrottleResult.OVER;
				lastNotOkTime = currTime;
				if ( !notOkList.contains(bBroker))
					notOkList.add( bBroker);
				try {
				//if ( (ind % 10) == 0) {
					logger.info(" =================================================================================================================going to sleep 5 seconds");
				
				Thread.sleep( 50);
				//}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				logger.info(" =================Finally wake up ========================going to sleep 5 seconds");
			}
			}			
		} else {
			logger.info("!!--+1+--!!!! isProceed: result:{}, notOkList size:{}, currentCount:{} count:{} time:{} timePeriod:{}",
					thRet, notOkList.size(), currentCount.get(), count, (currTime - startTime), timePeriod)		;
			if ( !(currentCount.get() > count)) {
				// available so PROCEED
				if ( !notOkList.isEmpty()) {
					notifyToProceed();
					logger.info("!!--+2+--!!!! isProceed: result:{}, notOkList size:{}, currentCount:{} count:{} time:{} timePeriod:{}",
							thRet, notOkList.size(), currentCount.get(), count, (currTime - startTime), timePeriod)		;
				}
				restThrottle();
				
			} else {
				// NOT good
				thRet = ThrottleResult.OVER;
				lastNotOkTime = currTime;
				if ( !notOkList.contains(bBroker))
					notOkList.add( bBroker);
			}
			
		}	
		logger.info("!!!!!! isProceed: result:{}, notOkList size:{}, currentCount:{} count:{} time:{} timePeriod:{}",
				thRet, notOkList.size(), currentCount.get(), count, (currTime - startTime), timePeriod)		;
		return thRet;
	}
	public boolean notifyToProceed() {
		boolean bRet = true;
		logger.info("!!!!!!notifyToProceed== notOkList size:{}, currentCount:{} count:{} time:{} timePeriod:{}",
				notOkList.size(), currentCount.get(), count, (System.currentTimeMillis() - startTime), timePeriod)		;
		//lock.lock();
		try {
			for ( int ind =0; ind < onRetryTimes; ind++) {
			
			List<CompletableFuture<Boolean>> cfbList = notOkList.stream().map( item -> CompletableFuture.supplyAsync( () -> item.onRetry()))
										.collect(Collectors.toList());
			List<Boolean> boolList = cfbList.stream().map(CompletableFuture::join)
										.collect( Collectors.toList());
			if (!boolList.contains( false) ) {
				logger.info("notifyToProceed--> all success. notOkList size:{}", notOkList.size());
				restThrottle();		
				break;
			} else {
				logger.info("notifyToProceed--> notifyToProcess not all success. notOkList size:{}", notOkList.size());
			}
			}
		} catch (Exception ex) {
			logger.error("THROTTLER can not notify broker:{}", ex.getMessage());
			bRet = false;
		} finally {
			logger.info("!!!unlock!!!notifyToProceed== notOkList size:{}, currentCount:{} count:{} time:{} timePeriod:{}",
					notOkList.size(), currentCount.get(), count, (System.currentTimeMillis() - startTime), timePeriod)		;
			//lock.unlock();
		}
		return bRet;
	}
	
	public int release() {
		try {
			lock.lock();
			currentCount.getAndDecrement();
			logger.info("====================================>Throttler release!! {}", currentCount.get());
			if ( currentCount.get() == 0) {
				startTime = lastOkTime;
			}
		} catch (Exception ex) {
			logger.error("Throttler cant release!! {}", ex.getMessage());
		} finally {
			lock.unlock();
		}
		
		if ( !(currentCount.get() > count)) {
			logger.info("==========currentCount:{} > count:{}======notOkList.size:{}====================>Throttler release!! {}", 
					currentCount.get(), count, notOkList.size(), currentCount.get());
			if ( notOkList.size() > 0) {
				notifyToProceed();
			}			
		}
		return currentCount.get();
		
	}
	public boolean restThrottle() {
		boolean bRet = true;
		currentCount.set(0);
		notOkList.clear();
		startTime = System.currentTimeMillis();
		lastOkTime = lastNotOkTime = 0L;
		logger.info(" reset Throttler-----");
		return bRet;
	}public long  getStartTime() {
		return startTime;
	}
	

}
