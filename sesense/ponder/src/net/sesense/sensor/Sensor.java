// based on BSN from ponder2 tutorial

package net.sesense.sensor;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Dictionary;
import java.util.Hashtable;

import java.util.Random;

public class Sensor extends Thread {
    private boolean exiting = false;

    public enum SensorType {
	TEMP, PRESS
	    };

    private Thread thread = null;
    boolean running = false;
    
    public int markValue = 0;
    public String markTime = "";

    int delay = 1000;

    String sensorType;

    String sensorName;
    String networkName;
    String rmiName;

    Random generator = null;
    
    private SensorReceiver sensorReceiver;

    public Sensor() {
	super();

	setDaemon(true);

	generator = new Random();
    }

    public void shutDown() {
	//System.out.println("Sensor: interrupting");
	exiting = true;

	try {
	    //Registry registry = LocateRegistry.getRegistry();
	    //registry.unbind(rmiName);
	    UnicastRemoteObject.unexportObject(sensorReceiver, true);

	} catch (Exception e) {
	    System.err.println("shutDown: " + e);
	    e.printStackTrace();
	}

	interrupt();
    }

    public void init(SensorType sensorType, String netName, String name) {
	this.sensorType = sensorType.toString();
	this.sensorName = name;
	this.networkName = netName;

	this.rmiName = netName + "_" + name;

	try {
	    this.sensorReceiver = new SensorReceiver();
	    Naming.rebind(rmiName, this.sensorReceiver);
	}

	catch (RemoteException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	catch (MalformedURLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	switch (sensorType) {
	case TEMP:
	    
	    break;
	case PRESS:
	    
	    break;
	}
    }

    public void startSensor() {
	Boolean wasRunning = running;
	running = true;

	if (! wasRunning) {
	    interrupt();
	}
    }

    public void stopSensor() {
	this.running = false;
    }

    public void mark(String time, int value) {
	markValue = value;
	markTime  = time;

	System.out.println(rmiName + ": mark: " + value + " at " + time);
    }

    public int getRate() {
	return delay;
    }

    public int getValue() {
	return markValue;
    }

    public String getTime() {
	return markTime;
    }

    private void setRate(int newDelay) {
	int oldDelay = delay;
	
	delay = newDelay;
	
	if (oldDelay > newDelay)
	    interrupt();
    }


    public void sample() {
	mark("right now", generator.nextInt(100));
    }

    public void run() {
	while (! exiting) {
	    System.out.println("loop");
	    try {
		if (running) {
		    sample();
		}
		
		if (running) {
		    Thread.sleep(delay);
		} else {
		    Thread.sleep(1000000);
		}
	    } catch (InterruptedException e) {
		//System.out.println("Sensor thread interrupted: " + e);
	    }
	}

	//System.out.println("sensor thread exiting");
    }

    public class SensorReceiver extends UnicastRemoteObject implements SensorInterface {
	protected SensorReceiver() throws RemoteException {
	    super();
	    // TODO Auto-generated constructor stub
	}

	public void setRate(int value) throws RemoteException {
	    Sensor.this.setRate(value);
	}

	public int getRate() throws RemoteException {
	    int result = Sensor.this.getRate();
	    return result;
	}

	public int getValue() throws RemoteException {
	    int result = Sensor.this.getValue();
	    return result;
	}

	public void start() throws RemoteException {
	    Sensor.this.startSensor();
	}

	public void stop() throws RemoteException {
	    Sensor.this.stopSensor();
	}

	public boolean running() throws RemoteException {
	    return Sensor.this.running;
	}

	public void shutDown() throws RemoteException {
	    Sensor.this.shutDown();
	}
    }
}
