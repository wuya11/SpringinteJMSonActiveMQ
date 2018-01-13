package com.tiantian.springintejms.test;

import com.tiantian.springintejms.entity.TestMqBean;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class TopicProducerSendTest {

	private static String user = "admin";
	private static String password = "admin";
	private static String url = "tcp://192.168.210.128:61616";

	public static void main(String[] args)throws Exception {
		// ConnectionFactory ：连接工厂，JMS 用它创建连接
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(user,password,url);
		// Connection ：JMS 客户端到JMS Provider 的连接
		Connection connection = connectionFactory.createConnection();
		// Connection 启动
		connection.start();
		System.out.println("Connection is start...");
		// Session： 一个发送或接收消息的线程
		Session session = connection.createSession(Boolean.TRUE,Session.AUTO_ACKNOWLEDGE);
		// Topicr ：消息的目的地;消息发送给谁.
		Topic  destination = session.createTopic("example.A");
		// MessageProducer：消息发送者
		MessageProducer producer = session.createProducer(destination);
		// 设置不持久化，此处学习，实际根据项目决定
		producer.setDeliveryMode(DeliveryMode.PERSISTENT);
		// 构造消息，此处写死，项目就是参数，或者方法获取
		sendMessage(session, producer);
		session.commit();

		connection.close();
		System.out.println("send text ok.");
	}

	public static void sendMessage(Session session, MessageProducer producer)
			throws Exception {
		for (int i = 1; i <= 10; i++) {//有限制
			TextMessage message = session.createTextMessage("ActiveMq 发送的消息" + i);
			// 发送消息到目的地方
			System.out.println("发送消息：" + "ActiveMq 发送的消息" + i);
			producer.send(message);
		}
	}

}