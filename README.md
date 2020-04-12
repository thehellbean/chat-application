# Chat Application

This is a basic centralized chat application. The repo contains source code for both a client and a server.

The server's main class is ChatServer. To run it, simply compile the server files and run `java ChatServer`

The client's main class is ChatClient. To run it, compile the client files and run `java ChatClient <server IP>`

The functionality supported is open messaging to everyone on the server, private messaging, and sending files.
To send a message to the entire server, simply enter the message.
To send a message to a single person, write `/msg:<username>`
To send a file, write `/msg:<username>:<path>`. Note that the path will be relative to the directory you are running the client from.

File transfers are done peer-to-peer, with the server transmitting the IP and port address so that a socket connection can be opened.
