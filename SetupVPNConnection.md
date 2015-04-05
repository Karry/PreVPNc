# Diagram #

<a href='http://prevpnc.googlecode.com/svn/wiki/images/howto_setup.svg'><img src='http://prevpnc.googlecode.com/svn/wiki/images/howto_setup.png' alt='VPN diagram' width='800' /></a>


# Setup PPTP VPN server on Windows #

FIXME

# Connect to OpenVPN server #

You should put your shared key to internal storage to **.vpn/openvpn`_`%NAME%.key** file, where %NAME% is alias for Session name in PreVPNc...

OpenVPN is very flexible VPN solution. It is hard create simple ui that allow configure all options at the same time. If you need more specific configuration for connect to your VPN server, you can write owns config template... (/media/internal/.vpn/openvpn\_template) Before connecting is called simple shell script that use this template for create config file:

**Default OpenVPN config template**
```
proto %PROTOCOL%
dev tun 
remote %HOST% 1194
secret /tmp/openvpn_%NAME%.key
mode %TOPOLOGY%
cipher %CIPHER%
```

**Script called before connecting**
```
#!/bin/sh

ROOT=/media/cryptofs/apps/usr/palm/applications/cz.karry.vpnc/
VPNROOT=/opt/vpnbox/

name=$1
host=$2
user=$3
pass=$4
topology=$5
protocol=$6
cipher=$7

cp /media/internal/.vpn/openvpn_$name.key $VPNROOT/tmp/openvpn_$name.key
cp /media/internal/.vpn/openvpn_$name.crt $VPNROOT/tmp/openvpn_$name.crt
cp /media/internal/.vpn/openvpn_$name.ca.crt $VPNROOT/tmp/openvpn_$name.ca.crt

template=$ROOT/scripts/openvpn_template
if [ -f /media/internal/.vpn/openvpn_template ] ; then
    template=/media/internal/.vpn/openvpn_template
fi

cat $template | sed "s/%NAME%/$name/g" | \
    sed "s/%USER%/$user/g" | \
    sed "s/%HOST%/$host/g" | \
    sed "s/%TOPOLOGY%/$topology/g" | \
    sed "s/%PROTOCOL%/$protocol/g" | \ 
    sed "s/%CIPHER%/$cipher/g" | \ 
    sed "s/%PASS%/$pass/g" > $VPNROOT/tmp/$name.vpn
```