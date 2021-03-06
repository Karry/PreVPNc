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

import cz.karry.vpnc.DnsSource;
import cz.karry.vpnc.TcpLogger;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author karry
 */
public abstract class AbstractVpnConnection extends Thread implements VpnConnection, DnsSource{

  private volatile ConnectionState connectionState = ConnectionState.INACTIVE;
  protected final String profileName;
  protected String log = "";
  protected String localAddress;
  protected final List<ConnectionStateListener> stateListeners = new LinkedList<ConnectionStateListener>();
  protected int MAX_LOG_SIZE = 2048;

  protected final Object connectionLock = new Object();
  protected int returnCode = 0;
  protected Process process;
  private final String connectedLogPart;
  private final String cmd;
  private int order;
  private final String displayName;

  public AbstractVpnConnection(String name, String displayName, String command, String connectedLogPart) {
    this.profileName = name;
    this.displayName = displayName;
    this.cmd = command;
    this.connectedLogPart = connectedLogPart;
  }

  public String getDisplayName(){
    return this.displayName;
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
        listener.stateChanged(this,state);
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
                || getConnectionState() == ConnectionState.CONNECTED
                || getConnectionState() == ConnectionState.DISCONNECTING
                ) {

          String line = null;
          line = stdout.readLine();

          if (line == null ) { // || line.length() <= 0
            TcpLogger.getInstance().log("wait for end...");
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

  @Override
  public String toString() {
    return this.getClass()+" {" + "profileName=" + profileName + '}';
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public int getOrder() {
    return this.order;
  }

  abstract protected void handleLog(String line);
}
