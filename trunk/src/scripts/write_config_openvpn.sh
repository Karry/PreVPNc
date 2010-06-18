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


cat $ROOT/scripts/openvpn_template | sed "s/%NAME%/$name/" | sed "s/%USER%/$user/" | sed "s/%HOST%/$host/" | sed "s/%TOPOLOGY%/$topology/" | sed "s/%PROTOCOL%/$protocol/" | sed "s/%CIPHER%/$cipher/" | sed "s/%PASS%/$pass/" > $VPNROOT/tmp/$name.vpn

