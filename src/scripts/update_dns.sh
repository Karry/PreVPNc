#!/bin/sh


if [ $1 = "flush" ] ; then
    echo nameserver 127.0.0.1 > /opt/vpnbox/etc/resolv.conf
fi

ln -sf /opt/vpnbox/etc/resolv.conf /etc/resolv.conf
echo nameserver $2 >> /opt/vpnbox/etc/resolv.conf
