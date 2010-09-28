

package cz.karry.vpnc.connections;

import cz.karry.vpnc.connections.VpnConnection.ConnectionState;

/**
 *
 * @author karry
 */
public interface ConnectionStateListener {

  public void stateChanged(VpnConnection connection, ConnectionState state);

}
