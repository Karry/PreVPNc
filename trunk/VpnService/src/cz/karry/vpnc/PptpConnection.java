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
      if (connectionState == ConnectionState.CONNECTING)
        connectionLock.wait();
    }
  }

  @Override
  public synchronized void start() {
    super.start();
    synchronized (connectionLock) {
      this.connectionState = ConnectionState.CONNECTING;
    }
  }

  @Override
  public void run() {
    BufferedReader stdout = null;
    BufferedReader stderr = null;
    String[] command = String.format("chroot /opt/vpnbox /usr/sbin/pppd call %s", profileName).split(" ");

    try {
      try {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(false);
        this.process = pb.start();
        stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
        stderr = new BufferedReader(new InputStreamReader(process.getErrorStream()));

        while (connectionState == ConnectionState.CONNECTING
                || connectionState == ConnectionState.CONNECTED) {
          if (stdout.ready() || stderr.ready()) {
            String line = null;
            if (stdout.ready()) {
              line = stdout.readLine();
            } else if (stderr.ready()) {
              line = stdout.readLine();
            }
            if (line == null) {
              process.waitFor();
              this.returnCode = process.exitValue();
              if (returnCode == 0) {
                synchronized (connectionLock) {
                  connectionState = ConnectionState.INACTIVE;
                }
              } else {
                synchronized (connectionLock) {
                  connectionState = ConnectionState.FAILED;
                }
              }
            } else {
              if (line.startsWith("local  IP address")) {
                this.localAddress = line.substring("local  IP address".length());
                synchronized (connectionLock) {
                  this.connectionState = ConnectionState.CONNECTED;
                  connectionLock.notifyAll();
                }
              }
              this.log += "\n" + line;
            }
          }

        }
        synchronized (connectionLock) {
          connectionLock.notifyAll();
        }
      } finally {
        if (stderr != null)
          stderr.close();
        if (stdout != null)
          stderr.close();
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
      if (connectionState == ConnectionState.CONNECTING
              || connectionState == ConnectionState.CONNECTED) {
        if (this.process != null)
          this.process.destroy();
      }
    }
  }
}
