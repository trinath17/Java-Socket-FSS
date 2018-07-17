import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class JavaClientFile {
	
	String dir;
	Socket clientsocket;
	InputStream inFromServer;
	OutputStream outToServer;
	BufferedInputStream bis;
	PrintWriter pw;
	String name, file, path;
	String host;
	int port;
	int c;
	int size = 10000000;
	List<String> filelist;
	String[] names = new String[10000];
	int len;
	
	public JavaClientFile(String dir,String host,int port){
		super();
		
		this.dir = dir;
		this.host = host;
		this.port = port;
		
		try {
			clientsocket = new Socket(host, port);
			inFromServer = clientsocket.getInputStream();
			pw = new PrintWriter(clientsocket.getOutputStream(), true);
			outToServer = clientsocket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	
	public void getFilesListFromServer(){
		
		try {
			
			ObjectOutputStream dout = new ObjectOutputStream(outToServer);
			dout.writeObject("getfiles");
		
			ObjectInputStream oin = new ObjectInputStream(inFromServer);
			@SuppressWarnings("unused")
			String s = (String) oin.readObject();
			//System.out.println(s);

			len = Integer.parseInt((String) oin.readObject());
			System.out.println("No files in server :" + len);

			String[] temp_names = new String[len];

			for(int i = 0; i < len; i++) {
				String filename = (String) oin.readObject();
				//System.out.println(filename);
				names[i] = filename;
				temp_names[i] = filename;
			}
			
			Arrays.sort(temp_names);
			
			System.out.println("Displaying list of files in server folder :");
			for(int i=0;i < temp_names.length;i++){
				System.out.println(i+1 +". "+temp_names[i]);
			}

		} 
		catch (Exception exc) {
			System.out.println("Exception: " + exc.getMessage());
		}
		
	}
	
	public void downloadFromServer(String filename){
		
		try {
			
			ObjectOutputStream dout = new ObjectOutputStream(outToServer);
			dout.writeObject("download");
			
			File directory = new File(dir);

			if (!directory.exists()) {
				directory.mkdir();
			}
			boolean complete = true;
			byte[] data = new byte[size];
			name = filename;
			file = new String("*" + name + "*");
			pw.println(file); 

			ObjectInputStream doin = new ObjectInputStream(inFromServer);
			String s = (String) doin.readObject();
			
			System.out.println(s);

			if(s.equals("Success")) {
				File f = new File(directory, name);
				FileOutputStream fileOut = new FileOutputStream(f);
				DataOutputStream dataOut = new DataOutputStream(fileOut);

				while (complete) {
					c = inFromServer.read(data, 0, data.length);
					if (c == -1) {
						complete = false;
						System.out.println("Completed");

					} else {
						dataOut.write(data, 0, c);
						dataOut.flush();
					}
				}
				fileOut.close();
			}
			else {
				System.out.println("Requested file not found on the server.");
			}
		} 
		catch (Exception exc) {
			System.out.println("Exception: " + exc.getMessage());
		}
		
	}
	
	public void uploadToServer(String filename){
		
		try {
			
			ObjectOutputStream dout = new ObjectOutputStream(outToServer);
			dout.writeObject("upload");
			
			name = filename;

			FileInputStream file = null;
			BufferedInputStream bis = null;

			boolean fileExists = true;
			path = dir + name;

			try {
				file = new FileInputStream(path);
				bis = new BufferedInputStream(file);
			} catch (FileNotFoundException excep) {
				fileExists = false;
				System.out.println("FileNotFoundException:" + excep.getMessage());
			}

			if (fileExists) {
				pw.println(name);
				System.out.println("Upload begins");
				
				sendBytes(bis, outToServer);
				System.out.println("Completed");
				
				boolean exists = false;
				for(int i = 0; i < len; i++){
					if(names[i].equals(name)){
						exists = true;
						break;
					}
				}

				if(!exists){
					names[len] = name;
					len++;
				}

				bis.close();
				file.close();
				outToServer.close();
			}
		} 
		catch (Exception exc) {
			System.out.println("Exception: " + exc.getMessage());
		}
		
	}
	
	public void shutdownserver(){
		
		if(!clientsocket.isClosed()){
			try {
				ObjectOutputStream dout = new ObjectOutputStream(outToServer);
				dout.writeObject("shutdown");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	private static void sendBytes(BufferedInputStream in , OutputStream out) throws Exception {
		int size = 1000000;
		byte[] data = new byte[size];
		@SuppressWarnings("unused")
		int bytes = 0;
		int c = in.read(data, 0, data.length);
		out.write(data, 0, c);
		out.flush();
	}

	public static void main(String args[]){
		
		String dir = null;
		
		if(args.length>0){
			dir = args[0];
		}else{
			System.out.println("Enter Client directory");
			System.exit(0);
		}
		
		
		JavaClientFile client = new JavaClientFile(dir, "localhost", 5555);
		
		//System.out.println("Choose operation to execute :");
		
		if(Integer.parseInt(args[1])==1){
			
			client.getFilesListFromServer();
			
		}else if(Integer.parseInt(args[1])==2){
			
			if(args.length == 3){
				String uploadfile = args[2];
				client.uploadToServer(uploadfile);
			}else{
				System.out.println("Enter uploading file name with command args as second argument");
			}
			
		}else if(Integer.parseInt(args[1])==3){
			
			if(args.length == 3){
				String downloadfile = args[2];
				client.downloadFromServer(downloadfile);
			}else{
				System.out.println("Enter downloading file name with command args as second argument");
			}
		}else if(Integer.parseInt(args[1])==4){
			client.shutdownserver();
		}else{
			System.out.println("wrong option entered");
			System.out.println("please enter correct option");
			System.exit(0);
		}
		
	}
}
