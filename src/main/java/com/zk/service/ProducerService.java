package com.zk.service;

import javax.jms.Destination;

public interface ProducerService {

    void sendMessage(Destination destination, final String message);

}
