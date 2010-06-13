package cz.karry.vpnc;

import ca.canucksoftware.systoolsmgr.CommandLine;
import com.palm.luna.LSException;
import com.palm.luna.service.LunaServiceThread;
import com.palm.luna.service.ServiceMessage;
import cz.karry.vpnc.VpnConnection.ConnectionState;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class LunaService extends LunaServiceThread {

  private static final String APP_ROOT = "/media/cryptofs/apps/usr/palm/applications/cz.karry.vpnc/";
  private static final String GATEWAY_REGEXP = "^[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}$";
  private static final String NETWOK_REGEXP = "^(default|[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}/[0-9]{1,2})$";
  private boolean pptpModulesLoaded = false;
  private final Map<String, VpnConnection> vpnConnections = new HashMap<String, VpnConnection>();
  /**
   * on PC (192.168.0.200) is possible read log by netcat... netcat -lv -p 1234
   */
  private TcpLogger tcpLogger = new TcpLogger("192.168.0.200", 1234, true);

  public LunaService() {
    super();
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
      if (state == VpnConnection.ConnectionState.CONNECTED) {
        reply.put("localAddress", conn.getLocalAddress());
      }
    }

    try {
      reply.put("profileName", name);
      reply.put("state", state);
      reply.put("log", log);
      tcpLogger.log("refresh info: "+reply.toString());
      msg.respond(reply.toString());
    } catch (LSException ex) {
      tcpLogger.log(ex.getMessage(), ex);
    } catch (JSONException ex) {
      tcpLogger.log(ex.getMessage(), ex);
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
  public void connectVpn(final ServiceMessage msg) throws JSONException, LSException {
    JSONObject jsonObj = msg.getJSONPayload();
    if ((!jsonObj.has("type"))
            || (!jsonObj.has("name"))
            || (!jsonObj.has("host"))
            || (!jsonObj.has("user"))
            || (!jsonObj.has("pass"))) {
      msg.respondError("1", "Improperly formatted request. ("+jsonObj.toString()+")");
      return;
    }

    String type = jsonObj.getString("type");
    String name = jsonObj.getString("name");
    String host = jsonObj.getString("host");
    String user = jsonObj.getString("user");
    String pass = jsonObj.getString("pass");

    if (!name.matches("^[a-zA-Z]{1}[a-zA-Z0-9]*$")) {
      msg.respondError("2", "Bad session name format.");
      return;
    }

    if (type.equals("pptp")) {
      connectPptpVpn(msg, name, host, user, pass);
      return;
    }

    msg.respondError("3", "Undefined vpn type (" + type + ").");
  }

  private void connectPptpVpn(final ServiceMessage msg, String name, String host, String user, String pass) throws JSONException, LSException {
    try {
      if (!loadModules()) {
        msg.respondError("101", "Can't load kernel modules.");
        return;
      }
      tcpLogger.log("modules loaded");

      // write user name and password to secrets file
      String[] arr = new String[5];
      arr[0] = String.format("%s/scripts/write_config.sh", APP_ROOT);
      arr[1] = String.format("%s", name);
      arr[2] = String.format("%s", host);
      arr[3] = String.format("%s", user);
      arr[4] = String.format("%s", pass);
      CommandLine cmd = new CommandLine(arr);
      if (!cmd.doCmd())
        throw new IOException(cmd.getResponse());

      tcpLogger.log("config writed");
      final PptpConnection conn = new PptpConnection(name);
      VpnConnection original = vpnConnections.put(name, conn);
      if (original != null)
        original.diconnect();

      conn.addStateListener(new ConnectionStateListener() {

        public void stateChanged(String profileName, ConnectionState state) {
          tcpLogger.log("connection " + profileName + ": " + state);
          JSONObject reply = new JSONObject();
          try {
            reply.put("profileName", profileName);
            reply.put("state", state);
            reply.put("stateChanged", true);
            reply.put("log", conn.getLog());
            if (state == VpnConnection.ConnectionState.CONNECTED) {
              reply.put("localAddress", conn.getLocalAddress());
            }
            msg.respond(reply.toString());
          } catch (LSException ex) {
            tcpLogger.log(ex.getMessage(), ex);
          } catch (JSONException ex) {
            tcpLogger.log(ex.getMessage(), ex);
          }
        }
      });
      conn.start();
      //conn.waitWhileConnecting();
    } catch (Exception ex) {
      msg.respondError("102", "Error while connecting: " + ex.getMessage() + " (" + ex.getClass().getName() + ")");
      return;
    }
  }

  /*
  private String[] readLines(File f) throws IOException {
  InputStreamReader in = null;
  try {
  in = new InputStreamReader(new BufferedInputStream(new FileInputStream(f)));
  int nch;
  char[] buff = new char[4096];
  String content = "";
  while ((nch = in.read(buff, 0, buff.length)) != -1) {
  content += new String(buff, 0, nch);
  }
  return content.split("\n");
  } finally {
  if (in != null)
  in.close();
  }
  }
   * 
   */
}
