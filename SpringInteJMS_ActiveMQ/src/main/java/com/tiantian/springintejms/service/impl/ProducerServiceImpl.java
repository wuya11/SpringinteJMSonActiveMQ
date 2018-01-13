package com.tiantian.springintejms.service.impl;

import java.io.Serializable;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

import com.tiantian.springintejms.service.ProducerService;

@Component
public class ProducerServiceImpl implements ProducerService {

	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	@Qualifier("responseQueue")
	private Destination responseDestination;
	public void sendMessage(Destination destination, final String message) {
		System.out.println("---------------生产者发送消息-----------------");
		System.out.println("---------------生产者发了一个消息：" + message);
		jmsTemplate.send(destination, new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				return session.createTextMessage(message);
			}
		});
	}
	public void sendMessage(final Destination destination, final Serializable obj) {
		jmsTemplate.convertAndSend(destination, obj);
	}
	
}
