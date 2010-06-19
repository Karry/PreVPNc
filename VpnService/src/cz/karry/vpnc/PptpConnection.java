package cz.karry.vpnc;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 *
 * @author karry
 */
public class PptpConnection extends VpnConnection {

  private final Object connectionLock = new Object();
  private int returnCode = 0;
  private Process process;

  public PptpConnection(String name) {
    super(name);
  }

  @Override
  public void waitWhileConnecting() throws InterruptedException {
    synchronized (connectionLock) {
      if (getConnectionState() == ConnectionState.CONNECTING)
        connectionLock.wait();
    }
  }

  @Override
  public synchronized void start() {
    super.start();
    synchronized (connectionLock) {
      this.setConnectionState(ConnectionState.CONNECTING);
    }
  }

  @Override
  public void run() {
    BufferedReader stdout = null;
    String[] command = String.format("chroot /opt/vpnbox /usr/sbin/pppd call %s", profileName).split(" ");

    try {
      try {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        this.process = pb.start();
        stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));

        while (getConnectionState() == ConnectionState.CONNECTING
                || getConnectionState() == ConnectionState.CONNECTED) {

          String line = null;
          line = stdout.readLine();

          if (line == null || line.length() <= 0) {
            process.waitFor();
            this.returnCode = process.exitValue();
            synchronized (connectionLock) {
              if (returnCode == 0 || (getConnectionState() == ConnectionState.DISCONNECTING)) {
                this.setConnectionState(ConnectionState.INACTIVE);
              } else {
                this.setConnectionState(ConnectionState.FAILED);
              }
            }
          } else {
            if (line.startsWith("local  IP address")) {
              this.localAddress = line.substring("local  IP address".length());
              synchronized (connectionLock) {
                this.setConnectionState(ConnectionState.CONNECTED);
                connectionLock.notifyAll();
              }
            }
            this.log += "\n" + line;
          }
        }
        synchronized (connectionLock) {
          connectionLock.notifyAll();
        }
      } finally {
        if (stdout != null)
          stdout.close();
      }
    } catch (Exception e) {
      synchronized (connectionLock) {
        connectionLock.notifyAll();
      }
      this.log += "\n\nException occured: " + e.getMessage() + " (" + e.getClass().getName() + ")";
    }
  }

  @Override
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
}

