package org.activiti.cloud.connector.impl;

import org.activiti.cloud.api.process.model.IntegrationRequest;
import org.activiti.cloud.api.process.model.IntegrationResult;
import org.activiti.cloud.connector.slack.ActivitiCloudSlackBot;
import org.activiti.cloud.connectors.starter.channels.IntegrationResultSender;
import org.activiti.cloud.connectors.starter.configuration.ConnectorProperties;
import org.activiti.cloud.connectors.starter.model.IntegrationResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Component
@EnableBinding(SlackConnectorChannels.class)
public class SlackConnector {

    private final Logger logger = LoggerFactory.getLogger(SlackConnector.class);

    @Value("${spring.application.name}")
    private String appName;

    @Autowired
    private ActivitiCloudSlackBot bot;

    @Autowired
    private ConnectorProperties connectorProperties;

    private final IntegrationResultSender integrationResultSender;

    public SlackConnector(IntegrationResultSender integrationResultSender) {

        this.integrationResultSender = integrationResultSender;
    }

    @StreamListener(value = SlackConnectorChannels.SLACK_CONNECTOR_CONSUMER)
    public void notifySlackChannel(IntegrationRequest event) throws InterruptedException {

        logger.info(">> Inside Slack Cloud Connector: " + SlackConnectorChannels.SLACK_CONNECTOR_CONSUMER);

        String var1 = SlackConnector.class.getSimpleName()+" was called for instance " + event.getIntegrationContext().getProcessInstanceId();

        // @TODO: add your code here
        try {
            bot.pushMessageToChannel("editors", "@everyone There is new content to review: " + (String)event.getIntegrationContext().getInBoundVariables().get("content"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Map<String, Object> results = new HashMap<>();
        // @TODO: set your results here
        Message<IntegrationResult> message = IntegrationResultBuilder.resultFor(event, connectorProperties)
                .withOutboundVariables(results)
                .buildMessage();

        integrationResultSender.send(message);
    }


}
