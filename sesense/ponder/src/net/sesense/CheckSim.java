package net.sesense;

import net.sesense.SimSensors;
import net.sesense.sensor.Network;
import net.sesense.sensor.Sensor.SensorType;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;

import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

// based no CheckRMI from ponder2 java

public class CheckSim {
    private static boolean isRunning() {
	boolean running = false;

	try {
	    SimSensorsInterface control = (SimSensorsInterface) Naming.lookup("//localhost/simsensors");
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

    private static void shutDown() {
	System.out.println("shutting down the sim");

	try {
	    SimSensorsInterface control = (SimSensorsInterface) Naming.lookup("//localhost/simsensors");
	    control.shutDown();
	} catch (Exception e) {
	    System.out.println("shutDown: " + e);
	}
    }

    public static void main(String[] args) {
	if (args.length >= 1) {
	    if (args[0].equals("shutdown")) {
		shutDown();
		return;
	    }
	}

	if (isRunning()) {
	    System.out.println("sim already running");
	    return;
	}

	SimSensors temp = new SimSensors();
	temp.run();
    }
}
