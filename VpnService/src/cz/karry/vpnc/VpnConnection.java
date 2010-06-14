package cz.karry.vpnc;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author karry
 */
abstract class VpnConnection extends Thread {

  private volatile ConnectionState connectionState = ConnectionState.INACTIVE;
  protected final String profileName;
  protected String log = "";
  protected String localAddress;
  protected final List<ConnectionStateListener> stateListeners = new LinkedList<ConnectionStateListener>();
  protected final static Object listenerCounterLock = new Object();
  protected static volatile int listenerCounter = 0;

  public enum ConnectionState {
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    FAILED,
    INACTIVE
  }

  public VpnConnection(String name) {
    this.profileName = name;
  }

  public String getProfileName() {
    return this.profileName;
  }

  public String getLog() {
    return this.log;
  }

  public String getLocalAddress() {
    return localAddress;
  }

  /**
   * return vpn connection state
   *
   * @return
   */
  public ConnectionState getConnectionState() {
    return this.connectionState;
  }

  public void setConnectionState(ConnectionState state) {
    if (state != this.connectionState){
      for (ConnectionStateListener listener : stateListeners){
        listener.stateChanged(profileName,state,listener.getId());
      }
    }
    this.connectionState = state;
  }

  /**
   * blocking method while vpn si connecting
   */
  abstract public void waitWhileConnecting() throws InterruptedException;

  abstract public void diconnect();

  public boolean addStateListener(ConnectionStateListener l) {
    int id = 0;
    synchronized( listenerCounterLock ){
      id = VpnConnection.listenerCounter++;
    }
    l.setId(id);
    return stateListeners.add(l);
  }

  public boolean removeConnectionListener(ConnectionStateListener l) {
    return stateListeners.remove(l);
  }

}
