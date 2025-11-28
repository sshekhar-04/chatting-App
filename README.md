
# 💬 Real-Time Room Chat Application 

This repository contains the backend implementation for a scalable, room-based chat application demonstrating advanced Spring Boot features, including WebSockets and MongoDB persistence.

## 🚀 Key Technologies

* **Framework:** Spring Boot 4.0.0 (Java)
* **Database:** MongoDB (using Spring Data MongoDB for persistence)
* **Real-time Protocol:** **WebSocket** using **STOMP** (Simple Text Oriented Messaging Protocol)
* **Messaging:** Spring's `SimpMessagingTemplate` for programmatic message broadcasting.

---

## ⚙️ Backend Architecture and Components

The application is built around three core layers: Configuration, Persistence, and Real-time Communication.

### 1. 🌐 WebSocket & STOMP Configuration (`WebSocketConfig`)

This class establishes the plumbing for real-time communication.

* **Native WebSocket Endpoint:** Defines the entry point for native WebSocket clients (like Postman or other servers).
    * **Connection URL:** `ws://localhost:8080/ws-chat`
* **Message Broker:** Configures the in-memory broker used for broadcasting messages.
    * **Broker Prefix:** `/topic` (e.g., messages are broadcast to `/topic/room/{id}`).
* **Application Prefix:** Designates the path for messages intended for Java application logic.
    * **Application Prefix:** `/app` (e.g., client sends messages to `/app/sendMessage/{id}`).

### 2. 🗄️ Persistence Layer (MongoDB)

This layer manages the permanent storage of room and message data.

* **Spring Data MongoDB:** Used to abstract database interaction.
* **Repository Pattern:** A standard Spring Data Repository interface is used (e.g., `RoomRepository`) which provides methods like `findByRoomId()` and `save()`.
* **Data Integrity:** The `Message` objects are stored as embedded documents within the main `Room` document, demonstrating a practical MongoDB data model for chat persistence.

### 3. 🎯 Controller & Message Handling (`ChatController`)

This is the core business logic layer where incoming messages are processed.

| Component | Annotation/Method | Function |
| :--- | :--- | :--- |
| **Routing** | `@MessageMapping("/sendMessage/{roomId}")` | Maps incoming STOMP **SEND** frames (from `/app`) to this Java method. |
| **Variable Extraction** | `@DestinationVariable String roomId` | Extracts the dynamic room ID from the message destination URL for use in database lookups. |
| **Persistence** | `roomRepository.save(room)` | Updates the room document in MongoDB with the new message record. |
| **Broadcasting** | `messagingTemplate.convertAndSend()` | Manually sends the saved `Message` object to the correct broker destination (`/topic/room/{roomId}`). This ensures persistence completes before broadcasting. |

---

## 🧪 Testing the API (Postman STOMP Guide)

To test the full server pipeline, use the native WebSocket endpoint (`/ws-chat`). **A Room with ID `123` must exist in your MongoDB for this test to succeed.**

### Step 1: Establish Connection

Connect to the native endpoint: `ws://localhost:8080/ws-chat`

### Step 2: Subscribe (Listen)

Send the `SUBSCRIBE` frame to the broker channel:

```text
SUBSCRIBE
id:sub-1
destination:/topic/room/123
ack:auto


^@

Send the SEND frame to the application handler to trigger the save and broadcast logic:

SEND
destination:/app/sendMessage/123
content-type:application/json

{"roomId": "123", "content": "Test message from API", "sender": "Postman Tester"}
^@
