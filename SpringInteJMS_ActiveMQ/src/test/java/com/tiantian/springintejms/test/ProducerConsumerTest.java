package com.tiantian.springintejms.test;

import javax.jms.Destination;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tiantian.springintejms.entity.Email;
import com.tiantian.springintejms.service.ProducerService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/applicationContext.xml")
public class ProducerConsumerTest {

	@Autowired
	private ProducerService producerService;
	@Autowired
	@Qualifier("queueDestination")
	private Destination destination;
	
	@Autowired
	@Qualifier("sessionAwareQueue")
	private Destination sessionAwareQueue;
	@Autowired
	@Qualifier("adapterQueue")
	private Destination adapterQueue;
	
	@Test
	public void testSend() {
		producerService.sendMessage(destination, "你好，消费者！");
	}
	
	@Test
	public void testSessionAwareMessageListener() {
		producerService.sendMessage(sessionAwareQueue, "测试SessionAwareMessageListener");
	}
	
	@Test
	public void testMessageListenerAdapter() {
		producerService.sendMessage(adapterQueue, "测试MessageListenerAdapter");
	}
	
	@Test
	public void testObjectMessage() {
		Email email = new Email("zhangsan@xxx.com", "主题", "内容");
		producerService.sendMessage(adapterQueue, email);
	}
	
}
