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


cat $ROOT/scripts/openvpn_template | sed "s/%NAME%/$name/g" | sed "s/%USER%/$user/g" | sed "s/%HOST%/$host/g" | sed "s/%TOPOLOGY%/$topology/g" | sed "s/%PROTOCOL%/$protocol/g" | sed "s/%CIPHER%/$cipher/g" | sed "s/%PASS%/$pass/g" > $VPNROOT/tmp/$name.vpn

