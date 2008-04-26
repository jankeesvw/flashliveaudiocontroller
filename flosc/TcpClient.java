import java.io.*;
import java.net.*;

/**
 *
 * TcpClient
 * <BR><BR>
 * TCP client for the TcpServer.
 *
 * Based on CSClient by Derek Clayton.
 *
 * @author  Ben Chun        ben@benchun.net
 * @version 1.0
 */

public class TcpClient extends Thread {
    private Thread thrThis;         // client thread
    private Socket socket;          // socket for connection
    private TcpServer server;      // server to which the client is connected
    private String ip;              // the ip of this client
    protected BufferedReader in;    // captures incoming messages
    protected PrintWriter out;      // sends outgoing messages

    /**
     * Constructor for the TcpClient.  Initializes the TcpClient properties.
     * @param   server    The server to which this client is connected.
     * @param   socket    The socket through which this client has connected.
    */
    public TcpClient(TcpServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();

        // --- init the reader and writer
        try {
	    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch(IOException ioe) {
            Debug.writeActivity("Client IP: " + ip + " could not be " 
            + "initialized and has been disconnected.");
            killClient();
        }
    }

    /**
     * Thread run method.  Monitors incoming messages.
    */	
    public void run() {
        try {
            char charBuffer[] = new char[1];

            // --- while we have an incoming stream
            while(in.read(charBuffer,0,1) != -1) {

                // --- create a string buffer to hold the incoming stream
                StringBuffer stringBuffer = new StringBuffer(8192);
            
                // --- while the stream hasn't ended
                while(charBuffer[0] != '\0') {
                    // --- add the character to our buffer
                    stringBuffer.append(charBuffer[0]);
                    in.read(charBuffer, 0 ,1);
                }
                
                // --- incoming messages should be XML-encoded flash
		server.handleOsc(stringBuffer.toString());
		}

        } catch(IOException ioe) {
            Debug.writeActivity("Client IP: " + ip + " caused a read error " 
            + ioe + " : " + ioe.getMessage() + "and has been disconnected.");
        } finally {
            killClient();
        }
    }

    /**
     * Gets the ip of this client.
     * @return   ip    this client's ip
    */
    public String getIP() {
        return ip;
    }
    
    /**
     * Sends a message to this client. Called by the server's broadcast method.
     * @param   message    The message to send.
    */
    public void send(String message) {
        // --- put the message into the buffer
        out.print(message);
        
        // --- flush the buffer and check for errors
        // --- if error then kill this client
        if(out.checkError()) {
            Debug.writeActivity("Client IP: " + ip + " caused a write error "
            + "and has been disconnected.");
            killClient();
        }
    }
 
    /**
     * Kills this client. 
    */   
    private void killClient() {
        // --- tell the server to remove the client from the client list    
        server.removeClient(this);

        // --- close open connections and references
        try {
            in.close();
            out.close();
            socket.close();            
            thrThis = null;
        } catch (IOException ioe) {
            Debug.writeActivity("Client IP: " + ip + " caused an error "
            + "while disconnecting.");
        }       
    }
}
