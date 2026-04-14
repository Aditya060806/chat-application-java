# Chat Application (Java Broadcast Chat)
<img width="1116" height="930" alt="image" src="https://github.com/user-attachments/assets/9f9c8a21-b695-4e60-be8d-32af9f8b93c8" />


Simple Java socket chat with:

1. One server
2. Multiple GUI clients
3. Broadcast messaging (everyone sees everyone)

## Project Structure

```text
chat-application-master/
  server_side/
    src/chat_app/Server.java
    bin/
  client_side/
    src/chat_app/Client.java
    bin/
```

## Requirements

1. Java JDK 8+
2. Terminal on Windows/macOS/Linux

Check Java:

```bash
javac -version
java -version
```

## How It Works

1. Server listens on a port (default 5055).
2. Each client connects with name + host + port.
3. Every normal message is broadcast to all connected users.
4. Server sends status messages as sender Server:
   1. user joined
   2. user left
   3. online users for newly joined client
5. In the client UI, your own name in server status messages is shown as you.

## Build

From project root:

```bash
cd server_side
javac -d bin src/chat_app/Server.java

cd ../client_side
javac -d bin src/chat_app/Client.java
```

## Run (LAN)

### 1) Start Server

```bash
cd server_side
java -cp bin chat_app.Server 5055
```

Server prints local IP hints. Use one of those IPs from other devices.

### 2) Start Clients

```bash
cd client_side
java -cp bin chat_app.Client <name> <server_host> <server_port>
```

Example:

```bash
java -cp bin chat_app.Client Ranbir 192.168.1.20 5055
java -cp bin chat_app.Client Suman 192.168.1.20 5055
```

## Run (Public Internet via Pinggy)

### 1) Start server normally

```bash
cd server_side
java -cp bin chat_app.Server 5055
```

### 2) In another terminal, start tunnel

```bash
ssh -o StrictHostKeyChecking=accept-new -p 443 -R0:localhost:5055 tcp@a.pinggy.io
```

You will get a TCP address like:

```text
tcp://something.run.pinggy-free.link:39345
```

### 3) Start clients with tunnel host and port

```bash
cd client_side
java -cp bin chat_app.Client Ranbir something.run.pinggy-free.link 39345
java -cp bin chat_app.Client Suman  something.run.pinggy-free.link 39345
```
## A screenshot of server:
<img width="697" height="211" alt="image" src="https://github.com/user-attachments/assets/9cc0eca4-8840-4777-899d-6f5c7e79241f" />


## Quit Behavior

1. If a user sends quit, only that user leaves.
2. Server keeps running.
3. Others get a server message that the user left.

## Common Issues

### Address already in use

Port already in use. Stop existing process or run on another port.

### Could not connect

1. Check host and port.
2. Ensure server is running.
3. If using Pinggy, ensure tunnel is running and not expired.
