#!/bin/sh

MOD_DIR=/media/cryptofs/apps/usr/palm/applications/cz.karry.vpnc/modules/`uname -r`

if [ -d $MOD_DIR ] ; then
    insmod $MOD_DIR/crypto/arc4.ko
    insmod $MOD_DIR/crypto/ecb.ko
    insmod $MOD_DIR/crypto/pcbc.ko
    insmod $MOD_DIR/drivers/net/bsd_comp.ko
    insmod $MOD_DIR/drivers/net/ifb.ko
    insmod $MOD_DIR/drivers/net/ppp_deflate.ko
    insmod $MOD_DIR/drivers/net/pppox.ko
    insmod $MOD_DIR/drivers/net/pppoe.ko
    insmod $MOD_DIR/drivers/net/ppp_mppe.ko

    #mount -o bind /proc /opt/vpnbox/proc
    exit 0
else
    echo>&2 "Unsupported kernel version: "`uname -r`
    exit 1
fi

