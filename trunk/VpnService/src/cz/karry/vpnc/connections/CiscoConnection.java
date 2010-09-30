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
public class CiscoConnection extends AbstractVpnConnection {

  private List<String> dnsAdresses = new LinkedList<String>();

  public CiscoConnection(String name, String displayName) {
    super(name,
            displayName,
            String.format("chroot " + LunaService.VPNBOX_DIR + " /usr/sbin/vpnc --debug 2  --dpd-idle 10 --no-detach /etc/vpnc/%s", name),
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
