# SpringinteJMSonActiveMQ
Spring 整合ActiveMQ定义消息管理。

本文主要讲述ActiveMQ与spring整合的方案。介绍知识点包括spring，jms，activemq基于配置文件模式管理消息，消息监听器类型，消息转换类介绍，spring对JMS事物管理。
1.spring整合activemq配置文件说明
1.1配置ConnectionFactory
       ConnectionFactory是用于产生到JMS服务器的链接的，Spring提供了多个ConnectionFactory，有SingleConnectionFactory和CachingConnectionFactory。SingleConnectionFactory对于建立JMS服务器链接的请求会一直返回同一个链接，并且会忽略Connection的close方法调用。CachingConnectionFactory继承了SingleConnectionFactory，所以它拥有SingleConnectionFactory的所有功能，同时它还新增了缓存功能，它可以缓存Session、MessageProducer和MessageConsumer。这里使用SingleConnectionFactory来作为示例。
1.2配置生产者
    配置好ConnectionFactory之后就需要配置生产者。生产者负责产生消息并发送到JMS服务器，这通常对应的是一个业务逻辑服务实现类。但是服务实现类是怎么进行消息的发送的呢？这通常是利用Spring提供的JmsTemplate类来实现的，所以配置生产者其实最核心的就是配置进行消息发送的JmsTemplate。对于消息发送者而言，它在发送消息的时候要知道自己该往哪里发，为此，在定义JmsTemplate的时候需要往里面注入一个Spring提供的ConnectionFactory对象。
1.3配置消费者
   生产者往指定目的地Destination发送消息后，接下来就是消费者对指定目的地的消息进行消费了。那么消费者是如何知道有生产者发送消息到指定目的地Destination了呢？这是通过Spring封装的消息监听容器MessageListenerContainer实现的，它负责接收信息，并把接收到的信息分发给真正的MessageListener进行处理。每个消费者对应每个目的地都需要有对应的MessageListenerContainer。对于消息监听容器而言，除了要知道监听哪个目的地之外，还需要知道到哪里去监听，也就是说它还需要知道去监听哪个JMS服务器，这是通过在配置MessageConnectionFactory的时候往里面注入一个ConnectionFactory来实现的。所以在配置一个MessageListenerContainer的时候有三个属性必须指定：
    一个是表示从哪里监听的ConnectionFactory；一个是表示监听什么的Destination；一个是接收到消息以后进行消息处理的MessageListener。
Spring一共提供了多种类型MessageListenerContainer，SimpleMessageListenerContainer和DefaultMessageListenerContainer。SimpleMessageListenerContainer会在一开始的时候就创建一个会话session和消费者Consumer，并且会使用标准的JMS MessageConsumer.setMessageListener()方法注册监听器让JMS提供者调用监听器的回调函数。它不会动态的适应运行时需要和参与外部的事务管理。兼容性方面，它非常接近于独立的JMS规范，但一般不兼容Java EE的JMS限制。大多数情况下使用的DefaultMessageListenerContainer，跟SimpleMessageListenerContainer相比，DefaultMessageListenerContainer会动态的适应运行时需要，并且能够参与外部的事务管理。它很好的平衡了对JMS提供者要求低、先进功能如事务参与和兼容Java EE环境。
1.4定义处理消息的MessageListener
       要定义处理消息的MessageListener只需要实现JMS规范中的MessageListener接口就可以了。MessageListener接口中只有一个方法onMessage方法，当接收到消息的时候会自动调用该方法。
1.5实例分析
    编写一个sessionAwareQueue目的队列，向改队列发送消息，接受消息成功后，并回复一条消息。监控消息函数为：ConsumerSessionAwareMessageListener。
    消息生产者定义发送消息方法
@Component
public class ProducerServiceImpl implements ProducerService {
    @Autowired
    private JmsTemplate jmsTemplate;
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
    消费者定义接收消息方法
public class ConsumerSessionAwareMessageListener implements
        SessionAwareMessageListener<TextMessage> {
    private Destination destination;    
    public void onMessage(TextMessage message, Session session) throws JMSException {
        System.out.println("收到一条消息");
        System.out.println("消息内容是：" + message.getText());
        MessageProducer producer = session.createProducer(destination);
        System.out.println("发送一条回复消息");
        Message textMessage = session.createTextMessage("回复消息内容：ConsumerSessionAwareMessageListener。。。");
        producer.send(textMessage);
    }

    public Destination getDestination() {
        return destination;
    }
    public void setDestination(Destination destination) {
        this.destination = destination;
    }

}
    测试发送消息
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
    @Test
    public void testSessionAwareMessageListener() {
        producerService.sendMessage(sessionAwareQueue, "测试SessionAwareMessageListener");
    }
    
}
    示例运行截图：


2.消息监听器MessageListener介绍
    在Spring整合JMS的应用中，在定义消息监听器的时候一共可以定义三种类型的消息监听器，分别是MessageListener、SessionAwareMessageListener和MessageListenerAdapter。下面就分别来介绍一下这几种类型的区别。
2.1消息监听器MessageListener
    MessageListener是最原始的消息监听器，它是JMS规范中定义的一个接口。其中定义了一个用于处理接收到的消息的onMessage方法，该方法只接收一个Message参数。示例代码如下：
public class ConsumerMessageListener implements MessageListener {  
   
    public void onMessage(Message message) {  
        //这里我们知道生产者发送的就是一个纯文本消息，所以这里可以直接进行强制转换，或者直接把onMessage方法的参数改成Message的子类TextMessage  
        TextMessage textMsg = (TextMessage) message;  
        System.out.println("接收到一个纯文本消息。");  
        try {  
            System.out.println("消息内容是：" + textMsg.getText());  
        } catch (JMSException e) {  
            e.printStackTrace();  
        }  
    }  
   
}  
2.2消息监听器SessionAwareMessageListener
   SessionAwareMessageListener是Spring为提供的，它不是标准的JMS MessageListener。MessageListener的设计只是纯粹用来接收消息的，假如在使用MessageListener处理接收到的消息时我们需要发送一个消息通知对方我们已经收到这个消息了，这个时候就需要在代码里面去重新获取一个Connection或Session。SessionAwareMessageListener的设计就是为了方便在接收到消息后发送一个回复的消息，它同样提供了一个处理接收到的消息的onMessage方法，但是这个方法可以同时接收两个参数，一个是表示当前接收到的消息Message，另一个就是可以用来发送消息的Session对象。
2.3MessageListenerAdapter
MessageListenerAdapter类实现了MessageListener接口和SessionAwareMessageListener接口，它的主要作用是将接收到的消息进行类型转换，然后通过反射的形式把它交给一个普通的Java类进行处理。MessageListenerAdapter会把接收到的消息做如下转换：

TextMessage转换为String对象；
BytesMessage转换为byte数组；
 MapMessage转换为Map对象；
ObjectMessage转换为对应的Serializable对象。
3.消息转换器MessageConverter介绍
    MessageConverter的作用主要有两方面，一方面它可以把非标准化Message对象转换成目标Message对象，这主要是用在发送消息的时候；另一方面它又可以把的Message对象转换成对应的目标对象，这主要是用在接收消息的时候。MessageConverter可用spring提供的简单模型或者自己编写转换定义类。Spring在初始化JmsTemplate的时候指定了其对应的MessageConverter为一个SimpleMessageConverter，所以如果平常没有什么特殊要求的时候可以直接使用JmsTemplate的convertAndSend系列方法进行消息发送，而不必繁琐的在调用send方法时自己new一个MessageCreator进行相应Message的创建。可在源码中查看SimpleMessageConverter的定义，如果觉得它不能满足业务的要求，那可以对它里面的部分方法进行重写，或者是完全实现自定义的MessageConverter。

4.spring对JMS的事务管理
    Spring提供了一个JmsTransactionManager用于对JMS ConnectionFactory做事务管理。这将允许JMS应用利用Spring的事务管理特性。JmsTransactionManager在执行本地资源事务管理时将从指定的ConnectionFactory绑定一个ConnectionFactory/Session这样的配对到线程中。JmsTemplate会自动检测这样的事务资源，并对它们进行相应操作。在Java EE环境中，ConnectionFactory会池化Connection和Session，这样这些资源将会在整个事务中被有效地重复利用。在一个独立的环境中，使用Spring的SingleConnectionFactory时所有的事务将公用一个Connection，但是每个事务将保留自己独立的Session。JmsTemplate可以利用JtaTransactionManager和能够进行分布式的 JMS ConnectionFactory处理分布式事务。   在Spring整合JMS的应用中，如果要进行本地的事务管理的话只需要在定义对应的消息监听容器时指定其sessionTransacted属性为true，如：
复制代码
<bean id="jmsContainer"  
    class="org.springframework.jms.listener.DefaultMessageListenerContainer">  
    <property name="connectionFactory" ref="connectionFactory" />  
    <property name="destination" ref="queueDestination" />  
    <property name="messageListener" ref="consumerMessageListener" />  
    <property name="sessionTransacted" value="true"/>  
</bean>  
复制代码
       该属性值默认为false，这样JMS在进行消息监听的时候就会进行事务控制，当在接收消息时监听器执行失败时JMS就会对接收到的消息进行回滚，对于SessionAwareMessageListener在接收到消息后发送一个返回消息时也处于同一事务下，但是对于其他操作如数据库访问等将不属于该事务控制。 如果想接收消息和数据库访问处于同一事务中，可配置一个外部的事务管理同时配置一个支持外部事务管理的消息监听容器（如DefaultMessageListenerContainer）。要配置这样一个参与分布式事务管理的消息监听容器，可以配置一个JtaTransactionManager，当然底层的JMS ConnectionFactory需要能够支持分布式事务管理，并正确地注册JtaTransactionManager。这样消息监听器进行消息接收和对应的数据库访问就会处于同一数据库控制下，当消息接收失败或数据库访问失败都会进行事务回滚操作。
