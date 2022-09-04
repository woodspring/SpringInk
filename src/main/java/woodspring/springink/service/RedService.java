package woodspring.springink.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import woodspring.springink.EventBusAsynchN2M.Reader;
import woodspring.springink.EventBusxBroker.NoticeReader;
import woodspring.springink.entities.Notification;
import woodspring.springink.EventBus.EventType;


@Service
public class RedService {
	private final static Logger logger = LoggerFactory.getLogger(RedService.class);
	public RedService() {}
	
	public String eventBusWithThrottler( ) {
		StringBuffer strB = new StringBuffer();
		List<Notification> noticerList = new ArrayList<>();		
		List<String> singleList = new ArrayList<>();
		singleList.add("Galaxies  ---- ");singleList.add("Andromeda Galaxy");singleList.add("Magellanic Clouds  ---- ");singleList.add("Whirlpool Galaxy");
		singleList.add(" Sombrero Galaxy ");singleList.add("NGC 3992");singleList.add("PGC 37617  ---- ");singleList.add("LEDA 37617");
		singleList.add(" Large Magellanic Cloud ");singleList.add("Small Magellanic Cloud,");singleList.add("Triangulum Galaxy.");singleList.add("small cloud");
		singleList.add(" Great Andromeda Nebula ");singleList.add("ellipticals");singleList.add("spirals ");singleList.add(" irregulars ");
		singleList.add(" starburst galaxies ");singleList.add(" active galaxies ");singleList.add("type-cD galaxies ");singleList.add("supergiant elliptical galaxies");
		singleList.add(" Milky Way galaxy ");singleList.add("Blazar ");singleList.add("radio galaxy ");singleList.add("active galactic nucleus");

		List<Notification> noticeList = new ArrayList<>();
		Notification notice1 = new Notification("FAKE WEB" ); notice1.setName("FAKE WEB" ); noticeList.add(notice1);
		Notification notice2 = new Notification("MAC EARTH"); notice2.setName("MAC EARTH"); noticeList.add(notice2);
		Notification notice3 = new Notification("VON GREEN"); notice3.setName("VON GREEN"); noticeList.add(notice3);
		
		List<Reader> readers = new ArrayList<>();
		Reader reader1 = new NoticeReader("BayView", EventType.TOPIC_A); readers.add(reader1);
		Reader reader2 = new NoticeReader("Finch", EventType.TOPIC_B); readers.add(reader2);
		Reader reader3 = new NoticeReader("Yonge", EventType.TOPIC_C); readers.add(reader3);
		Reader reader11 = new NoticeReader("King", EventType.TOPIC_A); readers.add(reader11);
		Reader reader12 = new NoticeReader("Queen", EventType.TOPIC_B); readers.add(reader12);
		Reader reader13 = new NoticeReader("Bay", EventType.TOPIC_C); readers.add(reader13);
		
		String firstSignle = " Start to Notice all of you";
		noticeList.stream().forEach(unit -> unit.publish( firstSignle));

		noticeList.stream().map( 
				 unit -> CompletableFuture.supplyAsync(() -> unit.publishMessages( singleList))
				 )
				.collect(Collectors.collectingAndThen( Collectors.toList(),
						compFuture -> compFuture.stream().map( CompletableFuture::join)))
				.forEach( item -> strB.append( item));
		readers.stream().forEach( reader -> strB.append( reader.getData()));
		
		return strB.toString();
	}

}
