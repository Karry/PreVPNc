/**
 * Copyright (c) 2010, Lukáš Karas <lukas.karas@centrum.cz>
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms and conditions of the GNU General Public License,
 * version 2, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin St - Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */
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
public class PptpConnection extends AbstractVpnConnection {

  private List<String> dnsAdresses = new LinkedList<String>();

  public PptpConnection(String name, String displayName) {
    super(  name, displayName,
            String.format("chroot " + LunaService.VPNBOX_DIR + " /usr/sbin/pppd call %s", name),
            "local  IP address");
  }

  protected void handleLog(String line) {
    if (line.startsWith("local  IP address"))
      this.localAddress = line.substring("local  IP address".length());

    if (line.startsWith("primary   DNS address ")){
      dnsAdresses.add( line.substring("primary   DNS address ".length()) );
      DnsManager.getInstance().addNameserver2(dnsAdresses.toArray(new String[dnsAdresses.size()]), (DnsSource) this);
    }
    if (line.startsWith("secondary DNS address ")){
      dnsAdresses.add( line.substring("secondary DNS address ".length()) );
      DnsManager.getInstance().addNameserver2(dnsAdresses.toArray(new String[dnsAdresses.size()]), (DnsSource) this);
    }
  }

}

