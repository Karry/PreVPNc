package cz.karry.vpnc.connections;

import cz.karry.vpnc.DnsManager;
import cz.karry.vpnc.DnsSource;
import cz.karry.vpnc.LunaService;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author karry
 */
public class CiscoConnection extends AbstractVpnConnection {

  private List<String> dnsAdresses = new LinkedList<String>();

  public CiscoConnection(String name, String displayName) {
    super(name,
            displayName,
            String.format("chroot " + LunaService.VPNBOX_DIR + " /usr/sbin/vpnc --debug 2 --no-detach /etc/vpnc/%s", name),
            "vpnc version");
  }

  @Override
  protected void handleLog(String line) {
    if (line.startsWith("nameserver ")){
      dnsAdresses.add( line.substring("nameserver ".length()) );
      DnsManager.getInstance().addNameserver2(dnsAdresses.toArray(new String[dnsAdresses.size()]), (DnsSource)this);
    }
  }
}
