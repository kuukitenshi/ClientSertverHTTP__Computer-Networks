# ClientSertverHTTP

This project was a collaborative effort involving two more students for our ```Computer Networks``` subject.

It was implemented in ```Java with version 17.0.4.1.``` 

The objective of the project was to create a ```simple client-server program```, creating a ```multithreaded server``` that receives, processes and response, multiple HTTP requests from the connected clients, allowing a maximum of 5 clients connected at the same time, making requests simultaneously.

In addition, it gave us the chance to gain knowledge about ```threads```, understand the workings of a ```client-server architecture```, and explore the ```HTTP protocol```, including the syntax of requests and responses, as well as error codes.

All the functionalities were successfully implemented, and we received a ```perfect score```.

---
## Compilation

To compile the server use the command:
```bash
$ javac ThreadState.java ServerThreads.java MyHttpServer.java
```

To compile the client use:
```bash
$ javac MyHttpClient.java TestMP1.java
```
The file ```TestMP1.java``` was provided because the client was design for could not be executed independently.

---
## Running

To run the ```server```, use the following command, where the ```port``` is the TCP port where the server will be hosted:
```bash
$ java MyHttpServer <port>
```

To run the ```client```, open other command line and use, where the ```port``` is the TCP port of the server.
```bash
$ java TestMP1 <hostname> <port>
```
