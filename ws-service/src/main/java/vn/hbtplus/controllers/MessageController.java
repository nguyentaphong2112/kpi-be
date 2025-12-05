package vn.hbtplus.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import vn.hbtplus.models.dto.Message;
import vn.hbtplus.utils.Utils;

@Controller
public class MessageController {

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/sendAll")
    @SendTo("/sendAll/messages")
    public Message send(final Message message) throws Exception {
        return message;
    }

    @MessageMapping("/sendToUser")
    public void sendToSpecificUser(@Payload Message message) {
        simpMessagingTemplate.convertAndSendToUser(Utils.getUserNameLogin(), "/sendToUser", message);
    }
}
