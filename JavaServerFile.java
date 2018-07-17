import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class JavaServerFile{
	
	@SuppressWarnings({ "resource", "static-access" })
	public static void main(String args[]) throws NumberFormatException, IOException{
		
		@SuppressWarnings("unused")
		Scanner sc = new Scanner(System.in);
		/*System.out.println("Enter server directory : ");
		String serverdir = sc.next();
		
		sc.nextLine();
		
		System.out.print("Enter server port number : ");
		int serverport = sc.nextInt();*/
		
		
		System.out.println("Server started...");
		System.out.println("Server is waiting for connections...");
		
		ServerSocket serversocket;
		
		serversocket = new ServerSocket(5555);
		
		int id = 1;
		
		String serverdir = null;
		
		if(args.length>0){
			serverdir = args[0];
		}else{
			System.out.println("Enter server directory");
			System.exit(0);
		}
		
		
		boolean isServerclosed = false;
		
		while (true) {
			
			if(!isServerclosed){
				
				Socket socket = serversocket.accept();
				//System.out.println("my server shutdown :: " +serversocket.isClosed());
				
				System.out.println("Client with ID " + id + " connected from " + socket.getInetAddress().getHostName() + "...");
				Thread server = new ServerThread(socket, serversocket, id, serverdir);
				id++;
				server.start();
				try {
					server.sleep(1000);
					isServerclosed = ((ServerThread) server).isServerClosed();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				System.out.println("server status :: "+isServerclosed);
				
			}else{
				break;
			}
			
			
			
		}
		
	}
}