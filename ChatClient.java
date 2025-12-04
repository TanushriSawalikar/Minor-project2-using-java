package com.chatapp.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {

    private static final String SERVER_IP = "localhost"; // Use "localhost" for local testing
    private static final int PORT = 7000;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Scanner scanner;

    public ChatClient() {
        this.scanner = new Scanner(System.in);
    }

    public void startClient() {
        try {
            // 1. Connect to the server
            socket = new Socket(SERVER_IP, PORT);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Connected to the server. Start chatting!");

            // 2. Start a separate thread to continuously read messages from the server
            new Thread(new ServerListener()).start();

            // 3. Main thread handles user input and sends messages
            sendMessages();

        } catch (IOException e) {
            System.err.println("Could not connect to server or connection lost.");
            closeEverything();
        }
    }

    // Inner class (Runnable) for listening to the server
    private class ServerListener implements Runnable {
        @Override
        public void run() {
            try {
                String serverResponse;
                while ((serverResponse = in.readLine()) != null) {
                    System.out.println(serverResponse);
                }
            } catch (IOException e) {
                // Connection closed or error occurred
                System.out.println("Connection to server lost.");
            } finally {
                closeEverything();
            }
        }
    }

    // Method to read input from the console and send to the server
    private void sendMessages() {
        String userInput;
        while (socket.isConnected()) {
            userInput = scanner.nextLine();
            out.println(userInput); // Send the message to the server
        }
    }

    private void closeEverything() {
        try {
            if (scanner != null) scanner.close();
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new ChatClient().startClient();
    }
}