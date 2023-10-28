import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;
import java.util.Scanner;

/**
 * Class that manages the connections to the server.
 *  
 * @author Laura Cunha - fc58188
 * @author Yichen Cao  - fc58165
 * @author José Sá     - fc58200
 *
 */
public class ServerThreads implements Runnable {

	private ThreadState state;
	private Socket socket;
	private int threadCount;
	private BufferedReader reader;
	private PrintWriter writer;
	private static String CRLF = "\r\n";

	/**
	 * Constructor for the ServerThreads, given a socket, a live thread number and the thread count it creates a ServerThread.
	 *
	 * @param socket 			A given Socket to open connection to the Client
	 * @param threadCount 		The number of the thread
	 * @requires 				{@code socket != null && threadCount >= 0}
	 * @throws IOException
	 */
	public ServerThreads(Socket socket, int threadCount) throws IOException {
		this.state = ThreadState.LIVE;
		this.socket = socket;
		this.threadCount = threadCount;
		this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
		this.writer = new PrintWriter (this.socket.getOutputStream());
	}

	@Override
	/**
	 * Method that runs the ServerThreads, it can receive requests and respond with the appropriate message.
	 */
	public void run() {

		System.out.println("Server HTTP/1.1. Starting new thread #" + this.threadCount +" (threads alive:" + MyHttpServer.checkActive() + ")");
		System.out.println("  Waiting for request...");

		while(this.state == ThreadState.LIVE) { //when state == DEAD, run() will stop running
			try {
				StringBuilder sbAnswer = new StringBuilder();
				String allRequest = readRequest();
				
				if(!allRequest.isEmpty() && this.state == ThreadState.LIVE) {
					String requestLine = allRequest.split("\n")[0];
					int requestStatusCode = numStatus(requestLine, allRequest, sbAnswer);
					answerPutStatus(sbAnswer, requestStatusCode);
					answerBody(requestLine, sbAnswer, requestStatusCode);
					sendAnswer(sbAnswer);
					System.out.println("request's status code:" + requestStatusCode + "     threads alive:" + MyHttpServer.checkActive() + "\n\n"); //debug message
					System.out.println("  Waiting for request...");
				}
			} catch (Exception IOException) {
				System.out.println("Error: " + IOException.getMessage());
				IOException.printStackTrace();
			}
		}
	}

	/**
	 * Method that reads requests sent from the client to the server, prints the request and returns the request in a String format.
	 *
	 * @returns A String containing the request read.
	 * @throws IOException
	 */
	private String readRequest() throws IOException {	

		StringBuilder charsRequest = new StringBuilder();
		int c = 0;

		do {
			c = reader.read(); 
			if (c == -1) { //if there's no character will close the connection to the Client
				close();
				break;
			}else
				charsRequest.append((char)c);
		}while(reader.ready());

		String request = charsRequest.toString();
		if(!request.isEmpty()) {
			System.out.println("Server HTTP/1.1 receiving request from thread #" + this.threadCount + ":");
			System.out.println("----------");
			System.out.println(request);
			System.out.println("----------");
		}
		return request;
	}


	/**
	 * Method that given a String url, reads the content of the url and turns it into a String, which is then returned. 
	 *
	 * @param url 			The url of the file.
	 * @returns A String with the contents of the given url.
	 * @requires 			{@code url != null}
	 * @throws IOException
	 */
	private String readFile(String url) {

		System.out.println("Server HTTP/1.1 reading file " + url + ".");
		StringBuilder fileContent = new StringBuilder();
		Scanner sc = null;
		
		try {
			sc = new Scanner(new File(url));
			while (sc.hasNext())
				fileContent.append(sc.nextLine() + "\n");
			sc.close();
		} catch (FileNotFoundException e) {
			System.out.println("Error: " + e.getMessage());
		}
		return fileContent.toString();
	}

	/**
	 * Method that given a request in String format, the full request, and a StringbUilder that will have the full answer, 
	 * it checks the request for errors and returns the appropriate error code if it finds an error, if not it returns 200.
	 *
	 * @param requestLine 				The request line of full request.
	 * @param allRequest 				The full request.
	 * @param sbAnswer 					A StringBuilder to append the message corresponding to the given code, that will have the full answer.
	 * @returns A value representing the appropriate status code
	 * @requires 						{@code requestLine != null && allRequest != null && sbAnswer != null}
	 * @throws IOException
	 */
	public int numStatus(String requestLine, String allRequest, StringBuilder sbAnswer)  {

		String[] methodUrlVersion = requestLine.split(" "); //contains the request line
		String[] requestAllLines = allRequest.split("\n"); 	//contains the header and body (if exists)

		if(requestLine.isEmpty())
			return 404;
		if (!MyHttpServer.checkAvailable(this))
			return 503;
		if(methodUrlVersion.length != 3)
			return 400;
		else {
			String method = methodUrlVersion[0];
			String url = methodUrlVersion[1];
			String versionCRFL = methodUrlVersion[2];

			if(!method.equals("GET") && !method.equals("POST"))
				return 501;
			else {
				if(method.equals("GET")) {
					if(!url.equals("/index.html") && !url.equals("/"))
						return 404;
				}
				if(method.equals("POST")) {
					if(!url.equals("/simpleForm.html"))
						return 404;

					//check if the body has the correct content length
					int index = requestAllLines.length-1;
					String body = requestAllLines[index];
					String[] lines = requestAllLines.toString().split(": ");//Content-Length||12\r\n

					for (int i = 0; i < lines.length; i++) {
						if(lines[i].equals("Content-Length")) {
							int contentLength = Integer.parseInt(lines[i+1]);
							if(body.length() != contentLength)
								return 400;
						}
					}
				}
			}
			if(versionCRFL.contains("/")) {
				String[] versionNum = versionCRFL.split("/");//"HTTP/1.1\r"
				if(!versionNum[0].equals("HTTP")) 
					return 400;
				if(!versionNum[1].equals("1.1\r")) 
					return 505;
			}else
				return 400;
			
			for (int i = 0; i < requestAllLines.length-1; i++) {
				if(!requestAllLines[i].endsWith("\r")) {
					return 400;
				}
			}
		}
		return 200;
	}

	/**
	 * Method that given a StringBuilder and an errorNum, it appends to the StringBuilder a message corresponding to the given errorNum.
	 *
	 * @param sbAnswer 			A StringBuilder to append the message corresponding to the given code, that will have the full answer.
	 * @param errorNum 			The status code to check
	 * @requires 				{@code sbAnswer != null}
	 */
	private void answerPutStatus(StringBuilder sbAnswer, int errorNum) {

		switch(errorNum){
			case 100: sbAnswer.append("HTTP/1.1 100 Continue\r\n"); break;
			case 200: sbAnswer.append("HTTP/1.1 200 OK\r\n"); break;
			case 301: sbAnswer.append("HTTP/1.1 301 Moved Permanently\r\n"); break;
			case 304: sbAnswer.append("HTTP/1.1 304 Not Modified\r\n"); break;
			case 400: sbAnswer.append("HTTP/1.1 400 Bad Request\r\n"); break;
			case 401: sbAnswer.append("HTTP/1.1 401 Unauthorized\r\n"); break;
			case 404: sbAnswer.append("HTTP/1.1 404 Not Found\r\n"); break;
			case 501: sbAnswer.append("HTTP/1.1 501 Not Implemented\r\n"); break;
			case 503: sbAnswer.append("HTTP/1.1 503 Service Unavailable\r\n"); break;
			case 505: sbAnswer.append("HTTP/1.1 505 Version Not Supported\r\n"); break;
		}
	}

	/**
	 * Method that given a requestLine, a StringBuilder and an statusCode, it appends to the StringBuilder the answer to the given requestLine.
	 *
	 * @param requestLine		 The request line given
	 * @param sbAnswer 			 A StringBuilder to append the message corresponding to the given code,  that will have the full answer.
	 * @param statusCode 		 The status code to check
	 * @requires 				{@code requestLine != null && sbAnswer != null}
	 */ 
	private void answerBody(String requestLine, StringBuilder sbAnswer, int statusCode) {

		if(statusCode == 200) {
			String method = requestLine.split(" ")[0];
			String url = requestLine.split(" ")[1];

			if(!url.equals("/")) //for the request of the browser
				url = url.substring(1);
			else
				url = "index.html";

			if(method.equals("GET")) {
				String fileContent = readFile(url);
				Date lastModified = new Date(new File(url).lastModified());
				sbAnswer.append("Date: " + new Date().toString() + CRLF);
				sbAnswer.append("Content-Length: " + fileContent.length() + CRLF);
				sbAnswer.append("Content-Type: text/html"+CRLF);
				sbAnswer.append("Connection: keep-alive" + CRLF);
				sbAnswer.append("Last-Modified: " + lastModified.toString()+ CRLF);
				sbAnswer.append(CRLF);
				sbAnswer.append(fileContent);

			}else if(method.equals("POST")) {
				sbAnswer.append("Date: " + new Date().toString() + CRLF);
				sbAnswer.append("Content-Length: " + 0 + CRLF);
				sbAnswer.append("Connection: keep-alive" + CRLF);
				sbAnswer.append(CRLF);
				sbAnswer.append("");
			}
		}else {
			sbAnswer.append("Date: " + new Date().toString() + CRLF);
			sbAnswer.append("Content-Length: " + 0 + CRLF);
			sbAnswer.append("Connection: keep-alive" + CRLF);
			sbAnswer.append(CRLF);
			sbAnswer.append("");
		}
	}

	/**
	 * Method that given a StringBuilder that contains the answer sends it to the Client.
	 *
	 * @param sbAnswer 		A StringBuilder that contains the answer to send to the Client
	 * @requires 			{@code sbAnswer != null}
	 * @throws IOException
	 */
	private void sendAnswer(StringBuilder sbAnswer) throws IOException {

		System.out.println("Server HTTP/1.1 sending reply...");
		this.writer.write(sbAnswer.toString());
		this.writer.flush();
	}

	/**
	 * Method that closes the current ServerThread and it's connection to the Client.
	 */
	private void close() {
		
		try {
			writer.close();
			reader.close();
			socket.close();
			this.state = ThreadState.DEAD;
		} catch (IOException e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Method that returns the current ThreadState.
	 *
	 * @returns The current ThreadState of ServerThreads.
	 * @ensures	{@code \result == LIVE || \result == DEAD}
	 */
	public ThreadState getState() {
		return this.state;
	}

}
