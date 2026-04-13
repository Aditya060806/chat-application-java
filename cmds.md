Server compile (run on host machine):
cd server_side
javac -d bin src/chat_app/Server.java

Server run:
java -cp bin chat_app.Server 5055

Client 1 (run on any device with Java + client code):
cd client_side
javac -d bin src/chat_app/Client.java
java -cp bin chat_app.Client Ranbir 10.24.109.81 5055

Client 2 (run on any other device):
cd client_side
javac -d bin src/chat_app/Client.java
java -cp bin chat_app.Client Suman 10.24.109.81 5055