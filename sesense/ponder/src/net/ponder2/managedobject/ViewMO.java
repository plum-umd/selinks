package net.ponder2.managedobject;

import net.sesense.PonderProxyServer;
import net.sesense.PonderProxyServer.PonderInterface;

import net.ponder2.Util;
import net.ponder2.PonderTalk;
import net.ponder2.ManagedObject;
import net.ponder2.apt.Ponder2op;

import net.ponder2.PonderTalkInterface;

import java.lang.StringBuffer;

import java.util.Vector;
import java.util.Set;
import java.util.Iterator;

import net.ponder2.exception.Ponder2ArgumentException;
import net.ponder2.exception.Ponder2OperationException;
import net.ponder2.objects.P2Object;
import net.ponder2.parser.P2Compiler;
import net.ponder2.parser.XMLParser;

// adapted from net.ponder2.PonderTalk

public class ViewMO implements ManagedObject {
    private boolean  trace = true;
    private P2Object myP2Object;
    private String ref;

    private P2Object proxy;
    private PonderProxyServer server;

    private Vector<String> filtersAsked = null;

    @Ponder2op("create:")
    public ViewMO(P2Object myP2Object, String ref) {
        this.myP2Object = myP2Object;
	this.ref = ref;

	this.filtersAsked = new Vector<String>();

	try {
	    this.proxy = Util.resolve("root/proxy");
	} catch (Exception e) {
	    debug("create:" + e);
	    debug("create: couldn't get a hold of the proxy, big whammy");
	    System.exit(1);
	}

	this.debug("created");
    }

    @Ponder2op("getRef")
	public String getRef() {
	return ref;
    }

    @Ponder2op("hasFilter:")
    public boolean hasFilter(String filter) {
	//System.out.println("hasFilter(" + Thread.currentThread() + ") asking for filter [" + filter + "]");

	P2Object result = null;

	try {
	    result = proxy.operation(myP2Object, "ask:cid:", P2Object.create("hasFilter ;; " + filter), P2Object.create(ref));
	} catch (Exception e) {
	    System.out.println("exception: " + e.getMessage());
	    System.exit(1);
	}

	String res = result.toString();

	filtersAsked.add(filter);

	return parseToBoolean(res);
    }

    @Ponder2op("filtersAsked")
	public String getFiltersAsked() {

	Iterator iter = filtersAsked.iterator();

	String ret = "";

	while(iter.hasNext()) {
	    ret += iter.next();
	    if (iter.hasNext()) {
		ret += " ;; ";
	    }
	}

	return ret;
    }

    static boolean parseToBoolean(String s) {
	if (s.equals("true")) { return true; }
	if (s.equals("false")) { return false; }

	System.out.println("parseToBoolean: not a boolean given: " + s);
	System.exit(1);

	return false;
    }

    private void debug(String msg) {
	System.out.println("ViewMO(" + ref + ") as " + Thread.currentThread() +": " + msg);
    }

}
