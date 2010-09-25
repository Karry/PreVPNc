package cz.karry.vpnc.connections;

import cz.karry.vpnc.DnsManager;
import cz.karry.vpnc.LunaService;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author karry
 */
public class PptpConnection extends AbstractVpnConnection {

  private List<String> dnsAdresses = new LinkedList<String>();

  public PptpConnection(String name) {
    super(  name,
            String.format("chroot " + LunaService.VPNBOX_DIR + " /usr/sbin/pppd call %s", name),
            "local  IP address");
  }

  protected void handleLog(String line) {
    if (line.startsWith("local  IP address"))
      this.localAddress = line.substring("local  IP address".length());

    if (line.startsWith("primary   DNS address ")){
      dnsAdresses.add( line.substring("primary   DNS address ".length()) );
      DnsManager.getInstance().addNameserver(dnsAdresses.toArray(new String[dnsAdresses.size()]), this);
    }
    if (line.startsWith("secondary DNS address ")){
      dnsAdresses.add( line.substring("secondary DNS address ".length()) );
      DnsManager.getInstance().addNameserver(dnsAdresses.toArray(new String[dnsAdresses.size()]), this);
    }
  }

}

