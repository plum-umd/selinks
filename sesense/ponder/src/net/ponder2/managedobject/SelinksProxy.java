package net.ponder2.managedobject;

import net.sesense.PonderProxyServer;
import net.sesense.PonderProxyServer.PonderInterface;

import net.ponder2.PonderTalk;
import net.ponder2.ManagedObject;
import net.ponder2.apt.Ponder2op;

import net.ponder2.PonderTalkInterface;

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

import java.lang.ClassCastException;

import java.util.Vector;
import java.util.Set;
import java.util.Iterator;

import net.ponder2.exception.Ponder2ArgumentException;
import net.ponder2.exception.Ponder2AuthorizationException;
import net.ponder2.exception.Ponder2OperationException;
import net.ponder2.objects.P2Object;
import net.ponder2.objects.P2Array;
import net.ponder2.parser.P2Compiler;
import net.ponder2.parser.XMLParser;

// adapted from net.ponder2.PonderTalk

public class SelinksProxy implements ManagedObject {
    private boolean  trace = true;
    private P2Object myP2Object;

    private PonderProxyServer server;

    @Ponder2op("create:")
    public SelinksProxy(P2Object myP2Object, int port) {
	//System.out.println("SelinksProxy(" + Thread.currentThread() + ": create");
        this.myP2Object = myP2Object;
	this.server = new PonderProxyServer(port, (PonderInterface) new PonderRequestInterface());
    }

    @Ponder2op("testcmd:")
    public void testcmd(String teststring) {
	server.notifyClients("testcmd", teststring);
    }

    @Ponder2op("ask:cid:")
	public String ask(String cmd, String cid) {

	//System.out.println("SelinksProxy(" + Thread.currentThread() + ") got ask: [" + cmd + "] cid: [" + cid + "]");

	return server.ask(cid, cmd);
    }

    public class PonderRequestInterface implements PonderInterface {
	public String execute(String cmd) {
	    try {
		return SelinksProxy.this.execute(cmd);
	    } catch (Exception e) {
		
	    }
	    return null;
	}
    }

    public String execute(String ponderTalk) {
	P2Object ovalues = null;
	P2Array  values = null;
	
	P2Object[] array = null;

	boolean need_decision = false;

	try {
	    ovalues = executePonderTalk(ponderTalk);
	} catch (Exception e) {
	    e.printStackTrace();
	    return "error ;; " + e;
	}




	try {
	    values = (P2Array) ovalues;

	    array = values.asArray();

	    if (array[0].toString().equals("granted")) {		
	    } else if (array[0].toString().equals("denied")) {
	    } else {
		need_decision = true;
	    }

	} catch (ClassCastException e) {
	    return "granted ;; " + ovalues.toString();
	} catch (Ponder2ArgumentException e) {
	    System.out.println("SelinksProxy: execute: execption: " + e);
	    System.exit(1);
	}

	Iterator iter = values.getValues().iterator();

	String ret = "";

	if (need_decision) {
	    ret += "granted";

	    if (iter.hasNext()) {
		ret = ret + " ;; ";
	    }
	}

	while (iter.hasNext()) {
	    ret = ret + iter.next().toString();
	    if (iter.hasNext()) {
		ret = ret + " ;; ";
	    }
	}

	return ret;
    }

    public P2Object executePonderTalk(String aPonderTalkString)
	throws Exception {
        try {
            if (trace) {
                System.out.println("executePonderTalk: " + aPonderTalkString);
            }
            String p2xml = P2Compiler.parse(aPonderTalkString);
            return new XMLParser().execute(myP2Object, p2xml);
        } finally {}
	//        catch (Exception e) {
	//	    System.out.println("executePonderTalk: " + e);
	//	    System.exit(1);
	//        }
	//	return null;
    }
}
