package com.mycompany.registration;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MessageTest {
    
    public MessageTest(){
    }

    @Test
    public void testValidateMessageLength_Success() {
        Message user = new Message(1, "+27718693002", "Hi Mike, can you join us for dinner tonight");
        assertEquals("Message ready to send.", user.validateMessageLength());
    }

    @Test
    public void testValidateMessageLength_Failure() {
        String longText = "x".repeat(260);
        Message user = new Message(1, "+27718693002", longText);
        assertEquals("Message exceeds 250 characters by 10, please reduce size.", user.validateMessageLength());
    }

    @Test
    public void testCheckRecipientCell_Success() {
        Message user = new Message(1, "+27718693002", "Hello");
        assertTrue(user.checkRecipientCell());
    }

    @Test
    public void testCheckRecipientCell_Failure() {
        Message user = new Message(1, "08575975889", "Hello");
        assertFalse(user.checkRecipientCell());
    }

    @Test
    public void testCheckMessageID_Valid() {
        Message user = new Message(1, "+27718693002", "Test message");
        assertTrue(user.checkMessageID());
        assertEquals(10, user.getMessageID().length());
    }
    
        @Test
    public void testCreateMessageHash() {
        Message user = new Message(0, "+27718693002", "Hi Mike, can you join us for dinner tonight");
        String expectedStart = user.getMessageID().substring(0, 2) + ":0:";
        String expectedEnd = "HITONIGHT";
        assertEquals(expectedStart + expectedEnd, user.getMessageHash());
    }
    
        @Test
    public void testMessageSendOptions() {
        Message send = new Message(1, "+27718693002", "Hi Mike, can you join us for dinner tonight");
        Message discard = new Message(2, "08575975889", "Hi Keegan, did you receive the payment?");

        // Simulate sending
        Message.messageList.add(send);
        Message.totalMessagesSent++;
        assertTrue(Message.messageList.contains(send));

        // Simulate discard (nothing added)
        assertFalse(Message.messageList.contains(discard));

        // Simulate store (file created, message not added)
        boolean result = discard.writeMessageToFile("test_message_" + discard.getMessageID() + ".json");
        assertTrue(result);
    }

    @Test
    public void testToJsonAndFromJson() {
        Message original = new Message(2, "+27718693002", "Hello again");
        String json = original.toJson();

        Message copy = Message.fromJson(json);
        assertEquals(original.getMessage(), copy.getMessage());
        assertEquals(original.getRecipient(), copy.getRecipient());
        assertEquals(original.getMessageID(), copy.getMessageID());
        assertEquals(original.getMessageHash(), copy.getMessageHash());
        assertEquals(original.getMessageNumber(), copy.getMessageNumber());
    }
}
