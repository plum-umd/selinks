package net.sesense;

import net.sesense.sensor.Network;
import net.sesense.sensor.Sensor.SensorType;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class SimSensors extends Thread {
    public static String rmiName = "simsensors";

    private SimSensorsInterface receiver = null;

    private Map<String, Network> networks;

    public SimSensors() {
	super();

	networks = new HashMap<String, Network>();

	try {
	    receiver = new SimSensorsReceiver();
	} catch (Exception e) {
	    System.out.println("SimSensors: " + e);
	}
    }

    private class SimSensorsReceiver extends UnicastRemoteObject implements SimSensorsInterface {
	public SimSensorsReceiver() throws RemoteException {
	    super();
	}

	public boolean isRunning() {
	    return SimSensors.this.isRunning();
	}
	public void shutDown() {
	    SimSensors.this.shutDown();
	}
    }

    public boolean isRunning() {
	return true;
    }

    public void shutDown() {
	//System.out.println("shutting down sensor simulation");
	unregister();

	Iterator i = networks.values().iterator();

	while (i.hasNext()) {
	    Network network = (Network) i.next();
	    network.shutDown();
	}

	interrupt();
    }

    private void unregister () {
	//System.out.println("unregistering from rmi");

	try {
	    Registry registry = LocateRegistry.getRegistry();
	    registry.unbind(rmiName);
	    UnicastRemoteObject.unexportObject(receiver, true);
	} catch (Exception e) {
	    System.err.println("unregister: " + e);
	    e.printStackTrace();
	}
    }

    private void register() {
	try {
	    Registry registry = LocateRegistry.getRegistry();
	    registry.rebind(rmiName, receiver);

	    //System.err.println("sensor simulation receiver ready in rmi");
	} catch (Exception e) {
	    System.err.println("register: " + e);
	    e.printStackTrace();
	}
    }

    public Network addNetwork(String networkName) {
	Network temp = new Network(networkName);

	networks.put(networkName, temp);

	return temp;
    }

    public void run() {
	Network a = addNetwork("net001");

	a.addSensor(SensorType.TEMP, "temp001");
	a.addSensor(SensorType.TEMP, "temp002");
	a.addSensor(SensorType.TEMP, "temp003");

	register();

	System.out.println("sensor simulation started");

	try {
	    while (true) {
		Thread.sleep(100000);
	    }
	} catch (Exception e) {
	    //System.out.println("run: " + e);
	}

	System.out.println("sensor simulation exiting");
    }

    private static boolean isRunningRemote() {
	boolean running = false;

	try {
	    SimSensorsInterface control = (SimSensorsInterface) Naming.lookup(rmiName);
	    running = control.isRunning();

	} catch (RemoteException e) {
	    running = false;
	} catch (NotBoundException e) {
	    running = false;
	} catch (Exception e) {
	    System.out.println("running: " + e);
	}

	return running;
    }

    private static void shutDownRemote() {
	//System.out.println("shutting down the sim");

	try {
	    SimSensorsInterface control = (SimSensorsInterface) Naming.lookup(rmiName);
	    control.shutDown();
	} catch (Exception e) {
	    System.out.println("shutDown: " + e);
	}
    }

    public static void main(String[] args) {
	if (args.length >= 1) {
	    if (args[0].equals("shutdown")) {
		shutDownRemote();
		return;
	    }
	}

	if (isRunningRemote()) {
	    System.out.println("sim already running");
	    return;
	}

	SimSensors temp = new SimSensors();
	temp.start();
    }
}
