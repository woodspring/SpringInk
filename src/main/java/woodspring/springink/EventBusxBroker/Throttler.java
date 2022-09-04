package woodspring.springink.EventBusxBroker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import woodspring.springink.EventBusAsynchN2M.NewsEventBus;

public class Throttler {
	private final static Logger logger = LoggerFactory.getLogger(Throttler.class);
	
	private static final int count = 10;
	private int currentCount = 0;
	private long timePeriod = 10 * 1000L;
	private long startTime = 0L;
	private long lastOkTime =0L, lastNotOkTime=0L;
	List<BusBroker> notOkList;
	private static Throttler instance = null;
	static Lock lock = new ReentrantLock();
	public static Throttler THROTTLER() {
		if ( instance==null) {
			try {
				lock.lock();
				if ( instance==null) {
					synchronized( instance) {
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
		currentCount++;
		if ( !((currTime - startTime) > timePeriod)) {
			// it is inTime
			if ( !((currentCount) > count)) {
				// PROCEED
				lastOkTime = currTime;
				currentCount++;
			} else {
				// NOT good
				thRet = ThrottleResult.OVER;
				lastNotOkTime = currTime;
				if ( !notOkList.contains(bBroker))
					notOkList.add( bBroker);
			}			
		} else {
			if ( !((currentCount) > count)) {
				// available so PROCEED
				if ( !notOkList.isEmpty())
					notifyToProceed();
				restThrottle();
			} else {
				// NOT good
				thRet = ThrottleResult.OVER;
				lastNotOkTime = currTime;
				if ( !notOkList.contains(bBroker))
					notOkList.add( bBroker);
			}
			
		}		
		return thRet;
	}
	public boolean notifyToProceed() {
		boolean bRet = true;
		lock.lock();
		try {
			
			List<CompletableFuture<Boolean>> cfbList = notOkList.stream().map( item -> CompletableFuture.supplyAsync( () -> item.onRetry()))
										.collect(Collectors.toList());
			List<Boolean> boolList = cfbList.stream().map(CompletableFuture::join)
										.collect( Collectors.toList());
			if (!boolList.contains( false) ) {
				restThrottle();				
			}
		} catch (Exception ex) {
			logger.error("THROTTLER can not notify broker:{}", ex.getMessage());
			bRet = false;
		} finally {
			lock.unlock();
		}
		return bRet;
	}
	
	public int release() {
		try {
			lock.lock();
			currentCount--;
			if ( currentCount == 0) {
				startTime = lastOkTime;
			}
		} catch (Exception ex) {
			logger.error("Throttler cant release!! {}", ex.getMessage());
		} finally {
			lock.unlock();
		}
		
		if ( !(currentCount > count)) {
			if ( notOkList.size() > 0) {
				notifyToProceed();
			}			
		}
		return currentCount;
		
	}
	public boolean restThrottle() {
		boolean bRet = true;
		currentCount=0;
		notOkList.clear();
		startTime = System.currentTimeMillis();
		lastOkTime = lastNotOkTime = 0L;		
		return bRet;
	}
	

}
