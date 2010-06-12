package cz.karry.vpnc;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Socket;

/**
 *
 * @author karry
 */
public class TcpLogger {

  private PrintWriter writer = null;

  public TcpLogger(String host, int port, boolean send) {
    if (send) {
      try {
        Socket socket = null;
        socket = new Socket(Inet4Address.getByName(host), port);
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.log("logger started...");
      } catch (IOException ex) {
        // do nothing
      }
    }
  }

  public void log(String msg, Throwable ex) {
    if (writer != null) {
      writer.println(msg);
      if (ex != null) {
        ex.printStackTrace(writer);
      }
      writer.flush();
    }
  }

  public void log(String msg) {
    this.log(msg, null);
  }
}
