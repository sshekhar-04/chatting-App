package com.ssd.testing.controller;

import com.ssd.testing.entities.Message;
import com.ssd.testing.entities.Room;
import com.ssd.testing.payload.MessageRequest;
import com.ssd.testing.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.time.LocalDateTime;

@Controller
@CrossOrigin(origins = "http://localhost:*")  // Secure: Replace with your actual frontend origin
public class ChatController {

    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    private final RoomRepository roomRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(RoomRepository roomRepository, SimpMessagingTemplate messagingTemplate) {
        this.roomRepository = roomRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/sendMessage/{roomId}")  // Clients send messages to /app/sendMessage/{roomId}
    public void sendMessage(@DestinationVariable String roomId, MessageRequest request) {
        logger.info("Starting message execution for room: {}", roomId);

        // Validate inputs to prevent errors
        if (roomId == null || roomId.trim().isEmpty()) {
            logger.error("Invalid room ID: {}", roomId);
            messagingTemplate.convertAndSend("/topic/room/" + roomId, "Error: Invalid room ID");
            return;
        }
        if (request == null || request.getContent() == null || request.getSender() == null) {
            logger.error("Invalid message request for room: {}", roomId);
            messagingTemplate.convertAndSend("/topic/room/" + roomId, "Error: Message content and sender are required");
            return;
        }

        try {
            Room room = roomRepository.findByRoomId(roomId);
            if (room == null) {
                logger.error("Room not found: {}", roomId);
                messagingTemplate.convertAndSend("/topic/room/" + roomId, "Error: Room not found");
                return;
            }

            Message message = new Message();
            message.setContent(request.getContent());
            message.setSender(request.getSender());
            message.setTimeStamp(LocalDateTime.now());

            room.getMessages().add(message);
            roomRepository.save(room);

            logger.info("Message saved and broadcasting to room: {}", roomId);
            messagingTemplate.convertAndSend("/topic/room/" + roomId, message);

        } catch (Exception e) {
            logger.error("Error processing message for room {}: {}", roomId, e.getMessage(), e);
            messagingTemplate.convertAndSend("/topic/room/" + roomId, "Error: Failed to send message");
        }
    }
}
