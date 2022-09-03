package woodspring.springink.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import woodspring.springink.service.BlueService;
import woodspring.springink.service.YellowService;

@RestController
public class InkController {
	@Autowired
	BlueService eventBus;
	
	@Autowired
	YellowService yellowService;
	
	@GetMapping(value = "/eventbus")
    public String eventBus() {

        return eventBus.doMessageSendReceive();
    }
	@GetMapping(value = "/statistics")
    public String statistics() {

        return yellowService.yellowService(30);
    }

}
