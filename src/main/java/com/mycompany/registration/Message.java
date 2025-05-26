package com.mycompany.registration;

import java.util.Random;
import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import java.io.FileWriter;
import java.io.IOException;

public class Message {
    
    // Static variables to track total messages sent and store all messages
    public static int totalMessagesSent = 0;
    public static List<Message> messageList = new ArrayList<>();
    
    private String messageID;
    private int messageNumber;
    private String recipient;
    private String message;
    private String messageHash;

    // Constructor initializes the message and generates ID and hash
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

    // Checks that the message ID is not null and exactly 10 digits
    public boolean checkMessageID() {
        return messageID != null && messageID.length() == 10;
    }

    // Validates the recipient phone number: must be <=13 characters and start with '+'
    public boolean checkRecipientCell() {
        return recipient != null && recipient.matches("\\+27\\d{9}");
    }

    // Creates a hash representation of the message using ID, number, and content
    public String createMessageHash() {
        String firstTwo = messageID.substring(0, 2);
        String[] words = message.trim().split("\\s+");
        String firstWord = words.length > 0 ? words[0].toUpperCase() : "";
        String lastWord = words.length > 1 ? words[words.length - 1].toUpperCase() : firstWord;
        return firstTwo + ":" + messageNumber + ":" + firstWord + lastWord;
    }

    // Returns a formatted String with message details
    public String printMessage() {
        return "Message ID: " + messageID +
                "\nMessage Hash: " + messageHash +
                "\nRecipient: " + recipient +
                "\nMessage: " + message;
    }

    // Getter methods
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


    
    
    // Converts this Message object into a JSON string using Gson
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
    // Static method to recreate a Message object from JSON string
    public static Message fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Message.class);
    }
    
    // Stores the current message object to a JSON file
    public boolean writeMessageToFile(String fileName) {
        Gson gson = new Gson(); 
        try (FileWriter writer = new FileWriter(fileName)) {
                writer.write(gson.toJson(this));
                return true;
        } catch (IOException e) {
            return false;
        }
    }
    
    // Wrapper with GUI – use only inside runApp()
    public void storeMessageToFileWithDialog() {
        String fileName = "message_" + messageID + ".json";
        if (writeMessageToFile(fileName)) {
            JOptionPane.showMessageDialog(null, "Message stored to file:\n" + fileName);
        } else {
            JOptionPane.showMessageDialog(null, "Error saving message.");
        }
    }
    
    

    
    // Runs the interactive app with menu options
    public static void runApp() {
        boolean running = true;
        while (running) {
            String menuOption = JOptionPane.showInputDialog(null,
                    "Choose an option:"
                            + "\n1) Send Messages"
                            + "\n2) Show Recently Sent Messages"
                            + "\n3) Quit",
                    "QuickChat Menu", JOptionPane.QUESTION_MESSAGE);

            if (menuOption == null) { // Cancel pressed
                break;
            }

            switch (menuOption) {
                case "1":
                    sendMessages(); // Trigger message sending process
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

    // Core logic to collect, validate, and send/store messages
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

         // Loop through message 
        for (int i = 1; i <= numMessages; i++) {
            
            String recipient = JOptionPane.showInputDialog("Enter recipient " + i + "'s number (include international code, max 13 chars):");
            if (recipient == null) return;
            
            if (!recipient.matches("\\+27\\d{9}")) {
                JOptionPane.showMessageDialog(null,
                        "Invalid cell phone number.\nMust start with +27 and be followed by exactly 9 digits.\nExample: +27831234567");
                i--; // Retry this message
                continue;
            }
           


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
                    message.storeMessageToFileWithDialog();
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
