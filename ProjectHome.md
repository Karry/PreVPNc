The goal of this project is create simple frontend for manage vpn connections on webOS based devices. Currently is supported OpenVPN, Cisco and MS PPTP (pptp only with Palm Pre with kernel version 2.6.24-palm-joplin-3430).

&lt;wiki:gadget url="http://www.ohloh.net/p/483419/widgets/project\_users.xml?style=gray" height="100" border="0"/&gt;

**For more information and help see Wiki pages:**
  * [FAQ](FAQ.md)
  * [Installation](Installation.md)
  * [SetupVPNConnection](SetupVPNConnection.md)
  * [Patching for notifications](PatchingForNotifications.md)

**Screenshots:**

<a href='http://prevpnc.googlecode.com/svn/wiki/screenshots/0.1.1/main.png'><img src='http://prevpnc.googlecode.com/svn/wiki/screenshots/0.1.1/main.png' alt='Main screen' width='200' /></a>
<a href='http://prevpnc.googlecode.com/svn/wiki/screenshots/0.1.1/log.png'><img src='http://prevpnc.googlecode.com/svn/wiki/screenshots/0.1.1/log.png' alt='Connection log' width='200' /></a>
<a href='http://prevpnc.googlecode.com/svn/wiki/screenshots/0.1.1/menu.png'><img src='http://prevpnc.googlecode.com/svn/wiki/screenshots/0.1.1/menu.png' alt='Connection log' width='200' /></a>


**What work:**
  * Connect to PPTP vpn server, OpenVPN or Cisco vpn

**Known problems:**
  * ~~pppd replace default route~~
  * ~~Callback sometimes don't arrive.~~
  * ~~Disconnect dosn't work~~

**TODO:**
  * ~~Handle VPN's dns records~~
  * ~~Implement better session handling in service~~
  * ~~Callback methods from service to mojo~~
  * ~~Improve GUI (for manage profiles)~~
  * ~~Load debug config from file~~
  * Add support for more vpn types (~~OpenVpn~~, ~~Cisco~~, ppp over SSH...)

**CHANGELOG:**

<ul>
<li>0.1.4<br>
<ul>
<li>Add optional configuration item "domain" for Cisco</li>
<li>Add support for notifications (optional Mojo patch)</li>
<li>Fix Cisco DNS problem</li>
</ul>
</li>
<li>0.1.3<br>
<ul>
<li>add Cisco support</li>
<li>DNS handling for PPTP and Cisco</li>
<li>configuration directory is mouted to ram (tmpfs)</li>
</ul>
</li>
<li>0.1.2 add OpenVPN support</li>
<li>0.1.1 add possibility define more vpn profiles</li>
<li>0.1.0 first release, works with pptp, ui looks terribly</li>
</ul>