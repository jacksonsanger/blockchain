package blockchain;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ReadHandler implements Runnable {
    private Socket clientSocket;
    private BCNode node;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
  
    // Constructor accepts input stream directly
    public ReadHandler(Socket clientSocket, BCNode node, ObjectInputStream ois, ObjectOutputStream oos) {
        this.clientSocket = clientSocket;
        this.node = node;
        this.ois = ois;
        this.oos = oos;
    }

    @Override
    public void run() {
        boolean running = true;
        try {
            // Wait for incoming blocks
            while (running) {
                Block receivedBlock = (Block) ois.readObject();  // blocking
                

                // Validate and add the block to the blockchain
                node.validateAndAddBlock(receivedBlock);
            }
        } catch (ClassNotFoundException e) {
            System.err.println("Error in ReadHandler: ");
            e.printStackTrace();
        } catch (IOException e) {
        	//if we get an io exception, we will tell our thread handler to stop running and remove the output stream from our list
        	running = false;
        	node.removeOutputStream(oos);
        	
        }
    }
}




