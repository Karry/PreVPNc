package cz.karry.vpnc;

import ca.canucksoftware.systoolsmgr.CommandLine;
import java.io.IOException;

/**
 *
 * @author karry
 */
public class CiscoConnection extends AbstractVpnConnection {

  public CiscoConnection(String name) {
    super(name,
            String.format("chroot /opt/vpnbox/ /usr/sbin/vpnc --debug 2 --no-detach /tmp/%s", name),
            "vpnc version");
  }

  @Override
  protected void handleLog(String line) {
    // nothing to do 
  }

  @Override
  public void setConnectionState(ConnectionState state) {
    super.setConnectionState(state);
    if (state == ConnectionState.CONNECTED) {
      updateDns(false, "127.0.0.1");
    } else if (state == ConnectionState.FAILED || state == ConnectionState.INACTIVE){
      updateDns(true, "127.0.0.1");
    }
  }
}
