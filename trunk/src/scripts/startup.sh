#!/bin/sh

ROOT=/media/cryptofs/apps/usr/palm/applications/cz.karry.vpnc/
VPNROOT=/opt/vpnbox/

# remount etc directory as tmpfs
umount -f /opt/vpnbox/etc
mount -t tmpfs tmpfs /opt/vpnbox/etc/ -o size=500k

## bind mount /proc directory
umount -f /opt/vpnbox/proc
mount -o bind /proc /opt/vpnbox/proc

cd /opt/vpnbox/etc/
zcat $ROOT/etc.tar.gz | tar -xf -
