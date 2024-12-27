package blockchain;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BCNode {
    private ArrayList<Block> blockchain;
    private int numZeros;
    private String prefixZeros;
    private ServerSocket serverSocket;
    private List<Socket> remoteNodeSockets;
    private List<ObjectOutputStream> outputStreams;

    // Constructor for BCNode: starts with the Genesis Block or connects to an existing blockchain
    public BCNode(int myPort, List<Integer> remotePorts) {
        this.blockchain = new ArrayList<>();
        this.numZeros = 5;  // Hardcode 5 leading zeros for proof of work
        this.prefixZeros = new String(new char[numZeros]).replace('\0', '0');
        this.remoteNodeSockets = new ArrayList<>();
        this.outputStreams = new ArrayList<>();

        try {
            // Step 1: Start the server to listen for incoming connections in a new thread
            serverSocket = new ServerSocket(myPort);
            System.out.println("BCNode listening on port: " + myPort);
            new Thread(new ConnectionHandler(serverSocket, this)).start();

            // Step 2: Connect to remote nodes and fetch the blockchain
            if (!remotePorts.isEmpty()) {
                connectToRemoteNodes(remotePorts);  // Connects to other nodes and fetches the blockchain
            } else {
                // If this is the first node, create the genesis block
                Block genesisBlock = new Block();  // Genesis block constructor (default)
                blockchain.add(genesisBlock);
            }

        } catch (IOException e) {
            System.out.println("Error initializing BCNode on port: " + myPort);
            e.printStackTrace();
        }
    }

    private void connectToRemoteNodes(List<Integer> remotePorts) {
        for (Integer port : remotePorts) {
            try {
                Socket socket = new Socket("localhost", port);  // Assuming all nodes are on localhost
                remoteNodeSockets.add(socket);
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                outputStreams.add(oos);
                
                // Fetch the blockchain from the remote node using the existing stream
                fetchBlockchainFromRemote(ois); 
                
                // Start a read handler to listen for future blocks from this node
                new Thread(new ReadHandler(socket, this, ois, oos)).start();
                
                System.out.println("Connected to node on port: " + port);
            } catch (IOException e) {
                System.err.println("Could not connect to node on port: " + port);
                e.printStackTrace();
            }
        }
    }

 // Fetch the blockchain from the first remote node
    private void fetchBlockchainFromRemote(ObjectInputStream ois) {
        if (!remoteNodeSockets.isEmpty()) {
            try {
                // Retrieve the blockchain from the remote node
                ArrayList<Block> remoteBlockchain = (ArrayList<Block>) ois.readObject();
                this.blockchain = remoteBlockchain;

                System.out.println("Successfully retrieved blockchain from a remote node.");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error fetching blockchain from remote node.");
                e.printStackTrace();
            }
        }
    }


    // Method to add a new block to the chain and send it to other nodes
    public void addBlock(Block newBlock) {
        // Get the hash of the last block in the blockchain
        Block lastBlock = blockchain.get(blockchain.size() - 1);
        newBlock.setPreviousHash(lastBlock.getHash());

        // Mine the block (Proof of Work)
        mineBlock(newBlock);
        
        //this method adds to the chain, validates it, and then keeps it if it is valid and removes if not
        validateAndAddBlock(newBlock);
    }


    // Mining: perform Proof of Work by adjusting the nonce until the hash matches numZeros of leading 0s
    private void mineBlock(Block block) {
        while (!block.getHash().substring(0, numZeros).equals(prefixZeros)) {
            block.setNonce(block.getNonce() + 1);   // Increment nonce
            block.setHash(block.calculateBlockHash()); // Recalculate the hash
        }
    }

    // Send the new block to all connected nodes
    private synchronized void sendBlockToAllNodes(Block block) {
        for (ObjectOutputStream oos : outputStreams) {
            try {
                oos.writeObject(block);  // Send the block
                oos.reset();
            } catch (IOException e) {
                System.out.println("Error sending block to a node.");
                e.printStackTrace();
            }
        }
    }


    // Validate the received block and add it if valid
    public synchronized void validateAndAddBlock(Block block) {
        // Check if the previous hash matches the last block's hash and if the block is mined correctly
        blockchain.add(block);
        // Validate the chain after adding the new block
        if (isChainValid()) {
            System.out.print("Local Blockchain is valid after adding the new block. Propagating block.\nEnter Option: ");
            // Send the new block to all connected nodes
            sendBlockToAllNodes(block);
        } else {
            System.out.print("Local Blockchain is invalid after adding the new block. Discarding block\nEnter option: ");
            blockchain.remove(blockchain.size() -1);
            return; // Do not propagate if the chain becomes invalid
        }
    }


    // Method to validate the blockchain
    public boolean isChainValid() {
        // Loop through all the blocks in the chain to validate each one
        for (int i = 1; i < blockchain.size(); i++) {
            Block currentBlock = blockchain.get(i);
            Block previousBlock = blockchain.get(i - 1);

            // Check if the current block's hash is valid
            if (!currentBlock.getHash().equals(currentBlock.calculateBlockHash())) {
                return false; // Block's hash is not correct
            }

            // Check if the previous hash in the current block matches the hash of the previous block
            if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                return false; // Chain is broken
            }

            // Check if the block was mined correctly (matches the correct # of leading 0s for POW)
            if (!currentBlock.getHash().substring(0, numZeros).equals(prefixZeros)) {
                return false; // Proof of Work not satisfied
            }
        }
        return true; // Blockchain is valid
    }

    // toString method to print the entire blockchain
    @Override
    public String toString() {
        String result = "";
        for (Block block : blockchain) {
            result += block.toString() + "\n"; // Concatenate block info
        }
        return result;
    }
    
    // Getter for blockchain
    public ArrayList<Block> getBlockchain() {
        return this.blockchain;
    }

    // Adds an output stream to the list
    public synchronized void addOutputStream(ObjectOutputStream oos) {
        if (oos != null) {
            this.outputStreams.add(oos);
        } else {
            System.out.println("Output stream is null.");
        } 
    }
    
    //removes output stream from list
    public synchronized void removeOutputStream(ObjectOutputStream oos) {
    	this.outputStreams.remove(oos);
    	System.out.print("Error in one of neighbor nodes, removing node from output streams.\nEnter Option: ");
    }
    
    // main exists inside of BCNode
    public static void main(String[] args) {
        Scanner keyScan = new Scanner(System.in);
        
        // Grab my port number on which to start this node
        System.out.print("Enter port to start (on current IP): ");
        int myPort = keyScan.nextInt();
        
        // Need to get what other Nodes to connect to
        System.out.print("Enter remote ports (current IP is assumed): ");
        keyScan.nextLine(); // skip the NL at the end of the previous scan int
        String line = keyScan.nextLine();
        List<Integer> remotePorts = new ArrayList<Integer>();
        if (line != "") {
            String[] splitLine = line.split(" ");
            for (int i=0; i<splitLine.length; i++) {
                remotePorts.add(Integer.parseInt(splitLine[i]));
            }
        }
        // Create the Node
        BCNode n = new BCNode(myPort, remotePorts);
        
        String ip = "";
        try {
             ip = Inet4Address.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }
        
        System.out.println("Node started on " + ip + ": " + myPort);
        
        // Node command line interface
        while(true) {
            System.out.println("\nNODE on port: " + myPort);
            System.out.println("1. Display Node's blockchain");
            System.out.println("2. Create/mine new Block");
            System.out.println("3. Kill Node");
            System.out.print("Enter option: ");
            int in = keyScan.nextInt();
            
            if (in == 1) {
                System.out.println(n);
                
            } else if (in == 2) {
                // Grab the information to put in the block
                System.out.print("Enter information for new Block: ");
                String blockInfo = keyScan.next();
                Block b = new Block(blockInfo);
                n.addBlock(b);
                
            } else if (in == 3) {
                // Take down the whole virtual machine (and all the threads)
                //   for this Node.  If we just let main end, it would leave
                //   up the Threads the node created.
                keyScan.close();
                System.exit(0);
            }
        }
    }
}
