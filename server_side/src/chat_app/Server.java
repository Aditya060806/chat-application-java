   package chat_app;

	import java.io.*; 
	import java.net.*;
import java.util.Enumeration;
import java.util.Hashtable;
	public class Server {
		private ServerSocket ss;
		private Hashtable<Socket, DataOutputStream> outputStreams = new Hashtable<Socket, DataOutputStream>();
		public Server(int port) throws IOException{
		        ss = new ServerSocket(port); // listens on all interfaces by default
		        printConnectionHints(port);
				while(true) {
					System.out.println ("Listening on port " + port);
					Socket s = ss.accept();//wait for incoming client
					System.out.println( "Connection from "+s );
					DataOutputStream dout = new DataOutputStream
							( s.getOutputStream() );
					outputStreams.put( s, dout );
					new ServerThread( this, s );
					}
		}
		Enumeration<DataOutputStream> getOutputStreams() { 
			return outputStreams.elements();
			}
		void sendToAll( String message ) {
			synchronized( outputStreams ) {
				for(Enumeration<?> e = getOutputStreams(); e.hasMoreElements(); ) {
					DataOutputStream dout = (DataOutputStream)e.nextElement();
					try { 
						dout.writeUTF( message ); 
						} catch( IOException ie ) {
							System.out.println( ie ); 
							}
					}
				}
			}
		void removeConnection( Socket s) {
			synchronized( outputStreams ) {
				System.out.println( "Removing connection to "+s );
				outputStreams.remove( s );
				try { 
					s.close(); 
					} catch( IOException ie ) {
						System.out.println( "Error closing "+s );
						ie.printStackTrace(); 
					}
				}
			}
	private void printConnectionHints(int port) {
		System.out.println("Server started.");
		System.out.println("Clients can connect using one of these local IPv4 addresses:");
		boolean foundAddress = false;
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = interfaces.nextElement();
				if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
					continue;
				}
				Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress address = addresses.nextElement();
					if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
						System.out.println("- " + address.getHostAddress() + ":" + port);
						foundAddress = true;
					}
				}
			}
		} catch (SocketException e) {
			System.out.println("Could not list local IP addresses: " + e.getMessage());
		}
		if (!foundAddress) {
			System.out.println("- No active non-loopback IPv4 address found. Use localhost:" + port + " on this machine.");
		}
	}
		static public void main( String args[] ) throws Exception {
		int port = args.length >= 1 ? Integer.parseInt(args[0]) : 5055;
			 new Server( port );
			}
		}
 class ServerThread extends Thread {
		private Server server;
		private Socket socket;
		public ServerThread( Server server, Socket socket ) {
			this.server = server;
			this.socket = socket;
	        start();
		}
	   public void run() {
		try { 
			DataInputStream din = new DataInputStream(socket.getInputStream() ); 
			while (true) {	
				String message = din.readUTF();
				String[] parts = message.split(" ", 2);
				if (parts.length < 2) {
					continue;
				}
		    	String sender = parts[0];
		    	String content = parts[1];
		    	if(!content.equals("quit")) {
		    		System.out.println( "Broadcasting from " + sender + ": " + content );
		    		server.sendToAll(message );
		    	}
		    	else {
		    		server.sendToAll("quited" );
		    		server.removeConnection( socket );
		    		break;
		    	}
				}
			} catch( IOException ie ) {
				System.out.println("close connection");
			} finally {
				server.removeConnection( socket );
		}
		}
	}

