#!/bin/sh

ROOT=/media/cryptofs/apps/usr/palm/applications/cz.karry.vpnc/
VPNROOT=/opt/vpnbox/

name=$1
host=$2

userid=$3
userpass=$4
userpasstype=$5
groupid=$6
grouppass=$7
grouppasstype=$8

script=$ROOT/scripts/vpnc-script
if [ -f /media/internal/.vpn/vpnc-script ] ; then
    script=/media/internal/.vpn/vpnc-script
fi
cp $script $VPNROOT/etc/vpnc/vpnc-script

template=$ROOT/scripts/cisco_template
if [ -f /media/internal/.vpn/cisco_template ] ; then
    template=/media/internal/.vpn/cisco_template
fi

cat $template | sed "s/%NAME%/$name/g" | sed "s/%HOST%/$host/g" | sed "s/%USERID%/$userid/g" | sed "s/%USERPASS%/$userpass/g" | sed "s/%USERPASSTYPE%/$userpasstype/g" | sed "s/%GROUPID%/$groupid/g" | sed "s/%GROUPPASS%/$grouppass/g" | sed "s/%GROUPPASSTYPE%/$grouppasstype/g"  > $VPNROOT/etc/vpnc/$name.conf

