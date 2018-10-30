package org.activiti.cloud.connector.slack;

import me.ramswaroop.jbot.core.slack.Bot;
import me.ramswaroop.jbot.core.slack.Controller;
import me.ramswaroop.jbot.core.slack.EventType;
import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.models.Message;
import org.activiti.api.process.model.builders.ProcessPayloadBuilder;
import org.activiti.api.process.model.payloads.StartProcessPayload;
import org.activiti.cloud.connector.impl.ProcessRuntimeChannels;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;

@Component
@EnableBinding(ProcessRuntimeChannels.class)
public class ActivitiCloudSlackBot extends Bot {


    Map<String, WebSocketSession> channelsMap = new ConcurrentHashMap<>();
    Map<String, String> channelsIdsMap = new ConcurrentHashMap<>();
    Map<String, String> cmdToProcessDef = new HashMap<>();

    @Value("${slackBotToken}")
    private String slackToken;

    public ActivitiCloudSlackBot() {
        cmdToProcessDef.put("request-article", "reviewnoti-b1287625-d4f4-40a3-8ce9-863a337e05a8");
    }

    @Autowired
    private ProcessRuntimeChannels processRuntimeChannels;

    @Override
    public String getSlackToken() {
        return slackToken;
    }

    @Override
    public Bot getSlackBot() {
        return this;
    }


    @Controller(events = {EventType.CHANNEL_JOINED})
    public void onChannelJoined(WebSocketSession session, Event event) {
        System.out.println("Joining Channel: " + event.getChannel().getName() +
                " Channel Id: " + event.getChannel().getId()
                + " and I am " + slackService.getCurrentUser().getName());
        channelsMap.put(event.getChannel().getName(), session);
        channelsIdsMap.put(event.getChannel().getName(), event.getChannel().getId());
    }

    @Controller(events = {EventType.DIRECT_MENTION, EventType.DIRECT_MESSAGE})
    public void onReceiveDM(WebSocketSession session, Event event) {
        reply(session, event, new Message("Hi, I am " + slackService.getCurrentUser().getName()));
    }

    public void pushMessageToChannel(String channel, String text) throws IOException {
        Message message = new Message(text);

        message.setType(EventType.MESSAGE.name().toLowerCase());
        message.setUser(slackService.getCurrentUser().getName());

        WebSocketSession webSocketSession = channelsMap.get(channel);
        if (webSocketSession != null) {
            message.setChannel(channelsIdsMap.get(channel));
            webSocketSession.sendMessage(new TextMessage(message.toJSONString()));
        } else {
            System.out.println("Channel " + channel + " doesn't exist");
        }
    }


    @Controller(pattern = "(^request-[a-z]+):([aA-zZ 1-9]+)")
    public void onReceiveMessage(WebSocketSession session, Event event, Matcher matcher) {
        if (!matcher.group(0).isEmpty()) {
            StartProcessPayload startProcessInstanceCmd = ProcessPayloadBuilder.start()
                    .withProcessDefinitionKey(cmdToProcessDef.get(matcher.group(1)))
                    .withVariable("title", matcher.group(2))
                    .build();
            processRuntimeChannels.myCmdProducer().send(MessageBuilder.withPayload(startProcessInstanceCmd).build());
            reply(session, event, new Message("Request Received: " + matcher.group(1) + "with title: " + matcher.group(2) + ". Process Started :)"));

        }

    }
}
