#!/bin/sh

ROOT=/media/cryptofs/apps/usr/palm/applications/cz.karry.vpnc/
VPNROOT=/opt/vpnbox/

name=$1
host=$2
user=$3
pass=$4

cp $VPNROOT/etc/ppp/chap-secrets /tmp/chap-secrets
cat /tmp/chap-secrets | grep -v "$name" > $VPNROOT/etc/ppp/chap-secrets
rm /tmp/chap-secrets
echo "\"$user\" $name \"$pass\" *" >> $VPNROOT/etc/ppp/chap-secrets
chmod og-r $VPNROOT/etc/ppp/chap-secrets


cat $ROOT/scripts/pptp_template | sed "s/%NAME%/$name/" | sed "s/%USER%/$user/" | sed "s/%HOST%/$host/" > $VPNROOT/etc/ppp/peers/$name

