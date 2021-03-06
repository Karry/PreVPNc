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

import cz.karry.vpnc.connections.CiscoConnection;
import cz.karry.vpnc.connections.ConnectionStateListener;
import cz.karry.vpnc.connections.OpenVPNConnection;
import cz.karry.vpnc.connections.PptpConnection;
import cz.karry.vpnc.connections.AbstractVpnConnection;
import cz.karry.vpnc.connections.VpnConnection.ConnectionState;
import ca.canucksoftware.systoolsmgr.CommandLine;
import com.palm.luna.LSException;
import com.palm.luna.service.LunaServiceThread;
import com.palm.luna.service.ServiceMessage;
import cz.karry.vpnc.connections.VpnConnection;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class LunaService extends LunaServiceThread {

  protected static final Object listenerCounterLock = new Object();
  protected static volatile int listenerCounter = 0;
  public static final String APP_ROOT = "/media/cryptofs/apps/usr/palm/applications/cz.karry.vpnc/";
  public static final String VPNBOX_DIR = "/opt/vpnbox/";
  public static final String GATEWAY_REGEXP = "^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$";
  public static final String NETWOK_REGEXP = "^(default|[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}/[0-9]{1,2})$";
  private boolean pptpModulesLoaded = false;
  private final Map<String, VpnConnection> vpnConnections = new HashMap<String, VpnConnection>();
  private final List<ServiceMessage> globalListeners = new LinkedList<ServiceMessage>();
  /**
   * on PC (192.168.0.200) is possible read log by netcat... netcat -lv -p 1234
   */
  private TcpLogger tcpLogger = TcpLogger.getInstance();

  public LunaService() {
    super();

    CommandLine cmd = new CommandLine(String.format("%s/scripts/startup.sh", APP_ROOT));
    if (!cmd.doCmd())
      tcpLogger.log("startup script fails! " + cmd.getResponse());
    else
      tcpLogger.log("startup script successfully edned");
  }

  /**
   * luna-send -t 1 luna://cz.karry.vpnc/random "{}"
   * 
   * @param msg
   * @throws JSONException
   * @throws LSException
   */
  @LunaServiceThread.PublicMethod
  public void random(ServiceMessage msg) throws JSONException, LSException {
    JSONObject reply = new JSONObject();
    reply.put("returnValue", "" + Math.random());
    msg.respond(reply.toString());
  }

  private boolean loadModules() throws IOException {
    if (!pptpModulesLoaded) {
      CommandLine cmd = new CommandLine(String.format("%s/modules/load_modules.sh", APP_ROOT));
      if (!cmd.doCmd())
        throw new IOException(cmd.getResponse());

      pptpModulesLoaded = true;
    }
    return true;
  }

  /**
   * sudo ip route add 192.168.100.0/24 via 192.168.100.1
   * sudo ip route add default via 192.168.100.1
   */
  @LunaServiceThread.PublicMethod
  public void addRoute(ServiceMessage msg) throws JSONException, LSException {

    if ((!msg.getJSONPayload().has("network")) || (!msg.getJSONPayload().has("gateway"))) {
      msg.respondError("1", "Improperly formatted request.");
      return;
    }
    String network = msg.getJSONPayload().getString("network").toLowerCase();
    String gateway = msg.getJSONPayload().getString("gateway").toLowerCase();

    if (!gateway.matches(GATEWAY_REGEXP)) {
      msg.respondError("2", "Bad gateway format.");
      return;
    }
    if (!network.matches(NETWOK_REGEXP)) {
      msg.respondError("3", "Bad network format.");
      return;
    }

    String cmdStr = String.format("ip route add %s via %s", network, gateway);
    CommandLine cmd = new CommandLine(cmdStr);
    if (!cmd.doCmd()) {
      msg.respondError("4", cmd.getResponse());
      return;
    }
    JSONObject reply = new JSONObject();
    reply.put("command", cmdStr);
    msg.respond(reply.toString());
  }

  /**
   * sudo ip route flush 192.168.100.0/24 via 192.168.100.1
   * sudo ip route flush default via 192.168.100.1
   */
  @LunaServiceThread.PublicMethod
  public void delRoute(ServiceMessage msg) throws JSONException, LSException {

    if ((!msg.getJSONPayload().has("network")) || (!msg.getJSONPayload().has("gateway"))) {
      msg.respondError("1", "Improperly formatted request.");
      return;
    }
    String network = msg.getJSONPayload().getString("network").toLowerCase();
    String gateway = msg.getJSONPayload().getString("gateway").toLowerCase();

    if (!gateway.matches(GATEWAY_REGEXP)) {
      msg.respondError("2", "Bad gateway format.");
      return;
    }
    if (!network.matches(NETWOK_REGEXP)) {
      msg.respondError("3", "Bad network format.");
      return;
    }

    String cmdStr = String.format("ip route flush %s via %s", network, gateway);
    CommandLine cmd = new CommandLine(cmdStr);
    if (!cmd.doCmd()) {
      msg.respondError("4", cmd.getResponse());
      return;
    }
    JSONObject reply = new JSONObject();
    reply.put("command", cmdStr);
    msg.respond(reply.toString());
  }

  @LunaServiceThread.PublicMethod
  public void connectionInfo(final ServiceMessage msg) throws JSONException, LSException {
    JSONObject jsonObj = msg.getJSONPayload();
    if (!jsonObj.has("name")) {
      msg.respondError("1", "Improperly formatted request.");
      return;
    }

    String name = jsonObj.getString("name");

    JSONObject reply = new JSONObject();
    reply.put("name", name);
    VpnConnection conn = vpnConnections.get(name);
    ConnectionState state = VpnConnection.ConnectionState.INACTIVE;
    String log = "";

    if (conn != null) {
      state = conn.getConnectionState();
      log = conn.getLog();
      if (state == AbstractVpnConnection.ConnectionState.CONNECTED) {
        reply.put("localAddress", conn.getLocalAddress());
      }
    }

    try {
      reply.put("profileName", name);
      reply.put("state", state);
      reply.put("log", log);
      //tcpLogger.log("refresh info: "+reply.toString());
      msg.respond(reply.toString());
    } catch (LSException ex) {
      tcpLogger.log(ex.getMessage(), ex);
    } catch (JSONException ex) {
      tcpLogger.log(ex.getMessage(), ex);
    }
  }

  @LunaServiceThread.PublicMethod
  public void addEventListener(final ServiceMessage msg) {
    globalListeners.add(msg);
    VpnConnection conn;
    synchronized (vpnConnections) {
      for (String name : vpnConnections.keySet()) {
        conn = vpnConnections.get(name);
        conn.addStateListener(new ConnectionStateListenerImpl(msg, conn, getNextListenerId()));
      }
    }
  }

  private VpnConnection addManagedConnection(String name, VpnConnection connection) {
    synchronized (vpnConnections) {
      for (ServiceMessage listener : globalListeners) {
        connection.addStateListener(new ConnectionStateListenerImpl(listener, connection, getNextListenerId()));
      }
      return vpnConnections.put(name, connection);
    }
  }

  @LunaServiceThread.PublicMethod
  public void listenOnChanges(final ServiceMessage msg) throws JSONException, LSException {
    JSONObject jsonObj = msg.getJSONPayload();
    if (!jsonObj.has("name")) {
      msg.respondError("1", "Improperly formatted request.");
      return;
    }
    String name = jsonObj.getString("name");

    VpnConnection conn = vpnConnections.get(name);
    if (conn != null) {
      tcpLogger.log("add listener for " + name);
      conn.addStateListener(new ConnectionStateListenerImpl(msg, conn, this.getNextListenerId()));
    }
  }

  @LunaServiceThread.PublicMethod
  public void getRegisteredConnections(final ServiceMessage msg) throws JSONException, LSException {
    JSONObject reply = new JSONObject();

    try {
      reply.put("connections", vpnConnections.keySet());
      msg.respond(reply.toString());
    } catch (LSException ex) {
      tcpLogger.log(ex.getMessage(), ex);
    } catch (JSONException ex) {
      tcpLogger.log(ex.getMessage(), ex);
    }
  }

  @LunaServiceThread.PublicMethod
  public void disconnectVpn(final ServiceMessage msg) throws JSONException, LSException {
    JSONObject jsonObj = msg.getJSONPayload();
    if (!jsonObj.has("name")) {
      msg.respondError("1", "Improperly formatted request. (" + jsonObj.toString() + ")");
      return;
    }

    String name = jsonObj.getString("name");
    VpnConnection conn = vpnConnections.get(name);
    if (conn == null) {
      msg.respondError("2", "Connection '" + name + "' is not registered.");
      return;
    }

    conn.addStateListener(new ConnectionStateListenerImpl(msg, conn, this.getNextListenerId()));
    conn.diconnect();
    //msg.respondTrue();
  }

  @LunaServiceThread.PublicMethod
  public void connectVpn(final ServiceMessage msg) throws JSONException, LSException {
    JSONObject jsonObj = msg.getJSONPayload();

    tcpLogger.log("invoke connectVpn " + jsonObj.toString());

    if ((!jsonObj.has("type"))
            || (!jsonObj.has("name"))
            || (!jsonObj.has("display_name"))
            || (!jsonObj.has("configuration"))) {
      msg.respondError("1", "Improperly formatted request. (" + jsonObj.toString() + ")");
      return;
    }

    String type = jsonObj.getString("type");
    String name = jsonObj.getString("name");
    String displayName = jsonObj.getString("display_name");
    JSONObject configuration = jsonObj.getJSONObject("configuration");

    if (!name.matches("^[a-zA-Z]{1}[a-zA-Z0-9]*$")) {
      msg.respondError("2", "Bad session name format.");
      return;
    }

    if (type.toLowerCase().equals("pptp")) {
      String host = configuration.getString("host").replaceAll("\n", "\\\\n");

      String user = configuration.getString("pptp_user").replaceAll("\n", "\\\\n");
      String pass = configuration.getString("pptp_password").replaceAll("\n", "\\\\n");
      String mppe = configuration.getString("pptp_mppe").replaceAll("\n", "\\\\n");
      String mppe_stateful = configuration.getString("pptp_mppe_stateful").replaceAll("\n", "\\\\n");
      connectPptpVpn(msg, name, displayName, host, user, pass, mppe, mppe_stateful);
      return;
    } else if (type.toLowerCase().equals("openvpn")) {
      String host = configuration.getString("host").replaceAll("\n", "\\\\n");

      String topology = configuration.getString("openvpn_topology");
      String protocol = configuration.getString("openvpn_protocol");
      String cipher = configuration.getString("openvpn_cipher");
      this.connectOpenVPN(msg, name, displayName, host, topology, protocol, cipher);
      return;
    } else if (type.toLowerCase().equals("cisco")) {
      String host = configuration.getString("host").replaceAll("\n", "\\\\n");

      String userid = configuration.getString("cisco_userid").replaceAll("\n", "\\\\n");
      String userpass = configuration.getString("cisco_userpass").replaceAll("\n", "\\\\n");
      String groupid = configuration.getString("cisco_groupid").replaceAll("\n", "\\\\n");
      String grouppass = configuration.getString("cisco_grouppass").replaceAll("\n", "\\\\n");
      String userpasstype = configuration.getString("cisco_userpasstype");
      String grouppasstype = configuration.getString("cisco_grouppasstype");
      String domain = configuration.has("cisco_domain")
              && configuration.getString("cisco_domain") != null
              && configuration.getString("cisco_domain").trim().length() > 0
              ? "Domain " + configuration.getString("cisco_domain") : "";
      tcpLogger.log("use domain \"" + domain + "\"");
      this.connectCiscoVpn(msg, name, displayName, host, userid, userpass, userpasstype, groupid, grouppass, grouppasstype, domain);
      return;
    }

    msg.respondError("3", "Undefined vpn type (" + type + ").");
  }

  private void connectOpenVPN(ServiceMessage msg,
          String name,
          String displayName,
          String host,
          String topology,
          String protocol,
          String cipher) throws JSONException, LSException {
    //String.format("chroot /opt/vpnbox/ /usr/sbin/openvpn /tmp/%s.vpn", profileName);
    try {
      String[] arr = new String[6];
      arr[0] = String.format("%s/scripts/write_config_openvpn.sh", APP_ROOT);
      arr[1] = String.format("%s", name);
      arr[2] = String.format("%s", host);
      arr[3] = String.format("%s", topology);
      arr[4] = String.format("%s", protocol);
      arr[5] = String.format("%s", cipher);

      CommandLine cmd = new CommandLine(arr);
      if (!cmd.doCmd())
        throw new IOException(cmd.getResponse());

      tcpLogger.log("config writed");
      OpenVPNConnection conn = new OpenVPNConnection(name, displayName);
      VpnConnection original = addManagedConnection(name, conn);
      if (original != null)
        original.diconnect();

      conn.addStateListener(new ConnectionStateListenerImpl(msg, conn, this.getNextListenerId()));
      conn.start();
      //conn.waitWhileConnecting();
    } catch (Exception ex) {
      msg.respondError("102", "Error while connecting: " + ex.getMessage() + " (" + ex.getClass().getName() + ")");
      return;
    }

  }

  private void connectPptpVpn(ServiceMessage msg, String name,
          String displayName,
          String host, String user, String pass, String mppe, String mppe_stateful) throws JSONException, LSException {
    try {
      if (!loadModules()) {
        msg.respondError("101", "Can't load kernel modules.");
        return;
      }
      tcpLogger.log("modules loaded");

      // write config to peer file, user name and password to secrets file
      String[] arr = new String[7];
      arr[0] = String.format("%s/scripts/write_config_pptp.sh", APP_ROOT);
      arr[1] = String.format("%s", name);
      arr[2] = String.format("%s", host);
      arr[3] = String.format("%s", user);
      arr[4] = String.format("%s", pass);
      arr[5] = String.format("%s", mppe);
      arr[6] = String.format("%s", mppe_stateful);
      CommandLine cmd = new CommandLine(arr);
      if (!cmd.doCmd())
        throw new IOException(cmd.getResponse());

      tcpLogger.log("config writed");
      PptpConnection conn = new PptpConnection(name, displayName);
      VpnConnection original = addManagedConnection(name, conn);
      if (original != null)
        original.diconnect();

      conn.addStateListener(new ConnectionStateListenerImpl(msg, conn, this.getNextListenerId()));
      conn.start();
      //conn.waitWhileConnecting();
    } catch (Exception ex) {
      msg.respondError("102", "Error while connecting: " + ex.getMessage() + " (" + ex.getClass().getName() + ")");
      return;
    }
  }

  private void connectCiscoVpn(
          ServiceMessage msg,
          String name,
          String displayName,
          String host,
          String userid,
          String userpass,
          String userpasstype,
          String groupid,
          String grouppass,
          String grouppasstype,
          String domain) throws JSONException, LSException {

    try {
      String[] arr = new String[10];
      arr[0] = String.format("%s/scripts/write_config_cisco.sh", APP_ROOT);
      arr[1] = String.format("%s", name);
      arr[2] = String.format("%s", host);
      arr[3] = String.format("%s", userid);
      arr[4] = String.format("%s", userpass);
      arr[5] = String.format("%s", userpasstype);
      arr[6] = String.format("%s", groupid);
      arr[7] = String.format("%s", grouppass);
      arr[8] = String.format("%s", grouppasstype);
      arr[9] = String.format("%s", domain);

      CommandLine cmd = new CommandLine(arr);
      if (!cmd.doCmd())
        throw new IOException(cmd.getResponse());

      tcpLogger.log("config writed");
      CiscoConnection conn = new CiscoConnection(name, displayName);
      VpnConnection original = addManagedConnection(name, conn);
      if (original != null)
        original.diconnect();

      conn.addStateListener(new ConnectionStateListenerImpl(msg, conn, this.getNextListenerId()));
      conn.start();
      //conn.waitWhileConnecting();
    } catch (Exception ex) {
      msg.respondError("102", "Error while connecting: " + ex.getMessage() + " (" + ex.getClass().getName() + ")");
      return;
    }

  }

  private int getNextListenerId() {
    int id = 0;
    synchronized (listenerCounterLock) {
      id = LunaService.listenerCounter++;
    }
    return id;
  }

  class ConnectionStateListenerImpl implements ConnectionStateListener {

    private final ServiceMessage msg;
    private final VpnConnection conn;
    private int id;

    public ConnectionStateListenerImpl(ServiceMessage msg, VpnConnection conn, int myId) {
      this.msg = msg;
      this.conn = conn;
      this.id = myId;
    }

    public void stateChanged(VpnConnection connection, ConnectionState state) {
      tcpLogger.log("connection " + connection.getProfileName() + ": " + state);
      JSONObject reply = new JSONObject();
      try {
        reply.put("profileName", connection.getProfileName());
        reply.put("displayName", connection.getDisplayName());
        reply.put("state", state);
        reply.put("stateChanged", true);
        reply.put("listenerId", this.id);
        reply.put("log", conn.getLog());
        if (state == AbstractVpnConnection.ConnectionState.CONNECTED) {
          reply.put("localAddress", conn.getLocalAddress());
        }
        msg.respond(reply.toString());
        tcpLogger.log("     " + reply.toString());
      } catch (LSException ex) {
        tcpLogger.log(ex.getMessage(), ex);
      } catch (JSONException ex) {
        tcpLogger.log(ex.getMessage(), ex);
      }
    }
  }
}
