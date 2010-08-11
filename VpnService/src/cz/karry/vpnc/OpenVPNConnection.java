package cz.karry.vpnc;

/**
 *
 * @author karry
 */
public class OpenVPNConnection extends AbstractVpnConnection {

  public OpenVPNConnection(String name) {
    super(  name,
            String.format("chroot /opt/vpnbox/ /usr/sbin/openvpn /tmp/%s.vpn", name),
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

