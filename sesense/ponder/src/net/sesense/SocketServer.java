package net.sesense;

import java.net.*;
import java.io.*;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SelectionKey;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import java.lang.StringBuffer;

import java.util.Vector;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;
	

// A trivial socket-based server.  Uses a instance of the Responds
// interface to interface with another object.
// PM: not so trivial anymore

public class SocketServer {
    private char   delim_in  = '\n';
    private String delim_in_string = "\n";
    private char   delim_out = '\n';
    private String delim_out_string = "\n";

    private int port;

    Vector<Client> clients;
    HashMap<String, Client> clientsById;

    ClientInterfaceFactory clientInterfaceFactory = null;

    ServerSocketChannel serverSocket = null;

    Selector selector = null;

    public SocketServer(int port, ClientInterfaceFactory cf) {
	this.clients = new Vector();
	this.clientsById = new HashMap<String, Client>();

	this.clientInterfaceFactory = cf;

	try {
	    this.selector = Selector.open();
	    
	    this.serverSocket = ServerSocketChannel.open();
	    this.serverSocket.configureBlocking(false);
	    this.serverSocket.socket().bind(new InetSocketAddress(port));
	    this.serverSocket.register(this.selector, SelectionKey.OP_ACCEPT, this);
	    
	} catch (Exception e) {
	    System.out.println("SocketServer: " + e);
	    System.exit(-1);
	}
    }

    public ClientInterface getClient(String id) {
	Client temp = (Client) clientsById.get(id);
	if (null == temp) { return null; }
	return temp.handler;
    }

    public Collection<ClientInterface> getClients() {
	Vector temp = new Vector();

	Iterator iter = clients.iterator();

	while (iter.hasNext()) {
	    Client client = (Client) iter.next();
	    ClientInterface ci = client.handler;
	    temp.add(ci);
	}

	return (Collection) temp;
    }
    
    public void runServer() throws IOException {
	try {
	    // main loop of server
	    while (true) {
		this.selector.select();
		//System.out.println("main loop mark (" + Thread.currentThread() + ")");

		Set keys = this.selector.selectedKeys();
		Iterator iter = keys.iterator();
		
		while (iter.hasNext()) {
		    SelectionKey key = (SelectionKey) iter.next();
		    
		    if (key.isAcceptable()) {
			System.out.println("accepting new client");
			ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
			SocketChannel clientChannel = serverChannel.accept();
			clientChannel.configureBlocking(false);
			
			Client client = new Client(clientChannel, this.selector, this.clientInterfaceFactory);

			this.clients.add(client);
			
		    } else if (key.isReadable()) {
			Client client = (Client) key.attachment();
			SocketChannel clientChannel = client.socketChannel;
			
			try {
			    client.handleInput(); 
			    
			} catch (ClientDisconnectedException e) {
			    System.out.println("client disconnected");
			    
			    client.close();
			    if (null != client.clientId) {
				clientsById.remove(client.clientId);
			    }
			    clients.remove(client);
			    
			    
			}
		    } else if (key.isWritable()) {
			Client client = (Client) key.attachment();
			client.write();
		    }
		    
		    iter.remove();
		}
	    }
	} catch (Exception e) {
	    System.out.println("main server loop: " + e);
	    e.printStackTrace();
	    System.exit(-1);
	}
    }

    class ClientDisconnectedException extends IOException {
	public ClientDisconnectedException() {
	    super();
	}
    }

    public interface ClientInterfaceFactory {
	public ClientInterface createClientInterface(ClientOutputInterface co);
    }

    public interface ClientInterface {
	public void handleLine(String line);
	public void handleDisconnect();
    }

    public interface ClientOutputInterface {
	public void setId(String id);
	public String getId();
	public void send(String msg);
	public void disconnect();
    }

    class Client implements ClientOutputInterface {
	public Socket socket = null;
	public SocketChannel socketChannel = null;

	public SelectionKey selectKey = null;
	public Selector selector      = null;

	StringBuffer buffer_in = null;

	ByteBuffer buffer_read = null;
	ByteBuffer buffer_send = null;

	PrintWriter buffer_out = null;

	boolean active = false;

	ClientInterface handler = null;

	public String clientId = null;

	public Client(SocketChannel socketChannel, Selector selector, ClientInterfaceFactory factory) throws IOException {
	    this.setSocketChannel(socketChannel, selector);

	    this.handler = factory.createClientInterface( (ClientOutputInterface) this);
	}

	public void setId(String id) {
	    if (null != clientId) {
		SocketServer.this.clientsById.remove(id);
	    }
	    SocketServer.this.clientsById.put(id, this);
	    clientId = id;
	}

	public String getId() {
	    return clientId;
	}

	public void close() throws IOException {
	    if (! active) { return; }

	    socketChannel.close();
	    selectKey.cancel();

	    active = false;
	}

	public void setSocketChannel(SocketChannel socketChannel, Selector selector) throws IOException {
	    this.disconnect();

	    this.socketChannel = socketChannel;
	    this.socket = socketChannel.socket();

	    this.buffer_send = ByteBuffer.allocateDirect(1024);
	    this.buffer_read = ByteBuffer.allocateDirect(1024);
	    this.buffer_in = new StringBuffer();

	    this.selectKey = socketChannel.register(selector, SelectionKey.OP_READ, this);
	    this.selector = selector;

	    this.active = true;
	}

	public void send(String msg) {
	    // need to do some locks on the buffer_send

	    buffer_send.put(msg.getBytes());
	    buffer_send.put((byte) SocketServer.this.delim_out);
	    // note: putChar seems to send 2 bytes

	    //System.out.println("sending [" + msg + "]");

	    try {
		wakeWrite();
	    } catch (Exception e) {
		System.out.println("send: " + e);
		System.exit(1);
	    }
	}

	private void wakeWrite() throws IOException {
	    boolean hadWrite = (0 != (selectKey.interestOps() & SelectionKey.OP_WRITE));

	    if (! hadWrite) {
		selectKey.interestOps(SelectionKey.OP_READ |
				      SelectionKey.OP_WRITE);
		selector.wakeup();
	    }
	}

	private void write() throws IOException {
	    int wrote;
	    buffer_send.flip();
	    wrote = socketChannel.write(buffer_send);

	    //System.out.println("wrote " + wrote + " bytes");

	    if (! buffer_send.hasRemaining()) {
		selectKey.interestOps(SelectionKey.OP_READ);
	    }

	    buffer_send.compact();
	}

	public void handleLine(String line) throws IOException {
	    //System.out.println("got line [" + line + "]");
	    handler.handleLine(line);
	}

	public void handleInput() throws IOException {
	    // http://gpwiki.org/index.php/Java:Simple_TCP_Networking

	    buffer_read.clear();

	    CharsetDecoder asciiDecoder = Charset.forName( "US-ASCII").newDecoder();

	    int read = 0;

	    try {
		read = socketChannel.read(buffer_read);
	    } catch (IOException e) {
		System.out.println("handleInput: " + e);
		throw new ClientDisconnectedException();
	    }

	    if (read == -1) {
		throw new ClientDisconnectedException();
	    }

	    buffer_read.flip();

	    String temp = asciiDecoder.decode(buffer_read).toString();
	    buffer_in.append(temp);

	    String lines = buffer_in.toString();
	    String line = null;
	    int dindex = lines.indexOf(delim_in_string);

	    while (dindex != -1) {
		buffer_in.delete(0, dindex+1);
		line = lines.split(delim_in_string)[0];
		line = line.trim();

		handleLine(line);

		lines = buffer_in.toString();
		dindex = lines.indexOf(delim_in_string);
	    }
	}

	public void disconnect() {
	    // do more stuff here
	    if (! this.active) { return; }
	    this.active = false;
	}
    }    
}    
