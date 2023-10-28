
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Driver class for HTTP server.
 * 
 * @author Laura Cunha - fc58188
 * @author Yichen Cao  - fc58165
 * @author José Sá     - fc58200
 *
 */
public class MyHttpServer {
	
	private static int threadCount; // counts consecutively 1.. can be 6+
	private static ArrayList<ServerThreads> threadList = new ArrayList<>();
	private static int liveThreadNum; // 1-5 threads can be active, 6+ cannot connect
	private static final int supportedThreadNum = 5;
	
	/**
	 * Main method that starts the threads and drives the HTTP server.
	 * 
	 * @param args 			The command line arguments.
	 */
	public static void main(String[] args){

		System.out.println("Server starting...");
		int port = Integer.parseInt(args[0]);
		System.out.println("Using Port: " + port + "\n");

		try(ServerSocket serverSocket = new ServerSocket(port)){
			threadCount = 1;
			
			while(true){
				Socket socket = serverSocket.accept();
				System.out.println("  Connecting... ");
				ServerThreads thread = new ServerThreads(socket, threadCount);
				System.out.println("");
				threadList.add(thread);
				(new Thread (thread)).start();
				threadCount += 1;
			}
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	
	/**
	 * Method that checks the number of active threads associated with this server.
	 *
	 * @returns A integer representing the number of active threads
	 * @ensures 		{@code liveThreadNum >= 0}
	 */
	public static int checkActive() {
		
		liveThreadNum = 0;
		for (ServerThreads eachThread : threadList) {
			if(eachThread.getState() == ThreadState.LIVE) 
				liveThreadNum++;
		}
		return liveThreadNum;
	}
	
	/**
	 * Method that given a thread, checks whether server service is available to it
	 * based on the number of active threads the server supports.
	 *
	 * @param thread		An existing thread within the server
	 * @returns A boolean value that confirms server's availability
	 * @requires 			{@code sbAnswer != null && this.threadList.contains(thread)}
	 */
	public static boolean checkAvailable(ServerThreads thread) {
		
		liveThreadNum = 0;
		for (ServerThreads eachThread : threadList) {
			if(eachThread.getState() == ThreadState.LIVE) 
				liveThreadNum++;
			if(thread.equals(eachThread)) {
				if (liveThreadNum<=supportedThreadNum)
					return true;
			}
		}
		return false;
	}
}
