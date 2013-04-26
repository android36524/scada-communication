package com.ht.scada.communication.web.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;

@Controller
@RequestMapping(value="/home")
public class HomeController {
	@PostConstruct
	private void init() {
		System.out.println("init");
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public String home() {
		System.out.println("home");
		return "index";
	}
}
