package net.ponder2.managedobject;

import net.ponder2.ManagedObject;
import net.ponder2.apt.Ponder2op;
import net.ponder2.objects.P2Object;
import net.ponder2.objects.P2Boolean;

public class Sesense implements ManagedObject {
    protected P2Object myP2Object;
	
    @Ponder2op("create")
	public Sesense(P2Object myP2Object) {
	super();
	this.myP2Object = myP2Object;
    }

    @Ponder2op("login:")
	public P2Boolean loginUsername(String username) {
	System.out.println("Sesense: user " + username + " attempting to login");
	return P2Boolean.True;
    }	

    @Ponder2op("login")
	public P2Boolean login() {
	System.out.println("Sesense: user attempting to login");
	return P2Boolean.True;
    }	
}
