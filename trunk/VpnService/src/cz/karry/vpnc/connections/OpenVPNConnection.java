package cz.karry.vpnc.connections;

import cz.karry.vpnc.LunaService;

/**
 *
 * @author karry
 */
public class OpenVPNConnection extends AbstractVpnConnection {

  public OpenVPNConnection(String name) {
    super(  name,
            String.format("chroot " + LunaService.VPNBOX_DIR + " /usr/sbin/openvpn /etc/openvpn/%s.vpn", name),
            "Initialization Sequence Completed");
  }

  @Override
  public synchronized void start() {
    log += "\n" + "-- you should put your OpenVPN shared key to /media/internal/.vpn/openvpn_"+profileName+".key" + "\n";
    super.start();
  }

  @Override
  protected void handleLog(String line) {
    throw new UnsupportedOperationException("Not supported yet.");
  }
}

