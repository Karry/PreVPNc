

package cz.karry.vpnc;

import cz.karry.vpnc.VpnConnection.ConnectionState;

/**
 *
 * @author karry
 */
public interface ConnectionStateListener {

  public void stateChanged(String profileName, ConnectionState state, int listenerId);

  public int getId();

  public void setId(int newId);

}
