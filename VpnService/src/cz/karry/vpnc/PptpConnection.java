package cz.karry.vpnc;

/**
 *
 * @author karry
 */
public class PptpConnection extends AbstractVpnConnection {

  private String dns;

  public PptpConnection(String name) {
    super(  name,
            String.format("chroot /opt/vpnbox /usr/sbin/pppd call %s", name),
            "local  IP address");
  }

  protected void handleLog(String line) {
    if (line.startsWith("local  IP address"))
      this.localAddress = line.substring("local  IP address".length());

    if (line.startsWith("primary   DNS address ")){
      this.dns = line.substring("primary   DNS address ".length());
      updateDns(true, dns);
    }
  }

  @Override
  public void setConnectionState(ConnectionState state) {
    super.setConnectionState(state);
    if (state == ConnectionState.FAILED || state == ConnectionState.INACTIVE){
      updateDns(true, "127.0.0.1");
    }
  }
}

