package net.ponder2.managedobject;

import net.ponder2.Util;
import net.ponder2.ManagedObject;
import net.ponder2.apt.Ponder2op;
import net.ponder2.objects.P2Object;
import net.ponder2.objects.P2Boolean;

import net.ponder2.exception.Ponder2AuthorizationException;
import net.ponder2.exception.Ponder2Exception;
import net.ponder2.exception.Ponder2ResolveException;

public class UserMO implements ManagedObject {
    protected String name, myPath;
    protected int userid;
    
    protected P2Object myP2Object;
	
    @Ponder2op("createname:userid:path:")
    public UserMO(P2Object myP2Object, String name, int userid, String myPath) {
	super();
	this.name = name;
	this.userid = userid;
	this.myPath = myPath;
	this.myP2Object = myP2Object;
    }

    private P2Object tryAction(String action, P2Object targetOID, P2Object... values) {
	P2Object actionResult = null;

	System.out.println("UserMO: user " + this.name + " attempting to perform action " + action);

	try {
	    actionResult = targetOID.operation(myP2Object, action, values);

	} catch (Ponder2AuthorizationException e) {
	    System.out.println("denied: " + e);
	    return P2Object.create("denied", "false");
	} catch (Exception e) {
	    System.out.println("exception: " + e.getMessage());
	    System.exit(1);
	}

	return P2Object.create("granted", actionResult.toString());
    }

    @Ponder2op("action:target:")
	public P2Object action(String action, P2Object targetOID) {
	return tryAction(action, targetOID);
    }

    @Ponder2op("action:target:view:")
	public P2Object actionViewed(String action, P2Object targetOID, P2Object viewOID) {
	return tryAction(action, targetOID, viewOID);
    }
    @Ponder2op("action:target:view:field:")
	public P2Object actionViewed(String action, P2Object targetOID, P2Object viewOID, P2Object field) {
	return tryAction(action, targetOID, viewOID, field);
    }

    /*
    @Ponder2op("read:")
	public P2Object read(P2Object targetOID) {
	return tryAction("read", targetOID);
    }

    @Ponder2op("read:view:")
	public P2Object readViewed(P2Object targetOID, P2Object viewOID) {
	return tryAction("read:", targetOID, viewOID);
    }

    @Ponder2op("login:")
	public P2Object login(P2Object siteOID) {
	return tryAction("login:", siteOID, P2Object.create(this.name));
    }
    */
}
