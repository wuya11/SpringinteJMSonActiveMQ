package com.tiantian.springintejms.test;

import com.tiantian.springintejms.entity.Email;
import com.tiantian.springintejms.entity.TestMqBean;
import com.tiantian.springintejms.service.ProducerService;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.*;

@RunWith(SpringJUnit4ClassRunner.class)
public class ProducerSendTest {

	@Test
	public static void main(String[] args) {
		ConnectionFactory connectionFactory;
		Connection connection;
		Session session;
		Destination destination_request,destination_response;
		MessageProducer producer;
		MessageConsumer consumer;
		connectionFactory = new ActiveMQConnectionFactory("admin", "admin", "tcp://192.168.210.128:61616");
		try {
			connection = connectionFactory.createConnection();
			connection.start();
			//第一个参数是是否是事务型消息，设置为true,第二个参数无效
			//第二个参数是
			//Session.AUTO_ACKNOWLEDGE为自动确认，客户端发送和接收消息不需要做额外的工作。异常也会确认消息，应该是在执行之前确认的
			//Session.CLIENT_ACKNOWLEDGE为客户端确认。客户端接收到消息后，必须调用javax.jms.Message的acknowledge方法。jms服务器才会删除消息。可以在失败的
			//时候不确认消息,不确认的话不会移出队列，一直存在，下次启动继续接受。接收消息的连接不断开，其他的消费者也不会接受（正常情况下队列模式不存在其他消费者）
			//DUPS_OK_ACKNOWLEDGE允许副本的确认模式。一旦接收方应用程序的方法调用从处理消息处返回，会话对象就会确认消息的接收；而且允许重复确认。在需要考虑资源使用时，这种模式非常有效。
			//待测试
			session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
			destination_request = session.createQueue("request-queue");
			destination_response = session.createQueue("response-queue");
			producer = session.createProducer(destination_request);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

			consumer = session.createConsumer(destination_response);
			//优先级不能影响先进先出。。。那这个用处究竟是什么呢呢呢呢
			TestMqBean bean = new TestMqBean();
			bean.setAge(13);
			for (int i = 0; i < 10; i++) {
				bean.setName("send to data -" + i);
				producer.send(session.createObjectMessage(bean));
			}
			producer.close();
			System.out.println("消息发送成功...");

			consumer.setMessageListener(new MessageListener() {
				@Override
				public void onMessage(Message message) {
					try {
						if (null != message) {
							TextMessage textMsg = (TextMessage) message;
							System.out.println("收到回馈消息" +textMsg.getText());
						}
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			});

		} catch (JMSException e) {
			e.printStackTrace();
		}
	}
}