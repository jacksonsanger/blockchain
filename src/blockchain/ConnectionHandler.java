package blockchain;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.io.*;

public class ConnectionHandler implements Runnable {
    private ServerSocket serverSocket;
    private BCNode node;  // Reference to the BCNode

    public ConnectionHandler(ServerSocket serverSocket, BCNode node) {
        this.serverSocket = serverSocket;
        this.node = node;
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Accept incoming connections
                Socket clientSocket = serverSocket.accept();
                //uncomment for debugging, commented out because it screws up user interface
                //System.out.println("Node connected: " + clientSocket.getInetAddress());

                // Set up input/output streams for communication
                ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream());
                //add the output stream to this node so we have a two way street
                node.addOutputStream(oos);
                ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());

              
                // Send the entire blockchain to the connected node
                oos.writeObject(node.getBlockchain());
                oos.reset();

                // Pass the input stream to ReadHandler to handle block reading
                new Thread(new ReadHandler(clientSocket, node, ois, oos)).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}




