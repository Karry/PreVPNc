The goal of this project is create simple frontend for manage vpn connections on webOS based devices. Currently is supported OpenVPN, Cisco and MS PPTP (pptp only with Palm Pre with kernel version 2.6.24-palm-joplin-3430).

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


  * 0.1.4
    * Add optional configuration item "domain" for Cisco
    * Add support for notifications (optional Mojo patch)
    * Fix Cisco DNS problem
  * 0.1.3
    * add Cisco support
    * DNS handling for PPTP and Cisco
    * configuration directory is mouted to ram (tmpfs)
  * 0.1.2 add OpenVPN support
  * 0.1.1 add possibility define more vpn profiles
  * 0.1.0 first release, works with pptp, ui looks terribly
