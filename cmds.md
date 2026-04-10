Server (run on the host machine that will accept connections):
java -cp server_side/bin chat_app.Server 5055

Client 1 (run on any device with Java + client code):
cd client_side
java -cp bin chat_app.Client Ranbir 10.24.109.81 5055

Client 2 (run on any other device):
cd client_side
java -cp bin chat_app.Client Suman 10.24.109.81 5055