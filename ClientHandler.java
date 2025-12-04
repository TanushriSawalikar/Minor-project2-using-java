package com.chatapp.server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {

    // List to hold all connected clients for broadcasting
    private static List<ClientHandler> handlers = new ArrayList<>();

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private String clientName;

    public ClientHandler(Socket socket) {
        try {
            this.clientSocket = socket;
            // Output stream to send data to the client
            this.out = new PrintWriter(clientSocket.getOutputStream(), true);
            // Input stream to read data from the client
            this.in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Prompt client for a name (this is a simple example)
            this.out.println("SERVER: Welcome! Please enter your name:");
            this.clientName = in.readLine();
            System.out.println("New user identified as: " + clientName);

            // Add this new handler to the shared list
            handlers.add(this);
            broadcastMessage("SERVER: " + clientName + " has joined the chat.");

        } catch (IOException e) {
            closeEverything(socket, in, out);
        }
    }

    // Main thread execution logic
    @Override
    public void run() {
        String messageFromClient;

        while (clientSocket.isConnected()) {
            try {
                // Keep reading messages from this specific client
                messageFromClient = in.readLine();
                if (messageFromClient == null) { // Handle disconnection
                    break;
                }
                broadcastMessage(clientName + ": " + messageFromClient);

            } catch (IOException e) {
                closeEverything(clientSocket, in, out);
                break;
            }
        }
    }

    // Method to send a message to all other connected clients
    public void broadcastMessage(String messageToSend) {
        for (ClientHandler handler : handlers) {
            // Check if the handler is not the sender
            if (handler != this) {
                handler.out.println(messageToSend);
            }
        }
    }

    // Method to gracefully close connection and remove from list
    public void closeEverything(Socket socket, BufferedReader reader, PrintWriter writer) {
        // Remove the handler from the list before closing
        handlers.remove(this);
        broadcastMessage("SERVER: " + clientName + " has left the chat.");
        System.out.println(clientName + " has disconnected.");
        try {
            if (reader != null) reader.close();
            if (writer != null) writer.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}