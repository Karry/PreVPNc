package cz.karry.vpnc;

import ca.canucksoftware.systoolsmgr.CommandLine;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author karry
 */
abstract class AbstractVpnConnection extends Thread implements VpnConnection{

  private volatile ConnectionState connectionState = ConnectionState.INACTIVE;
  protected final String profileName;
  protected String log = "";
  protected String localAddress;
  protected final List<ConnectionStateListener> stateListeners = new LinkedList<ConnectionStateListener>();
  protected final static Object listenerCounterLock = new Object();
  protected static volatile int listenerCounter = 0;
  protected int MAX_LOG_SIZE = 2048;

  protected final Object connectionLock = new Object();
  protected int returnCode = 0;
  protected Process process;
  private final String connectedLogPart;
  private final String cmd;

  public AbstractVpnConnection(String name, String command, String connectedLogPart) {
    this.profileName = name;
    this.cmd = command;
    this.connectedLogPart = connectedLogPart;
  }

  public String getProfileName() {
    return this.profileName;
  }

  public String getLog() {
    return this.log;
  }

  public String getLocalAddress() {
    return localAddress;
  }

  /**
   * return vpn connection state
   *
   * @return
   */
  public ConnectionState getConnectionState() {
    return this.connectionState;
  }

  public void setConnectionState(ConnectionState state) {
    if (state != this.connectionState){
      for (ConnectionStateListener listener : stateListeners){
        listener.stateChanged(profileName,state,listener.getId());
      }
    }
    this.connectionState = state;
  }

  @Override
  public void run() {
    BufferedReader stdout = null;
    BufferedReader stderr = null;
    TcpLogger.getInstance().log("   --- run command: "+cmd);
    String[] command = cmd.split(" ");

    try {
      try {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        this.process = pb.start();
        stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
        stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        while (getConnectionState() == ConnectionState.CONNECTING
                || getConnectionState() == ConnectionState.CONNECTED) {

          String line = null;
          line = stdout.readLine();

          if (line == null ) { // || line.length() <= 0
            process.waitFor();

            // I know, we set redirectErrorStream, but it dosn't work correctly in all cases
            // so, we read all stderr now...
            while ((line = stderr.readLine()) != null){
              this.log += "\n" + line;
            }
            
            this.returnCode = process.exitValue();
            synchronized (connectionLock) {
              if (returnCode == 0 || (getConnectionState() == ConnectionState.DISCONNECTING)) {
                this.setConnectionState(ConnectionState.INACTIVE);
              } else {
                this.setConnectionState(ConnectionState.FAILED);
              }
            }
          } else {
            handleLog(line);
            if (line.startsWith( connectedLogPart )) {
              synchronized (connectionLock) {
                this.setConnectionState(ConnectionState.CONNECTED);
                connectionLock.notifyAll();
              }
            }
            this.log += "\n" + line;
            if (log.length() > MAX_LOG_SIZE)
              log = log.substring(log.length() - MAX_LOG_SIZE);
          }
        }
        synchronized (connectionLock) {
          connectionLock.notifyAll();
        }
      } finally {
        if (stdout != null)
          stdout.close();
        if (stderr != null)
          stderr.close();
      }
    } catch (Exception e) {
      synchronized (connectionLock) {
        connectionLock.notifyAll();
      }
      this.log += "\n\nException occured: " + e.getMessage() + " (" + e.getClass().getName() + ")";
    }
  }


  public void diconnect() {
    synchronized (connectionLock) {
      if (getConnectionState() == ConnectionState.CONNECTING
              || getConnectionState() == ConnectionState.CONNECTED) {

        setConnectionState(ConnectionState.DISCONNECTING);
        if (this.process != null)
          this.process.destroy();
      }
    }
  }

  public boolean addStateListener(ConnectionStateListener l) {
    int id = 0;
    synchronized( listenerCounterLock ){
      id = AbstractVpnConnection.listenerCounter++;
    }
    l.setId(id);
    return stateListeners.add(l);
  }

  public boolean removeConnectionListener(ConnectionStateListener l) {
    return stateListeners.remove(l);
  }

  /**
   * blocking method while vpn si connecting
   */
  public void waitWhileConnecting() throws InterruptedException {
    synchronized (connectionLock) {
      if (getConnectionState() == ConnectionState.CONNECTING)
        connectionLock.wait();
    }
  }

  @Override
  public synchronized void start() {
    synchronized (connectionLock) {
      this.setConnectionState(ConnectionState.CONNECTING);
    }
    super.start();
  }

  public void updateDns(boolean flush, String nameserver){
    String str = String.format("%s/scripts/update_dns.sh %s %s", LunaService.APP_ROOT, flush ? "flush" : "noflush", nameserver);
    if (str != null){
      try {
        CommandLine cmd = new CommandLine(str);
        if (!cmd.doCmd())
          throw new IOException("Command "+str +" failed "+cmd.getResponse());
      } catch (Exception e) {
        TcpLogger.getInstance().log(e.getMessage(), e);
      }
    }
  }

  abstract protected void handleLog(String line);
}
