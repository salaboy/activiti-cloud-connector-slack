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
@EnableBinding(ValidateConnectorChannels.class)
public class ValidateConnector {

    private final Logger logger = LoggerFactory.getLogger(ValidateConnector.class);

    @Value("${spring.application.name}")
    private String appName;

    @Autowired
    private ConnectorProperties connectorProperties;

    private final IntegrationResultSender integrationResultSender;

    public ValidateConnector(IntegrationResultSender integrationResultSender) {

        this.integrationResultSender = integrationResultSender;
    }

    @StreamListener(value = ValidateConnectorChannels.VALIDATE_CONNECTOR_CONSUMER)
    public void notifySlackChannel(IntegrationRequest event) {

        logger.info(">> Inside Slack Cloud Connector: " + ValidateConnectorChannels.VALIDATE_CONNECTOR_CONSUMER);

        String var1 = ValidateConnector.class.getSimpleName()+" was called for instance " + event.getIntegrationContext().getProcessInstanceId();

        logger.info(">> By default we are approving content to cover the happy path first");

        Map<String, Object> results = new HashMap<>();
        // @TODO: set your results here
        results.put("approved", true);

        Message<IntegrationResult> message = IntegrationResultBuilder.resultFor(event, connectorProperties)
                .withOutboundVariables(results)
                .buildMessage();

        integrationResultSender.send(message);
    }


}
