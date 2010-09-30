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

  private static TcpLogger instance;

  public static TcpLogger getInstance() {
    if (instance == null)
      instance = new TcpLogger("/media/internal/.vpn/debug.conf");
    return instance;
  }

  private PrintWriter writer = null;

  private TcpLogger(String configFile) {
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
