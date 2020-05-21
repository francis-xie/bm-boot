package com.emis.admin;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface emisNode extends Remote
{
    emisNode [] getChildNode () throws RemoteException;
    emisProperty [] getProperties() throws RemoteException;
    emisFunction [] getFunctions () throws RemoteException;
    String getNodeName () throws RemoteException;
    boolean isRunning() throws RemoteException;
}
