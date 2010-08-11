package cz.karry.vpnc;

/**
 *
 * @author karry
 */
public class CiscoConnection extends AbstractVpnConnection {


  public CiscoConnection(String name) {
    super(  name,
            String.format("chroot /opt/vpnbox/ /usr/sbin/vpnc --debug 1 --no-detach /tmp/%s", name),
            "vpnc version" );
  }

  @Override
  protected void handleLog(String line) {
    // nothing to do 
  }


}

