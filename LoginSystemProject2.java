package com.mycompany.loginsystemproject2;

import java.util.*;
import java.io.*;
import java.util.regex.Pattern;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class LoginSystemProject2 {

    private static boolean exit = false;
    private static int maxMessages = 0;
    private static int totalMessages = 0;
    private static int messageCounter = 0;

    static final JSONArray messageStorage = new JSONArray();
    static Scanner scanner = new Scanner(System.in);

    // Stored login details
    static String storedUsername = "";
    static String storedPassword = "";

    // 1. Five arrays required by rubric - populated by users, no hardcoding
    static ArrayList<String> sentMessages = new ArrayList<>();
    static ArrayList<String> disregardedMessages = new ArrayList<>();
    static ArrayList<JSONObject> storedMessages = new ArrayList<>();
    static ArrayList<String> messageHashes = new ArrayList<>();
    static ArrayList<Long> messageIds = new ArrayList<>();

    // ---------------- MAIN METHOD ----------------
    public static void main(String[] args) {

        System.out.println("Welcome to QuickChat");

        // ---------------- REGISTRATION ----------------
        String username;
        while (true) {
            System.out.print("Enter Username (must contain '_' and max 5 chars): ");
            username = scanner.nextLine();
            if (checkUserName(username)) {
                break;
            }
            System.out.println("Invalid username.");
        }

        String password;
        while (true) {
            System.out.print("Enter Password (8+ chars, capital letter, number, special char): ");
            password = scanner.nextLine();
            if (checkPasswordComplexity(password)) {
                break;
            }
            System.out.println("Invalid password.");
        }

        storedUsername = username;
        storedPassword = password;
        System.out.println(registerUser(username, password));

        // ---------------- CELL NUMBER ----------------
        String cellPhone;
        while (true) {
            System.out.print("Enter Cell Phone (+27 followed by 9 digits): ");
            cellPhone = scanner.nextLine();
            if (checkCellPhoneNumber(cellPhone)) {
                System.out.println("Cell phone number successfully added.");
                break;
            }
            System.out.println("Invalid number.");
        }

        // ---------------- LOGIN ----------------
        boolean status = false;
        while (!status) {
            System.out.print("Enter Username: ");
            String loginUser = scanner.nextLine();
            System.out.print("Enter Password: ");
            String loginPass = scanner.nextLine();
            status = loginUser(loginUser, loginPass, storedUsername, storedPassword);
            System.out.println(returnLoginStatus(status));
        }

        // Load JSON into Stored Messages array at start
        loadStoredMessagesFromJSON();

        // ---------------- MESSAGE LIMIT ----------------
        try {
            System.out.print("How many messages do you wish to send? ");
            maxMessages = Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        // ---------------- MENU - 2. Fourth option added ----------------
        while (!exit && totalMessages < maxMessages) {
            System.out.println("\nSelect an Option:");
            System.out.println("1. Post Message");
            System.out.println("2. Previous Messages");
            System.out.println("3. Exit");
            System.out.println("4. Stored Messages"); // From your picture
            System.out.print("Choice: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice.");
                continue;
            }

            switch (choice) {
                case 1 -> {
                    if (totalMessages < maxMessages) {
                        sendMessage();
                    } else {
                        System.out.println("Maximum messages reached.");
                    }
                }
                case 2 -> showRecentlySentMessages();
                case 3 -> {
                    saveMessagesToJSON();
                    exit = true;
                    System.out.println("Goodbye!");
                }
                case 4 -> storedMessagesMenu(); // 2a-2f from picture
                default -> System.out.println("Invalid choice.");
            }
        }

        if (totalMessages >= maxMessages) {
            System.out.println("Maximum messages reached. Exiting.");
            saveMessagesToJSON();
        }
        scanner.close();
    }

    // ---------------- USERNAME VALIDATION ----------------
    public static boolean checkUserName(String username) {
        return username.contains("_") && username.length() <= 5;
    }

    // ---------------- PASSWORD VALIDATION ----------------
    public static boolean checkPasswordComplexity(String password) {
        String regex = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{8,}$";
        return Pattern.matches(regex, password);
    }

    // ---------------- CELL NUMBER VALIDATION ----------------
    public static boolean checkCellPhoneNumber(String number) {
        return number.matches("^\\+27\\d{9}$");
    }

    // ---------------- REGISTER USER ----------------
    public static String registerUser(String username, String password) {
        if (!checkUserName(username)) {
            return "Username is incorrect.";
        }
        if (!checkPasswordComplexity(password)) {
            return "Password is incorrect.";
        }
        return "User registered successfully!";
    }

    // ---------------- LOGIN USER ----------------
    public static boolean loginUser(String username, String password, String storedUsername, String storedPassword) {
        return username.equals(storedUsername) && password.equals(storedPassword);
    }

    // ---------------- LOGIN STATUS ----------------
    public static String returnLoginStatus(boolean status) {
        if (status) {
            return "Login successful! Welcome back!";
        } else {
            return "Username or password incorrect.";
        }
    }

    // ---------------- SEND MESSAGE ----------------
    static void sendMessage() {
        if (totalMessages >= maxMessages) {
            System.out.println("Maximum messages reached.");
            return;
        }

        long messageId = 1000000L + new Random().nextInt(9000000);
        messageCounter++;

        System.out.print("Input recipient number (+CCXXXXXXXXXXX): ");
        String recipient = scanner.nextLine();
        recipient = checkRecipient(recipient);
        if (recipient == null) {
            return;
        }

        System.out.print("Enter your message (max 250 characters): ");
        String message = scanner.nextLine();
        if (message.length() > 250) {
            System.out.println("Message exceeds 250 characters.");
            return;
        }

        String[] words = message.trim().split("\\s+");
        String hash = String.format("%02d:%d:%s%s",
                Integer.parseInt(Long.toString(messageId).substring(0, 2)),
                messageCounter,
                words[0].toUpperCase(),
                words.length > 1 ? words[words.length - 1].toUpperCase() : "");

        System.out.println("\nSelect action:");
        System.out.println("1. Post");
        System.out.println("2. Cancel");
        System.out.println("3. Archive");

        int action = Integer.parseInt(scanner.nextLine());

        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("MessageID", messageId);
        jsonMessage.put("MessageHash", hash);
        jsonMessage.put("Recipient", recipient);
        jsonMessage.put("Message", message);

        // Add to all 5 tracking arrays
        messageHashes.add(hash);
        messageIds.add(messageId);
        messageStorage.add(jsonMessage);

        if (action == 2) {
            disregardedMessages.add(message); // Disregarded Messages array
            System.out.println("Message cancelled.");
            return;
        }

        if (action == 3) {
            storedMessages.add(jsonMessage); // Stored Messages array
            System.out.println("Message archived.");
            return;
        }

        sentMessages.add(message); // Sent Messages array
        totalMessages++;
        System.out.println("\nMessage Sent.");
        System.out.println("Message ID: " + messageId);
        System.out.println("Message Hash: " + hash);
        System.out.println("Recipient: " + recipient);
        System.out.println("Message: " + message);
    }

    // ---------------- SAVE JSON ----------------
    static void saveMessagesToJSON() {
        try (FileWriter file = new FileWriter("storedMessages.json")) {
            file.write(messageStorage.toJSONString());
            file.flush();
            System.out.println("Messages saved.");
        } catch (IOException e) {
            System.out.println("Error saving file.");
        }
    }

    // Read JSON file into Stored Messages array
    static void loadStoredMessagesFromJSON() {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader("storedMessages.json")) {
            JSONArray arr = (JSONArray) parser.parse(reader);
            for (Object obj : arr) {
                JSONObject msg = (JSONObject) obj;
                storedMessages.add(msg);
                messageHashes.add((String) msg.get("MessageHash"));
                messageIds.add((Long) msg.get("MessageID"));
            }
        } catch (FileNotFoundException e) {
            // First run
        } catch (IOException | ParseException e) {
            System.out.println("Error loading JSON: " + e.getMessage());
        }
    }

    // ---------------- RECIPIENT VALIDATION ----------------
    private static String checkRecipient(String recipient) {
        if (recipient == null || !recipient.matches("^\\+\\d{9,12}$")) {
            System.out.println("Invalid number.");
            return null;
        }
        return recipient;
    }

    // ---------------- SHOW MESSAGES ----------------
    static void showRecentlySentMessages() {
        if (messageStorage.isEmpty()) {
            System.out.println("No stored messages.");
        } else {
            System.out.println("Stored Messages:");
            for (int i = 0; i < messageStorage.size(); i++) {
                System.out.println(messageStorage.get(i));
            }
        }
    }

    // ------------------ 2. Stored Messages Menu a-f from your picture ------------------
    static void storedMessagesMenu() {
        System.out.println("\n--- Stored Messages Menu ---");
        System.out.println("a. Display sender and recipient of all stored messages");
        System.out.println("b. Display the longest stored message");
        System.out.println("c. Search for a message ID and display recipient + message");
        System.out.println("d. Search all messages stored for a particular recipient");
        System.out.println("e. Delete a message using the message hash");
        System.out.println("f. Display report of all stored messages");
        System.out.print("Choice: ");

        String choice = scanner.nextLine().toLowerCase();

        switch (choice) {
            case "a" -> displaySenderRecipient();
            case "b" -> displayLongestMessage();
            case "c" -> searchByMessageId();
            case "d" -> searchByRecipient();
            case "e" -> deleteByHash();
            case "f" -> displayReport();
            default -> System.out.println("Invalid choice.");
        }
    }

    // 2a. Display sender and recipient
    static void displaySenderRecipient() {
        if (storedMessages.isEmpty()) {
            System.out.println("No stored messages.");
            return;
        }
        System.out.println("\nStored Messages - Sender & Recipient:");
        for (JSONObject msg : storedMessages) {
            System.out.println("Recipient: " + msg.get("Recipient") + " | Message: " + msg.get("Message"));
        }
    }

    // 2b. Display longest stored message - matches your test data Message 2
    static void displayLongestMessage() {
        if (storedMessages.isEmpty()) {
            System.out.println("No stored messages.");
            return;
        }
        JSONObject longest = storedMessages.get(0);
        for (JSONObject msg : storedMessages) {
            String text = (String) msg.get("Message");
            if (text.length() > ((String) longest.get("Message")).length()) {
                longest = msg;
            }
        }
        System.out.println("\nLongest Message: " + longest.get("Message"));
    }

    // 2c. Search by Message ID - matches test: message 4
    static void searchByMessageId() {
        System.out.print("Enter Message ID: ");
        try {
            long id = Long.parseLong(scanner.nextLine());
            for (JSONObject msg : storedMessages) {
                if ((Long) msg.get("MessageID") == id) {
                    System.out.println("Recipient: " + msg.get("Recipient"));
                    System.out.println("Message: " + msg.get("Message"));
                    return;
                }
            }
            for (int i = 0; i < messageIds.size(); i++) {
                if (messageIds.get(i) == id) {
                    System.out.println("Recipient: Sent Message");
                    System.out.println("Message: " + sentMessages.get(i));
                    return;
                }
            }
            System.out.println("Message ID not found.");
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID.");
        }
    }

    // 2d. Search all messages for a particular recipient - matches test: +2783884567
    static void searchByRecipient() {
        System.out.print("Enter Recipient number: ");
        String recipient = scanner.nextLine();
        boolean found = false;
        for (JSONObject msg : storedMessages) {
            if (msg.get("Recipient").equals(recipient)) {
                System.out.println("Message: " + msg.get("Message"));
                found = true;
            }
        }
        if (!found) System.out.println("No messages for that recipient.");
    }

    // 2e. Delete by Message Hash - matches test: Message 2
    static void deleteByHash() {
        System.out.print("Enter Message Hash to delete: ");
        String hash = scanner.nextLine();
        for (int i = 0; i < storedMessages.size(); i++) {
            JSONObject msg = storedMessages.get(i);
            if (msg.get("MessageHash").equals(hash)) {
                System.out.println("Message: \"" + msg.get("Message") + "\" successfully deleted.");
                storedMessages.remove(i);
                messageHashes.remove(i);
                messageIds.remove(i);
                messageStorage.remove(i);
                return;
            }
        }
        System.out.println("Hash not found.");
    }

    // 2f. Display report with Message Hash, Recipient, Message columns
    static void displayReport() {
        if (storedMessages.isEmpty() && sentMessages.isEmpty()) {
            System.out.println("No messages to report.");
            return;
        }
        System.out.println("\n--- Report: All Sent + Stored Messages ---");
        System.out.printf("%-15s | %-15s | %s\n", "Message Hash", "Recipient", "Message");
        System.out.println("-------------------------------------------------------------");

        for (JSONObject msg : storedMessages) {
            System.out.printf("%-15s | %-15s | %s\n",
                    msg.get("MessageHash"), msg.get("Recipient"), msg.get("Message"));
        }
        for (int i = 0; i < sentMessages.size(); i++) {
            System.out.printf("%-15s | %-15s | %s\n",
                    messageHashes.get(i), "Sent", sentMessages.get(i));
        }
    }
}