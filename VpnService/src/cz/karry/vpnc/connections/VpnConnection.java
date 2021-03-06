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

/**
 *
 * @author karry
 */
public interface VpnConnection {

  public enum ConnectionState {
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    FAILED,
    INACTIVE
  }

  public String getProfileName();

  public String getDisplayName();
  
  public void diconnect();

  public ConnectionState getConnectionState();

  public String getLocalAddress();

  public String getLog();

  public boolean addStateListener(ConnectionStateListener connectionStateListenerImpl);

}
