# Simplified Blockchain Network in Java

## Overview

This project implements a simplified blockchain network in Java, featuring core blockchain concepts like cryptographic hashing, proof-of-work mining, and block validation. The network is designed as a peer-to-peer distributed system, utilizing Java sockets and a multithreaded architecture to manage connections and propagate blocks across nodes. Each node maintains its own copy of the blockchain, ensuring decentralization and resilience.

### Single-Node Blockchain
- **Blockchain Structure**: Each block contains data, a timestamp, a nonce, its hash, and the hash of the previous block, forming a secure chain.
- **Proof-of-Work (PoW) Mining**: Implements computational difficulty by requiring block hashes to meet a specific criteria (e.g., leading zeros).
- **Validation**: Ensures block integrity through hash verification and chaining rules.
- **Genesis Block**: Automatically initializes each blockchain.

### Peer-to-Peer Network
- **Socket-Based Communication**: Nodes communicate using persistent Java sockets and ObjectStreams.
- **Multithreaded Connection Management**: Separate threads handle incoming connections (`ConnectionHandler`) and read data from peers (`ReadHandler`), enabling simultaneous communication with multiple nodes.
- **Distributed Ledger**: Each node validates and updates its blockchain independently while synchronizing with connected peers.
- **Block Propagation**: Mined blocks are shared across the network, ensuring consensus and consistency.
- **Fault Tolerance**: Handles node disconnection gracefully to ensure continued network operations.

### User Interface
- Command-line interface for each node:
  - Display the blockchain.
    - Create and mine new blocks.
      - Gracefully stop the node.

## Implementation Details

- **Hashing**: Uses SHA-256 for cryptographic security.
- **Mining**: Incrementally adjusts the nonce to achieve a valid hash.
- **Networking**: Implements a server-client architecture using Java sockets.
- **Multithreading**:
  - `ConnectionHandler` thread manages new socket connections.
    - `ReadHandler` threads handle incoming data streams, validating and propagating blocks in real-time.
- **Synchronization**: Ensures thread-safe operations on shared resources like the blockchain.

## How to Run

1. **Compile the Code**: Ensure all source files are compiled and available.
2. **Start a Node**:
   - Run the `main` method in `BCNode`.
      - Provide a port number and a list of remote ports to connect to (if any).
      3. **Interact with the Node**:
         - Follow the menu prompts to display the blockchain, create/mine new blocks, or stop the node.
	 4. **Test with Multiple Nodes**:
	    - Start several nodes on different ports to form a network.
	       - Test block propagation and consensus by adding blocks from different nodes.

## Key Classes and Responsibilities

### `Block`
- Holds block data, timestamp, nonce, and cryptographic hashes.
- Computes block hashes using SHA-256.
- Includes methods for mining, validation, and serialization.

### `BCNode`
- Maintains the blockchain and handles node-specific operations.
- Adds and validates blocks.
- Manages network connections and propagates blocks.

### `ConnectionHandler`
- Manages incoming socket connections in a separate thread.
- Initializes streams for communication with new nodes.

### `ReadHandler`
- Listens for incoming blocks from connected nodes in individual threads.
- Validates and propagates blocks across the network.

## Testing Scenarios
- **Blockchain Integrity**: Validate blocks to ensure no tampering occurs.
- **Block Propagation**: Ensure new blocks propagate to all nodes in the network.
- **Simultaneous Mining**: Test nodes racing to mine blocks; verify only one block is accepted.
- **Node Failures**: Ensure the network remains operational when nodes disconnect.
- **Race Conditions**: Identify and handle potential synchronization issues during concurrent operations.

## Technologies Used
- **Programming Language**: Java
- **Networking**: Java Sockets and ObjectStreams
- **Concurrency**: Java Threads
- **Hashing**: SHA-256 with `MessageDigest`

## Future Improvements
- Implement a more sophisticated consensus algorithm for resolving conflicting blocks.
- Introduce mechanisms for dynamically discovering new nodes in the network.
- Add transaction functionality to simulate real-world blockchain applications.