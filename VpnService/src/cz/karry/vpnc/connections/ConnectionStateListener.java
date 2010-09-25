

package cz.karry.vpnc.connections;

import cz.karry.vpnc.connections.VpnConnection.ConnectionState;

/**
 *
 * @author karry
 */
public interface ConnectionStateListener {

  public void stateChanged(String profileName, ConnectionState state);

}
