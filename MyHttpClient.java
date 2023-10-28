import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Client that sends HTTP Requests to a server located at given host and port.
 *  
 * @author Laura Cunha - fc58188
 * @author Yichen Cao  - fc58165
 * @author José Sá     - fc58200
 *
 */
public class MyHttpClient {

	private final String hostName;
	private final int portNumber;
	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private static String CRLF = "\r\n";

	/**
 	* Constructor for the Client, it receives a name and a port number to create the client.
	*
 	* @param hostName 		The name of the host.
 	* @param portNumber 	The number of the port.
 	* @requires 			{@code hostName != null && portNumber > 0}
	* @throws IOException
	*/
	public MyHttpClient(String hostName, int portNumber) throws IOException {

		this.hostName = hostName;
		this.portNumber = portNumber;
		this.socket = new Socket(this.hostName, this.portNumber);
		this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		this.writer = new PrintWriter (this.socket.getOutputStream());
	}

	/**
 	* Method that given an objectName sends a GET request from the client for said objectName.
	*
 	* @param objectName 	The name of the object to request.
 	* @requires 			{@code objectName != null}
	* @throws IOException
	*/
	public void getResource(String objectName) throws IOException {

		StringBuilder sbRequest = new StringBuilder();

		sbRequest.append("GET /" + objectName + " HTTP/1.1" + CRLF);
		sbRequest.append("Host: "+ this.hostName + ":" + this.portNumber + CRLF);
		sbRequest.append("Content-Length: " + 0 + CRLF);
		sbRequest.append("Conection: keep-alive" + CRLF);
		sbRequest.append(CRLF);
		sbRequest.append("");
		sendRequest(sbRequest);
		readAnswer();
	}

	/**
 	* Method that given an array of strings, containing in order, the student name and the student id, sends a POST request from the client.
	*
 	* @param data 			The name of the student and the id of the student in that order.
 	* @requires				{@code data != null}
	* @throws IOException
	*/
	public void postData(String[] data) throws IOException {

		//data format e.g.: {"name1: value1", "name2: value2"}
		
		StringBuilder sbRequest = new StringBuilder();
		String[] nameArray = data[0].split(": ");
		String[] idArray = data[1].split(": ");
		String body = "StudentName=" + nameArray[1] + "&StudentID=" + idArray[1];

		sbRequest.append("POST /simpleForm.html HTTP/1.1" + CRLF);
		sbRequest.append("Host: " + this.hostName + ":" + this.portNumber + CRLF);
		sbRequest.append("Content-Length: " + body.length() + CRLF);
		sbRequest.append("Conection: keep-alive" + CRLF);
		sbRequest.append(CRLF);
		sbRequest.append(body);
		sendRequest(sbRequest);
		readAnswer();
	}

	/**
 	* Method that given the name of a method not implemented sends a request from the client with that method.
	*
 	* @param wrongMethodName 	The name of the unimplemented method.
 	* @requires					{@code wrongMethodName != null}
	* @throws IOException
	*/
	public void sendUnimplementedMethod(String wrongMethodName) throws IOException {

		StringBuilder sbRequest = new StringBuilder();
		
		sbRequest.append(wrongMethodName + " /index.html HTTP/1.1" + CRLF);
		sbRequest.append("Host: " + this.hostName + ":" + this.portNumber + CRLF);
		sbRequest.append("Content-Length: " + 0 + CRLF);
		sbRequest.append("Conection: keep-alive" + CRLF);
		sbRequest.append(CRLF);
		sbRequest.append("");
		sendRequest(sbRequest);
		readAnswer();
	}

	/**
 	* Method that given an integer (1, 2 or 3) sends one of three types of bad GET requests from the client:
 	* 	1- GET request without "\r\n" at the end;
	* 	2- GET request with extra spaces;
	* 	3- GET request without the HTTP version field.
	*
 	* @param type 			A number that depending on the type of bad request you want to send.
 	* @requires 			{@code type >= 1 && type <= 3}
	* @throws IOException
	*/
	public void malformedRequest(int type) throws IOException {

		StringBuilder sbRequest = new StringBuilder();

		switch (type) {
		case 1:
			sbRequest.append("GET /index.html HTTP/1.1");
			sbRequest.append("Host: " + this.hostName + ":" + this.portNumber + CRLF);
			sbRequest.append("Content-Length: " + 0 + CRLF);
			sbRequest.append("Conection: keep-alive" + CRLF);
			sbRequest.append(CRLF);
			sbRequest.append("");
			sendRequest(sbRequest);
			readAnswer();
			break;
		case 2:
			sbRequest.append("GET /  index.html     HTTP/  1.1" + CRLF);
			sbRequest.append("Host: " + this.hostName + ":" + this.portNumber + CRLF);
			sbRequest.append("Content-Length: " + 0 + CRLF);
			sbRequest.append("Conection: keep-alive" + CRLF);
			sbRequest.append(CRLF);
			sbRequest.append("");
			sendRequest(sbRequest);
			readAnswer();
			break;
		case 3:
			sbRequest.append("GET /index.html " + CRLF);
			sbRequest.append("Host: " + this.hostName + ":" + this.portNumber + CRLF);
			sbRequest.append("Content-Length: " + 0 + CRLF);
			sbRequest.append("Conection: keep-alive" + CRLF);
			sbRequest.append(CRLF);
			sbRequest.append("");
			sendRequest(sbRequest);
			readAnswer();
			break;
		}
	}

	/**
 	* Method that given a StringBuilder sends the resulting String to the Server.
	*
 	* @param sbRequest 			The name of the given StringBuilder to be used
 	* @requires					{@code sbRequest != null}
	* @throws IOException
	*/
	private void sendRequest(StringBuilder sbRequest) throws IOException {

		System.out.println("\nClient HTTP/1.1 sending request...");
		this.writer.write(sbRequest.toString());
		this.writer.flush();
	}

	/**
 	* Method that receives an answer from the server the client is connected to, which it then formats and prints.
	*
	* @throws IOException
	*/
	private void readAnswer() throws IOException {
		
		//Read char by char from the server response and add them (this contains \r\n)
		StringBuilder sbAnswerChar = new StringBuilder();
		while(reader.ready() || sbAnswerChar.length() == 0) {
			char c = (char)reader.read();
			sbAnswerChar.append(c);
		}
		String answer = sbAnswerChar.toString();
		if(!answer.isEmpty()) {
			System.out.println("Client HTTP/1.1 received answer:");
			System.out.println("----------");
			System.out.println(answer);
			System.out.println("----------\n\n");
			
		}

	}

	/**
 	* Method that closes the Client and it's connection to the server.
	*
	* @throws IOException
	*/
	public void close() {

		try {
			writer.close();
			reader.close();
			socket.close();
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}



}



