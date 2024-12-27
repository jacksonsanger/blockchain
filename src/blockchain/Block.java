package blockchain;

import java.security.MessageDigest;
import java.util.Date;
import java.io.Serializable;

public class Block implements Serializable {
    private static final long serialVersionUID = 1L;
	private String data;       // Data stored in the block
    private long timestamp;    // Time when block was created
    private int nonce;         // Number used to find a valid hash during mining
    private String hash;       // Hash of this block
    private String previousHash; // Hash of the previous block in the chain

    // Constructor for a block with data
    public Block(String data) {
        this.data = data;
        this.timestamp = new Date().getTime();
        this.nonce = 0; // Nonce starts at 0
        this.previousHash = ""; // Initialize to empty string
        this.hash = calculateBlockHash(); // Calculate the hash for this block
    }

    // Genesis Block Constructor (default)
    public Block() {
        this.data = "Genesis Block";
        this.timestamp = new Date().getTime();
        this.nonce = 0; // Nonce starts at 0
        this.previousHash = ""; // Initialize to empty string
        this.hash = calculateBlockHash(); // Calculate the hash for this block
    }

    // Method to calculate the hash of the block
    public String calculateBlockHash() {
        // Create the combined data to hash
        String dataToHash = previousHash + Long.toString(timestamp) + Integer.toString(nonce) + data;

        MessageDigest digest;
        byte[] hashBytes = null;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            hashBytes = digest.digest(dataToHash.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        StringBuffer buffer = new StringBuffer();
        for (byte b : hashBytes) {
            buffer.append(String.format("%02x", b));
        }
        return buffer.toString();
    }

    // Getters and Setters
    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    @Override
    // Printing the block's data members
    public String toString() {
        return "Block {" +
                "data='" + data + '\'' +
                '}';
    }
}
