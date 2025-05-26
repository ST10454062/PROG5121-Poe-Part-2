package com.mycompany.registration;

import java.util.Random;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import java.io.FileWriter;
import java.io.IOException;

public class Message {
    
    public static int totalMessagesSent = 0;
    public static List<Message> messageList = new ArrayList<>();
    
    private String messageID;
    private int messageNumber;
    private String recipient;
    private String message;
    private String messageHash;

    public Message(int messageNumber, String recipient, String message) {
        this.messageNumber = messageNumber;
        this.recipient = recipient;
        this.message = message;
        this.messageID = generateMessageID();
        this.messageHash = createMessageHash();
    }

    // Generates a random 10-digit message ID as a String
    private String generateMessageID() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<10; i++) {
            sb.append(random.nextInt(10)); // append digit 0-9
        }
        return sb.toString();
    }

    public boolean checkMessageID() {
        return messageID != null && messageID.length() == 10;
    }

    public boolean checkRecipientCell() {
        // check length <= 13 and starts with +
        return recipient != null && recipient.length() <= 13 && recipient.startsWith("+");
    }

    public String createMessageHash() {
        // Format: first two digits of messageID : messageNumber : first word + last word of message (all uppercase)
        String firstTwo = messageID.substring(0, 2);
        String[] words = message.trim().split("\\s+");
        String firstWord = words.length > 0 ? words[0].toUpperCase() : "";
        String lastWord = words.length > 1 ? words[words.length - 1].toUpperCase() : firstWord;
        return firstTwo + ":" + messageNumber + ":" + firstWord + lastWord;
    }

    public String printMessage() {
        return "Message ID: " + messageID +
                "\nMessage Hash: " + messageHash +
                "\nRecipient: " + recipient +
                "\nMessage: " + message;
    }

    public String getMessage() {
        return message;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessageID() {
        return messageID;
    }

    public String getMessageHash() {
        return messageHash;
    }

    public int getMessageNumber() {
        return messageNumber;
    }

    // Message length validation: max 250 chars
    public String validateMessageLength() {
        int length = message.length();
        if (length <= 250) {
            return "Message ready to send.";
        } else {
            int excess = length - 250;
            return "Message exceeds 250 characters by " + excess + ", please reduce size.";
        }
    }

    
    
    
    
    
    // *** JSON serialization: convert this Message object to JSON string ***
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
    // *** JSON deserialization: create Message object from JSON string ***
    public static Message fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Message.class);
    }
    
    
    public void storeMessageToFile() {
        String fileName = "message_" + messageID + ".json"; // message_<id>.json format
        Gson gson = new Gson(); // Optional if you already have toJson()
        
        try (FileWriter writer = new FileWriter(fileName)) {
                writer.write(gson.toJson(this));
                JOptionPane.showMessageDialog(null, "Message stored to file:\n" + fileName);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving message: " + e.getMessage());
        }
    }
    
    
    /*public static List<Message> loadStoredMessages() {
    List<Message> storedMessages = new ArrayList<>();
    File folder = new File("path/to/messages/folder");
    File[] files = folder.listFiles((dir, name) -> name.startsWith("message_") && name.endsWith(".json"));
    if (files != null) {
        for (File file : files) {
            try {
                String json = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
                Message msg = Message.fromJson(json);
                storedMessages.add(msg);
            } catch (IOException e) {
                // handle error or skip
            }
        }
    }
    return storedMessages;
}
*/
    
   
    
    
    public static void runApp() {
        boolean running = true;
        while (running) {
            String menuOption = JOptionPane.showInputDialog(null,
                    "Choose an option:"
                            + "\n1) Send Messages"
                            + "\n2) Show Recently Sent Messages"
                            + "\n3) Quit",
                    "QuickChat Menu", JOptionPane.QUESTION_MESSAGE);

            if (menuOption == null) {
                // user pressed cancel or closed the dialog
                break;
            }

            switch (menuOption) {
                case "1":
                    sendMessages();
                    break;
                case "2":
                    JOptionPane.showMessageDialog(null, "Coming Soon.");
                    break;
                case "3":
                    running = false;
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "Invalid option. Please select 1, 2, or 3.");
                    break;
            }
        }
    }

    public static void sendMessages() {
        // Ask how many messages user wants to send
        String input = JOptionPane.showInputDialog("Enter number of messages to send:");
        if (input == null) return;

        int numMessages = 0;
        try {
            numMessages = Integer.parseInt(input);
            if (numMessages <= 0) {
                JOptionPane.showMessageDialog(null, "Number must be greater than 0.");
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid number entered.");
            return;
        }

        for (int i = 1; i <= numMessages; i++) {
            String recipient = JOptionPane.showInputDialog("Enter recipient number (include international code, max 13 chars):");
            if (recipient == null) return;

            String messageText = JOptionPane.showInputDialog("Enter message (max 250 characters):");
            if (messageText == null) return;

            Message message = new Message(i, recipient, messageText);

            // Validate recipient
            if (!message.checkRecipientCell()) {
                JOptionPane.showMessageDialog(null,
                        "Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.");
                i--; // allow to re-enter this message
                continue;
            }

            // Validate message length
            String validationMsg = message.validateMessageLength();
            if (!validationMsg.equals("Message ready to send.")) {
                JOptionPane.showMessageDialog(null, validationMsg);
                i--; // re-enter this message
                continue;
            }

            // Ask send options
            String[] options = {"Send Message", "Disregard Message", "Store Message to send later"};
            int choice = JOptionPane.showOptionDialog(null,
                    "Choose an action for the message:",
                    "Send Message",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    options,
                    options[0]);

            switch (choice) {
                case 0: // Send message
                    messageList.add(message);
                    totalMessagesSent++;
                    JOptionPane.showMessageDialog(null, "Message sent.\n\n" + message.printMessage());
                    break;
                case 1: // Disregard
                    JOptionPane.showMessageDialog(null, "Message disregarded and not saved");
                    break;
                case 2: // Store message to send later
                    message.storeMessageToFile();
                    JOptionPane.showMessageDialog(null, "Message successfully stored to send later:\n\n"
                            + "Message ID: " + message.getMessageID() + "\n"
                            + "Message Number: " + message.getMessageNumber() + "\n"
                            + "Recipient: " + message.getRecipient() + "\n"
                            + "Message: " + message.getMessage() + "\n"
                            + "Message Hash: " + message.getMessageHash());
                    break;
                default:
                    JOptionPane.showMessageDialog(null, "No valid option selected. Message disregarded.");
                    break;
            }
        }

        JOptionPane.showMessageDialog(null, "Total messages sent: " + totalMessagesSent);
    }
}
