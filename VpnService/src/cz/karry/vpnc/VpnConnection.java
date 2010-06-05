
package cz.karry.vpnc;

/**
 *
 * @author karry
 */
abstract class VpnConnection extends Thread{

  protected volatile ConnectionState connectionState = ConnectionState.INACTIVE;
  protected final String profileName;
  protected String log = "";
  protected String localAddress;

  public enum ConnectionState{
    CONNECTING,
    CONNECTED,
    FAILED,
    INACTIVE
  }

  public VpnConnection(String name){
    this.profileName = name;
  }

  public String getProfileName(){
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

  /**
   * blocking method while vpn si connecting
   */
  abstract public void waitWhileConnecting() throws InterruptedException;
  abstract public void diconnect();

}
