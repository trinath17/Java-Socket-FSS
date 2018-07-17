import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ServerThread extends Thread {
	
	int n;
	int m;
	String name, f, ch, fileData;
	String filename;
	
	Socket socket;
	ServerSocket serversocket;
	int count;
	String dir;
	public boolean isServerclosed = false;

	public ServerThread(Socket socket,ServerSocket serversocket, int count, String dir) {
		this.socket = socket;
		this.count = count;
		this.dir = dir;
		this.serversocket = serversocket;
	}

	public void run() {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			InputStream inFromClient = socket.getInputStream();
			
			ObjectInputStream din = new ObjectInputStream(inFromClient);
			String validator = (String) din.readObject();
			
			OutputStream output = socket.getOutputStream();
			ObjectOutputStream oout = new ObjectOutputStream(output);
			
			if(validator.equals("getfiles")){
				
				oout.writeObject("Server says Hi!");

				File ff = new File(dir);
				ArrayList<String> names = new ArrayList<String>(Arrays.asList(ff.list()));
				
				oout.writeObject(String.valueOf(names.size()));

				for(String name: names) {
					oout.writeObject(name);
				}
				
			}else if(validator.equals("shutdown")){
				if(!serversocket.isClosed()){
					socket.close();
					serversocket.close();
					isServerclosed = true;
					System.out.println("server shutdowning");
					System.out.println("server shutdowns successfully");
				}else{
					System.out.println("server isn't setup");
				}
			}else{
				
				name = in.readLine();
				ch = name.substring(0, 1);

				if (ch.equals("*")) {
					n = name.lastIndexOf("*");
					filename = name.substring(1, n);
					
					FileInputStream file = null;
					BufferedInputStream bis = null;
					boolean fileExists = true;
					System.out.println("Request to download file " + filename + " recieved from " + socket.getInetAddress().getHostName() + "...");
					filename = dir + filename;
					//System.out.println(filename);
					try {
						file = new FileInputStream(filename);
						bis = new BufferedInputStream(file);
					} 
					catch (FileNotFoundException excep) {
						fileExists = false;
						System.out.println("FileNotFoundException:" + excep.getMessage());
					}
					if (fileExists) {
						
						oout.writeObject("Success");
						System.out.println("Download begins");
						sendBytes(bis, output);
						System.out.println("Completed");
						bis.close();
						file.close();
						oout.close();
						output.close();
					}
					else {
						oout = new ObjectOutputStream(output);
						oout.writeObject("FileNotFound");
						bis.close();
						file.close();
						oout.close();
						output.close();
					}
				} 
				else{
					try {
						boolean complete = true;
						System.out.println("Request to upload file " + name + " recieved from " + socket.getInetAddress().getHostName() + "...");
						File directory = new File(dir);
						if (!directory.exists()) {
							System.out.println("Dir made");
							directory.mkdir();
						}

						int size = 10000000;
						byte[] data = new byte[size];
						File fc = new File(directory, name);
						FileOutputStream fileOut = new FileOutputStream(fc);
						DataOutputStream dataOut = new DataOutputStream(fileOut);

						while (complete) {
							m = inFromClient.read(data, 0, data.length);
							if (m == -1) {
								complete = false;
								System.out.println("Completed");
							} else {
								dataOut.write(data, 0, m);
								dataOut.flush();
							}
						}
						fileOut.close();
					} catch (Exception exc) {
						System.out.println(exc.getMessage());
					}
				}
				
			}
			
		} 
		catch (Exception ex) {
			System.out.println(ex.getMessage());
		}
	}
	
	public boolean isServerClosed(){
		return isServerclosed;
	}

	private static void sendBytes(BufferedInputStream in , OutputStream out) throws Exception {
		int size = 10000000;
		byte[] data = new byte[size];
		@SuppressWarnings("unused")
		int bytes = 0;
		int c = in .read(data, 0, data.length);
		out.write(data, 0, c);
		out.flush();
	}

}
