package net.sesense;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SimSensorsInterface extends Remote {
    void shutDown() throws RemoteException;
    boolean isRunning() throws RemoteException;
}
