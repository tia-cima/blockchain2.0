# Blockchain Project

This repository contains three interconnected applications that work together to create and maintain a simple blockchain-based system. The applications include:

1. **Blockchain**: A full node responsible for managing the blockchain, handling peer-to-peer communication, and exposing REST endpoints for monitoring.
2. **Miner**: An application dedicated to mining blocks for the blockchain by solving Proof-of-Work (PoW).
3. **Transactions**: An application for creating and broadcasting transactions to the blockchain network.

This is an evolution of the previous blockchain application, which handles the communication over REST and the miner is written in Python. 
For a better communication, serialization and compatibility, every component is now written in Java 21 with Spring Boot and Netty.

---

## Features

This is a very solid blockchain implementation. Each *blockchain* application is a full node which handles the blockchain
updates, connection with other peers, transaction acceptance and broadcast.

For adding blocks into the blockchain, a PoW is required (more details in the following document). Once the miner produces
a correct block, is sent to the full node for further validation and if its valid it will be added to the blockchain. 

All the communications are handled using Netty over TCP protocol. The full node acts as both server and client, while miner and transaction generator
only as a client, but the technology behind doesn't change.

The full node, on bootstrap, if not set as genesis node will act as client and ask to other peers for the full blockchain and the mempool
where transaction are saved. If is a genesis node, no other nodes are currently online in the network and will create a genesis block and an initial 
transaction.

---

## Project Structure

### 1. Blockchain
- **Description**: This application manages the blockchain and mempool, handles peer-to-peer communication with other nodes, and provides REST APIs for monitoring.
- **Key Features**:
    - Maintains the blockchain and validates blocks.
    - Handles transactions and propagates them across the network.
    - Exposes REST endpoints for monitoring.

#### REST Endpoints
- **GET** `/api/mempool`
    - Description: Fetches the current mempool containing pending transactions.
    - Example Response:
      ```json
      [
          {
              "sender": "user1",
              "recipient": "user2",
              "amount": 50.5,
              "data": "Transaction note",
              "timestamp": "2024-12-29T12:34:56Z"
          }
      ]
      ```

- **GET** `/api/blockchain`
    - Description: Fetches the current state of the blockchain.
    - Example Response:
      ```json
      [
          {
              "index": 0,
              "timestamp": "2024-12-28T12:00:00Z",
              "previousHash": "0",
              "hash": "0000abcd1234",
              "nonce": 12345,
              "difficulty": 4,
              "transactions": []
          },
          {
              "index": 1,
              "timestamp": "2024-12-29T12:34:56Z",
              "previousHash": "0000abcd1234",
              "hash": "0000efgh5678",
              "nonce": 67890,
              "difficulty": 4,
              "transactions": [
                  {
                      "sender": "user1",
                      "recipient": "user2",
                      "amount": 50.5,
                      "data": "Transaction note",
                      "timestamp": "2024-12-29T12:34:56Z"
                  }
              ]
          }
      ]
      ```

### 2. Miner
- **Description**: This application mines new blocks by solving Proof-of-Work (PoW) challenges. It connects to the blockchain node to submit mined blocks and fetch transactions from the mempool.
- **Key Features**:
    - Fetches pending transactions from the blockchain.
    - Mines blocks by solving PoW.
    - Submits mined blocks to the blockchain node.
    - The PoW accepts every hash with a leading DIFFICULTY number of zeros 

### 3. Transactions
- **Description**: This application adds each second a transaction to the blockchain network.
- **Key Features**:
    - Creates transactions with sender, recipient, amount, and optional data.
    - Broadcasts transactions to the blockchain nodes.

---

## Getting Started

### Prerequisites
- Java 21
- Maven

### Running the Applications

#### Blockchain Node
1. Navigate to the `blockchain` directory.
2. Build and run the application:
    ```bash
    mvn clean install
    java -jar target/blockchain-1.0.0.jar
    ```
3. The application will start a server and connect to peers listed in the configuration.

#### Miner
1. Navigate to the `miner` directory.
2. Build and run the application:
    ```bash
    mvn clean install
    java -jar target/miner-1.0.0.jar
    ```

#### Transactions
1. Navigate to the `transactions` directory.
2. Build and run the application:
    ```bash
    mvn clean install
    java -jar target/transactions-1.0.0.jar
    ```

P.S the execution order is very important, because executing the miner before the full node is obviously not correct.

---

## Configuration

### Blockchain Node
- Configure the list of peers in `application.properties`:
    ```properties
    blockchain.peers=localhost:2999,localhost:3000
    ```
- Optionally, set the node as the genesis node (default value is true, but if a GENESIS=false env var exists will be false):

### Miner and Transactions
- Update the `application.properties` file with the blockchain node's address:
    ```properties
    blockchain.node-host=localhost
    blockchain.node-port=2999
    ```

---

## License
This project is licensed under the MIT License. See the LICENSE file for details.

---

## Next steps
Currently, some feature are missing like blockchain persistence, transaction validation and automatic host discover. 
Those will be implemented in the future.
