package com.ht.scada.communication;

import com.ht.scada.common.middleware.service.impl.JmsServiceImpl;

public class JmsHandler {
	private JmsServiceImpl jmsService;

	private JmsHandler(JmsServiceImpl jmsService) {
		this.jmsService = jmsService;
	}
	
}
