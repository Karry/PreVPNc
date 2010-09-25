
package cz.karry.vpnc.connections;

/**
 *
 * @author karry
 */
public interface VpnConnection {

  public enum ConnectionState {
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    FAILED,
    INACTIVE
  }
  
  public void diconnect();

  public ConnectionState getConnectionState();

  public String getLocalAddress();

  public String getLog();

  public boolean addStateListener(ConnectionStateListener connectionStateListenerImpl);

}
