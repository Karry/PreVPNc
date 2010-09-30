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

import cz.karry.vpnc.LunaService;

/**
 *
 * @author karry
 */
public class OpenVPNConnection extends AbstractVpnConnection {

  public OpenVPNConnection(String name, String displayName) {
    super(  name,
            displayName,
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

