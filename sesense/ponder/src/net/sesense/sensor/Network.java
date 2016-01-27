// adapted from BSNController from ponder2 tutorial

package net.sesense.sensor;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import net.sesense.sensor.Sensor;
import net.sesense.sensor.Sensor.SensorType;

public class Network {
    private Map<String, Sensor> sensors;
    private String networkName;

    public Network(String networkName) {
	//super();
	this.networkName = networkName;
	sensors = new HashMap<String, Sensor>();
    }

    public void shutDown() {
	Iterator i = sensors.values().iterator();

	while (i.hasNext()) {
	    Sensor sensor = (Sensor) i.next();
	    sensor.shutDown();
	}
    }

    public void addSensor(SensorType type, String name) {
	Sensor sensor = new Sensor();

	sensors.put(name, sensor);

	sensor.init(type, networkName, name);

	sensor.start(); // thread fork

	sensor.startSensor();
    }
}
