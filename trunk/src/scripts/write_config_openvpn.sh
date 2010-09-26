#!/bin/sh

ROOT=/media/cryptofs/apps/usr/palm/applications/cz.karry.vpnc/
VPNROOT=/opt/vpnbox/

name=$1
host=$2
topology=$3
protocol=$4
cipher=$5

cp /media/internal/.vpn/openvpn_$name.key $VPNROOT/tmp/openvpn_$name.key
cp /media/internal/.vpn/openvpn_$name.crt $VPNROOT/tmp/openvpn_$name.crt
cp /media/internal/.vpn/openvpn_$name.ca.crt $VPNROOT/tmp/openvpn_$name.ca.crt

template=$ROOT/scripts/openvpn_template
if [ -f /media/internal/.vpn/openvpn_template ] ; then
    template=/media/internal/.vpn/openvpn_template
fi

cat $template | sed "s/%NAME%/$name/g" | sed "s/%HOST%/$host/g" | sed "s/%TOPOLOGY%/$topology/g" | sed "s/%PROTOCOL%/$protocol/g" | sed "s/%CIPHER%/$cipher/g" | sed "s/%PASS%/$pass/g" > $VPNROOT/etc/openvpn/$name.vpn

