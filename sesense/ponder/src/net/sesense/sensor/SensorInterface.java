// based on BSNInterface from ponder2 tutorial

package net.sesense.sensor;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SensorInterface extends Remote {
    public void setRate(int rate) throws RemoteException;
    public int getRate() throws RemoteException;
    public int getValue() throws RemoteException;
    public void start() throws RemoteException;
    public void stop() throws RemoteException;
    public boolean running() throws RemoteException;
    public void shutDown() throws RemoteException;
}
