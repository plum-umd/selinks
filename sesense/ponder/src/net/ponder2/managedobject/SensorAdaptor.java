// based on BSNAdaptor from ponder2 tutorial

package net.ponder2.managedobject;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import net.ponder2.ManagedObject;
import net.ponder2.apt.Ponder2op;
import net.ponder2.objects.P2Object;
import net.ponder2.objects.P2Boolean;

import net.sesense.sensor.SensorInterface;

public class SensorAdaptor implements ManagedObject {
    boolean isSim = true;
    String name;
    String network;
    String rmiName;
    SensorInterface sensor;

    @Ponder2op("create:name:sim:")
	public SensorAdaptor(String aNetwork, String aName, boolean sim) {
	isSim = sim;
	rmiName = aNetwork + "_" + aName;
	name = aName;
	network = aNetwork;

	if (isSim) {
	    getSensor();
	}
    }

    @Ponder2op("read")
	public P2Boolean read() {
	return P2Boolean.True;
    }

    @Ponder2op("read:")
	public P2Boolean readViewed(P2Object view) {
	return P2Boolean.True;
    }

    @Ponder2op("read:field:")
	public P2Boolean readViewedField(P2Object view) {
	return P2Boolean.True;
    }

    @Ponder2op("setRate:")
	public void setRate(int anInteger) {
	try {
	    if (getSensor())
		sensor.setRate(anInteger);
	}
	catch (RemoteException e) {
	    // We can't seem to contact the BSN
	    sensor = null;
	}
    }

    @Ponder2op("getRate")
	public int getRate() {
	int value = 0;
	try {
	    if (getSensor())
		value = sensor.getRate();
	}
	catch (RemoteException e) {
	    // We can't seem to contact the BSN
	    sensor = null;
	}
	return value;
    }

    @Ponder2op("getValue")
	public int getValue() {
	int value = 0;
	try {
	    if (getSensor())
		value = sensor.getValue();
	}
	catch (RemoteException e) {
	    sensor = null;
	}
	return value;
    }

    @Ponder2op("start")
	public void startSensor() {
	try {
	    sensor.start();
	} catch (RemoteException e) {
	}
    }

    @Ponder2op("stop")
	public void stopSensor() {
	try {
	    sensor.stop();
	} catch (RemoteException e) {
	}
    }

    private boolean getSensor() {
	if (name != null && sensor == null)
	    try {
		System.out.println("SensorAdaptor looking up " + rmiName);
		sensor = (SensorInterface) Naming.lookup(rmiName);
	    }
	    catch (MalformedURLException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    catch (RemoteException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    catch (NotBoundException e) {
		System.out.println("Cannot find my node " + rmiName);
	    }
	return sensor != null;
    }
}
