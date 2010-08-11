package cz.karry.vpnc;

/**
 *
 * @author karry
 */
public class PptpConnection extends AbstractVpnConnection {

  public PptpConnection(String name) {
    super(  name,
            String.format("chroot /opt/vpnbox /usr/sbin/pppd call %s", name),
            "local  IP address");
  }

  protected void handleLog(String line) {
    if (line.startsWith("local  IP address"))
      this.localAddress = line.substring("local  IP address".length());
  }
}

