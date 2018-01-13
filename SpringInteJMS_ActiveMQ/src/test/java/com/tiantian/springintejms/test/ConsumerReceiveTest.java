package com.tiantian.springintejms.test;

import com.tiantian.springintejms.entity.TestMqBean;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.*;
import java.util.Date;

@RunWith(SpringJUnit4ClassRunner.class)
public class ConsumerReceiveTest {

	@Test
	public static void main(String[] args) {
		ConnectionFactory connectionFactory;
		// Connection ：JMS 客户端到JMS Provider 的连接
		Connection connection = null;
		// Session： 一个发送或接收消息的线程
		final Session session;
		// Destination ：消息的目的地;消息发送给谁.
		Destination destination_request,destination_response;
		// 消费者，消息接收者
		MessageConsumer consumer;
		//回复接收到的消息
		final MessageProducer producer;
		connectionFactory = new ActiveMQConnectionFactory("admin", "admin", "tcp://192.168.210.128:61616");
		try {
			// 构造从工厂得到连接对象
			connection = connectionFactory.createConnection();
			// 启动
			connection.start();
			// 获取操作连接
			//这个最好还是有事务
			session = connection.createSession(Boolean.FALSE, Session.AUTO_ACKNOWLEDGE);
			// 获取session注意参数值xingbo.xu-queue是一个服务器的queue，须在在ActiveMq的console配置
			destination_request = session.createQueue("request-queue");
			destination_response = session.createQueue("response-queue");
			consumer = session.createConsumer(destination_request);

			producer= session.createProducer(destination_response);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

			consumer.setMessageListener(new MessageListener() {
				@Override
				public void onMessage(Message message) {
					try {
						TestMqBean bean = (TestMqBean) ((ObjectMessage) message).getObject();
						System.out.println(bean);
						if (null != message) {
							System.out.println("收到消息" + bean.getName());
							Message textMessage = session.createTextMessage("已经成功收到消息，现在开始回复"+new Date().toString());
							producer.send(textMessage);
						}
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}