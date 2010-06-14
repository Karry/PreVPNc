package cz.karry.vpnc;

import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Socket;
import java.util.Properties;

/**
 *
 * @author karry
 */
public class TcpLogger {

  private PrintWriter writer = null;

  TcpLogger(String configFile) {
    Properties prop = new Properties();
    Socket socket;
    try {
      prop.load(new FileInputStream(configFile));
      if (prop.getProperty("debug") != null &&
              prop.getProperty("port") != null &&
              prop.getProperty("address") != null &&
              prop.getProperty("debug").toLowerCase().equals("true")){
        int port = Integer.parseInt(prop.getProperty("port"));
        String address = prop.getProperty("address");
        socket = new Socket(Inet4Address.getByName(address), port);
        writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        this.log("logger started...");
      }
    } catch (Exception ex) {
      System.out.println("exception when reading debug config "+ex.getMessage());
      ex.printStackTrace();
      // what we can do? I don't know how to debug WebOS Java services
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
