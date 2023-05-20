package com.example.redis.service;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

@Service
public class ChatService implements MessageListener {

    private final RedisMessageListenerContainer container;
    private final RedisTemplate<String, String> redisTemplate;

    public ChatService(RedisMessageListenerContainer container, RedisTemplate<String, String> redisTemplate) {
        this.container = container;
        this.redisTemplate = redisTemplate;
    }

    public void enterChatRoomAndExit(String chatRoomName) {
        container.addMessageListener(this, new ChannelTopic(chatRoomName));
        redisTemplate.convertAndSend(chatRoomName, "hello");
        container.removeMessageListener(this);
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        System.out.println("Message: " + message.toString());
    }
}
