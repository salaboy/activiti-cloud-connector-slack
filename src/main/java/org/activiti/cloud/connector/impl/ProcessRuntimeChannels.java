package org.activiti.cloud.connector.impl;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface ProcessRuntimeChannels {

    String MY_CMD_PRODUCER = "myCmdProducer";
    String MY_CMD_RESULTS = "myCmdResults";

    @Output(MY_CMD_PRODUCER)
    MessageChannel myCmdProducer();

    @Input(MY_CMD_RESULTS)
    SubscribableChannel myCmdResults();
}
