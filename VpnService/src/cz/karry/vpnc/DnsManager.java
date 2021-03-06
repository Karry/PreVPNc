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
package cz.karry.vpnc;

import cz.karry.vpnc.connections.ConnectionStateListener;
import cz.karry.vpnc.connections.VpnConnection;
import cz.karry.vpnc.connections.VpnConnection.ConnectionState;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author karry
 */
public class DnsManager implements Comparator<DnsSource>, DnsSource {

  public static final String RESOLVCONF_FILE = LunaService.VPNBOX_DIR + "/etc/resolv.conf";
  private static DnsManager instance;

  public static DnsManager getInstance() {
    if (instance == null)
      instance = new DnsManager();
    return instance;
  }

  private final Map<DnsSource, String[]> nameservers = new TreeMap<DnsSource, String[]>(this);
  private final Object lock = new Object();
  private int order;

  private DnsManager() {
    synchronized (lock) {
      String[] defaultNameserver = {"127.0.0.1"};
      addNameserver2(defaultNameserver, this);
    }
  }

  public String[] addNameserver2(String[] ip, final DnsSource who) {
    String all = "";
    for (int i = 0; i < ip.length; i++)
      all += (i == ip.length - 1) ? ip[i] : ip[i] + ", ";
    TcpLogger.getInstance().log(" object " + who + " add dns servers: " + all);

    synchronized (lock) {
      if (who instanceof VpnConnection) {
        ((VpnConnection) who).addStateListener(new ConnectionStateListener() {

          public void stateChanged(VpnConnection connection, ConnectionState state) {
            synchronized (lock) {
              if (state == ConnectionState.INACTIVE || state == ConnectionState.FAILED) {
                TcpLogger.getInstance().log(" remove dns records from " + who + "");
                nameservers.remove(who);
                updateResolvConf();
              }

            }
          }
        });
      }
      who.setOrder(nameservers.size());
      String[] old = nameservers.put(who, ip);
      updateResolvConf();
      return old;
    }
  }

  private void updateResolvConf() {
    String[] arr;
    synchronized (lock) {
      String content = "";
      for (Object adder : nameservers.keySet()) {
        content += "# added by " + adder.toString() + "\n";
        arr = nameservers.get(adder);
        for (String ip : arr) {
          content += "nameserver " + ip + "\n";
        }
      }
      try {
        OutputStream os = null;
        try {
          os = new FileOutputStream(RESOLVCONF_FILE);
          os.write(content.getBytes());
        } finally {
          if (os != null)
            os.close();
        }
      } catch (IOException ex) {
        TcpLogger.getInstance().log("write to resolv.conf file failed", ex);
      }
    }
  }

  public int compare(DnsSource o1, DnsSource o2) {
    return o2.getOrder() - o1.getOrder();
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public int getOrder() {
    return this.order;
  }
}
