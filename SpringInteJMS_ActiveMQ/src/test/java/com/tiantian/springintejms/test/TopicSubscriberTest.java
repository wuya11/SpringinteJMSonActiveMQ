package com.tiantian.springintejms.test;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class TopicSubscriberTest {
	private static String user = "admin";
	private static String password = "admin";
	private static String url = "tcp://192.168.210.128:61616";
	public static void main(String[] args) throws Exception{
		// ConnectionFactory ：连接工厂，JMS 用它创建连接
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user,password,url);
		// Connection ：JMS 客户端到JMS Provider 的连接
		Connection connection = connectionFactory.createConnection();
		connection.start();
		// Session： 一个发送或接收消息的线程
		final Session session = connection.createSession(Boolean.TRUE, Session.AUTO_ACKNOWLEDGE);
		// Destination ：消息的目的地;消息发送给谁.
		Topic destination=session.createTopic("example.A");
		// 消费者，消息接收者
		MessageConsumer consumer = session.createConsumer(destination);
		consumer.setMessageListener(new MessageListener(){//有事务限制
			@Override
			public void onMessage(Message message) {
				try {
					TextMessage textMessage=(TextMessage)message;
					System.out.println("接收到消息："+textMessage.getText());
				} catch (JMSException e1) {
					e1.printStackTrace();
				}
				try {
					session.commit();
				} catch (JMSException e) {
					e.printStackTrace();
				}
			}
		});
	}
}