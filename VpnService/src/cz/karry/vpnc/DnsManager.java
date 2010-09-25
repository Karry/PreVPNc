package cz.karry.vpnc;

import cz.karry.vpnc.connections.ConnectionStateListener;
import cz.karry.vpnc.connections.VpnConnection;
import cz.karry.vpnc.connections.VpnConnection.ConnectionState;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author karry
 */
public class DnsManager {

  public static final String RESOLVCONF_FILE = LunaService.VPNBOX_DIR + "/etc/resolv.conf";
  private static DnsManager instance;
  private final Map<Object, String[]> nameservers = new HashMap<Object, String[]>();
  private final Object lock = new Object();

  public static DnsManager getInstance() {
    if (instance == null)
      instance = new DnsManager();
    return instance;
  }

  private DnsManager() {
    synchronized (lock) {
      String[] defaultNameserver = {"127.0.0.1"};
      nameservers.put(this, defaultNameserver);
      updateResolvConf();
    }
  }

  public String[] addNameserver(String[] ip, final VpnConnection who) {
    String all = "";
    for (int i=0; i<ip.length; i++)
      all += (i==ip.length-1)? ip[i]: ip[i]+", ";
    TcpLogger.getInstance().log(" connection "+who+" add dns servers: "+all);

    synchronized (lock) {
      who.addStateListener(new ConnectionStateListener() {

        public void stateChanged(String profileName, ConnectionState state) {
          synchronized (lock) {
            if (state == ConnectionState.INACTIVE || state == ConnectionState.FAILED) {
              nameservers.remove(who);
              updateResolvConf();
            }

          }
        }
      });
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
}
