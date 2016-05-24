package com.zk.activitymq.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.SessionAwareMessageListener;

import javax.jms.*;

public class ConsumerSessionAwareMessageListener implements SessionAwareMessageListener<TextMessage> {

    private Destination destination;

    @Override
    public void onMessage(TextMessage textMessage, Session session) throws JMSException {
        System.out.println("收到一条消息");
        System.out.println("消息内容是：" + textMessage.getText());
        MessageProducer producer = session.createProducer(destination);
        Message tt = session.createTextMessage("ConsumerSessionAwareMessageListener。。。");
        producer.send(tt);
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }
}
