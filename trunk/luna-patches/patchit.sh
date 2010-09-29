#!/bin/sh

## download latest version of vpn notification patch
wget http://prevpnc.googlecode.com/svn/trunk/luna-patches/bar-assistant.patch -O /media/internal/bar-assistant.patch

## make root writable
/usr/sbin/rootfs_open -w

## backup current bar-assistant
cp /usr/lib/luna/system/luna-systemui/app/controllers/bar-assistant.js /usr/lib/luna/system/luna-systemui/app/controllers/bar-assistant.js.original

## patching bar-assistant
if [ -x /media/cryptofs/apps/usr/bin/patch ] ; then
    cat /media/internal/bar-assistant.patch | /media/cryptofs/apps/usr/bin/patch -p0
else
    cat /media/internal/bar-assistant.patch | patch -p0
fi

echo "now reboot your phone..."

