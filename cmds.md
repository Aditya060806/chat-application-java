# Commands Cheat Sheet

## 1) Compile

Server:
```bash
cd server_side
javac -d bin src/chat_app/Server.java
```

Client:
```bash
cd client_side
javac -d bin src/chat_app/Client.java
```

## 2) Run on Local Network (LAN)

Start server:
```bash
cd server_side
java -cp bin chat_app.Server 5055
```

Start clients (same WiFi/LAN):
```bash
cd client_side
java -cp bin chat_app.Client Ranbir 10.24.xxx.xx 5055
java -cp bin chat_app.Client Suman  10.24.xxx.xx 5055
```

## 3) Run Publicly (Internet) using Pinggy

Start server first:
```bash
cd server_side
java -cp bin chat_app.Server 5055
```

Open another terminal and start tunnel:
```bash
ssh -o StrictHostKeyChecking=accept-new -p 443 -R0:localhost:5055 tcp@a.pinggy.io
```

Tunnel output example:
```text
tcp://something.run.pinggy-free.link:39345
```

Run clients with host + port from tunnel output:
```bash
cd client_side
java -cp bin chat_app.Client Ranbir something.run.pinggy-free.link 39345
java -cp bin chat_app.Client Suman  something.run.pinggy-free.link 39345
```

## 4) Notes

1. quit makes only that user leave.
2. Server continues running.
3. Server sends joined/left and online-user status messages.