# Chat Application (Group Chat, Java Sockets)

This is a console-based group chat application built with Java sockets.

It has two modules:

- `server_side`: runs the chat server
- `client_side`: runs chat clients

All messages are broadcast to all connected clients.

## What Is Happening

1. Server starts and listens on a TCP port (default `5055`).
2. Server prints local IPv4 addresses so you know what IP clients should use.
3. Multiple clients connect to the same server IP and port.
4. Any message from a client is broadcast to everyone.
5. If a client sends `quit`, server sends `quited` and all connected clients exit.

## How It Is Happening

### Server behavior

- Uses `ServerSocket` to accept incoming connections.
- Stores each connection output stream in a hashtable.
- Starts one `ServerThread` per connected client.
- Each thread reads incoming UTF messages in format:
  - `sender content...`
- Broadcasts each normal message to all clients.

### Client behavior

- Connects to server host and port.
- Starts one background receive thread.
- Main thread reads terminal input and sends:
  - `name content...`
- Prints all received messages as:
  - `sender:content`

## Message Protocol

- Normal message:
  - `sender content...`
  - Example: `Bob hello everyone`
- Exit message from client:
  - `sender quit`
- Server shutdown notification to all clients:
  - `quited`

## Project Structure

```
chat-application-master/
  client_side/
    src/chat_app/Client.java
    bin/
  server_side/
    src/chat_app/Server.java
    bin/
```

## Requirements

- Java JDK 8 or newer
- Terminal/console access

Check Java:

```bash
javac -version
java -version
```

## Build

From project root:

```bash
cd server_side
javac -d bin src/chat_app/Server.java

cd ../client_side
javac -d bin src/chat_app/Client.java
```

## Run

### 1. Start server

```bash
cd server_side
java -cp bin chat_app.Server
```

Or custom port:

```bash
java -cp bin chat_app.Server 5055
```

Server startup now prints connect hints, for example:

```text
Server started.
Clients can connect using one of these local IPv4 addresses:
- 192.168.1.20:5055
Listening on port 5055
```

### 2. Start clients

Client command format:

```text
java -cp bin chat_app.Client <name> [server_host] [server_port]
```

The exact style you asked for:

```text
java -cp bin chat_app.Client Bob 192.168.1.20 5055
```

More examples:

```text
java -cp bin chat_app.Client Alice
java -cp bin chat_app.Client Alice 192.168.1.20
java -cp bin chat_app.Client Alice 192.168.1.20 5055
```

## LAN and Internet Usage

### LAN (same network)

1. Run server on machine A.
2. Note the IP shown by server (example `192.168.1.20`).
3. Run clients on any machine in same LAN using that IP.

### Internet (different networks)

1. Run server on a machine with stable internet access.
2. Open firewall for TCP port `5055` (or your chosen port).
3. Configure router port forwarding to server machine.
4. Remote clients connect to your public IP.

## Known Limitations

- No authentication.
- No encryption (plaintext traffic).
- If one user sends `quit`, all users are forced to exit.
- No user list or join/leave notifications.

## Troubleshooting

### `ClassNotFoundException`

- Re-compile first.
- Ensure classpath points to `bin`.

### `Address already in use: bind`

- Another process is already using the port.
- Stop it or choose another port.

### Cannot connect from another machine

- Verify server IP and port.
- Check firewall rules.
- Check router port forwarding for internet access.

## License

No license file is currently included.
