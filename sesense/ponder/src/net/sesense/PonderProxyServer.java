package net.sesense;

import net.sesense.SocketServer;
import net.sesense.SocketServer.ClientInterface;
import net.sesense.SocketServer.ClientInterfaceFactory;
import net.sesense.SocketServer.ClientOutputInterface;

import java.net.*;
import java.io.*;

import java.lang.StringBuffer;

import java.util.Vector;
import java.util.Set;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Random;

import net.ponder2.exception.Ponder2ArgumentException;
import net.ponder2.exception.Ponder2OperationException;
import net.ponder2.objects.P2Object;
import net.ponder2.parser.P2Compiler;
import net.ponder2.parser.XMLParser;

// adapted from net.ponder2.PonderTalk

public class PonderProxyServer {
    public int port = 6666;
    private ServerThread thread;
    private PonderInterface ponderInterface;

    private SocketServer socketServer;

    public PonderProxyServer(int port, PonderInterface pi) {
	this.port = port;
	this.ponderInterface = pi;
	this.thread = new ServerThread();
	this.socketServer = this.thread.socketServer;
	thread.start();
    }

    public String ask(String clientId, String cmd) {
	Handler client = (Handler) socketServer.getClient(clientId);

	if (null == client) {
	    System.out.println("PonderProxyServer: ask: no such client [" + clientId + "]");
	    System.exit(1);
	}

	return client.ask(cmd);
    }

    public interface PonderInterface {
	public String execute(String s);
    }

    class HandlerFactory implements ClientInterfaceFactory {
	public ClientInterface createClientInterface(ClientOutputInterface co) {
	    Handler temp = new Handler(co);
	    return (ClientInterface) temp;
	}
    }

    public class ResponseAlarm {
	public String response = null;
	public String resId;
	public Thread sleeper;

	public ResponseAlarm(String resId, Thread sleeper) {
	    this.resId   = resId;
	    this.sleeper = sleeper;
	}

	public void wakeup() {
	    this.sleeper.interrupt();
	}
    }

    class Handler implements ClientInterface {
	public Vector listens = null;
	public ClientOutputInterface outputInterface = null;

	public String clientId = "000";

	private String delim = " :: ";
	private String listDelim = " ;; ";

	private HashMap<String, ResponseAlarm> responses;

	private Random random = null;

	public Handler(ClientOutputInterface co) {
	    this.outputInterface = co;
	    this.random = new Random();
	    this.responses = new HashMap<String, ResponseAlarm>();
	}

	public String ask(String msg) {
	    // !!! thread safety failure potential here, fix maybe sometime
	    // lock responses hash at least

	    //System.out.println("Handler.ask(" + Thread.currentThread() + "): asking [" + msg + "]");

	    String resId = String.valueOf(random.nextInt());

	    ResponseAlarm temp = new ResponseAlarm(resId, Thread.currentThread());
	    responses.put(resId, temp);

	    sendQuery(resId, msg);

	    try {
		while(true) {
		    //System.out.println("sleeping and such");
		    Thread.sleep(100000);
		}
	    } catch (InterruptedException e) {

	    }

	    String ret = temp.response;
	    responses.remove(resId);

	    return ret;
	}

	private void send(String msg) {
	    outputInterface.send(msg);
	}

	public void sendQuery(String resId, String query) {
	    sendType("query", resId + listDelim + query);
	}

	public void sendType(String type, String msg) {
	    send(clientId + delim + type + delim + msg);
	}

	public void ponderListen(String target) {
	    listens.add(target);
	    sendType("response", "good");
	}

	public void ponderIgnore(String target) {
	    listens.remove(target);
	    sendType("response", "good");
	}

	public void handleResult(String[] opts) {
	    if (opts.length != 2) {
		sendType("error", "bad result: wrong number of items");
		return;
	    }

	    String resId  = opts[0];
	    String result = opts[1];

	    ResponseAlarm alarm = responses.get(resId);

	    if (null == alarm) {
		System.out.println("handleResult: got a response, but didn't ask for one");
		System.exit(1);
	    }

	    alarm.response = result;
	    alarm.wakeup();
	}

	public void handlePonder(final String inputLine) {

	    Thread responder = new Thread() {
		    public void run() {
			String output = ponderInterface.execute(inputLine);
			if (output == null) output = "error ;; false";
			System.out.println("handlePonder: result: " + output);
			sendType("response", output);
		    }
		};

	    responder.start();
	}

	public void handleLine(String inputLine) {
	    String[] parts = inputLine.split(" :: ");
	    if (parts.length != 3) {
		System.out.println("handleLine: received line did not have 3 parts: " + inputLine);
		System.exit(1);
	    }

	    if (! clientId.equals(parts[0])) {
		this.outputInterface.setId(parts[0]);
	    }
	    this.clientId = parts[0];

	    if (parts[1].equals("ponder")) {
		handlePonder(parts[2]);
	    } else if (parts[1].equals("command")) {
		handleCmd(parts[2].split(" ;; "));
	    } else if (parts[1].equals("result")) {
		handleResult(parts[2].split(" ;; "));
	    } else {
		System.out.println("handleLine: unknown line type: " + parts[1]);
		System.exit(1);
	    }
	}

	public void handleCmd(String[] opts) {
	    if (opts.length < 2) {
		sendType("error", "bad command: no command given");
		return;
	    }

	    String cmd = opts[1];

	    if (cmd.equalsIgnoreCase("listen") || cmd.equalsIgnoreCase("ignore")) {
		if (opts.length < 3) {
		    sendType("error", "bad command: listen and ignore require a target ponder command");
		    return;
		}

		String target = opts[2];

		if (cmd.equalsIgnoreCase("listen")) {
		    ponderListen(target);
		} else if (cmd.equalsIgnoreCase("ignore")) {
		    ponderIgnore(target);
		}
	    }
	}

	public void handleDisconnect() {

	}
    }

    public void notifyClients(String cmd, String opt) {
	Iterator iter = thread.socketServer.getClients().iterator();
	
	while (iter.hasNext()) {
	    Handler ci = (Handler) iter.next();
	    
	    try {
		ci.sendType("notify", cmd + " ;; " + opt);
	    } catch (Exception e) {
		System.out.println("notifyClients: " + e);
	    }
	}
    }

    class ServerThread extends Thread {
	private SocketServer socketServer;

	public ServerThread() {
	    int port = PonderProxyServer.this.port;

	    this.socketServer = new SocketServer(port, (ClientInterfaceFactory) new HandlerFactory());

	    System.out.println("ServerThread: selinks proxy server thread created at port " + port);
	}

	public void run() {
	    // Run the socket server
	    try {
		socketServer.runServer();
	    } catch (Exception e) {
		System.out.println("socket server exception: " + e.getMessage());
		System.exit(1);
	    }
	}
    }
}
